package com.epages.restdocs.raml;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;

import org.springframework.restdocs.constraints.ValidatorConstraintResolver;
import org.springframework.restdocs.payload.FieldDescriptor;

/**
 * ConstrainedFields can be used to add constraint information to a {@link FieldDescriptor}
 * If these are present in the descriptor they are used to enrich the generated type information (e.g. JsonSchema)
 */
public class ConstrainedFields {
        private final ValidatorConstraintResolver validatorConstraintResolver = new ValidatorConstraintResolver();

        private final Class<?> classHoldingConstraints;

        public ConstrainedFields(Class<?> classHoldingConstraints) {
            this.classHoldingConstraints = classHoldingConstraints;
        }

        /**
         * Create a field description with constraints for bean property with the same name
         * @param path json path of the field
         */
        public FieldDescriptor withPath(String path) {
            return fieldWithPath(path).attributes(key("validationConstraints")
                    .value(this.validatorConstraintResolver.resolveForProperty(path, classHoldingConstraints)));
        }

        /**
         *
         * Create a field description with constraints for bean property with a name differing from the path
         * @param jsonPath json path of the field
         * @param beanPropertyName name of the property of the bean that is used to get the field constraints
         */
        public FieldDescriptor withMappedPath(String jsonPath, String beanPropertyName) {
            return fieldWithPath(jsonPath).attributes(key("validationConstraints")
                    .value(this.validatorConstraintResolver.resolveForProperty(beanPropertyName, classHoldingConstraints)));
        }
    }