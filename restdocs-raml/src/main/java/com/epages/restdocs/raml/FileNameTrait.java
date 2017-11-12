package com.epages.restdocs.raml;

import org.springframework.restdocs.operation.Operation;
import org.springframework.util.StringUtils;

public interface FileNameTrait {

    String REQUEST_FILE_NAME_SUFFIX = "-request.json";

    String RESPONSE_FILE_NAME_SUFFIX = "-response.json";

    String SCHEMA_PART = "-schema";

    default String getRequestFileName(String operationName) {
        return operationName + REQUEST_FILE_NAME_SUFFIX;
    }

    default String getResponseFileName(String operationName) {
        return operationName + RESPONSE_FILE_NAME_SUFFIX;
    }

    default boolean shouldGenerateRequestSchemaFile(Operation operation, RamlResourceSnippetParameters ramlResourceSnippetParameters) {
        return !StringUtils.isEmpty(operation.getRequest().getContentAsString()) && !ramlResourceSnippetParameters.getRequestFields().isEmpty();
    }

    default String getRequestSchemaFileName(String operationName) {
        return operationName + SCHEMA_PART + REQUEST_FILE_NAME_SUFFIX;
    }

    default boolean shouldGenerateResponseSchemaFile(Operation operation, RamlResourceSnippetParameters ramlResourceSnippetParameters) {
        return !StringUtils.isEmpty(operation.getResponse().getContentAsString()) && !ramlResourceSnippetParameters.getResponseFields().isEmpty();
    }

    default String getResponseSchemaFileName(String operationName) {
        return operationName + SCHEMA_PART + RESPONSE_FILE_NAME_SUFFIX;
    }
}
