package de.hftstuttgart.restcontroller;

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

    // For now we use the same path for the unit tests and the task uploads
    @Value("${mojec.dir.uut}")
    private String uutDirPath;

    @RequestMapping(method = RequestMethod.POST)
    public void uploadUnitTestFile(@RequestParam("unitTestFile") MultipartFile unitTestFileRef) throws IOException {
        File file = new File(uutDirPath, unitTestFileRef.getOriginalFilename());
        unitTestFileRef.transferTo(file);
        LOG.info("Uploaded unit test file: " + file);
    }
}
