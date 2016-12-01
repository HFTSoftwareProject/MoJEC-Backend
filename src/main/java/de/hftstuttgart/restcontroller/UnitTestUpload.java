package de.hftstuttgart.restcontroller;

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

@RestController
@RequestMapping("/v1/unittest")
@MultipartConfig()
public class UnitTestUpload {
    private static final Logger LOG = Logger.getLogger(UnitTestUpload.class);

    @Value("${mojec.dir.parent}")
    private String parentDir;

    @Value("${mojec.dir.assignment.prefix}")
    private String folderNamePrefix;

    @Value("${mojec.dir.test.folder.name}")
    private String testFolderName;

    @RequestMapping(method = RequestMethod.POST)
    public void uploadUnitTestFile(@RequestParam("unitTestFile") MultipartFile unitTestFileRef, @RequestParam("assignmentId") String assignmentId) throws IOException {
        // Create one folder per assignment
        String subFolderPath = parentDir + File.separator + folderNamePrefix + assignmentId + File.separator + testFolderName;
        new File(subFolderPath).mkdirs();
        File file = new File(subFolderPath, unitTestFileRef.getOriginalFilename());
        unitTestFileRef.transferTo(file);
        UnzipUtil.unzip(file);

        LOG.info("Uploaded unit test file: " + file);
    }
}
