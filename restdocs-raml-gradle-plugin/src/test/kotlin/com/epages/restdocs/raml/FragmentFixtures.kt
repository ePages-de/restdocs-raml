package com.epages.restdocs.raml


interface FragmentFixtures {

    fun rawPrivateFragment() = """
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

    fun rawFragmentWithoutSchema() = """
        /payment-integrations/{paymentIntegrationId}:
          get:
            description:
            responses:
              200:
                body:
                  application/hal+json:
                    example: !include payment-integration-get-response.json
        """.trimIndent()

    fun rawMinimalFragment() = """
        /payment-integrations/{paymentIntegrationId}:
          get:
            description: "some"
        """.trimIndent()

    fun rawFullFragment() = """
        /tags/{id}:
          uriParameters:
            id:
              type: string
              description: The id
          put:
            description: Update a tag
            securedBy: ["scope-one", "scope-two"]
            is: ["private"]
            queryParameters:
              some:
                description: some
                type: integer
              other:
                description: other
                type: string
            body:
              application/hal+json:
                schema: !include tags-create-schema-request.json
                example: !include tags-create-request.json
            responses:
              200:
                body:
                  application/hal+json:
                    schema: !include tags-list-schema-response.json
                    example: !include tags-list-response.json

        """.trimIndent()

    fun parsedFragmentMap(stringProvider: () -> String) = RamlParser.parseFragment(stringProvider())


}
