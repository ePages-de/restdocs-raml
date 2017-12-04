package com.epages.restdocs.raml;

import static org.assertj.core.api.BDDAssertions.then;

import org.junit.Test;
import org.springframework.restdocs.request.RequestDocumentation;

import com.epages.restdocs.raml.ParameterDescriptorWithRamlType.RamlScalarType;

public class ParameterDescriptorWithRamlTypeTest {

    private ParameterDescriptorWithRamlType descriptor;

    @Test
    public void should_convert_restdocs_parameter_descriptor() {
        whenParameterDescriptorCreatedFromRestDocsParameter();

        then(descriptor.isOptional()).isTrue();
        then(descriptor.getDescription()).isNotNull();
        then(descriptor.getType()).isEqualTo(RamlScalarType.STRING);
    }

    private void whenParameterDescriptorCreatedFromRestDocsParameter() {
        descriptor = ParameterDescriptorWithRamlType.from(RequestDocumentation.parameterWithName("some")
                .description("some")
                .optional());
    }
}