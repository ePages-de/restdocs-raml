package com.epages.restdocs.raml;

import static java.util.Collections.emptyMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.restdocs.operation.Operation;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OperationHandlerChain {

    @NonNull
    private final List<OperationHandler> operationHandlers;

    public Map<String, Object> process(Operation operation, RamlResourceSnippetParameters parameters) {
        return operationHandlers.stream().map(handler -> handler.generateModel(operation, parameters))
                .reduce((m, m1) -> {
                    Map<String, Object> combined = new HashMap<>();
                    combined.putAll(m);
                    combined.putAll(m1);
                    return combined;
                }).orElse(emptyMap());
    }
}
