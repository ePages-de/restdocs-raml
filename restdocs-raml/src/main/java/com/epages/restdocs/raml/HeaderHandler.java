package com.epages.restdocs.raml;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.headers.RequestHeadersSnippet;
import org.springframework.restdocs.headers.ResponseHeadersSnippet;
import org.springframework.restdocs.operation.Operation;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class HeaderHandler implements OperationHandler {

    private final String modelNamePrefix;
    private final Function<List<HeaderDescriptor>, HeadersValidator> validatorSupplier;
    private final Function<RamlResourceSnippetParameters, List<HeaderDescriptor>> descriptorSupplier;
    private final Function<Operation, HttpHeaders> headersSupplier;

    static HeaderHandler requestHeaderHandler() {
        return new HeaderHandler("request",
                RequestHeaderSnippetValidator::new,
                RamlResourceSnippetParameters::getRequestHeaders,
                o -> o.getRequest().getHeaders());
    }

    static HeaderHandler responseHeaderHandler() {
        return new HeaderHandler("response",
                ResponseHeaderSnippetValidator::new,
                RamlResourceSnippetParameters::getResponseHeaders,
                o -> o.getResponse().getHeaders());
    }

    @Override
    public Map<String, Object> generateModel(Operation operation, RamlResourceSnippetParameters parameters) {
        List<HeaderDescriptor> headers = descriptorSupplier.apply(parameters);
        if (!headers.isEmpty()) {
            validatorSupplier.apply(headers).validateHeaders(operation);
            Map<String, Object> model = new HashMap<>();
            model.put(modelNamePrefix + "HeadersPresent", true);
            model.put(modelNamePrefix + "Headers", mapDescriptorsToModel(headers, headersSupplier.apply(operation)));
            return model;
        }
        return emptyMap();
    }

    private List<Map<String, String>> mapDescriptorsToModel(List<HeaderDescriptor> headerDescriptors, HttpHeaders presentHeaders) {
        return headerDescriptors.stream().map(headerDescriptor -> {
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("name", headerDescriptor.getName());
            headerMap.put("description", (String) headerDescriptor.getDescription());
            headerMap.put("example", presentHeaders.getFirst(headerDescriptor.getName()));
            return headerMap;
        }).collect(toList());
    }

    private interface HeadersValidator {
        void validateHeaders(Operation operation);
    }

    private static class RequestHeaderSnippetValidator extends RequestHeadersSnippet implements HeadersValidator {
        private RequestHeaderSnippetValidator(List<HeaderDescriptor> descriptors) {
            super(descriptors);
        }

        @Override
        public void validateHeaders(Operation operation) {
            super.createModel(operation);
        }
    }

    private static class ResponseHeaderSnippetValidator extends ResponseHeadersSnippet implements HeadersValidator {
        private ResponseHeaderSnippetValidator(List<HeaderDescriptor> descriptors) {
            super(descriptors);
        }

        @Override
        public void validateHeaders(Operation operation) {
            super.createModel(operation);
        }
    }
}
