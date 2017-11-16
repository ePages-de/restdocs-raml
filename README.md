# Spring REST Docs RAML Integration

![](https://img.shields.io/github/license/ePages-de/restdocs-raml.svg?branch=master)
[ ![Build Status](https://travis-ci.org/ePages-de/restdocs-raml.svg)](https://travis-ci.org/ePages-de/restdocs-raml)
[ ![Coverage Status](https://coveralls.io/repos/github/ePages-de/restdocs-raml/badge.svg?branch=master)](https://coveralls.io/r/ePages-de/restdocs-raml)
[ ![Download](https://api.bintray.com/packages/epages/maven/restdocs-raml/images/download.svg)](https://bintray.com/epages/maven/restdocs-raml/_latestVersion)

This is an extension that adds [RAML (RESTful API Modeling Language)](https://raml.org) as an output format to [Spring REST Docs](https://projects.spring.io/spring-restdocs/).

## Motivation

Spring REST Docs is a great tool to produce documentation for your RESTful services that is accurate and readable.

It offers support for AsciiDoc and Markdown. This is great for generating simple HTML-based documentation. 
But both are markup languages and thus it is hard to get any further than statically generated HTML. 

RAML is a lot more flexible. 
With RAML you get a machine-readable description of your API. There is a rich ecosystem around it that contains tools to:
- generate a HTML representation of your API - [raml2html](https://www.npmjs.com/package/raml2html)
- interact with your API using an API console - [api-console](https://github.com/mulesoft/api-console)

Also, RAML is supported by many REST clients like [Postman](https://www.getpostman.com) and [Paw](https://paw.cloud). Thus having a RAML representation of an API is a great plus when starting to work with it.

## Getting started

### Project structure

The project consists of two components:

- [restdocs-raml](restdocs-raml) - contains the actual Spring REST Docs extension. 
This is most importantly the [RamlResourceDocumentation](restdocs-raml/src/main/java/com/epages/restdocs/raml/RamlResourceDocumentation.java) which is the entrypoint to use the extension in your tests. The [RamlResourceSnippet](restdocs-raml/src/main/java/com/epages/restdocs/raml/RamlResourceSnippet.java). 

The snippet generates a RAML fragment for each documenated resource. 
- [restdocs-raml-gradle-plugin](restdocs-raml-gradle-plugin) - adds a gradle plugin that aggregates the RAML fragment produced  by `RamlResourceSnippet` into one `RAML` file for the whole products.

### Usage

The artifacts are published in 'jcenter' - so you firstly need 
