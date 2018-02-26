package com.github.hanleyt;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("Given a JerseyExtension")
class JerseyExtensionTest {

    @Nested
    @DisplayName("when configured with a simple resource")
    class SimpleResource {

        @RegisterExtension
        JerseyExtension jerseyExtension = new JerseyExtension(this::configureJersey);

        private ResourceConfig configureJersey(ExtensionContext extensionContext) {
            assertThat(extensionContext).isNotNull();
            return new ResourceConfig(DummyResource.class);
        }

        @Test
        @DisplayName("the injected web target can access the resource")
        void web_target_is_injected(WebTarget target) {
            assertThat(target).isNotNull();
            String values = target.path("values").request().get(String.class);
            assertThat(values).isEqualTo(DummyResource.DEFAULT_VALUES);
        }

        @Test
        @DisplayName("the injected client can access the resource")
        void client_is_injected(Client client) {
            assertThat(client).isNotNull();
            URI baseUri = UriBuilder.fromUri("http://localhost/").port(TestProperties.DEFAULT_CONTAINER_PORT).build();
            String values = client.target(baseUri).path("values").request().get(String.class);
            assertThat(values).isEqualTo(DummyResource.DEFAULT_VALUES);
        }

    }


    @Nested
    @DisplayName("when the configuration depends on another extension")
    @ExtendWith(ExtensionNeededToConfigureJersey.class)
    class DependsOnOtherExtension {

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
        @DisplayName("the injected web target can access the resource")
        void web_target_is_injected(WebTarget target) {
            assertThat(target).isNotNull();
            String values = target.path("values").request().get(String.class);
            assertThat(values).isEqualTo(ExtensionNeededToConfigureJersey.TEST_VALUE);
        }

        @Test
        @DisplayName("the injected client can access the resource")
        void client_is_injected(Client client) {
            assertThat(client).isNotNull();
            URI baseUri = UriBuilder.fromUri("http://localhost/").port(TestProperties.DEFAULT_CONTAINER_PORT).build();
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

        public static ExtensionContext.Store getStore(ExtensionContext context) {
            return context.getStore(ExtensionContext.Namespace.GLOBAL);
        }
    }

}
