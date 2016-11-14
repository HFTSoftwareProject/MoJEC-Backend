package de.hftstuttgart.models;

import java.util.List;

public class UserResult {

    private User user;
    private List<TestResult> testResults;
    private List<CompilationError> compilationErrors;

    public UserResult(User user, List<TestResult> testResults) {
        this.user = user;
        this.testResults = testResults;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<TestResult> getTestResults() {
        return testResults;
    }

    public void setTestResults(List<TestResult> testResults) {
        this.testResults = testResults;
    }

    public List<CompilationError> getCompilationErrors() {
        return compilationErrors;
    }

    public void setCompilationErrors(List<CompilationError> compilationErrors) {
        this.compilationErrors = compilationErrors;
    }
}
