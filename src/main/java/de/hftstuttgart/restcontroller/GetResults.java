package de.hftstuttgart.restcontroller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/v1/results")
public class GetResults {

   /**
    * localhost:8080/v1/results
    */
   @RequestMapping(method = RequestMethod.GET)
   private String getResultsCsv() {
      String result = null;
      try {
         result = new String(Files.readAllBytes(Paths.get("../JUTA3/07-results/report/Report.csv")));
      } catch (IOException e) {
         e.printStackTrace();
      }
      return result;
   }

}
