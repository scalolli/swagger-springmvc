package com.mangofactory.swagger.springmvc;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mangofactory.swagger.springmvc.util.Utils;
import com.wordnik.swagger.core.DocumentationSchema;
import lombok.Getter;
import org.springframework.beans.BeanUtils;
import org.springframework.web.method.HandlerMethod;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;


/**
 * Represents a class from the Model, described in the Swagger format
 *
 * @author martypitt
 */
public class MvcModelResource {
    @Getter
    private HashMap<String, DocumentationSchema> propertyNameVsDocumentation = Maps.newHashMap();

    public MvcModelResource(HandlerMethod handlerMethod) {
        Set<Class> types = Sets.newHashSet();
        //For Collection types handlerMethod.getMethod().getParameterTypes() it just returns the type as List
        //and hence we loose the type information we need to handle them separately.
        for (Class type : handlerMethod.getMethod().getParameterTypes()) {
            if (!Collection.class.isAssignableFrom(type)) {
                types.add(type);
            }
        }

        //now handle the collection types such that, we take into consideration their reference type, by loading all
        //the generic parameter types for this method
        for (Type type : handlerMethod.getMethod().getGenericParameterTypes()) {
            types.add((Class)((ParameterizedTypeImpl) type).getActualTypeArguments()[0]);
        }
        if ((Utils.isListType(handlerMethod.getMethod().getReturnType()) && handlerMethod.getMethod().getGenericReturnType() != null)) {
            //todo: currently we are only supporting a list return type hence this..
            Class referenceType = (Class) ((ParameterizedType) handlerMethod.getMethod().getGenericReturnType()).getActualTypeArguments()[0];
            Collections.addAll(types, referenceType);
        } else {
            Collections.addAll(types, handlerMethod.getMethod().getReturnType());
        }

        populateModels(types);
    }

    private void populateModels(Set<Class> typesToProcess) {
        for (Class parameter : typesToProcess) {
            if (!BeanUtils.isSimpleProperty(parameter)) {
                MvcModelReader modelReader = new MvcModelReader(parameter);
                DocumentationSchema schema = new DocumentationSchema();
                Map<String, DocumentationSchema> subProperties = Maps.newHashMap();
                DocumentationSchema propertySchema = null;
                //todo: refactor this code and also handle the condition when return type is a list
                for (Map.Entry<String, ModelProperty> property : modelReader.getNameVsProperty().entrySet()) {
                    if (!BeanUtils.isSimpleProperty(property.getValue().getClassType())) {
                        if (property.getValue().getClassType() == List.class) {
                            populateModels(Sets.newHashSet((Class) property.getValue().getMemberDescription().getReferencedClassType()));
                            propertySchema = createDocumentationSchemaForList(property);
                        } else {
                            populateModels(Sets.newHashSet((Class) property.getValue().getClassType()));
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
