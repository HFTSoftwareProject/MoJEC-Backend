package de.hftstuttgart;

import com.google.gson.*;
import de.hftstuttgart.models.CompilationError;
import de.hftstuttgart.models.TestResult;
import de.hftstuttgart.models.User;
import de.hftstuttgart.models.UserResult;
import org.apache.log4j.Logger;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.springframework.util.StringUtils;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class JUnitTestHelper {

    private static final Logger LOG = Logger.getLogger(JUnitTestHelper.class);

    private final String COMPILER_OUTPUT_FOLDER = "compiledFiles";

    private CompilationError compilationError;

    public List<TestResult> runUnitTests(String uutDirPath, String resultPath, User user) throws IOException, ClassNotFoundException {
        File dir = new File(uutDirPath);
        List<String> paths = getFilePathsToCompile(dir);

        // Compile the tests and the task classes
        File compileOutputDir = new File(uutDirPath + File.separator + COMPILER_OUTPUT_FOLDER);
        Boolean compileResult = compile(paths, compileOutputDir);

        if (compileResult) {
            LOG.info("Compilation successful");
        } else {
            // We can't work if the compilation failed.
            LOG.warn("Compilation failed");
            TestResult testResult = new TestResult();
            testResult.setCompilationError(compilationError);
            return Collections.singletonList(testResult);
        }

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
            testResult.setCompilationError(compilationError);

            testResults.add(testResult);
        }
        writeTestResultsToFile(testResults, resultPath,user);
        return testResults;
    }

    private Boolean compile(List<String> pathsToCompile, File compileDir) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        MyDiagnosticListener listener = new MyDiagnosticListener();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(listener, null, Charset.forName("UTF-8"));

        Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjectsFromStrings(pathsToCompile);

        if (!compileDir.exists()) {
            compileDir.mkdir();
        }
        List<String> options = new ArrayList<>();
        options.add("-d");
        options.add(compileDir.getAbsolutePath());

            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, listener, options, null, fileObjects);
        return task.call();
    }

    private void writeTestResultsToFile(List<TestResult> testResults, String resultsPath,User user) throws IOException {
        UserResult userResult = new UserResult(user, testResults);
        Gson gson = new GsonBuilder().registerTypeAdapter(Failure.class, new FailureSerializer()).create();
        String resultJson = gson.toJson(userResult);
        Files.write(Paths.get(resultsPath), resultJson.getBytes());
    }

    private List<String> getFilePathsToCompile(File dir) {
        File[] javaFiles = dir.listFiles((dir1, name) -> StringUtils.endsWithIgnoreCase(name, ".java"));
        List<String> pathsArray = Arrays.stream(javaFiles).map(File::getAbsolutePath).collect(Collectors.toList());
        return pathsArray;
    }

    /**
     * Serializes a {@link Failure}.
     *
     * We are (for now) only interested in:
     * <ul>
     *     <li>testHeader - The test method name</li>
     *     <li>message - A optional message summarizing the thrown Exception</li>
     *     <li>trace - The stack trace of the Exception as a String</li>
     * </ul>
     *
     */
    private static class FailureSerializer implements JsonSerializer<Failure> {
        @Override
        public JsonElement serialize(Failure src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            result.addProperty("testHeader", src.getTestHeader());
            result.addProperty("message", src.getMessage());
            result.addProperty("trace", src.getTrace());
            return result;
        }
    }

    private class MyDiagnosticListener implements DiagnosticListener {
        public void report(Diagnostic diagnostic) {
            compilationError = new CompilationError();
            compilationError.setCode(diagnostic.getCode());
            compilationError.setColumnNumber(diagnostic.getColumnNumber());
            compilationError.setLineNumber(diagnostic.getLineNumber());
            compilationError.setKind(diagnostic.getKind().toString());
            compilationError.setPosition(diagnostic.getPosition());
            compilationError.setStartPosition(diagnostic.getStartPosition());
            compilationError.setEndPosition(diagnostic.getEndPosition());
            compilationError.setSource(diagnostic.getSource().toString());
            compilationError.setMessage(diagnostic.getMessage(Locale.ENGLISH));
        }
    }
}