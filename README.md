# Jersey-JUnit
A JUnit 5 extension for testing JAX-RS and Jersey-based applications using the Jersey test framework.

[![Build Status](https://travis-ci.org/hanleyt/jersey-junit.svg?branch=master)](https://travis-ci.org/hanleyt/jersey-junit)

Usage
------

Register the extension in your test class programmatically using `@RegisterExtension`. 
You must pass the extension a function that accepts an extension context and returns a configured Application.
This will start and stop the Jersey test container for each test.

 ```java
    @RegisterExtension
    JerseyExtension jerseyExtension = new JerseyExtension(this::configureJersey);

    private Application configureJersey(ExtensionContext extensionContext) {
        return new ResourceConfig(DummyResource.class);
    }
 ```