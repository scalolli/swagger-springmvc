package com.mangofactory.swagger.springmvc;

import lombok.Data;

@Data
public class ModelProperty {

	private final String name;
	private final String type;
	private final Class<?> classType;
	private String description;
	private AllowableValues allowableValues;
	
	private CollectionMemberDescription memberDescription;
}
