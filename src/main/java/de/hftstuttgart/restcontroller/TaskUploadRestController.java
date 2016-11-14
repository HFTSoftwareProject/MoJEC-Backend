package de.hftstuttgart.restcontroller;

import com.google.gson.Gson;
import de.hftstuttgart.JUnitTestHelper;
import de.hftstuttgart.models.TestResult;
import de.hftstuttgart.models.User;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.annotation.MultipartConfig;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/v1/task")
@MultipartConfig()
public class TaskUploadRestController {
    private static final Logger LOG = Logger.getLogger(TaskUploadRestController.class);

    @Value("${mojec.dir.uut}")
    private String uutDirPath;

    @Value("${mojec.path.results}")
    private String resultPath;

    @RequestMapping(method = RequestMethod.POST)
    public List<TestResult> uploadAndTestFile(@RequestParam("taskFile") MultipartFile taskFileRef, @RequestParam("user") String userJson) {
        File taskFile = new File(uutDirPath, taskFileRef.getOriginalFilename());
        Gson gson= new Gson();
        User user = gson.fromJson(userJson, User.class);
        try {
            taskFileRef.transferTo(taskFile);
            LOG.info("Uploaded File: " + taskFile);

            JUnitTestHelper testHelper = new JUnitTestHelper();
            return testHelper.runUnitTests(uutDirPath, resultPath,user);

        } catch (IOException e) {
            LOG.error("Failed to upload file " + taskFile, e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
