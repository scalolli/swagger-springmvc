package com.mangofactory.swagger.springmvc;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.google.common.collect.Maps;
import com.mangofactory.swagger.ControllerDocumentation;
import com.mangofactory.swagger.SwaggerConfiguration;
import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.DocumentationEndPoint;
import com.wordnik.swagger.core.DocumentationOperation;
import com.wordnik.swagger.core.DocumentationSchema;

@Slf4j
/**
 * Reader which scans an ApplicationContext, looking for methods or
 * classes annotated with <code>@RequestMapping</code>.
 * 
 * Discovered methods are then handed off to be introspected, and have their 
 * corresponding documentation generated.
 * @author martypitt
 *
 */
public class MvcApiReader {

	private final WebApplicationContext context;
	private final SwaggerConfiguration config;
	@Getter
	private Map<String, HandlerMapping> handlerMappingBeans;
	
	@Getter
	private Documentation resourceListing;
	
	private final Map<Class<?>,DocumentationEndPoint> resourceListCache = Maps.newHashMap();
	private final Map<Class<?>,ControllerDocumentation> apiCache = Maps.newHashMap();
    private final HashMap<String, DocumentationSchema> propertyNameVsDocumentation = new HashMap<>();
	
	public MvcApiReader(WebApplicationContext context, SwaggerConfiguration swaggerConfiguration)
	{
		this.context = context;
		config = swaggerConfiguration;
		handlerMappingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.context, HandlerMapping.class, true, false);
		buildMappingDocuments();
	}
	
	private void buildMappingDocuments() {
		resourceListing = config.newDocumentation();
        resourceListing.setModels(propertyNameVsDocumentation);
		
		log.debug("Discovered {} candidates for documentation",handlerMappingBeans.size());
		for (HandlerMapping handlerMapping : handlerMappingBeans.values())
		{
			if (RequestMappingHandlerMapping.class.isAssignableFrom(handlerMapping.getClass()))
			{
				processMethod((RequestMappingHandlerMapping) handlerMapping);
			} else {
				log.debug("Not documenting mapping of type {}, as it is not of a recognized type.",handlerMapping.getClass().getName());
			}
		}
	}

	private void addApiListingIfMissing(
			MvcApiResource resource) {
		if (resourceListCache.containsKey(resource.getControllerClass()))
			return;
		
		DocumentationEndPoint endpoint = resource.describeAsEndpoint();
		if (endpoint != null)
		{
			resourceListCache.put(resource.getControllerClass(),endpoint);
			log.debug("Added resource listing: {}",resource.toString());
			resourceListing.addApi(endpoint);
		}
	}

	private void processMethod(RequestMappingHandlerMapping handlerMapping) {
		for (Entry<RequestMappingInfo, HandlerMethod> entry : handlerMapping.getHandlerMethods().entrySet()) {
			HandlerMethod handlerMethod = entry.getValue();
			RequestMappingInfo mappingInfo = entry.getKey();
			
			MvcApiResource resource = new MvcApiResource(handlerMethod,config);
			
			// Don't document our own controllers
			if (resource.isInternalResource())
				continue;
			
			addApiListingIfMissing(resource);
			
			ControllerDocumentation apiDocumentation = getApiDocumentation(resource);
            addModels(handlerMethod, apiDocumentation);

            for (String requestUri : mappingInfo.getPatternsCondition().getPatterns())
			{
				DocumentationEndPoint endPoint = apiDocumentation.getEndPoint(requestUri);
				appendOperationsToEndpoint(mappingInfo,handlerMethod,endPoint);
				
			}
		}
	}

	private ControllerDocumentation getApiDocumentation(MvcApiResource resource) {
		if (!apiCache.containsKey(resource.getControllerClass()))
		{
			ControllerDocumentation emptyApiDocumentation = resource.createEmptyApiDocumentation();
			if (emptyApiDocumentation != null)
				apiCache.put(resource.getControllerClass(),emptyApiDocumentation);
		}
		return apiCache.get(resource.getControllerClass());
	}

	private void appendOperationsToEndpoint(
			RequestMappingInfo mappingInfo, HandlerMethod handlerMethod, DocumentationEndPoint endPoint) {
		ApiMethodReader methodDoc = new ApiMethodReader(handlerMethod);
		for (RequestMethod requestMethod : mappingInfo.getMethodsCondition().getMethods())
		{
			DocumentationOperation operation = methodDoc.getOperation(requestMethod);
			endPoint.addOperation(operation);
		}
	}

	private void addModels(HandlerMethod handlerMethod, ControllerDocumentation apiDocumentation) {
		MvcModelResource resource = new MvcModelResource(handlerMethod);
        HashMap<String, DocumentationSchema> nameVsDocumentation = resource.getPropertyNameVsDocumentation();
        //set the models for this method directly on the api documentation.
        if (apiDocumentation.getModels() == null) {
            apiDocumentation.setModels(nameVsDocumentation);
        } else {
            apiDocumentation.getModels().putAll(nameVsDocumentation);
        }
        propertyNameVsDocumentation.putAll(nameVsDocumentation);
	}

	public ControllerDocumentation getDocumentation(
			String apiName) {

		for (ControllerDocumentation documentation : apiCache.values())
		{
			if (documentation.matchesName(apiName))
				return documentation;
		}
		log.error("Could not find a matching resource for api with name '" + apiName + "'");
		return null;
	}
}