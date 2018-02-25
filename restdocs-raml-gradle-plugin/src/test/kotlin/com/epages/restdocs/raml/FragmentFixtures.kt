package com.epages.restdocs.raml


interface FragmentFixtures {

    fun rawPrivateResourceFragment() = """
        /payment-integrations/{paymentIntegrationId}:
          get:
            description:
            is: [ "private" ]
            responses:
              200:
                body:
                  application/hal+json:
                    schema: !include payment-integration-get-schema-response.json
                    example: !include payment-integration-get-response.json
        """.trimIndent()

    fun rawResourceFragment() = """
        /payment-integrations/{paymentIntegrationId}:
          get:
            description:
            responses:
              200:
                body:
                  application/hal+json:
                    schema: !include payment-integration-get-schema-response.json
                    example: !include payment-integration-get-response.json
        """.trimIndent()

    fun parsedPrivateResourceFragmentMap() = RamlParser.parseFragment((rawPrivateResourceFragment().byteInputStream()))

    fun parsedResourceFragmentMap() = RamlParser.parseFragment((rawResourceFragment().byteInputStream()))

}
