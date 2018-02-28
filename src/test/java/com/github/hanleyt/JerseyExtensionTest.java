package com.github.hanleyt;

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

import static com.google.common.truth.Truth.assertThat;

@DisplayName("JerseyExtension should")
class JerseyExtensionTest {

    @Test
    @DisplayName("only be registered programmatically")
    void only_be_registered_programmatically() throws NoSuchMethodException {
        Constructor<JerseyExtension> constructor = JerseyExtension.class.getDeclaredConstructor();
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        constructor.setAccessible(true);
        Exception exception = Assertions.assertThrows(Exception.class, constructor::newInstance);
        assertThat(exception).hasCauseThat().isInstanceOf(IllegalStateException.class);
        assertThat(exception).hasCauseThat().hasMessageThat().isEqualTo("JerseyExtension must be registered programmatically");
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
            assertThat(target).isNotNull();
            String values = target.path("values").request().get(String.class);
            assertThat(values).isEqualTo(DummyResource.DEFAULT_VALUES);
        }

        @Test
        @DisplayName("access the resource using the injected Client and URI")
        void client_is_injected(Client client, URI baseUri) {
            assertThat(client).isNotNull();
            String values = client.target(baseUri).path("values").request().get(String.class);
            assertThat(values).isEqualTo(DummyResource.DEFAULT_VALUES);
        }

    }

    @Nested
    @DisplayName("when registered and configured with a resource that depends on another extension")
    @ExtendWith(ExtensionNeededToConfigureJersey.class)
    class ResourceWithDependenciesApp {

        @RegisterExtension
        JerseyExtension jerseyExtension = new JerseyExtension(this::configureJersey);

        private ResourceConfig configureJersey(ExtensionContext extensionContext) {
            assertThat(extensionContext).isNotNull();
            String testValue = ExtensionNeededToConfigureJersey.getStore(extensionContext).get(String.class, String.class);
            assertThat(testValue).isNotEmpty();
            ResourceConfig resourceConfig = new ResourceConfig();
            resourceConfig.register(new DummyResource(testValue));
            return resourceConfig;
        }

        @Test
        @DisplayName("access the resource using the injected WebTarget")
        void web_target_is_injected(WebTarget target) {
            assertThat(target).isNotNull();
            String values = target.path("values").request().get(String.class);
            assertThat(values).isEqualTo(ExtensionNeededToConfigureJersey.TEST_VALUE);
        }

        @Test
        @DisplayName("access the resource using the injected Client and URI")
        void client_is_injected(Client client, URI baseUri) {
            assertThat(client).isNotNull();
            String values = client.target(baseUri).path("values").request().get(String.class);
            assertThat(values).isEqualTo(ExtensionNeededToConfigureJersey.TEST_VALUE);
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
            assertThat(jerseyTest).isNotNull();
        }

        @Test
        @DisplayName("create the Client and add it to the store")
        void client_is_added_to_the_store() {
            Client client = JerseyExtension.getStore(extensionContext).get(Client.class, Client.class);
            assertThat(client).isNotNull();

            JerseyTest jerseyTest = JerseyExtension.getStore(extensionContext).get(JerseyTest.class, JerseyTest.class);
            assertThat(client).isSameAs(jerseyTest.client());
        }

        @Test
        @DisplayName("create the WebTarget and add it to the store")
        void web_target_is_added_to_the_store() {
            WebTarget webTarget = JerseyExtension.getStore(extensionContext).get(WebTarget.class, WebTarget.class);
            assertThat(webTarget).isNotNull();

            JerseyTest jerseyTest = JerseyExtension.getStore(extensionContext).get(JerseyTest.class, JerseyTest.class);
            assertThat(webTarget.getUri()).isEqualTo(jerseyTest.target().getUri());
        }

        @Test
        @DisplayName("create the URI and add it to the store")
        void uri_is_added_to_the_store() {
            URI baseUri = JerseyExtension.getStore(extensionContext).get(URI.class, URI.class);
            assertThat(baseUri).isNotNull();

            WebTarget webTarget = JerseyExtension.getStore(extensionContext).get(WebTarget.class, WebTarget.class);
            assertThat(baseUri).isEqualTo(webTarget.getUri());
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
                assertThat(jerseyTest).isNull();
            }

            @Test
            @DisplayName("the Client has been removed from the store")
            void client_is_added_to_the_store() {
                Client client = JerseyExtension.getStore(extensionContext).get(Client.class, Client.class);
                assertThat(client).isNull();
            }

            @Test
            @DisplayName("the WebTarget has been removed from the store")
            void web_target_is_added_to_the_store() {
                WebTarget webTarget = JerseyExtension.getStore(extensionContext).get(WebTarget.class, WebTarget.class);
                assertThat(webTarget).isNull();
            }

            @Test
            @DisplayName("the URI has been removed from the store")
            void uri_is_added_to_the_store() {
                URI baseUri = JerseyExtension.getStore(extensionContext).get(URI.class, URI.class);
                assertThat(baseUri).isNull();
            }

        }

    }

}
