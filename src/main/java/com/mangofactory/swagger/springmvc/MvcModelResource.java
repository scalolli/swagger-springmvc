package com.mangofactory.swagger.springmvc;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wordnik.swagger.core.ApiProperty;
import com.wordnik.swagger.core.DocumentationSchema;
import lombok.Getter;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;

import javax.annotation.Nullable;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;


/**
 * Represents a class from the Model, described in the Swagger format
 * @author martypitt
 *
 */
public class MvcModelResource {
    // NOTE : Currently, this class handles the parsing, as well as the POJO aspects.
	// should probably split this out later.

    @Getter
    private HashMap<String, DocumentationSchema> propertyNameVsDocumentation = Maps.newHashMap();

    public MvcModelResource(HandlerMethod handlerMethod) {
        List<Class> types = Lists.newArrayList();
        Collections.addAll(types, handlerMethod.getMethod().getParameterTypes());
        Collections.addAll(types, handlerMethod.getMethod().getReturnType());
        populateModels(types);
    }

    private void populateModels(List<Class> typesToProcess) {
       for(Class parameter : typesToProcess) {
           if (!BeanUtils.isSimpleProperty(parameter)) {
               MvcModelReader modelReader = new MvcModelReader(parameter);
               DocumentationSchema schema = new DocumentationSchema();
               Map<String, DocumentationSchema> subProperties = Maps.newHashMap();
               DocumentationSchema propertySchema = null;
               //todo: refactor this code and also handle the condition when return type is a list
               for(Map.Entry<String, ModelProperty> property : modelReader.getNameVsProperty().entrySet()) {
                   if (!BeanUtils.isSimpleProperty(property.getValue().getClassType())) {
                       if (property.getValue().getClassType() == List.class) {
                           populateModels(Arrays.asList((Class) property.getValue().getMemberDescription().getReferencedClassType()));
                           propertySchema = createDocumentationSchemaForList(property);
                       } else {
                           populateModels(Arrays.asList((Class) property.getValue().getClassType()));
                           propertySchema = createDocumentationSchema(property);
                       }
                   } else {
                      propertySchema = createDocumentationSchema(property);
                   }
                   subProperties.put(property.getKey(), propertySchema);
               }
               schema.setProperties(subProperties);
               this.propertyNameVsDocumentation.put(parameter.getSimpleName(), schema);
           }
       }
    }

    private DocumentationSchema createDocumentationSchemaForList(Map.Entry<String, ModelProperty> property) {
        DocumentationSchema schema = new DocumentationSchema();
        schema.setType("Array");
        DocumentationSchema tagSchema = new DocumentationSchema();
        tagSchema.setType(property.getValue().getMemberDescription().getReferencedClassType().getSimpleName());
        schema.setItems(tagSchema);
        return schema;
    }

    private DocumentationSchema createDocumentationSchema(Map.Entry<String, ModelProperty> value) {
        DocumentationSchema schema = new DocumentationSchema();
        schema.setType(value.getValue().getType());
        schema.setDescription(value.getValue().getDescription());
        return schema;
    }
}
