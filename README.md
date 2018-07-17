# Spring REST Docs RAML Integration

![](https://img.shields.io/github/license/ePages-de/restdocs-raml.svg?branch=master)
[ ![Build Status](https://travis-ci.org/ePages-de/restdocs-raml.svg)](https://travis-ci.org/ePages-de/restdocs-raml)
[ ![Coverage Status](https://coveralls.io/repos/github/ePages-de/restdocs-raml/badge.svg?branch=master)](https://coveralls.io/r/ePages-de/restdocs-raml)
[ ![Download](https://api.bintray.com/packages/epages/maven/restdocs-raml/images/download.svg)](https://bintray.com/epages/maven/restdocs-raml/_latestVersion)
[![Join the chat at https://gitter.im/restdocs-raml/restdocs-raml](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/restdocs-raml/restdocs-raml?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

This is an extension that adds [RAML (RESTful API Modeling Language)](https://raml.org) as an output format to [Spring REST Docs](https://projects.spring.io/spring-restdocs/).

Check out our [introductory blog post](https://developer.epages.com/blog/api-experience/restful-api-documentation-with-spring-rest-docs-and-raml/) for a quick overview. 

Also there is a [recording of a talk held at the Spring IO 2018](https://www.youtube.com/watch?v=VwKc34W96Cw) about _Documenting RESTful APIs with Spring REST Docs and RAML_.

<!-- TOC depthFrom:2 -->

- [Motivation](#motivation)
- [Getting started](#getting-started)
    - [Project structure](#project-structure)
    - [Build configuration](#build-configuration)
    - [Usage with Spring REST Docs](#usage-with-spring-rest-docs)
    - [Documenting Bean Validation constraints](#documenting-bean-validation-constraints)
    - [Migrate existing Spring REST Docs tests](#migrate-existing-spring-rest-docs-tests)
    - [Running the gradle plugin](#running-the-gradle-plugin)
    - [Gradle plugin configuration](#gradle-plugin-configuration)
- [Generate HTML](#generate-html)
- [Generate an interactive API console](#generate-an-interactive-api-console)
- [Compatibility with Spring Boot 2 (WebTestClient)](#compatibility-with-spring-boot-2-webtestclient)
- [Limitations](#limitations)
    - [Rest Assured](#rest-assured)
    - [Maven plugin](#maven-plugin)

<!-- /TOC -->

## Motivation

[Spring REST Docs](https://projects.spring.io/spring-restdocs/) is a great tool to produce documentation for your RESTful services that is accurate and readable.

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
This is most importantly the [RamlResourceDocumentation](restdocs-raml/src/main/java/com/epages/restdocs/raml/RamlResourceDocumentation.java) which is the entrypoint to use the extension in your tests. The [RamlResourceSnippet](restdocs-raml/src/main/java/com/epages/restdocs/raml/RamlResourceSnippet.java) is the  snippet generating a RAML fragment for each documenated resource. 
- [restdocs-raml-gradle-plugin](restdocs-raml-gradle-plugin) - adds a gradle plugin that aggregates the RAML fragment produced  by `RamlResourceSnippet` into one `RAML` file for the whole project.

### Build configuration

```groovy
buildscript {
    repositories {
    //..
        jcenter() //1
    }
    dependencies {
        //..
        classpath 'com.epages:restdocs-raml-gradle-plugin:0.4.1' //2
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
    testCompile 'com.epages:restdocs-raml:0.4.1' //5
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
4. add repositories used for dependency resolution. We need `jcenter` for `restdocs-raml` and `jitpack` for one of the dependencies we use internally
5. add the actual `restdocs-raml` dependency to the test scope
6. `spring-boot` specifies an old version of `org.json:json`. We use [everit-org/json-schema](https://github.com/everit-org/json-schema) to generate json schema files. This project depends on a newer version of `org.json:json`. As versions from BOM always override transitive versions coming in through maven dependencies, you need to add an explicit dependency to `org.json:json:20170516`
7. add configuration options for restdocs-raml-gradle-plugin see [Gradle plugin configuration](#gradle-plugin-configuration)

See the [build.gradle](restdocs-raml-sample/build.gradle) for the setup used in the sample project.

### Usage with Spring REST Docs

The class [RamlResourceDocumentation](restdocs-raml/src/main/java/com/epages/restdocs/raml/RamlResourceDocumentation.java) contains the entry point for using the [RamlResourceSnippet](restdocs-raml/src/main/java/com/epages/restdocs/raml/RamlResourceSnippet.java).

The most basic form does not take any parameters:

```java
mockMvc
  .perform(get("/notes"))
  .andExpect(status().isOk())
  .andDo(document("notes-list", ramlResource()));
```

This test will produce the following files in the snippets directory:

- raml-resource.raml
- notes-list-response.json

The `raml-resource.raml` is the raml fragment describing this resource.

```yaml
/notes:
  get:
    description: notes-list
    responses:
      200:
        body:
          application/hal+json:
            example: !include notes-list-response.json
```

Just like you are used to do with Spring REST Docs we can also describe request fields, response fields, path variables, parameters, and links.
Furthermore you can add a text description for your resource.
The extension also discovers `JWT` tokens in the `Authorization` header and will document the required scopes from it.

The following example uses `RamlResourceSnippetParameters` to document response fields and links.
We paid close attention to keep the API as similar as possible to what you already know from Spring REST Docs.
`fieldWithPath` and `linkWithRel` are actually still the static methods you would use in your using Spring REST Docs test.

```java
mockMvc
	.perform(get("/notes/{id}", noteId))
	.andExpect(status().isOk())
  .andDo(document("notes-get",
	    ramlResource(RamlResourceSnippetParameters.builder()
        .description("Get a note by id")
        .pathParameters(parameterWithName("id").description("The note id"))
        .responseFields(
			    fieldWithPath("title").description("The title of the note"),
				  fieldWithPath("body").description("The body of the note"),
				  fieldWithPath("_links").description("Links to other resources"))
				.links(
				  linkWithRel("self").description("This self reference"),
				  linkWithRel("note-tags").description("The link to the tags associated with this note"))
		.build()))
);
```

In this case there is one additional file generated - `notes-get-schema-response.json` - which contains the json schema generated form the documented response fields and the links.

The `raml-resource.raml` fragment would look like this. 

```yaml
/notes/{id}:
  uriParameters:
    id:
      description: The note id
      type: string
  get:
    description: Get a note by id
    responses:
      200:
        body:
          application/hal+json:
            schema: !include notes-get-schema-response.json
            example: !include notes-get-response.json
```

It now carries a reference to the json schema.

```json
{
  "type" : "object",
  "properties" : {
    "_links" : {
      "description" : "Links to other resources",
      "type" : "object",
      "properties" : {
        "note-tags" : {
          "description" : "The link to the tags associated with this note",
          "type" : "object",
          "properties" : {
            "href" : {
              "type" : "string"
            }
          }
        },
        "self" : {
          "description" : "This self reference",
          "type" : "object",
          "properties" : {
            "href" : {
              "type" : "string"
            }
          }
        }
      }
    },
    "body" : {
      "description" : "The body of the note",
      "type" : "string"
    },
    "title" : {
      "description" : "The title of the note",
      "type" : "string"
    }
  }
}
```

Please see the [ApiDocumentation](restdocs-raml-sample/src/test/java/com/example/notes/ApiDocumentation.java) in the sample application for a detailed example.

**:warning: Use `template URIs` to refer to path variables in your request**

Note how we use the `urlTemplate` to build the request with [`RestDocumentationRequestBuilders`](https://docs.spring.io/spring-restdocs/docs/current/api/org/springframework/restdocs/mockmvc/RestDocumentationRequestBuilders.html#get-java.lang.String-java.lang.Object...-). This makes the `urlTemplate` available in the snippet and we can render it into the `RAML` fragments. 

 ```java
mockMvc.perform(get("/notes/{id}", noteId))
 ```

### Documenting Bean Validation constraints 

Similar to the way Spring REST Docs allows to use [bean validation constraints](https://docs.spring.io/spring-restdocs/docs/current/reference/html5/#documenting-your-api-constraints) to enhance your documentation, you can also use the constraints from your model classes to let `restdocs-raml` enrich the generated JsonSchemas. 
`restdocs-raml` provides the class [com.epages.restdocs.raml.ConstrainedFields](restdocs-raml/src/main/java/com/epages/restdocs/raml/ConstrainedFields.java) to generate `FieldDescriptor`s that contain information about the constraints on this field. 

Currently the following constraints are considered when generating JsonSchema from `FieldDescriptor`s that have been created via `com.epages.restdocs.raml.ConstrainedFields`
- `NotNull`, `NotEmpty`, and `NotBlank` annotated fields become required fields in the JsonSchema
- for String fields annotated with `NotEmpty`, and `NotBlank` the `minLength` constraint in JsonSchema is set to 1
- for String fields annotated with `Length` the `minLength` and `maxLength` constraints in JsonSchema are set to the value of the corresponding attribute of the annotation

Example:

The model class carries the bean validation annotations
```java
public class NoteInput {
	
	@NotBlank
	private final String title;

	@Length(max = 1024)
	private final String body;

	private final List<URI> tagUris;
//...
}
```

```java
//create a ConstrainedFields instance with the model class carrying the bean validation constraint annotations
private ConstrainedFields noteFields = new ConstrainedFields(NoteInput.class);
//...
resultActions
    .andDo(document("notes-create",
        ramlResource(RamlResourceSnippetParameters.builder()
            .description("Create a note")
            .requestFields(
              //use the ConstrainedFields instance to create the field descriptors
                noteFields.withPath("title").description("The title of the note"),
                noteFields.withPath("body").description("The body of the note"),
                noteFields.withPath("tags").description("An array of tag resource URIs"))
            .build())));
```

This results in the following JsonSchema:
```
{
  "type" : "object",
  "properties" : {
    "title" : {
      "description" : "The title of the note",
      "type" : "string",
      "minLength" : 1
    },
    "body" : {
      "description" : "The body of the note",
      "type" : "string",
      "minLength" : 0,
      "maxLength" : 1024
    },
    "tags" : {
      "description" : "An array of tag resource URIs",
      "type" : "array"
    }
  },
  "required" : [ "title" ]
}
```

### Migrate existing Spring REST Docs tests

For convenience when applying `restdocs-raml` to an existing project that uses Spring REST Docs, we introduced [RamlDocumentation](restdocs-raml/src/main/java/com/epages/restdocs/raml/RamlDocumentation.java).

In your tests you can just replace calls to `MockMvcRestDocumentation.document` with the corresponding variant of `RamlDocumentation.document`.

`RamlDocumentation.document` will execute the specified snippets and also add a `RamlResourceSnippet` equipped with the input from your snippets.

Here is an example:

```java
resultActions
  .andDo(
    RamlDocumentation.document(operationName,
      requestFields(fieldDescriptors().getFieldDescriptors()),
      responseFields(
        fieldWithPath("comment").description("the comment"),
        fieldWithPath("flag").description("the flag"),
        fieldWithPath("count").description("the count"),
        fieldWithPath("id").description("id"),
        fieldWithPath("_links").ignored()
      ),
      links(linkWithRel("self").description("some"))
  )
);
```

This will do exactly the same as using `MockMvcRestDocumentation.document` without `restdocs-raml`.
Additionally it will add a `RamlResourceSnippet` with the descriptors you provided in the `RequestFieldsSnippet`, `ResponseFieldsSnippet`, and `LinksSnippet`.

### Running the gradle plugin

`restdocs-raml-gradle-plugin` is responsible for picking up the generated `RAML` fragments and aggregate them into a set of top-level `RAML` files that describe the complete API.
For this purpose we use the `ramldoc` task:

```
./gradlew ramldoc
```

For our [sample project](restdocs-raml-sample) this creates the following files in the output directory (`build/ramldoc`).

```
.
├── api-public.raml
├── api.raml
├── notes-create-request.json
├── notes-create-schema-request.json
├── notes-get-response.json
├── notes-get-schema-response.json
├── notes-list-response.json
├── notes-list-schema-response.json
├── notes-public.raml
├── notes.raml
├── tags-create-request.json
├── tags-create-schema-request.json
├── tags-get-response.json
├── tags-get-schema-response.json
├── tags-list-response.json
├── tags-list-schema-response.json
├── tags-patch-request.json
├── tags-patch-schema-request.json
├── tags-public.raml
└── tags.raml
```

`api.raml` is the top-level `RAML` file. 
`api-public.raml` does not contain the resources that you have marked as _private_ using `RamlResourceSnippetParameters.privateResource`.
The file `api-public.raml` if you set `separatePublicApi` property to true (see [Gradle plugin configuration](#gradle-plugin-configuration))

*api.raml*
```yaml
#%RAML 1.0
title: Notes API
baseUri: http://localhost:8080/
/tags: !include tags.raml
/notes: !include notes.raml
```

The top-level file just links to the top-level resources - here we have `tags` and `notes`.

*notes.raml*
```yaml
  post:
    description: notes-create
    body:
      application/hal+json:
        type: !include notes-create-schema-request.json
        example: !include notes-create-request.json
  /{id}:
    patch:
      description: tags-patch
      body:
        application/hal+json:
          type: !include tags-patch-schema-request.json
          example: !include tags-patch-request.json
    get:
      description: notes-get
      responses:
        200:
          body:
            application/hal+json:
              type: !include notes-get-schema-response.json
              example: !include notes-get-response.json
```

### Gradle plugin configuration

The `restdocs-raml-gradle-plugin` takes the following configuration options - all are optional.

Name | Description | Default value
---- | ----------- | -------------
apiTitle | The title of the generated top-level `RAML` file | empty
apiBaseUri | The base uri added to the top-level `RAML` file | empty
ramlVersion | `RAML` version header - `0.8` or `1.0` | `1.0`
separatePublicApi | Should the plugin generate an additional `api-public.raml` that does not contain the resources marked as private | `false`
outputDirectory | The output directory | `build/ramldoc`
outputFileNamePrefix | The file name prefix of the top level RAML file | `api` which results in `api.raml`
snippetsDirectory | The directory Spring REST Docs generated the snippets to | `build/generated-snippets`

## Generate HTML

We can use [raml2html](https://www.npmjs.com/package/raml2html) to generate HTML out of our `RAML` file.

Our [sample project](restdocs-raml-sample/build.gradle) contains a setup to do this via docker. We use the [gradle-docker-plugin](https://github.com/bmuschko/gradle-docker-plugin) and a [docker image containing `raml2html`](https://github.com/mattjtodd/docker-raml2html).

So you can generate HTML for our sample project using

```
cd restdocs-raml-sample
./gradlew raml2html
``` 

You find the HTML file under `build/ramldoc/api.raml.html` after running the gradle task.

:warning: the current version of raml2html is only working with RAML 1.0 - you have to fall back to raml2html version 3 - `npm install -g raml2html@3.0.1`

## Generate an interactive API console

[api-console](https://github.com/mulesoft/api-console) is a great tool to generate an interactive playground for your API using the RAML file generated from `restdocs-raml`. 

You can install the `api-console-cli` using npm.

```
npm install -g api-console-cli
```

Start the API console
```
cd restdocs-raml-sample
./gradlew ramldoc
cd build/ramldoc/
api-console dev api.raml
open http://localhost:8081
``` 

## Compatibility with Spring Boot 2 (WebTestClient)

`restdocs-raml` is compatible with Spring REST Docs 2 and the new `WebTestClient` since version `0.2.3`.

We adopted the Spring REST Docs sample project that shows the usage of `WebTestClient` to use `restdocs-raml` to verify the compatibility.
See https://github.com/mduesterhoeft/spring-restdocs/tree/master/samples/web-test-client.

## Limitations

### Rest Assured

Spring REST Docs also supports REST Assured to write tests that produce documentation. We currently have not tried REST Assured with our project.

### Maven plugin

Currently only a gradle plugin exists to aggregate the `RAML` fragments. 
