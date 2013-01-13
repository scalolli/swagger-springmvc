package com.mangofactory.swagger.springmvc;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wordnik.swagger.core.ApiProperty;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;

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

	private final Class<?> modelClass;
	private Map<String, ModelProperty> nameVsProperty = Maps.newHashMap();
    public static final String ARRAY = "array";

    public MvcModelResource(Class<?> modelClass)
	{
		this.modelClass = modelClass;
		populateModelProperties();
	}

	private void populateModelProperties() {
        populateProperties(modelClass);
    }

    private void populateProperties(Class classToExtractPropertiesFrom) {
        Field[] fields = classToExtractPropertiesFrom.getDeclaredFields();
        List<Field> fieldList = Arrays.asList(fields);
        List<Field> simpleFields = newArrayList(filter(fieldList, new Predicate<Field>() {
            @Override
            public boolean apply(Field field) {
                return BeanUtils.isSimpleProperty(field.getType());
            }
        }));
        List<Field> complexFields = newArrayList(filter(fieldList, new Predicate<Field>() {
            @Override
            public boolean apply(Field field) {
                return !BeanUtils.isSimpleProperty(field.getType()) && !isListOfValues(field);
            }
        }));
        List<Field> listFields = newArrayList(filter(fieldList, new Predicate<Field>() {
            @Override
            public boolean apply(Field field) {
                return isListOfValues(field);
            }
        }));

        for(Field field : simpleFields) {
            nameVsProperty.put(field.getName(), getModelProperty(field, null));
        }

        for(Field field : complexFields) {
            nameVsProperty.put(field.getName(), getModelProperty(field, null));
            // todo: Not sure if I need to populate the sub type's properties here itself
//            populateProperties(field.getType());
        }

        for(Field listField : listFields) {
            populatePropertiesForList(listField);
        }
    }

    private boolean isListOfValues(Field field) {
        return (field.getType() == List.class || field.getType() == Array.class) && field.getGenericType() instanceof ParameterizedType;
    }

    private void populatePropertiesForList(Field field) {
        Class actualTypeParameter = ((Class)((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
        ModelProperty modelProperty = getModelProperty(field, ARRAY);
        CollectionMemberDescription collectionMemberDescription = new CollectionMemberDescription();
        if(BeanUtils.isSimpleProperty(actualTypeParameter)) {
            collectionMemberDescription.setType(actualTypeParameter.getSimpleName().toLowerCase());
        } else {
            //todo: since it is a complex type we need to populate properties for its fields as well?
//            populateProperties(actualTypeParameter);
            collectionMemberDescription.setReferenceType(actualTypeParameter.getSimpleName().toLowerCase());
        }
        modelProperty.setMemberDescription(collectionMemberDescription);
        nameVsProperty.put(field.getName(), modelProperty);
    }

    private ModelProperty getModelProperty(Field field, String alternateTypeName) {
        ModelProperty modelProperty = new ModelProperty(field.getName(),
                alternateTypeName == null ? field.getType().getSimpleName().toLowerCase() : alternateTypeName);
        PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(modelClass, field.getName());
        ApiProperty apiProperty = AnnotationUtils.findAnnotation(field.getType().getClass(), ApiProperty.class);
        if(apiProperty == null) {
            apiProperty = AnnotationUtils.findAnnotation(descriptor.getReadMethod(), ApiProperty.class);
        }
        if(apiProperty != null) {
            //todo: there are other properties for this annotation how do we handle them?
            if(apiProperty.allowableValues() != null) {
                AllowableValues values = new AllowableValues();
                values.setValues(Arrays.asList(apiProperty.allowableValues().split(",")));
                values.setValueType(AllowableValueType.LIST);
                modelProperty.setAllowableValues(values);
            }
            if(apiProperty.value() != null) {
                modelProperty.setDescription(apiProperty.value());
            }
        }
        return modelProperty;
    }

    public ModelProperty getProperty(String propertyName) {
		return nameVsProperty.get(propertyName);
	}

}
