package de.hftstuttgart.rest.v1.unittest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.FileInputStream;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    MockMvc mockMvc;

    @Before
    public void setup() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void validUnitTestFileTest() throws Exception {

        File file = new File(Thread.currentThread().getContextClassLoader().getResource("tests.zip").getFile());
        MockMultipartFile mockFile = new MockMultipartFile("unitTestFile", new FileInputStream(file));

        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/v1/unittest")
                .file(mockFile)
                .param("assignmentId", "111"))
                .andExpect(status().is(200));
    }

    @Test
    public void corruptedZipTest() throws Exception {

        File file = new File(Thread.currentThread().getContextClassLoader().getResource("corrupted.zip").getFile());
        MockMultipartFile mockFile = new MockMultipartFile("unitTestFile", new FileInputStream(file));

        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/v1/unittest")
                .file(mockFile)
                .param("assignmentId", "222"))
                .andExpect(status().is(400));
    }

    @Test
    public void renamedTxtFileTest() throws Exception {

        File file = new File(Thread.currentThread().getContextClassLoader().getResource("textfile.zip").getFile());
        MockMultipartFile mockFile = new MockMultipartFile("unitTestFile", new FileInputStream(file));

        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/v1/unittest")
                .file(mockFile)
                .param("assignmentId", "333"))
                .andExpect(status().is(400));

    }

    @Test
    public void noAssignmentIdTest() throws Exception {

        File file = new File(Thread.currentThread().getContextClassLoader().getResource("textfile.zip").getFile());
        MockMultipartFile mockFile = new MockMultipartFile("unitTestFile", new FileInputStream(file));

        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/v1/unittest")
                .file(mockFile))
                .andExpect(status().is(400));

    }

}
