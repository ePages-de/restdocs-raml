package com.epages.restdocs.raml;

import static org.springframework.restdocs.payload.PayloadDocumentation.applyPathPrefix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.restdocs.payload.FieldDescriptor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class FieldDescriptors {

    private final List<FieldDescriptor> fieldDescriptors;

    public FieldDescriptors(FieldDescriptor... fieldDescriptors) {
        this.fieldDescriptors = Arrays.asList(fieldDescriptors);
    }

    public FieldDescriptors and(FieldDescriptor... additionalDescriptors) {
        return andWithPrefix("", additionalDescriptors);
    }

    public FieldDescriptors andWithPrefix(String pathPrefix, FieldDescriptor... additionalDescriptors) {
        List<FieldDescriptor> combinedDescriptors = new ArrayList<>(fieldDescriptors);
        combinedDescriptors.addAll(applyPathPrefix(pathPrefix, Arrays.asList(additionalDescriptors)));
        return new FieldDescriptors(combinedDescriptors);
    }
}
