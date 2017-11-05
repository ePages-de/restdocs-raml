package com.epages.restdocs.raml;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import java.util.List;
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
    public void should_add_field_information() {
        givenResponseWithJsonBody();

        whenModelGeneratedWithFieldDescriptors(fieldWithPath("comment").description("some"));

        then(model).containsEntry("responseFieldsPresent", true);
        then(model).containsKeys("responseFields");
        List<Map<Object, Object>> requestFields = (List<Map<Object, Object>>) model.get("responseFields");
        then(requestFields).hasSize(1);
        then(requestFields.get(0)).containsEntry("path", "comment");
        then(requestFields.get(0)).containsEntry("description", "some");
        then(requestFields.get(0)).containsEntry("type", "string");
        then(requestFields.get(0)).containsEntry("required", true);
        then(requestFields.get(0)).containsEntry("last", true);
    }

    private void whenModelGenerated() {
        model = responseHandler.generateModel(operation, RamlResourceSnippetParameters.builder().build());
    }
    private void whenModelGeneratedWithFieldDescriptors(FieldDescriptor... fieldDescriptors) {
        model = responseHandler.generateModel(operation, RamlResourceSnippetParameters.builder()
                .responseFieldDescriptors(fieldDescriptors)
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

    private void givenResponseWithoutBody() {
        operation = new OperationBuilder()
                .response()
                .status(200)
                .build();
    }
}