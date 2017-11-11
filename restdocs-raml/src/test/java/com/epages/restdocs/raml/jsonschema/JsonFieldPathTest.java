package com.epages.restdocs.raml.jsonschema;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class JsonFieldPathTest {

    @Test
    public void should_get_remaining_segments() throws Exception {
        final JsonFieldPath jsonFieldPath = JsonFieldPath.compile(fieldWithPath("a.b.c"));

        then(jsonFieldPath.remainingSegments(ImmutableList.of("a"))).contains("b", "c");
        then(jsonFieldPath.remainingSegments(ImmutableList.of("a", "b"))).contains("c");
        then(jsonFieldPath.remainingSegments(ImmutableList.of("a", "b", "c"))).isEmpty();
        then(jsonFieldPath.remainingSegments(ImmutableList.of("d", "e", "c"))).contains("a", "b", "c");
        then(jsonFieldPath.remainingSegments(emptyList())).contains("a", "b", "c");
    }

}