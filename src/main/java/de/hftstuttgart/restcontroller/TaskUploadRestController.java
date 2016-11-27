package de.hftstuttgart.restcontroller;

import com.google.common.io.Files;
import com.google.gson.Gson;
import de.hftstuttgart.utils.JUnitTestHelper;
import de.hftstuttgart.exceptions.FileTypeNotSupportedException;
import de.hftstuttgart.models.UserResult;
import de.hftstuttgart.utils.UnzipUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Value("${mojec.dir.junit}")
    private String junitLibDirPath;

    @RequestMapping(method = RequestMethod.POST)
    public UserResult uploadAndTestFile(@RequestParam("taskFile") MultipartFile taskFileRef) throws IOException, ClassNotFoundException {
        File taskFile = new File(uutDirPath, taskFileRef.getOriginalFilename());
        taskFileRef.transferTo(taskFile);
        String fileExtension = Files.getFileExtension(taskFile.getName());

        if (!"zip".equals(fileExtension)) {
            throw new FileTypeNotSupportedException("The file type " + fileExtension + " is not supported. Only 'zip' is supported.");
        }

        List<File> unzippedFiles = UnzipUtil.unzip(taskFile);

        Gson gson= new Gson();
        JUnitTestHelper testHelper = new JUnitTestHelper(junitLibDirPath);
        LOG.info("Uploaded File: " + taskFile);
        UserResult userResult = null;
        try {
            userResult = testHelper.runUnitTests(uutDirPath, unzippedFiles);
        } finally {
            deleteCreatedFiles(unzippedFiles, testHelper.getCompileOutputDir());
        }

        return userResult;

    }

    private void deleteCreatedFiles(List<File> unzippedFiles, File compileOutputDir) {
        // Delete all .java files
        for (File file : unzippedFiles) {
            if (file.exists()) {
                file.delete();
            }
        }

        // delete compiler output dir. Currently only one flat folder is supported
        if (compileOutputDir.exists()) {
            // Delete all containing files
            for (File file : compileOutputDir.listFiles()) {
                if (file.exists()) {
                    file.delete();
                }
            }
            // Delete the directory.
            compileOutputDir.delete();
        }

    }
}
