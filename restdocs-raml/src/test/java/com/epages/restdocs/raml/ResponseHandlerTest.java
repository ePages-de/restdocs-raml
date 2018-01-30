package com.epages.restdocs.raml;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import java.util.Map;

import org.junit.Test;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.snippet.SnippetException;

public class ResponseHandlerTest {

    private ResponseHandler responseHandler = new ResponseHandler();

    private Operation operation;

    private Map<String, Object> model;

    @Test
    public void should_add_response_information_to_model() {
        givenResponseWithJsonBody();

        whenModelGenerated();

        then(model).containsOnlyKeys("responseBodyFileName", "responseBodyPresent", "contentTypeResponse");
        then(model.get("responseBodyPresent")).isEqualTo(true);
        then(model.get("responseBodyFileName")).isEqualTo("test-response.json");
        then(model.get("contentTypeResponse")).isEqualTo(APPLICATION_JSON_VALUE);
    }

    @Test
    public void should_do_nothing_on_empty_body() {
        givenResponseWithoutBody();

        whenModelGenerated();

        then(model).isEmpty();
    }

    @Test
    public void should_fail_on_missing_field_documentation() {
        givenResponseWithJsonBody();

        thenThrownBy(() -> whenModelGeneratedWithFieldDescriptors(fieldWithPath("another").description("some")))
                .isInstanceOf(SnippetException.class)
                .hasMessageContaining("Fields with the following paths were not found in the payload: [another]")
                .hasMessageContaining("comment")
        ;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_add_schema_file() {
        givenResponseWithJsonBody();

        whenModelGeneratedWithFieldDescriptors(fieldWithPath("comment").description("some"));

        then(model).containsEntry("responseFieldsPresent", true);
        then(model).containsEntry("responseSchemaFileName", "test-schema-response.json");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_default_content_type_to_json() {
        givenResponseWithJsonBodyWithoutContentType();

        whenModelGeneratedWithFieldDescriptors(fieldWithPath("comment").description("some"));

        then(model.get("contentTypeResponse")).isEqualTo(APPLICATION_JSON_VALUE);
    }

    private void whenModelGenerated() {
        model = responseHandler.generateModel(operation, RamlResourceSnippetParameters.builder().build());
    }
    private void whenModelGeneratedWithFieldDescriptors(FieldDescriptor... fieldDescriptors) {
        model = responseHandler.generateModel(operation, RamlResourceSnippetParameters.builder()
                .responseFields(fieldDescriptors)
                .build());
    }

    private void givenResponseWithJsonBody() {
        operation = new OperationBuilder()
                .response()
                .status(200)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .content("{\"comment\": \"some\"}")
                .build();
    }

    private void givenResponseWithJsonBodyWithoutContentType() {
        operation = new OperationBuilder()
                .response()
                .status(200)
                .content("{\"comment\": \"some\"}")
                .build();
    }

    private void givenResponseWithoutBody() {
        operation = new OperationBuilder()
                .response()
                .status(200)
                .build();
    }
}