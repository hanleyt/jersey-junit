package com.github.hanleyt;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class JerseyExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final Collection<Class<?>> INJECTABLE_PARAMETER_TYPES = Arrays.asList(Client.class, WebTarget.class, URI.class);

    private final Function<ExtensionContext, Application> applicationProvider;
    private final BiFunction<ExtensionContext, ClientConfig, ClientConfig> configProvider;

    private JerseyExtension() {
        throw new IllegalStateException("JerseyExtension must be registered programmatically");
    }

    public JerseyExtension(Supplier<Application> applicationSupplier) {
        this.applicationProvider = (unused) -> applicationSupplier.get();
        this.configProvider = null;
    }

    public JerseyExtension(Supplier<Application> applicationSupplier, BiFunction<ExtensionContext, ClientConfig, ClientConfig> configProvider) {
        this.applicationProvider = (unused) -> applicationSupplier.get();
        this.configProvider = configProvider;
    }

    public JerseyExtension(Function<ExtensionContext, Application> applicationProvider) {
        this.applicationProvider = applicationProvider;
        this.configProvider = null;
    }

    public JerseyExtension(Function<ExtensionContext, Application> applicationProvider, BiFunction<ExtensionContext, ClientConfig, ClientConfig> configProvider) {
        this.applicationProvider = applicationProvider;
        this.configProvider = configProvider;
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        JerseyTest jerseyTest = initJerseyTest(context);
        getStore(context).put(Client.class, jerseyTest.client());
        getStore(context).put(WebTarget.class, jerseyTest.target());
    }

    private JerseyTest initJerseyTest(ExtensionContext context) throws Exception {
        JerseyTest jerseyTest = new JerseyTest() {
            @Override
            protected Application configure() {
                getStore(context).put(URI.class, getBaseUri());
                return applicationProvider.apply(context);
            }

			@Override
			protected void configureClient(ClientConfig config) {
				if (configProvider != null) {
					config = configProvider.apply(context, config);
				}
				super.configureClient(config);
			}
        };
        jerseyTest.setUp();
        getStore(context).put(JerseyTest.class, jerseyTest);
        return jerseyTest;
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        ExtensionContext.Store store = getStore(context);
        store.remove(JerseyTest.class, JerseyTest.class).tearDown();
        INJECTABLE_PARAMETER_TYPES.forEach(store::remove);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> parameterType = parameterContext.getParameter().getType();
        return INJECTABLE_PARAMETER_TYPES.contains(parameterType);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> parameterType = parameterContext.getParameter().getType();
        return getStore(extensionContext).get(parameterType, parameterType);
    }

    public static ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.GLOBAL);
    }

}