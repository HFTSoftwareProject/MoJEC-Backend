package de.hftstuttgart.models;

import java.util.List;

/**
 * Created by DELL on 10-11-2016.
 */
public class UserResult {
    private  User user;
    private List<TestResult> testResults;

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
}
