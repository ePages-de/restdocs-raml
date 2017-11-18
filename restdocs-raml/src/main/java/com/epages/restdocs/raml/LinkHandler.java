package com.epages.restdocs.raml;


import static java.util.Collections.emptyMap;

import java.util.List;
import java.util.Map;

import org.springframework.restdocs.hypermedia.HypermediaDocumentation;
import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.restdocs.hypermedia.LinksSnippet;
import org.springframework.restdocs.operation.Operation;

/**
 * Handle {@link org.springframework.restdocs.hypermedia.LinkDescriptor}
 *
 * Links are added to the model as part of the response in {@link ResponseHandler}.
 * So this implementation just takes care about triggering the validation if the documented links
 */
public class LinkHandler implements OperationHandler {

    @Override
    public Map<String, Object> generateModel(Operation operation, RamlResourceSnippetParameters parameters) {
        if (!parameters.getLinks().isEmpty()) {
            new LinkSnippetWrapper(parameters.getLinks()).validateLinks(operation);
        }
        return emptyMap();
    }

    static class LinkSnippetWrapper extends LinksSnippet {

        LinkSnippetWrapper(List<LinkDescriptor> descriptors) {
            //using ContentTypeLinkExtractor would be more flexible but we cannot access it here
            super(HypermediaDocumentation.halLinks(), descriptors);
        }

        /**
         * delegate to createModel which will validate the links.
         * That is checking that all documented links exist in the response and also if all existing links are documented
         * @param operation
         */
        void validateLinks(Operation operation) {
            createModel(operation);
        }
    }
}


