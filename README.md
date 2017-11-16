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

### Build configuration

```groovy
buildscript {
    repositories {
    //..
        jcenter() //1
    }
    dependencies {
        //..
        classpath 'com.epages:restdocs-raml-gradle-plugin:0.1.0' //2
    }
}
//..
apply plugin: 'com.epages.restdocs-raml' //3

repositories { //4
    jcenter()
    maven { url 'https://jitpack.io' }
}

//..

dependencies {
    //..
    testCompile 'com.epages:restdocs-raml:0.1.0' //5
    testCompile 'org.json:json:20170516' //6
}

ramldoc { //7
    apiTitle = 'Notes API'
    apiBaseUri = 'http://localhost:8080/'
    ramlVersion = "1.0"
}
```

1. add jcenter repository to `buildscript` to resolve the `restdocs-raml-gradle-plugin`
2. add the dependency to `restdocs-raml-gradle-plugin`
3. apply `restdocs-raml-gradle-plugin`
4. add repositories used for dependency resolution. We need `jcenter` for `restdocs-raml` and `jitpack` for one of the depencencies we use internally
5. add the actual `restdocs-raml` dependency to the test scope
6. `spring-boot` specifies an old version of `org.json:json`. We use [everit-org/json-schema](https://github.com/everit-org/json-schema) to generate json schema files. This project depends a newer version of `org.json:json`. As versions from BOM always override transitive versions coming in through maven dependencies, you need to add an explicit dependency to `org.json:json:20170516`
7. optional - add configuration options for restdocs-raml-gradle-plugin see [Gradle plugin configuration](#gradle-plugin-configuration)

See the [build.gradle](restdocs-raml-sample/build.gradle) for the setup used in the sample project.

### Usage with Spring REST Docs

The class [RamlResourceDocumentation](restdocs-raml/src/main/java/com/epages/restdocs/raml/RamlResourceDocumentation.java) contains the entrypoint for using the [RamlResourceSnippet](restdocs-raml/src/main/java/com/epages/restdocs/raml/RamlResourceSnippet.java).

The most basic form does not take and parameters:

```java
resultActions.andDo(document("notes-list", ramlResource()));
```

### Running the gradle plugin

`restdocs-raml-gradle-plugin` is responsible for picking up the generated `RAML` fragments and aggregate them into a set of top-level `RAML` files that describe the complete API.
For this purpose we use the `ramldoc` task:

```
./gradlew ramldoc
```

### Gradle plugin configuration

The `restdocs-raml-gradle-plugin` takes the following configuration options - all are optional.

Name | Description | Default value
---- | ----------- | -------------
apiTitle | The title of the generated top-level `RAML` file | empty
apiBaseUri | The base uri added to the top-level `RAML` file | empty
ramlVersion | `RAML` version header - `0.8` or `1.0` | `1.0`
outputDirectory | The output directory | `build/ramldoc`
snippetsDirectory | The directory Spring REST Docs generated the snippets to | `build/generated-snippets`
