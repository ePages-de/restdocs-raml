package com.epages.restdocs.raml;

import static java.util.Collections.emptyMap;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.request.PathParametersSnippet;

public class PathParameterHandler implements ParameterHandlerTrait, OperationHandler {

    @Override
    public Map<String, Object> generateModel(Operation operation, RamlResourceSnippetParameters parameters) {
        List<ParameterDescriptorWithRamlType> pathParameters = parameters.getPathParameters();
        if (!pathParameters.isEmpty()) {
            new PathParametersSnippetWrapper(pathParameters).validatePathParameters(operation);
            Map<String, Object> model = new HashMap<>();
            model.put("pathParametersPresent", true);
            model.put("pathParameters", mapParameterDescriptorsToModel(pathParameters));
            return model;
        }
        return emptyMap();
    }

    static class PathParametersSnippetWrapper extends PathParametersSnippet {

        PathParametersSnippetWrapper(List<ParameterDescriptorWithRamlType> descriptors) {
            super(descriptors.stream().map(d -> parameterWithName(d.getName())
                    .description(d.getDescription()))
                    .collect(Collectors.toList()));
        }

        void validatePathParameters(Operation operation) {
            super.createModel(operation);
        }
    }
}
