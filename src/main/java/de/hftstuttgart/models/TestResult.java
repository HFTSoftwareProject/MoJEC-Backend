package de.hftstuttgart.models;

import org.junit.runner.notification.Failure;

import java.util.List;

public class TestResult {

    private String testName;
    private int testCount;
    private int failureCount;
    private CompilationError compilationError;
    private List<Failure> testFailures;

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public int getTestCount() {
        return testCount;
    }

    public void setTestCount(int testCount) {
        this.testCount = testCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public CompilationError getCompilationError() {
        return compilationError;
    }

    public void setCompilationError(CompilationError compilationError) {
        this.compilationError = compilationError;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public List<Failure> getTestFailures() {
        return testFailures;
    }

    public void setTestFailures(List<Failure> testFailures) {
        this.testFailures = testFailures;
    }

    @Override
    public String toString() {
        return "TestResult{" +
                "testName='" + testName + '\'' +
                ", testCount=" + testCount +
                ", failureCount=" + failureCount +
                ", testFailures=" + testFailures +
                '}';
    }
}
