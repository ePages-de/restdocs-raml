package com.epages.restdocs.raml.jsonschema;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.OBJECT;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.restdocs.payload.FieldDescriptor;

import com.epages.restdocs.raml.jsonschema.JsonSchemaFromFieldDescriptorsGenerator.MultipleNonEqualFieldDescriptors;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import lombok.SneakyThrows;

public class JsonSchemaFromFieldDescriptorsGeneratorTest {

    private JsonSchemaFromFieldDescriptorsGenerator generator = new JsonSchemaFromFieldDescriptorsGenerator();

    private Schema schema;

    private List<FieldDescriptor> fieldDescriptors;

    private String schemaString;

    @Test
    @SuppressWarnings("unchecked")
    public void should_generate_complex_schema() throws IOException {
        givenFieldDescriptors();

        whenSchemaGenerated();

        then(schema).isInstanceOf(ObjectSchema.class);
        ObjectSchema objectSchema = (ObjectSchema) schema;
        then(objectSchema.definesProperty("id"));
        then(objectSchema.getPropertySchemas().get("id")).isInstanceOf(StringSchema.class);

        then(objectSchema.definesProperty("shippingAddress"));
        Schema shippingAddressSchema = objectSchema.getPropertySchemas().get("shippingAddress");
        then(shippingAddressSchema).isInstanceOf(ObjectSchema.class);
        then(shippingAddressSchema.getDescription()).isNotEmpty();

        then(objectSchema.definesProperty("billingAddress"));
        Schema billingAddressSchema = objectSchema.getPropertySchemas().get("billingAddress");
        then(billingAddressSchema).isInstanceOf(ObjectSchema.class);
        then(billingAddressSchema.getDescription()).isNotEmpty();
        then(billingAddressSchema.definesProperty("firstName")).isTrue();
        then(billingAddressSchema.definesProperty("valid")).isTrue();

        then(objectSchema.getPropertySchemas().get("lineItems")).isInstanceOf(ArraySchema.class);
        ArraySchema lineItemSchema = (ArraySchema) objectSchema.getPropertySchemas().get("lineItems");
        then(lineItemSchema.getDescription()).isNull();
        then(lineItemSchema.getAllItemSchema().definesProperty("name")).isTrue();
        then(lineItemSchema.getAllItemSchema().definesProperty("_id")).isTrue();
        then(lineItemSchema.getAllItemSchema().definesProperty("quantity")).isTrue();
        then(lineItemSchema.getAllItemSchema()).isInstanceOf(ObjectSchema.class);

        thenSchemaIsValid();
        thenSchemaValidatesJson("{\n" +
                "    \"id\": \"1\",\n" +
                "    \"lineItems\": [\n" +
                "        {\n" +
                "            \"name\": \"some\",\n" +
                "            \"_id\": \"2\",\n" +
                "            \"quantity\": {\n" +
                "                \"value\": 1,\n" +
                "                \"unit\": \"PIECES\"\n" +
                "            }\n" +
                "        }\n" +
                "    ],\n" +
                "    \"billingAddress\": {\n" +
                "        \"firstName\": \"some\",\n" +
                "        \"valid\": true\n" +
                "    },\n" +
                "    \"paymentLineItem\": {\n" +
                "        \"lineItemTaxes\": [\n" +
                "            {\n" +
                "                \"value\": 1\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}");
    }

    @Test
    public void should_generate_schema_for_top_level_array() {
        givenFieldDescriptorWithTopLevelArray();

        whenSchemaGenerated();

        then(schema).isInstanceOf(ArraySchema.class);
        then(((ArraySchema) schema).getAllItemSchema().definesProperty("id")).isTrue();
        thenSchemaIsValid();
        thenSchemaValidatesJson("[{\"id\": \"some\"}]");
    }

    @Test
    public void should_generate_schema_primitive_array() {
        givenFieldDescriptorWithPrimitiveArray();

        whenSchemaGenerated();

        then(schema).isInstanceOf(ObjectSchema.class);
        thenSchemaIsValid();
        thenSchemaValidatesJson("{\"a\": [1]}");
    }

    @Test
    public void should_fail_on_unknown_field_type() {
        givenFieldDescriptorWithInvalidType();

        thenThrownBy(this::whenSchemaGenerated).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_handle_duplicate_fields() {
        givenEqualFieldDescriptorsWithSamePath();

        whenSchemaGenerated();

        thenSchemaIsValid();
    }

    @Test
    public void should_fail_on_duplicate_fields_with_different_properties() {
        givenDifferentFieldDescriptorsWithSamePath();

        thenThrownBy(this::whenSchemaGenerated).isInstanceOf(MultipleNonEqualFieldDescriptors.class);
    }

    @SneakyThrows
    private void thenSchemaIsValid() {

        final ProcessingReport report = JsonSchemaFactory.byDefault()
                .getSyntaxValidator()
                .validateSchema(JsonLoader.fromString(schemaString));
        then(report.isSuccess()).describedAs("schema invalid - validation failures: %s", report).isTrue();
    }

    private void whenSchemaGenerated() {
        schemaString = generator.generateSchema(fieldDescriptors);
        schema = SchemaLoader.load(new JSONObject(schemaString));
    }

    private void givenFieldDescriptorWithPrimitiveArray() {
        fieldDescriptors = singletonList(fieldWithPath("a[]").description("some").type(ARRAY));
    }
    private void givenFieldDescriptorWithTopLevelArray() {
        fieldDescriptors = singletonList(fieldWithPath("[]['id']").description("some").type(STRING));
    }

    private void givenFieldDescriptorWithInvalidType() {
        fieldDescriptors = singletonList(fieldWithPath("id").description("some").type("invalid-type"));
    }

    private void givenEqualFieldDescriptorsWithSamePath() {
        fieldDescriptors = Arrays.asList(
                fieldWithPath("id").description("some").type(STRING),
                fieldWithPath("id").description("some").type(STRING)
        );
    }

    private void givenDifferentFieldDescriptorsWithSamePath() {
        fieldDescriptors = Arrays.asList(
                fieldWithPath("id").description("some").type(STRING),
                fieldWithPath("id").description("some").type(STRING),
                fieldWithPath("id").description("some").type(STRING).optional()
        );
    }

    private void givenFieldDescriptors() {
        fieldDescriptors =  Arrays.asList(
                fieldWithPath("id").description("some").type(STRING),
                fieldWithPath("lineItems[*].name").description("some").type(STRING),
                fieldWithPath("lineItems[*]._id").description("some").type(STRING),
                fieldWithPath("lineItems[*].quantity.value").description("some").type(NUMBER),
                fieldWithPath("lineItems[*].quantity.unit").description("some").type(STRING),
                fieldWithPath("shippingAddress").description("some").type(OBJECT),
                fieldWithPath("billingAddress").description("some").type(OBJECT),
                fieldWithPath("billingAddress.firstName").description("some").type(STRING),
                fieldWithPath("billingAddress.valid").description("some").type(BOOLEAN),
                fieldWithPath("paymentLineItem.lineItemTaxes").description("some").type(ARRAY)
        );
    }

    private void thenSchemaValidatesJson(String json) {
        schema.validate(json.startsWith("[") ? new JSONArray(json) : new JSONObject(json));
    }
}