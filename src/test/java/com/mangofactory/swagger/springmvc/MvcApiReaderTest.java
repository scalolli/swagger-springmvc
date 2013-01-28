package com.mangofactory.swagger.springmvc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.server.test.context.WebContextLoader;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMethod;

import com.mangofactory.swagger.ControllerDocumentation;
import com.mangofactory.swagger.springmvc.controller.DocumentationController;
import com.mangofactory.swagger.springmvc.test.TestConfiguration;
import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.DocumentationOperation;
import com.wordnik.swagger.core.DocumentationParameter;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
		loader=WebContextLoader.class,
		classes=TestConfiguration.class)
public class MvcApiReaderTest {

	@Autowired
	private DocumentationController controller;
	
	@Test
	public void findsDeclaredHandlerMethods() throws IOException {
		Documentation resourceListing = controller.getResourceListing();
		assertThat(resourceListing.getApis(),hasSize(1));
		Documentation petsDocumentation = controller.getApiDocumentation("pets");
		assertThat(petsDocumentation, is(notNullValue()));
        assertNotNull(petsDocumentation.getModels());
//        assertEquals(1, petsDocumentation.getModels().size());
//        assertEquals(6, petsDocumentation.getModels().get("Pet").getProperties().size());
//        ObjectMapper mapper = new ObjectMapper();
//        System.out.println("Json is: " + mapper.defaultPrettyPrintingWriter().writeValueAsString(petsDocumentation));
    }
	
	@Test
	public void findsExpectedMethods()
	{
		ControllerDocumentation petsDocumentation = controller.getApiDocumentation("pets");
		DocumentationOperation operation = petsDocumentation.getEndPoint("/pets/{petId}",RequestMethod.GET);
		assertThat(operation, is(notNullValue()));
		assertThat(operation.getParameters(),hasSize(1));
		DocumentationParameter parameter = operation.getParameters().get(0);
	}
}
