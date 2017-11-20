package com.epages.restdocs.raml;

import static java.util.Collections.emptyMap;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.request.RequestParametersSnippet;

public class RequestParameterHandler implements ParameterHandlerTrait, OperationHandler {

    @Override
    public Map<String, Object> generateModel(Operation operation, RamlResourceSnippetParameters parameters) {
        List<ParameterDescriptorWithRamlType> requestParameters = parameters.getRequestParameters();
        if (!requestParameters.isEmpty()) {
            new RequestParameterSnippetWrapper(requestParameters).validateRequestParameters(operation);
            Map<String, Object> model = new HashMap<>();
            model.put("requestParametersPresent", true);
            model.put("requestParameters", mapParameterDescriptorsToModel(requestParameters));
            return model;
        }
        return emptyMap();
    }

    static class RequestParameterSnippetWrapper extends RequestParametersSnippet {

        RequestParameterSnippetWrapper(List<ParameterDescriptorWithRamlType> descriptors) {
            super(descriptors.stream().map(d -> parameterWithName(d.getName())
                    .description(d.getDescription()))
                    .collect(Collectors.toList()));
        }

        void validateRequestParameters(Operation operation) {
            super.createModel(operation);
        }

    }
}
