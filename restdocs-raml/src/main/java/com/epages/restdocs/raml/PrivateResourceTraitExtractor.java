package com.epages.restdocs.raml;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.List;

import org.springframework.restdocs.operation.Operation;

public class PrivateResourceTraitExtractor implements TraitExtractor {

    @Override
    public List<String> extractTraits(Operation operation, RamlResourceSnippetParameters parameters) {
        if (parameters.isPrivateResource()) {
            return singletonList("private");
        }

        return emptyList();
    }
}
