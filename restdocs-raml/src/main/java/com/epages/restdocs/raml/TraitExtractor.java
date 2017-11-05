package com.epages.restdocs.raml;

import java.util.List;

import org.springframework.restdocs.operation.Operation;

public interface TraitExtractor {

    List<String> extractTraits(Operation operation, RamlResourceSnippetParameters parameters);
}
