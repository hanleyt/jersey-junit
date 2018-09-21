package com.github.hanleyt;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JerseyExtension should")
class JerseyExtensionTest {

    @Test
    @DisplayName("only be registered programmatically")
    void only_be_registered_programmatically() throws NoSuchMethodException {
        Constructor<JerseyExtension> constructor = JerseyExtension.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        Exception exception = Assertions.assertThrows(Exception.class, constructor::newInstance);
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertTrue(exception.getCause().getMessage().contains("JerseyExtension must be registered programmatically"));
    }

    @Nested
    @DisplayName("when registered and configured with a simple resource")
    class SimpleResourceApp {

        @RegisterExtension
        JerseyExtension jerseyExtension = new JerseyExtension(this::configureJersey);

        private Application configureJersey() {
            return new ResourceConfig(DummyResource.class);
        }

        @Test
        @DisplayName("access the resource using the injected WebTarget")
        void web_target_is_injected(WebTarget target) {
            assertNotNull(target);
            String values = target.path("values").request().get(String.class);
            assertEquals(DummyResource.DEFAULT_VALUES, values);
        }

        @Test
        @DisplayName("access the resource using the injected Client and URI")
        void client_is_injected(Client client, URI baseUri) {
            assertNotNull(client);
            String values = client.target(baseUri).path("values").request().get(String.class);
            assertEquals(DummyResource.DEFAULT_VALUES, values);
        }

    }

    @Nested
    @DisplayName("when registered and configured with a resource that depends on another extension")
    @ExtendWith(ExtensionNeededToConfigureJersey.class)
    class ResourceWithDependenciesApp {

        @RegisterExtension
        JerseyExtension jerseyExtension = new JerseyExtension(this::configureJersey);

        private ResourceConfig configureJersey(ExtensionContext extensionContext) {
            assertNotNull(extensionContext);
            String testValue = ExtensionNeededToConfigureJersey.getStore(extensionContext).get(String.class, String.class);
            assertFalse(testValue.isEmpty());
            ResourceConfig resourceConfig = new ResourceConfig();
            resourceConfig.register(new DummyResource(testValue));
            return resourceConfig;
        }

        @Test
        @DisplayName("access the resource using the injected WebTarget")
        void web_target_is_injected(WebTarget target) {
            assertNotNull(target);
            String values = target.path("values").request().get(String.class);
            assertEquals(ExtensionNeededToConfigureJersey.TEST_VALUE, values);
        }

        @Test
        @DisplayName("access the resource using the injected Client and URI")
        void client_is_injected(Client client, URI baseUri) {
            assertNotNull(client);
            String values = client.target(baseUri).path("values").request().get(String.class);
            assertEquals(ExtensionNeededToConfigureJersey.TEST_VALUE, values);
        }
    }

    @Nested
    @DisplayName("when registered and configured with a simple resource and a client configuration function.")
    class SimpleResourceWithClientConfigurationApp {
    	boolean configureClientCalled = false;

        @RegisterExtension
        JerseyExtension jerseyExtension = new JerseyExtension(this::configureJersey, this::configureJerseyClient);

        private Application configureJersey() {
            return new ResourceConfig(DummyResource.class);
        }

        private ClientConfig configureJerseyClient(ExtensionContext extensionContext, ClientConfig clientConfig) {
            assertNotNull(extensionContext);
            assertNotNull(clientConfig);
            configureClientCalled = true;
    		return clientConfig;
    	}
    	
        @Test
        @DisplayName("access the resource using the injected WebTarget")
        void web_target_is_injected(WebTarget target) {
            assertNotNull(target);
            String values = target.path("values").request().get(String.class);
            assertEquals(DummyResource.DEFAULT_VALUES, values);
            assertTrue(configureClientCalled);
        }

        @Test
        @DisplayName("access the resource using the injected Client and URI")
        void client_is_injected(Client client, URI baseUri) {
            assertNotNull(client);
            String values = client.target(baseUri).path("values").request().get(String.class);
            assertEquals(DummyResource.DEFAULT_VALUES, values);
            assertTrue(configureClientCalled);
        }

    }

    @Nested
    @DisplayName("when registered and configured with a resource that depends on another extension and a client configuration function.")
    @ExtendWith(ExtensionNeededToConfigureJersey.class)
    class ResourceWithDependenciesWithClientConfigurationApp {
    	boolean configureClientCalled = false;
    	
        @RegisterExtension
        JerseyExtension jerseyExtension = new JerseyExtension(this::configureJersey, this::configureJerseyClient);

        private ResourceConfig configureJersey(ExtensionContext extensionContext) {
            assertNotNull(extensionContext);
            String testValue = ExtensionNeededToConfigureJersey.getStore(extensionContext).get(String.class, String.class);
            assertFalse(testValue.isEmpty());
            ResourceConfig resourceConfig = new ResourceConfig();
            resourceConfig.register(new DummyResource(testValue));
            return resourceConfig;
        }

        private ClientConfig configureJerseyClient(ExtensionContext extensionContext, ClientConfig clientConfig) {
            assertNotNull(extensionContext);
            assertNotNull(clientConfig);
            configureClientCalled = true;
    		return clientConfig;
    	}
    	
        @Test
        @DisplayName("access the resource using the injected WebTarget")
        void web_target_is_injected(WebTarget target) {
            assertNotNull(target);
            String values = target.path("values").request().get(String.class);
            assertEquals(ExtensionNeededToConfigureJersey.TEST_VALUE, values);
            assertTrue(configureClientCalled);
        }

        @Test
        @DisplayName("access the resource using the injected Client and URI")
        void client_is_injected(Client client, URI baseUri) {
            assertNotNull(client);
            String values = client.target(baseUri).path("values").request().get(String.class);
            assertEquals(ExtensionNeededToConfigureJersey.TEST_VALUE, values);
        }
    }


    private static class ExtensionNeededToConfigureJersey implements BeforeEachCallback, AfterEachCallback {

        private static final String TEST_VALUE = "testValue";

        @Override
        public void beforeEach(ExtensionContext context) {
            getStore(context).put(String.class, TEST_VALUE);
        }

        @Override
        public void afterEach(ExtensionContext context) {
            getStore(context).remove(String.class);
        }

        private static ExtensionContext.Store getStore(ExtensionContext context) {
            return context.getStore(ExtensionContext.Namespace.GLOBAL);
        }
    }

    @Nested
    @DisplayName("when beforeEach is called")
    @TestInstance(Lifecycle.PER_CLASS)
    class StorePopulationTests {

        private JerseyExtension jerseyExtension;
        private ExtensionContext extensionContext;

        @BeforeAll
        void setUp() throws Exception {
            jerseyExtension = new JerseyExtension((unused) -> new ResourceConfig());
            extensionContext = new DummyExtensionContext();
            jerseyExtension.beforeEach(extensionContext);
        }

        @Test
        @DisplayName("create the JerseyTest and add it to the store")
        void jersey_test_is_added_to_the_store() {
            JerseyTest jerseyTest = JerseyExtension.getStore(extensionContext).get(JerseyTest.class, JerseyTest.class);
            assertNotNull(jerseyTest);
        }

        @Test
        @DisplayName("create the Client and add it to the store")
        void client_is_added_to_the_store() {
            Client client = JerseyExtension.getStore(extensionContext).get(Client.class, Client.class);
            assertNotNull(client);

            JerseyTest jerseyTest = JerseyExtension.getStore(extensionContext).get(JerseyTest.class, JerseyTest.class);
            assertTrue(client == jerseyTest.client());
        }

        @Test
        @DisplayName("create the WebTarget and add it to the store")
        void web_target_is_added_to_the_store() {
            WebTarget webTarget = JerseyExtension.getStore(extensionContext).get(WebTarget.class, WebTarget.class);
            assertNotNull(webTarget);

            JerseyTest jerseyTest = JerseyExtension.getStore(extensionContext).get(JerseyTest.class, JerseyTest.class);
            assertEquals(webTarget.getUri(), jerseyTest.target().getUri());
        }

        @Test
        @DisplayName("create the URI and add it to the store")
        void uri_is_added_to_the_store() {
            URI baseUri = JerseyExtension.getStore(extensionContext).get(URI.class, URI.class);
            assertNotNull(baseUri);

            WebTarget webTarget = JerseyExtension.getStore(extensionContext).get(WebTarget.class, WebTarget.class);
            assertEquals(baseUri, webTarget.getUri());
        }

        @Nested
        @DisplayName("and afterEach is called")
        @TestInstance(Lifecycle.PER_CLASS)
        class AfterEachShould {

            @BeforeAll
            void setUp() throws Exception {
                jerseyExtension.afterEach(extensionContext);
            }

            @Test
            @DisplayName("the JerseyTest has been removed from the store")
            void jersey_test_is_added_to_the_store() {
                JerseyTest jerseyTest = JerseyExtension.getStore(extensionContext).get(JerseyTest.class, JerseyTest.class);
                assertNull(jerseyTest);
            }

            @Test
            @DisplayName("the Client has been removed from the store")
            void client_is_added_to_the_store() {
                Client client = JerseyExtension.getStore(extensionContext).get(Client.class, Client.class);
                assertNull(client);
            }

            @Test
            @DisplayName("the WebTarget has been removed from the store")
            void web_target_is_added_to_the_store() {
                WebTarget webTarget = JerseyExtension.getStore(extensionContext).get(WebTarget.class, WebTarget.class);
                assertNull(webTarget);
            }

            @Test
            @DisplayName("the URI has been removed from the store")
            void uri_is_added_to_the_store() {
                URI baseUri = JerseyExtension.getStore(extensionContext).get(URI.class, URI.class);
                assertNull(baseUri);
            }

        }

    }

}
