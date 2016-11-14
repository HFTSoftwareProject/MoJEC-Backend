package de.hftstuttgart.models;

import javax.tools.Diagnostic;
import java.util.List;

public class UserResult {

    private User user;
    private List<TestResult> testResults;
    private List<Diagnostic> compilationErrors;

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

    public List<Diagnostic> getCompilationErrors() {
        return compilationErrors;
    }

    public void setCompilationErrors(List<Diagnostic> compilationErrors) {
        this.compilationErrors = compilationErrors;
    }


}
