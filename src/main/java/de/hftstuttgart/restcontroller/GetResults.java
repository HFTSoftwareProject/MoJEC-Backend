package de.hftstuttgart.restcontroller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


@RestController
@RequestMapping("/v1/results")
public class GetResults {
    private static final Logger LOG = Logger.getLogger(GetResults.class);

    @Value("${mojec.dir.results}")
    private String resultsDir;

    /**
     * localhost:8080/v1/results
     */
    @RequestMapping(method = RequestMethod.GET)
    private String getResultsCsv() {
        String result = null;
        try {
            result = new String(Files.readAllBytes(Paths.get(resultsDir)));
        } catch (IOException e) {
            LOG.error("Failed to read results. Path: " + resultsDir, e);
        }
        return result;
    }
}
