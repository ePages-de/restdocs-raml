package com.epages.restdocs.raml

import spock.lang.Specification


class RamlFragmentsTest extends Specification {


    def "should aggregate prefix on first level"() {
        given:
            def fragments = new RamlFragments([
                    new RamlFragment(["/carts", "  get:"]),
                    new RamlFragment(["/carts", "  post:"])
            ])
        when:
            def groupedFragments = fragments.groupByFirstPathPart(false)
        then:
            groupedFragments.size() == 1
    }

    def "should aggregate common path"() {
        given:
            def fragments = new RamlFragments([
                    new RamlFragment(["/carts/{cartId}", "  get:"]),
                    new RamlFragment(["/carts/{cartId}", "  post:"]),
                    new RamlFragment(["/carts/{cartId}/line-items", "  post:"]),
                    new RamlFragment(["/payment-methods", "  get:"]),
            ])
        when:
            def groupedFragments = fragments.groupByFirstPathPart(false)
        then:
            groupedFragments.size() == 2
            groupedFragments.collect(){ it.commonPath }.contains("/carts")
            groupedFragments.collect(){ it.commonPath }.contains("/payment-methods")

            def cartsGroup = groupedFragments.find(){ it.commonPath ==  "/carts"}
            cartsGroup.ramlFragments.size() == 2
            cartsGroup.ramlFragments.head().path.startsWith("/{cartId}")
            cartsGroup.ramlFragments.find { it.remainingContent.contains("  get:")}
            cartsGroup.ramlFragments.find { it.remainingContent.contains("  post:")}
    }

    def "should recognize private resource"() {
        given:
            def fragment = new RamlFragment(["/carts/{cartId}", "  get:", '''  is: ["some", "private"]'''])
        when:
            def privateResource = fragment.privateResource
        then:
             privateResource
    }

    def "should recognize public resource"() {
        given:
            def fragment = new RamlFragment(["/carts/{cartId}", "  get:", '''  is: ["some"]'''])
        when:
            def privateResource = fragment.privateResource
        then:
            !privateResource
    }
}
