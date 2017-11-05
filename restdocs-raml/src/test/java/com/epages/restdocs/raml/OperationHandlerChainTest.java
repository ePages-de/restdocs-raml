package com.epages.restdocs.raml;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.restdocs.operation.Operation;

public class OperationHandlerChainTest {

    private OperationHandlerChain operationHandlerChain;

    private Map<String, Object> model;
    @Test
    public void should_aggregate_model_maps_of_handlers() {
        givenHandlerChain();

        whenHandlerChainProcessed();

        then(model).containsEntry("key1", "key1");
        then(model).containsEntry("key2", "key2");
    }

    private void whenHandlerChainProcessed() {
        model = operationHandlerChain.process(new OperationBuilder().build(), RamlResourceSnippetParameters.builder().build());
    }

    private void givenHandlerChain() {
        operationHandlerChain = new OperationHandlerChain(Arrays.asList(new TestHandler("key1"), new TestHandler("key2")));
    }

    private static class TestHandler implements OperationHandler {

        public TestHandler(String key) {
            this.key = key;
        }

        private final String key;

        @Override
        public Map<String, Object> generateModel(Operation operation, RamlResourceSnippetParameters parameters) {
            Map<String, Object> model = new HashMap<>();
            model.put(key, key);
            return model;
        }
    }

}