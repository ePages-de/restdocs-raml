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

public class PathParameterHandlerTest {

    private PathParameterHandler pathParameterHandler = new PathParameterHandler();

    private Operation operation;

    private Map<String, Object> model;

    @SuppressWarnings("unchecked")
    @Test
    public void should_add_path_parameters_to_model() {
        givenRequestWithoutBody();

        whenGenerateInvokedWithPathParameters();

        then(model).containsEntry("pathParametersPresent", true);
        then(model).containsKey("pathParameters");
        then(model.get("pathParameters")).isInstanceOf(List.class);
        List<Map<String, Object>> pathParameters = (List<Map<String, Object>>) model.get("pathParameters");
        then(pathParameters).hasSize(2);
        then(pathParameters.get(0)).containsEntry("name", "id");
        then(pathParameters.get(0)).containsEntry("type", "string");
        then(pathParameters.get(0)).containsEntry("description", "an id");

        then(pathParameters.get(1)).containsEntry("name", "other");
        then(pathParameters.get(1)).containsEntry("type", "integer");
        then(pathParameters.get(1)).containsEntry("description", "other");
    }

    @Test
    public void should_do_nothing_if_no_path_parameters_documented() {
        givenRequestWithoutBody();

        whenGenerateInvokedWithoutPathParameters();

        then(model).isEmpty();
    }

    @Test
    public void should_fail_on_invalid_path_parameter_documentation() {
        givenRequestWithoutBody();

        thenThrownBy(this::whenGenerateInvokedWithInvalidPathParameters).isInstanceOf(SnippetException.class);
    }

    private void whenGenerateInvokedWithPathParameters() {
        model = pathParameterHandler.generateModel(operation, RamlResourceSnippetParameters.builder()
                .pathParameters(
                        parameterWithName("id").description("an id"),
                        parameterWithName("other").type(INTEGER).description("other")
                ).build());
    }

    private void whenGenerateInvokedWithInvalidPathParameters() {
        model = pathParameterHandler.generateModel(operation, RamlResourceSnippetParameters.builder()
                .pathParameters(
                        parameterWithName("id").description("an id"),
                        parameterWithName("other-x").type(INTEGER).description("other")
                ).build());
    }

    private void whenGenerateInvokedWithoutPathParameters() {
        model = pathParameterHandler.generateModel(operation, RamlResourceSnippetParameters.builder().build());
    }

    private void givenRequestWithoutBody() {
        operation = new OperationBuilder()
                .attribute(ATTRIBUTE_NAME_URL_TEMPLATE, "http://localhost:8080/some/{id}/other/{other}")
                .request("http://localhost:8080/some/12/other/34")
                .method("POST")
                .build();
    }
}