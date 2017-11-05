package com.epages.restdocs.raml;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;

import org.springframework.restdocs.operation.Operation;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TraitExtractorChain implements OperationHandler {

    private final List<TraitExtractor> traitExtractors;

    @Override
    public Map<String, Object> generateModel(Operation operation, RamlResourceSnippetParameters parameters) {
        List<String> traitsToApply = traitExtractors.stream()
                .flatMap(t -> t.extractTraits(operation, parameters).stream())
                .collect(toList());

        if (!traitsToApply.isEmpty()) {
            return singletonMap("traits", "[ " + String.join(",", traitsToApply.stream().map(s -> String.format("\"%s\"", s)).collect(toList())) + " ]");
        }

        return emptyMap();
    }
}
