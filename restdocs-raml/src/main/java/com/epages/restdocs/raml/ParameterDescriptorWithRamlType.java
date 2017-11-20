package com.epages.restdocs.raml;

import static com.epages.restdocs.raml.ParameterDescriptorWithRamlType.RamlScalarType.STRING;

import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.snippet.IgnorableDescriptor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * RAML query and path parameters have to have a type. The ParameterDescriptor does not contain one.
 * So we add a subclass that contains a type
 */
@RequiredArgsConstructor
@Getter
public class ParameterDescriptorWithRamlType extends IgnorableDescriptor<ParameterDescriptorWithRamlType> {

    private RamlScalarType type = STRING;

    private final String name;

    private boolean optional;

    public ParameterDescriptorWithRamlType type(RamlScalarType type) {
        this.type = type;
        return this;
    }

    public ParameterDescriptorWithRamlType optional() {
        this.optional = true;
        return this;
    }

    protected static ParameterDescriptorWithRamlType from(ParameterDescriptor parameterDescriptor) {
        ParameterDescriptorWithRamlType newDescriptor = new ParameterDescriptorWithRamlType(parameterDescriptor.getName());
        newDescriptor.description(parameterDescriptor.getDescription());
        if (parameterDescriptor.isOptional()) {
            newDescriptor.optional();
        }
        if (parameterDescriptor.isIgnored()) {
            newDescriptor.ignored();
        }
        newDescriptor.type(STRING);
        return newDescriptor;
    }

    public enum RamlScalarType {
        NUMBER("number"),
        INTEGER("integer"),
        STRING("string"),
        BOOLEAN("boolean"),
        TIME_ONLY("time-only"),
        DATE_ONLY("date-only"),
        DATETIME_ONLY("datetime-only"),
        DATETIME("datetime");

        @Getter
        private String typeName;

        RamlScalarType(String typeName) {
            this.typeName = typeName;
        }
    }
}
