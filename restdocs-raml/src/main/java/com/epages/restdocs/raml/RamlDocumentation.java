package com.epages.restdocs.raml;

import static com.epages.restdocs.raml.RamlResourceDocumentation.ramlResource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.headers.RequestHeadersSnippet;
import org.springframework.restdocs.headers.ResponseHeadersSnippet;
import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.restdocs.hypermedia.LinksSnippet;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.request.PathParametersSnippet;
import org.springframework.restdocs.request.RequestParametersSnippet;
import org.springframework.restdocs.snippet.Snippet;

/**
 * Convenience class to migrate to restdocs-raml in a non-invasive way.
 * It it a wrapper and replacement for MockMvcRestDocumentation that transparently adds a RamlResourceSnippet with the descriptors provided in the given snippets.
 */
public class RamlDocumentation {

    public static RestDocumentationResultHandler document(String identifier,
                                                          Snippet... snippets) {
        return document(identifier, "", false, null, null, Function.identity(), snippets);
    }

    public static RestDocumentationResultHandler document(String identifier,
                                                          OperationRequestPreprocessor requestPreprocessor,
                                                          Snippet... snippets) {
        return document(identifier, "", false, requestPreprocessor, null, Function.identity(), snippets);
    }

    public static RestDocumentationResultHandler document(String identifier,
                                                          OperationResponsePreprocessor responsePreprocessor,
                                                          Snippet... snippets) {
        return document(identifier, "", false, null, responsePreprocessor, Function.identity(), snippets);
    }

    public static RestDocumentationResultHandler document(String identifier,
                                                          OperationRequestPreprocessor requestPreprocessor,
                                                          OperationResponsePreprocessor responsePreprocessor,
                                                          Snippet... snippets) {
        return document(identifier, "", false, requestPreprocessor, responsePreprocessor, Function.identity(), snippets);
    }

    public static RestDocumentationResultHandler document(String identifier,
                                                          boolean privateResource,
                                                          OperationRequestPreprocessor requestPreprocessor,
                                                          Snippet... snippets) {
        return document(identifier, "", privateResource, requestPreprocessor, null, Function.identity(), snippets);
    }

    public static RestDocumentationResultHandler document(String identifier,
                                                          boolean privateResource,
                                                          OperationResponsePreprocessor responsePreprocessor,
                                                          Snippet... snippets) {
        return document(identifier, "", privateResource, null, responsePreprocessor, Function.identity(), snippets);
    }

    public static RestDocumentationResultHandler document(String identifier,
                                                          String description,
                                                          boolean privateResource,
                                                          Snippet... snippets) {
        return document(identifier, description, privateResource, null, null, Function.identity(), snippets);
    }

    public static RestDocumentationResultHandler document(String identifier,
                                                          String description,
                                                          boolean privateResource,
                                                          OperationRequestPreprocessor requestPreprocessor,
                                                          OperationResponsePreprocessor responsePreprocessor,
                                                          Snippet... snippets) {
        return document(identifier, description, privateResource, requestPreprocessor, responsePreprocessor, Function.identity(), snippets);
    }

    public static RestDocumentationResultHandler document(String identifier,
                                                          String description,
                                                          boolean privateResource,
                                                          OperationRequestPreprocessor requestPreprocessor,
                                                          OperationResponsePreprocessor responsePreprocessor,
                                                          Function<List<Snippet>, List<Snippet>> snippetFilter,
                                                          Snippet... snippets) {

        Snippet[] enhancedSnippets = enhanceSnippetsWithRaml(description, privateResource, snippetFilter, snippets);

        if (requestPreprocessor != null && responsePreprocessor != null) {
            return MockMvcRestDocumentation.document(identifier, requestPreprocessor, responsePreprocessor, enhancedSnippets);
        } else if (requestPreprocessor != null) {
            return MockMvcRestDocumentation.document(identifier, requestPreprocessor, enhancedSnippets);
        } else if (responsePreprocessor != null) {
            return MockMvcRestDocumentation.document(identifier, responsePreprocessor, enhancedSnippets);
        }

        return MockMvcRestDocumentation.document(identifier, enhancedSnippets);
    }

    protected static Snippet[] enhanceSnippetsWithRaml(String description,
                                                       boolean privateResource,
                                                       Function<List<Snippet>, List<Snippet>> snippetFilter,
                                                       Snippet... snippets) {

        List<RequestFieldsSnippet> requestFieldsSnippets = new ArrayList<>();
        List<ResponseFieldsSnippet> responseFieldsSnippets = new ArrayList<>();
        List<LinksSnippet> linkSnippets = new ArrayList<>();
        List<RequestParametersSnippet> requestParametersSnippets = new ArrayList<>();
        List<PathParametersSnippet> pathParametersSnippets = new ArrayList<>();
        List<RequestHeadersSnippet> requestHeadersSnippets = new ArrayList<>();
        List<ResponseHeadersSnippet> responseHeadersSnippets = new ArrayList<>();

        List<Snippet> ramlSnippets = new ArrayList<>();

        List<Snippet> otherSnippets = new ArrayList<>();

        for (Snippet snippet : snippets) {
            if (snippet instanceof RequestFieldsSnippet) {
                requestFieldsSnippets.add((RequestFieldsSnippet) snippet);
            } else if (snippet instanceof ResponseFieldsSnippet) {
                responseFieldsSnippets.add((ResponseFieldsSnippet) snippet);
            } else if (snippet instanceof LinksSnippet) {
                linkSnippets.add((LinksSnippet) snippet);
            } else if (snippet instanceof RequestParametersSnippet) {
                requestParametersSnippets.add((RequestParametersSnippet) snippet);
            } else if (snippet instanceof PathParametersSnippet) {
                pathParametersSnippets.add((PathParametersSnippet) snippet);
            } else if (snippet instanceof RequestHeadersSnippet) {
                requestHeadersSnippets.add((RequestHeadersSnippet) snippet);
            } else if (snippet instanceof ResponseHeadersSnippet) {
                responseHeadersSnippets.add((ResponseHeadersSnippet) snippet);
            } else if (snippet instanceof RamlResourceSnippet) {
                ramlSnippets.add(snippet);
            } else {
                otherSnippets.add(snippet);
            }
        }

        List<Snippet> enhancedSnippets = new ArrayList<>();
        enhancedSnippets.addAll(requestFieldsSnippets);
        enhancedSnippets.addAll(responseFieldsSnippets);
        enhancedSnippets.addAll(linkSnippets);
        enhancedSnippets.addAll(requestParametersSnippets);
        enhancedSnippets.addAll(pathParametersSnippets);
        enhancedSnippets.addAll(requestHeadersSnippets);
        enhancedSnippets.addAll(responseHeadersSnippets);
        enhancedSnippets.addAll(otherSnippets);

        if (ramlSnippets.isEmpty()) { // No RamlResourceSnippet, so we configure our own based on the info of the other snippets
            RamlResourceSnippetParameters ramlParameters = RamlResourceSnippetParameters.builder()
                    .description(description)
                    .privateResource(privateResource)
                    .requestFields(requestFieldsSnippets.stream().map(DescriptorExtractor::extract).flatMap(List::stream).toArray(FieldDescriptor[]::new))
                    .responseFields(responseFieldsSnippets.stream().map(DescriptorExtractor::extract).flatMap(List::stream).toArray(FieldDescriptor[]::new))
                    .links(linkSnippets.stream().map(DescriptorExtractor::extract).flatMap(List::stream).toArray(LinkDescriptor[]::new))
                    .requestParameters(requestParametersSnippets.stream().map(DescriptorExtractor::extract).flatMap(List::stream).toArray(ParameterDescriptor[]::new))
                    .pathParameters(pathParametersSnippets.stream().map(DescriptorExtractor::extract).flatMap(List::stream).toArray(ParameterDescriptor[]::new))
                    .requestHeaders(requestHeadersSnippets.stream().map(DescriptorExtractor::extract).flatMap(List::stream).toArray(HeaderDescriptor[]::new))
                    .responseHeaders(responseHeadersSnippets.stream().map(DescriptorExtractor::extract).flatMap(List::stream).toArray(HeaderDescriptor[]::new))
                    .build();
            enhancedSnippets.add(ramlResource(ramlParameters));
        } else {
            enhancedSnippets.addAll(ramlSnippets);
        }

        enhancedSnippets = snippetFilter.apply(enhancedSnippets);

        return enhancedSnippets.toArray(new Snippet[0]);
    }

}
