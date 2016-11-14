package de.hftstuttgart;

import de.hftstuttgart.models.TestResult;
import de.hftstuttgart.models.User;
import de.hftstuttgart.models.UserResult;
import org.apache.log4j.Logger;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.springframework.util.StringUtils;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JUnitTestHelper {

    private static final Logger LOG = Logger.getLogger(JUnitTestHelper.class);

    private final String COMPILER_OUTPUT_FOLDER = "compiledFiles";

    private List<Diagnostic> compilationErrors;

    public UserResult runUnitTests(String uutDirPath, User user) throws IOException, ClassNotFoundException {
        File dir = new File(uutDirPath);
        List<String> paths = getFilePathsToCompile(dir);

        // Compile the tests and the task classes
        File compileOutputDir = new File(uutDirPath + File.separator + COMPILER_OUTPUT_FOLDER);
        compile(paths, compileOutputDir);

        // Load compiled classes into classloader
        URL url = compileOutputDir.toURI().toURL();
        URL[] urls = {url};
        ClassLoader classLoader = new URLClassLoader(urls);

        List<String> classNames = new ArrayList<>();
        for (String path : paths) {
            Path p = Paths.get(path);
            String fileName = p.getFileName().toString();
            classNames.add(fileName.substring(0, fileName.indexOf(".")));
        }

        // Separate test classes and tasks
        List<String> tests = new ArrayList<>();
        List<String> tasks = new ArrayList<>();
        for (String name : classNames) {
            if (name.toLowerCase().endsWith("test")) {
                tests.add(name);
            } else {
                tasks.add(name);
            }
        }

        // Run JUnit tests
        JUnitCore junit = new JUnitCore();
        List<TestResult> testResults = new ArrayList<>();
        for (String testName : tests) {
            LOG.info("Running JUnit test " + testName);
            Class<?> junitTestClass = Class.forName(testName, true, classLoader);
            Result junitResult = junit.run(junitTestClass);

            TestResult testResult = new TestResult();
            testResult.setTestName(testName);
            testResult.setTestCount(junitResult.getRunCount());
            testResult.setFailureCount(junitResult.getFailureCount());
            testResult.setTestFailures(junitResult.getFailures());

            testResults.add(testResult);
        }

        UserResult userResult = new UserResult(user, testResults);
        userResult.setCompilationErrors(compilationErrors);
        return userResult;
    }

    private void compile(List<String> pathsToCompile, File compileDir) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        MyDiagnosticListener listener = new MyDiagnosticListener();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(listener, null, Charset.forName("UTF-8"));
        Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjectsFromStrings(pathsToCompile);

        if (!compileDir.exists()) {
            compileDir.mkdir();
        }

        // Set the compiler option for a specific output path
        List<String> options = new ArrayList<>();
        options.add("-d");
        options.add(compileDir.getAbsolutePath());

        // compile it
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, listener, options, null, fileObjects);
        Boolean compileResult = task.call();
        if (!compileResult) {
            File file = new File(((JavaFileObject) compilationErrors.get(compilationErrors.size() - 1).getSource()).toUri().getPath());
            LOG.warn("Compilation of file '" + file.getPath() + "' failed");
            pathsToCompile.removeIf(path -> path.equalsIgnoreCase(file.getPath()));
            if (pathsToCompile.size() > 0) {
                compile(pathsToCompile, compileDir);
            }
        }
    }

    private List<String> getFilePathsToCompile(File dir) {
        File[] javaFiles = dir.listFiles((dir1, name) -> StringUtils.endsWithIgnoreCase(name, ".java"));
        List<String> pathsArray = Arrays.stream(javaFiles).map(File::getAbsolutePath).collect(Collectors.toList());
        return pathsArray;
    }

    private class MyDiagnosticListener implements DiagnosticListener {
        public void report(Diagnostic diagnostic) {
            if (compilationErrors == null) {
                compilationErrors = new ArrayList<>();
            }
            compilationErrors.add(diagnostic);
        }
    }
}