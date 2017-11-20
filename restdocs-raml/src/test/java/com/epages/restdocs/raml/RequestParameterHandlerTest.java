package com.epages.restdocs.raml;

import static com.epages.restdocs.raml.ParameterDescriptorWithRamlType.RamlScalarType.INTEGER;
import static com.epages.restdocs.raml.RamlResourceDocumentation.parameterWithName;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.springframework.restdocs.generate.RestDocumentationGenerator.ATTRIBUTE_NAME_URL_TEMPLATE;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.SnippetException;

public class RequestParameterHandlerTest {

    private RequestParameterHandler requestParameterHandler = new RequestParameterHandler();

    private Operation operation;

    private Map<String, Object> model;

    @Test
    @SuppressWarnings("unchecked")
    public void should_add_request_parameters_to_model() {
        givenRequest();

        whenGenerateInvokedWithParameters();

        then(model).containsEntry("requestParametersPresent", true);
        then(model).containsKey("requestParameters");
        then(model.get("requestParameters")).isInstanceOf(List.class);
        List<Map<String, Object>> pathParameters = (List<Map<String, Object>>) model.get("requestParameters");
        then(pathParameters).hasSize(2);
        then(pathParameters.get(0)).containsEntry("name", "test-param-string");
        then(pathParameters.get(0)).containsEntry("type", "string");
        then(pathParameters.get(0)).containsEntry("description", "some");

        then(pathParameters.get(1)).containsEntry("name", "test-param-int");
        then(pathParameters.get(1)).containsEntry("type", "integer");
        then(pathParameters.get(1)).containsEntry("description", "other");
    }

    @Test
    public void should_do_nothing_if_no_path_parameters_documented() {
        givenRequest();

        whenGenerateInvokedWithoutParameters();

        then(model).isEmpty();
    }

    @Test
    public void should_fail_on_invalid_path_parameter_documentation() {
        givenRequest();

        thenThrownBy(this::whenGenerateInvokedWithInvalidParameters).isInstanceOf(SnippetException.class);
    }

    private void whenGenerateInvokedWithParameters() {
        model = requestParameterHandler.generateModel(operation, RamlResourceSnippetParameters.builder()
                .requestParameters(
                        parameterWithName("test-param-string").description("some"),
                        parameterWithName("test-param-int").type(INTEGER).description("other")
                ).build());
    }

    private void whenGenerateInvokedWithInvalidParameters() {
        model = requestParameterHandler.generateModel(operation, RamlResourceSnippetParameters.builder()
                .requestParameters(
                        parameterWithName("test-param-string").description("an id"),
                        parameterWithName("other-x").type(INTEGER).description("other")
                ).build());
    }

    private void whenGenerateInvokedWithoutParameters() {
        model = requestParameterHandler.generateModel(operation, RamlResourceSnippetParameters.builder().build());
    }

    private void givenRequest() {
        operation = new OperationBuilder()
                .attribute(ATTRIBUTE_NAME_URL_TEMPLATE, "http://localhost:8080/some")
                .request("http://localhost:8080/some")
                .param("test-param-string", "some")
                .param("test-param-int", "1")
                .method("POST")
                .build();
    }
}