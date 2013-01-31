package com.mangofactory.swagger.springmvc;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.mangofactory.swagger.springmvc.util.Util;
import com.wordnik.swagger.core.ApiProperty;
import lombok.Getter;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: basu
 * Date: 26/1/13
 * Time: 11:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class MvcModelReader {

    private final Class<?> modelClass;
    @Getter
    private Map<String, ModelProperty> nameVsProperty = Maps.newHashMap();
    public static final String ARRAY = "Array";
    public static final String LIST = "List";

    public MvcModelReader(Class<?> modelClass)
    {
        this.modelClass = modelClass;
        populateModelProperties();
    }

    private void populateModelProperties() {
        if (Util.isListType(modelClass)) {
            //populate properties for list type since we want to support only list types for now
//            populatePropertiesForList(modelClass.getTypeParameters()[0]);
        } else {
            populatePropertiesForNonCollectionType(modelClass);
        }
    }

    private void populatePropertiesForNonCollectionType(Class classToExtractPropertiesFrom) {
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
                return !BeanUtils.isSimpleProperty(field.getType()) && !Util.isListType(field.getClass());
            }
        }));
        List<Field> listFields = newArrayList(filter(fieldList, new Predicate<Field>() {
            @Override
            public boolean apply(Field field) {
                return Util.isListType(field.getType());
            }
        }));

        for(Field field : simpleFields) {
            nameVsProperty.put(field.getName(), getModelProperty(field, null));
        }

        for(Field field : complexFields) {
            nameVsProperty.put(field.getName(), getModelProperty(field, null));
            // todo: Not sure if I need to populate the sub type's properties here itself
//            populatePropertiesForNonCollectionType(field.getType());
        }

        for(Field listField : listFields) {
            populatePropertiesForList(listField);
        }
    }

    private void populatePropertiesForList(Field field) {
        Class actualTypeParameter = ((Class)((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
        ModelProperty modelProperty = getModelProperty(field, ARRAY);
        CollectionMemberDescription collectionMemberDescription = new CollectionMemberDescription();
        if(BeanUtils.isSimpleProperty(actualTypeParameter)) {
            collectionMemberDescription.setType(actualTypeParameter.getSimpleName());
        } else {
            //todo: since it is a complex type we need to populate properties for its fields as well?
//            populatePropertiesForNonCollectionType(actualTypeParameter);
            collectionMemberDescription.setReferenceType(actualTypeParameter.getSimpleName());
        }
        collectionMemberDescription.setReferencedClassType(actualTypeParameter);
        modelProperty.setMemberDescription(collectionMemberDescription);
        nameVsProperty.put(field.getName(), modelProperty);
    }

    private ModelProperty getModelProperty(Field field, String alternateTypeName) {
        ModelProperty modelProperty = new ModelProperty(field.getName(),
                alternateTypeName == null ? field.getType().getSimpleName() : alternateTypeName, field.getType());
        PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(modelClass, field.getName());
        //first try to find if the @ApiProperty annotation has been applied on the field
        ApiProperty apiProperty = AnnotationUtils.findAnnotation(field.getType().getClass(), ApiProperty.class);
        //if the annotation @ApiProperty has not been applied on the field, then check if it has been applied on the
        //read method.
        if(apiProperty == null && descriptor != null) {
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
