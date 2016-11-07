package de.hftstuttgart.restcontroller;

import de.hftstuttgart.JUnitTestHelper;
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
    public void uploadFile(@RequestParam("taskFile") MultipartFile taskFileRef) {
        File taskFile = new File(uutDirPath, taskFileRef.getOriginalFilename());
        try {
            taskFileRef.transferTo(taskFile);
            LOG.info("Uploaded File: " + taskFile);

            JUnitTestHelper testHelper = new JUnitTestHelper();
            testHelper.runUnitTests(uutDirPath, resultPath);

        } catch (IOException e) {
            LOG.error("Failed to upload file " + taskFile, e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
