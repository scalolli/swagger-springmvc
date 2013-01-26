package com.mangofactory.swagger.springmvc;

import lombok.Data;

@Data
public class CollectionMemberDescription {

	private String type;
	private String referenceType;
    private Class referencedClassType;
}
