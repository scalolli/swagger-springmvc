package com.mangofactory.swagger.springmvc;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.mangofactory.swagger.springmvc.test.Pet;

public class MvcModelReaderTest {

	private MvcModelReader model;
	@Before
	public void setup()
	{
		model = new MvcModelReader(Pet.class);
	}
	@Test
	public void describesStringPropertyCorrectly()
	{
		ModelProperty property = model.getProperty("name");
		assertThat(property.getName(), equalTo("name"));
		assertThat(property.getType(),equalTo("string"));
	}
	@Test
	public void describesLongPropertyCorrectly()
	{
		ModelProperty property = model.getProperty("id");
		assertThat(property.getType(), equalTo("long"));
	}
	@Test
	public void describesCollectionOfPrimitiveCorrectly()
	{
		ModelProperty property = model.getProperty("photoUrls");
		assertThat(property.getType(), equalTo("array"));
		CollectionMemberDescription memberDescription = property.getMemberDescription();
		assertThat(memberDescription, notNullValue());
		assertThat(memberDescription.getType(),equalTo("string"));
	}
	@Test
	public void describesCollectionOfReferenceTypesCorrectly()
	{
		ModelProperty property = model.getProperty("tags");
		assertThat(property.getType(), equalTo("array"));
		CollectionMemberDescription memberDescription = property.getMemberDescription();
		assertThat(memberDescription, notNullValue());
		assertThat(memberDescription.getReferenceType(),equalTo("tag"));
	}
	@Test
	public void describesReferenceTypeCorrectly()
	{
		ModelProperty property = model.getProperty("category");
		assertThat(property.getType(), equalTo("category"));
	}
	@Test
	public void includesDescriptionInProperty()
	{
		ModelProperty property = model.getProperty("status");
		assertThat(property.getDescription(),equalTo("pet status in the store"));
	}
	@Test
	public void propertyIncludesAllowableValues()
	{
		ModelProperty property = model.getProperty("status");
		AllowableValues allowableValues = property.getAllowableValues();
		assertThat(allowableValues,notNullValue());
		assertThat(allowableValues.getValueType(), equalTo(AllowableValueType.LIST));
		assertThat(allowableValues.getValues(), contains("available","pending","sold"));
	}
	
}
