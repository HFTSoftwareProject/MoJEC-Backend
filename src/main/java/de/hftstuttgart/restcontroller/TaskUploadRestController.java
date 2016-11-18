package de.hftstuttgart.restcontroller;

import com.google.common.io.Files;
import com.google.gson.Gson;
import de.hftstuttgart.JUnitTestHelper;
import de.hftstuttgart.exceptions.FileTypeNotSupportedException;
import de.hftstuttgart.models.User;
import de.hftstuttgart.models.UserResult;
import de.hftstuttgart.utils.UnzipUtil;
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
    public UserResult uploadAndTestFile(@RequestParam("taskFile") MultipartFile taskFileRef, @RequestParam("user") String userJson) throws IOException, ClassNotFoundException {
        File taskFile = new File(uutDirPath, taskFileRef.getOriginalFilename());
        taskFileRef.transferTo(taskFile);
        String fileExtension = Files.getFileExtension(taskFile.getName());

        if (!"zip".equals(fileExtension)) {
            throw new FileTypeNotSupportedException("The file type " + fileExtension + " is not supported. Only 'zip' is supported.");
        }

        List<File> unzippedFiles = UnzipUtil.unzip(taskFile);

        Gson gson= new Gson();
        User user = gson.fromJson(userJson, User.class);
        LOG.info("Uploaded File: " + taskFile);

        JUnitTestHelper testHelper = new JUnitTestHelper();
        UserResult userResult = testHelper.runUnitTests(uutDirPath, unzippedFiles, user);

        return userResult;

    }
}
