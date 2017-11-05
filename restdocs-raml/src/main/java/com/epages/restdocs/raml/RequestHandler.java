package com.epages.restdocs.raml;

import static java.util.Collections.emptyMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.util.StringUtils;

public class RequestHandler extends AbstractFieldHandler {

    static final String REQUEST_BODY_FILE_NAME_SUFFIX = "-request.json";

    @Override
    public Map<String, Object> generateModel(Operation operation, RamlResourceSnippetParameters parameters) {
        final OperationRequest request = operation.getRequest();

        if (!StringUtils.isEmpty(request.getContentAsString())) {
            Map<String, Object> model = new HashMap<>();
            model.put("requestBodyFileName", getRequestFileName(operation));
            model.put("requestBodyPresent", true);
            model.put("contentTypeRequest", request.getHeaders().getContentType().getType() + "/" + request.getHeaders().getContentType().getSubtype());
            if (!parameters.getRequestFieldDescriptors().isEmpty()) {
                validateRequestFieldsAndInferTypeInformation(operation, parameters);
                model.put("requestFields", transformDescriptorsToModel(parameters.getRequestFieldDescriptors()));
                model.put("requestFieldsPresent", true);
            }
            return model;
        }
        return emptyMap();
    }

    private void validateRequestFieldsAndInferTypeInformation(Operation operation, RamlResourceSnippetParameters parameters) {
        new RequestFieldsSnippetWrapper(parameters.getRequestFieldDescriptors()).validateFieldsAndInferTypeInformation(operation);
    }

    private String getRequestFileName(Operation operation) {
        return operation.getName() + REQUEST_BODY_FILE_NAME_SUFFIX;
    }

    /**
     * We need the wrapper to take advantage of the validation of fields and the inference of type information.
     *
     * This is baked into {@link org.springframework.restdocs.payload.AbstractFieldsSnippet#createModel(Operation)} and is not accessible separately.
     */
    static class RequestFieldsSnippetWrapper extends RequestFieldsSnippet {

        RequestFieldsSnippetWrapper(List<FieldDescriptor> descriptors) {
            super(descriptors);
        }

        void validateFieldsAndInferTypeInformation(Operation operation) {
            super.createModel(operation);
        }
    }
}
