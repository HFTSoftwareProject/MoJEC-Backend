package de.hftstuttgart.restcontroller;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("v1/test")
public class RunAnt {
   private static final Logger LOG = Logger.getLogger(RunAnt.class);

   /**
    * localhost:8080/v1/test
    * Runs the ant test script by running a shell script which runs the ant script (yeah...)
    */
   @RequestMapping(method = RequestMethod.POST)
   private void runAntScript() {
      try {
         Runtime.getRuntime().exec("sh runant.sh");
      } catch (IOException e) {
         LOG.error("Failed to run runant.sh script.", e);
      }
   }
}
