package de.hftstuttgart.restcontroller;

import de.hftstuttgart.models.UserResult;
import de.hftstuttgart.utils.JUnitTestHelper;
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
import java.util.UUID;

@RestController
@RequestMapping("/v1/task")
@MultipartConfig()
public class TaskUpload {
    private static final Logger LOG = Logger.getLogger(TaskUpload.class);

    @Value("${mojec.dir.parent}")
    private String parentPath;

    @Value("${mojec.dir.junit}")
    private String junitLibDirPath;

    @Value("${mojec.dir.assignment.prefix}")
    private String assignmentFolderPrefix;

    @Value("${mojec.dir.test.folder.name}")
    private String testFolderName;

    @RequestMapping(method = RequestMethod.POST)
    public UserResult uploadAndTestFile(@RequestParam("taskFile") MultipartFile taskFileRef, @RequestParam("assignmentId") String assignmentId) throws IOException, ClassNotFoundException {
        // Save the zip file in the assignment folder
        String assignmentFolderPath = parentPath + File.separator + assignmentFolderPrefix + assignmentId;
        File taskFile = new File(assignmentFolderPath, String.valueOf(UUID.randomUUID())); // UUID to keep this file name unique
        taskFileRef.transferTo(taskFile);

        List<File> unzippedFiles = UnzipUtil.unzip(taskFile);

        JUnitTestHelper testHelper = new JUnitTestHelper(junitLibDirPath);
        LOG.info("Uploaded File: " + taskFile);
        UserResult userResult;
        try {
            userResult = testHelper.runUnitTests(parentPath, assignmentFolderPrefix, assignmentId, testFolderName, unzippedFiles);
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
