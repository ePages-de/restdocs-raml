package com.epages.restdocs.raml;

import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import org.junit.Test;
import org.springframework.restdocs.payload.FieldDescriptor;

public class FieldDescriptorsTest {

    private FieldDescriptors fieldDescriptors;

    @Test
    public void should_combine_descriptors() {
        fieldDescriptors = givenFieldDescriptors();

        then(fieldDescriptors.and(fieldWithPath("c")).getFieldDescriptors())
                .extracting(FieldDescriptor::getPath).contains("a", "b", "c");
    }

    @Test
    public void should_combine_descriptors_with_prefix() {
        fieldDescriptors = givenFieldDescriptors();

        then(fieldDescriptors.andWithPrefix("d.", fieldWithPath("c")).getFieldDescriptors())
                .extracting(FieldDescriptor::getPath).contains("a", "b", "d.c");
    }

    private FieldDescriptors givenFieldDescriptors() {
        return new FieldDescriptors(fieldWithPath("a"), fieldWithPath("b"));
    }
}