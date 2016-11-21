package de.hftstuttgart.models;

import javax.tools.Diagnostic;
import java.util.List;

public class UserResult {

    private List<TestResult> testResults;
    private List<Diagnostic> compilationErrors;

    public UserResult(List<TestResult> testResults) {

        this.testResults = testResults;
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
