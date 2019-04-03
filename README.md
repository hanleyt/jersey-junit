# Jersey-JUnit
A [JUnit 5 extension](https://junit.org/junit5/docs/current/user-guide/#extensions) for testing JAX-RS and Jersey-based applications using the Jersey test framework.

[![Build Status](https://travis-ci.org/hanleyt/jersey-junit.svg?branch=master)](https://travis-ci.org/hanleyt/jersey-junit)
[![codecov](https://codecov.io/gh/hanleyt/jersey-junit/branch/master/graph/badge.svg)](https://codecov.io/gh/hanleyt/jersey-junit)
[![jitpack](https://jitpack.io/v/hanleyt/jersey-junit.svg)](https://jitpack.io/#hanleyt/jersey-junit)


Set Up
-----
Add the following dependency to your gradle build file:

```testCompile group: 'com.github.hanleyt', name: 'jersey-junit', version: '1.2.1'```

Ensuring you have the jitpack repo in your list of repos:

```maven { url 'https://jitpack.io/' }```

Note you must be using [JUnit 5.1](https://junit.org/junit5/docs/current/release-notes/index.html#release-notes-5.1.0) or higher.

Usage
------

Register the extension in your test class [programmatically](https://junit.org/junit5/docs/current/user-guide/#extensions-registration-programmatic) using `@RegisterExtension`. This will start and stop the Jersey test container for each test.

You must pass the JerseyExtension constructor a supplier that returns a configured Application. 
If the ExtensionContext is required to configure the application, you can instead pass a function that accepts an extension context and returns a configured Application.

 ```java
    @RegisterExtension
    JerseyExtension jerseyExtension = new JerseyExtension(this::configureJersey);

    private Application configureJersey(ExtensionContext extensionContext) {
        return new ResourceConfig(DummyResource.class);
    }
 ```

If the client configuration needs to be customized, then you can also pass in a function that accepts an ClientConfig and returns a modified ClientConfig object.

 ```java
    @RegisterExtension
    JerseyExtension jerseyExtension = new JerseyExtension(this::configureJersey, this::configureJerseyClient);

    private Application configureJersey(ExtensionContext extensionContext) {
        return new ResourceConfig(DummyResource.class);
    }
    
    private ClientConfig configureJerseyClient(ExtensionContext extensionContext, ClientConfig clientConfig) {
		clientConfig.connectorProvider(new ApacheConnectorProvider());
		return clientConfig;
	}
	
    
 ```

 
 You can then [inject](https://junit.org/junit5/docs/current/user-guide/#writing-tests-dependency-injection) the WebTarget, Client or base URI as test method or constructor parameters.
 
  ```java
     @Test
     void web_target_is_injected(WebTarget target, Client client, URI baseUri) {
        assertThat(target).isNotNull();
        String values = target.path("values").request().get(String.class);
        assertThat(values).isEqualTo(DummyResource.DEFAULT_VALUES);
     }
  ```
  
  See the [JerseyExtensionTest](https://github.com/hanleyt/jersey-junit/blob/master/src/test/java/com/github/hanleyt/JerseyExtensionTest.java) for more usage examples.
