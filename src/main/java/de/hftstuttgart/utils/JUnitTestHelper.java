package de.hftstuttgart.utils;

import com.google.common.io.Files;
import de.hftstuttgart.models.TestResult;

import de.hftstuttgart.models.UserResult;
import org.apache.log4j.Logger;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

    public UserResult runUnitTests(String unitTestDir, List<File> taskFiles) throws IOException, ClassNotFoundException {

        List<File> unitTestFiles = getUnitTestFiles(unitTestDir);
        List<File> filesToCompile = new ArrayList<>();
        filesToCompile.addAll(taskFiles);
        filesToCompile.addAll(unitTestFiles);

        compileOutputDir = new File(unitTestDir + File.separator + COMPILER_OUTPUT_FOLDER_PREFIX + UUID.randomUUID().toString());
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
            // Check if the current test was successfully compiled
            for (Diagnostic error : compilationErrors) {
                File failedCompilationFile = new File((((JavaFileObject) error.getSource()).toUri().getPath()));
                if (failedCompilationFile.getAbsolutePath().equals(testFile.getAbsolutePath())) {
                    currentTestCompiled = false;
                }
            }
            if (!currentTestCompiled) {
                break;
            }

            String testName = Files.getNameWithoutExtension(testFile.getPath());
            LOG.info("Running JUnit test " + testName);
            Class<?> junitTestClass = classLoader.loadClass(testName);
            Result junitResult = junit.run(junitTestClass);

            List<String> successfulTestNames = getSuccessfulTestNames(junitTestClass.getDeclaredMethods(), junitResult.getFailures());

            TestResult testResult = new TestResult();
            testResult.setTestName(testName);
            testResult.setTestCount(junitResult.getRunCount());
            testResult.setFailureCount(junitResult.getFailureCount());
            testResult.setTestFailures(junitResult.getFailures());
            testResult.setSuccessfulTests(successfulTestNames);

            testResults.add(testResult);
        }

        UserResult userResult = new UserResult(testResults);
        userResult.setCompilationErrors(compilationErrors);
        return userResult;
    }

    private List<String> getSuccessfulTestNames(Method[] methods, List<Failure> failures) {
        List<Method> publicMethods = Arrays.stream(methods)
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .collect(Collectors.toList());
        for (Failure failure : failures) {
            publicMethods.removeIf(method -> method.getName().equals(failure.getDescription().getMethodName()));
        }

        List<String> successfulTestNames = publicMethods.stream()
                .map(Method::getName)
                .collect(Collectors.toList());

        return successfulTestNames;
    }

    private void compile(List<File> files, File outputDir) {
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
        options.add("-d");
        options.add(outputDir.getAbsolutePath());
        options.add("-cp");
        String cp = buildClassPath(junitLibDirPath, compileOutputDir.getAbsolutePath());
        LOG.debug("Classpath for compilation: " + cp);
        options.add(cp);


        // compile it
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, listener, options, null, fileObjects);
        Boolean compileResult = task.call();
        if (!compileResult) {
            // If the compilation failed, remove the failed file from the pathsToCompile list and try to compile again
            File currentFile = new File(((JavaFileObject) compilationErrors.get(compilationErrors.size() - 1).getSource()).toUri().getPath());
            LOG.warn("Compilation of file '" + currentFile.getPath() + "' failed");
            files.removeIf(file -> file.getPath().equalsIgnoreCase(currentFile.getPath()));
            if (files.size() > 0) {
                compile(files, outputDir);
            }
        }
    }

    /**
     * This function builds a classpath from the passed Strings
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

    private List<File> getUnitTestFiles(String unitTestDirPath) {
        File dir = new File(unitTestDirPath);
        File[] unitTestFilesArray = dir.listFiles((dir1, name) -> StringUtils.endsWithIgnoreCase(name, "test.java"));
        List<File> unitTestFiles = new ArrayList<>();
        Collections.addAll(unitTestFiles, unitTestFilesArray);
        return unitTestFiles;
    }

    public File getCompileOutputDir() {
        return compileOutputDir;
    }

    private class MyDiagnosticListener implements DiagnosticListener {
        public void report(Diagnostic diagnostic) {
            compilationErrors.add(diagnostic);
        }
    }
}