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

public class RequestHandlerTest {

    private RequestHandler requestHandler = new RequestHandler();

    private Operation operation;

    private Map<String, Object> model;

    @Test
    public void should_add_request_information_to_model() {
        givenRequestWithJsonBody();

        whenModelGenerated();

        then(model).containsOnlyKeys("requestBodyFileName", "requestBodyPresent", "contentTypeRequest");
        then(model.get("requestBodyPresent")).isEqualTo(true);
        then(model.get("requestBodyFileName")).isEqualTo("test-request.json");
        then(model.get("contentTypeRequest")).isEqualTo(APPLICATION_JSON_VALUE);
    }

    @Test
    public void should_do_nothing_on_empty_body() {
        givenRequestWithoutBody();

        whenModelGenerated();

        then(model).isEmpty();
    }

    @Test
    public void should_fail_on_missing_field_documentation() {
        givenRequestWithJsonBody();

        thenThrownBy(() -> whenModelGeneratedWithFieldDescriptors(fieldWithPath("another").description("some")))
                .isInstanceOf(SnippetException.class)
                .hasMessageContaining("Fields with the following paths were not found in the payload: [another]")
                .hasMessageContaining("comment")
        ;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_add_field_information() {
        givenRequestWithJsonBody();

        whenModelGeneratedWithFieldDescriptors(fieldWithPath("comment").description("some"));

        then(model).containsEntry("requestFieldsPresent", true);
        then(model).containsKeys("requestFields");
        List<Map<Object, Object>> requestFields = (List<Map<Object, Object>>) model.get("requestFields");
        then(requestFields).hasSize(1);
        then(requestFields.get(0)).containsEntry("path", "comment");
        then(requestFields.get(0)).containsEntry("description", "some");
        then(requestFields.get(0)).containsEntry("type", "string");
        then(requestFields.get(0)).containsEntry("required", true);
        then(requestFields.get(0)).containsEntry("last", true);
    }

    private void whenModelGenerated() {
        model = requestHandler.generateModel(operation, RamlResourceSnippetParameters.builder().build());
    }
    private void whenModelGeneratedWithFieldDescriptors(FieldDescriptor... fieldDescriptors) {
        model = requestHandler.generateModel(operation, RamlResourceSnippetParameters.builder()
                .requestFieldDescriptors(fieldDescriptors)
                .build());
    }

    private void givenRequestWithJsonBody() {
        operation = new OperationBuilder()
                .request("http://localhost:8080/some/123")
                .method("POST")
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .content("{\"comment\": \"some\"}")
                .build();
    }

    private void givenRequestWithoutBody() {
        operation = new OperationBuilder()
                .request("http://localhost:8080/some/123")
                .method("POST")
                .build();
    }
}