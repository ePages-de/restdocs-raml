package com.example.notes;

import java.nio.file.Paths;

import org.raml.v2.api.RamlModelBuilder;
import org.raml.v2.api.RamlModelResult;
import org.raml.v2.api.model.common.ValidationResult;
import org.raml.v2.api.model.v10.api.Api;

public class RamlValidator {

    public static void main(String[] args) {
        RamlModelResult ramlModelResult = new RamlModelBuilder().buildApi(Paths.get("/Users/mduesterhoeft/dev/ng-checkout/build/ramldoc/api-public.raml").toFile());
        if (ramlModelResult.hasErrors()) {
            for (ValidationResult validationResult : ramlModelResult.getValidationResults())
            {
                System.out.println(validationResult.getMessage());
            }
        } else {
            System.out.println("validation successful");
            Api api = ramlModelResult.getApiV10();
        }
    }
}
