package de.hftstuttgart.utils;

import com.google.common.io.Files;
import de.hftstuttgart.models.TestResult;
import de.hftstuttgart.models.UserResult;
import org.apache.log4j.Logger;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JUnitTestHelper {

    private static final Logger LOG = Logger.getLogger(JUnitTestHelper.class);
    private static final String COMPILER_OUTPUT_FOLDER_PREFIX = "compiledFiles_";

    private final String junitLibDirPath;

    private List<Diagnostic> compilationErrors = new ArrayList<>();
    public File compileOutputDir;

    @Autowired
    public JUnitTestHelper(@Value("${mojec.dir.junit}") String junitLibDirPath) {
        this.junitLibDirPath = junitLibDirPath;
    }

    /**
     * Runs the JUnit tests and return the result
     *
     * @param parentPath                Path of the parent folder of the current assignment. Default: /tmp
     * @param assignmentFolderPrefix    Prefix of the assignment subfolder
     * @param assignmentId              ID of the current assignment
     * @param testFolderName            Folder name of the unit test subfolder
     * @param taskFiles                 List of unzipped .java files to be tested
     *
     * @return                          {@link UserResult} with a list of the {@link TestResult} of the executed UnitTests
     *                                  and a List of {@link Diagnostic} with the not compilable task files
     */
    public UserResult runUnitTests(String parentPath,
                                   String assignmentFolderPrefix,
                                   String assignmentId,
                                   String testFolderName,
                                   List<File> taskFiles) throws IOException, ClassNotFoundException {

        // Get the path of the current assignment. E.g. /tmp/Assignment_123
        String assignmentDirPath = parentPath + File.separator + assignmentFolderPrefix + assignmentId;
        List<File> unitTestFiles = getUnitTestFiles(assignmentDirPath, testFolderName);

        // Create list of all files we need to compile i.e. UnitTests and TaskFiles
        List<File> filesToCompile = new ArrayList<>();
        filesToCompile.addAll(taskFiles);
        filesToCompile.addAll(unitTestFiles);

        // create temp folder for the compilation output
        compileOutputDir = new File(assignmentDirPath + File.separator + COMPILER_OUTPUT_FOLDER_PREFIX + UUID.randomUUID().toString());
        compile(filesToCompile, compileOutputDir);

        // Load compiled classes into classloader
        URL url = compileOutputDir.toURI().toURL();
        URL[] urls = {url};
        // It's important to set the context loader as parent, otherwise the test runs will fail
        ClassLoader classLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());

        // Run JUnit tests
        JUnitCore junit = new JUnitCore();
        List<TestResult> testResults = new ArrayList<>();
        for (File testFile : unitTestFiles) {
            boolean currentTestCompiled = true;
            // Check if the current test was successfully compiled. If not, continue with the next unit test
            for (Diagnostic error : compilationErrors) {
                File failedCompilationFile = new File((((JavaFileObject) error.getSource()).toUri().getPath()));
                if (failedCompilationFile.getAbsolutePath().equals(testFile.getAbsolutePath())) {
                    currentTestCompiled = false;
                }
            }
            if (!currentTestCompiled) {
                continue;
            }

            String testName = Files.getNameWithoutExtension(testFile.getPath());
            LOG.info("Running JUnit test " + testName);
            Class<?> junitTestClass = classLoader.loadClass(testName);

            // Add the run listener to JUnit in order to be able to see the failures
            MyRunListener runListener = new MyRunListener();
            junit.addListener(runListener);
            Result junitResult = junit.run(junitTestClass);
            junit.removeListener(runListener);

            // Build the result
            List<String> successfulTestNames = runListener.getSuccessFullTestNames();
            TestResult testResult = new TestResult();
            testResult.setTestName(testName);
            testResult.setTestCount(junitResult.getRunCount());
            testResult.setFailureCount(junitResult.getFailureCount());
            testResult.setTestFailures(junitResult.getFailures());
            testResult.setSuccessfulTests(successfulTestNames);

            testResults.add(testResult);
        }

        // Add the failed compilations to the result
        UserResult userResult = new UserResult(testResults);
        userResult.setCompilationErrors(compilationErrors);
        return userResult;
    }

    /**
     * @param files         Files to compile
     * @param outputDir     Output folder where the .class files are stored.
     */
    private void compile(List<File> files, File outputDir) {
        // Create the compiler and add a diagnostic listener to get the compilation errors
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        MyDiagnosticListener listener = new MyDiagnosticListener();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(listener, null, Charset.forName("UTF-8"));
        File[] fileArray = new File[files.size()];
        fileArray = files.toArray(fileArray);
        Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjects(fileArray);

        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        // Set the compiler option for a specific output path
        List<String> options = new ArrayList<>();
        options.add("-d"); // output dir
        options.add(outputDir.getAbsolutePath());
        options.add("-cp"); // custom classpath
        String cp = buildClassPath(junitLibDirPath, compileOutputDir.getAbsolutePath());
        LOG.debug("Classpath for compilation: " + cp);
        options.add(cp);

        // compile it
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, listener, options, null, fileObjects);
        Boolean compileResult = task.call();

        // If the compilation failed, remove the failed file from the pathsToCompile list and try to compile again without this file
        if (!compileResult) {
            File currentFile = new File(((JavaFileObject) compilationErrors.get(compilationErrors.size() - 1).getSource()).toUri().getPath());
            LOG.warn("Compilation of file '" + currentFile.getPath() + "' failed");
            files.removeIf(file -> file.getPath().equalsIgnoreCase(currentFile.getPath()));
            if (files.size() > 0) {
                compile(files, outputDir);
            }
        }
    }

    /**
     * This function builds a classpath from the passed Strings.
     * We need this because the JUnit4 and Hamcrest libraries needs to be added.
     *
     * @param paths classpath elements
     * @return returns the complete classpath with wildcards expanded
     */
    private static String buildClassPath(String... paths) {
        StringBuilder sb = new StringBuilder();
        for (String path : paths) {
            if (path.endsWith("*")) {
                path = path.substring(0, path.length() - 1);
                File pathFile = new File(path);
                // TODO pathFile can be null if no lib folder is given
                for (File file : pathFile.listFiles()) {
                    if (file.isFile() && file.getName().endsWith(".jar")) {
                        sb.append(path);
                        sb.append(file.getName());
                        sb.append(System.getProperty("path.separator"));
                    }
                }
            } else {
                sb.append(path);
                sb.append(System.getProperty("path.separator"));
            }
        }
        return sb.toString();
    }

    /**
     * Get a List of all UnitTest files.
     */
    private List<File> getUnitTestFiles(String assignmentDirPath, String testFolderName) {
        String unitTestDirPath = assignmentDirPath + File.separator + testFolderName; // e.g. /tmp/Assignment_123/UnitTests
        File unitTestDir = new File(unitTestDirPath);
        File[] unitTestFilesArray = unitTestDir.listFiles();
        List<File> unitTestFiles = new ArrayList<>();
        Collections.addAll(unitTestFiles, unitTestFilesArray);
        return unitTestFiles;
    }

    public File getCompileOutputDir() {
        return compileOutputDir;
    }

    /**
     * Custom DiagnosticListener to get the compilation errors
     */
    private class MyDiagnosticListener implements DiagnosticListener {
        public void report(Diagnostic diagnostic) {
            compilationErrors.add(diagnostic);
        }
    }

    /**
     * Custom JUnit RunListener which enables us to record the successful and the failed tests.
     */
    private static class MyRunListener extends RunListener {
        private Set<Description> allTests = new LinkedHashSet<>();
        private Set<Description> failedTests = new LinkedHashSet<>();

        @Override
        public void testFinished(Description description) throws Exception {
            super.testFinished(description);
            if (description.isTest()) {
                allTests.add(description);
            }
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            super.testFailure(failure);
            Description description = failure.getDescription();
            if (description != null && description.isTest()) {
                failedTests.add(description);
            }
        }

        List<String> getSuccessFullTestNames() {
            Set<Description> tmp = new LinkedHashSet<>(allTests);
            tmp.removeAll(failedTests);
            return tmp.stream().map(Description::getMethodName).collect(Collectors.toList());
        }
    }
}