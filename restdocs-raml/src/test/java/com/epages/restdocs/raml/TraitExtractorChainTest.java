package com.epages.restdocs.raml;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.Map;

import org.junit.Test;

public class TraitExtractorChainTest {

    private TraitExtractorChain traitExtractorChain;

    private Map<String, Object> model;

    @Test
    public void should_add_private_trait() {
        givenTraitExtractorChain();

        whenModelGeneratedForPrivateResource();

        then(model).containsEntry("traits", "[ \"private\" ]");
    }

    @Test
    public void should_do_nothing_if_no_traits_added() {
        givenTraitExtractorChain();

        whenModelGenerated();

        then(model).isEmpty();
    }

    private void whenModelGeneratedForPrivateResource() {
        model = traitExtractorChain.generateModel(new OperationBuilder().build(), RamlResourceSnippetParameters.builder()
                .privateResource(true)
                .build());
    }

    private void whenModelGenerated() {
        model = traitExtractorChain.generateModel(new OperationBuilder().build(), RamlResourceSnippetParameters.builder().build());
    }

    private void givenTraitExtractorChain() {
        traitExtractorChain = new TraitExtractorChain(singletonList(new PrivateResourceTraitExtractor()));
    }

}