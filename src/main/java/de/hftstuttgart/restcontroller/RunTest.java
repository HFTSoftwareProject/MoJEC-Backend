package de.hftstuttgart.restcontroller;

import com.google.gson.Gson;
import de.hftstuttgart.models.TestResult;
import org.apache.log4j.Logger;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("v1/test")
public class RunTest {
   private static final Logger LOG = Logger.getLogger(RunTest.class);

   @Value("${mojec.dir.uut}")
   private String uutDirPath;

   @Value("${mojec.dir.results}")
   private String resultDirPath;

   /**
    * Runs the uploaded JUnit tests on the uploaded tasks.
    */
   @RequestMapping(method = RequestMethod.POST)
   private List<TestResult> runUnitTests() throws IOException, ClassNotFoundException {
      File dir = new File(uutDirPath);
      String[] paths = getFilePathsToCompile(dir);

      JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      // TODO we can collect the output stream to analyze it in case of an compilation error
      int compilerResult = compiler.run(null, null, null, paths);
      if (compilerResult != 0) {
         LOG.warn("Compilation failed");
      }

      // Load compiled classes into classloader
      URL url = dir.toURI().toURL();
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
      writeTestResultsToFile(testResults);
      return testResults;
   }

   private void writeTestResultsToFile(List<TestResult> testResults) throws IOException {
      Gson gson = new Gson();
      String resultJson = gson.toJson(testResults);
      Files.write(Paths.get(resultDirPath), resultJson.getBytes());
   }

   private String[] getFilePathsToCompile(File dir) {
      File[] javaFiles = dir.listFiles((dir1, name) -> StringUtils.endsWithIgnoreCase(name, ".java"));
      String[] pathsArray = Arrays.stream(javaFiles).map(File::getAbsolutePath).toArray(String[]::new);
      return pathsArray;
   }
}
