package com.github.hanleyt;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.execution.ExtensionValuesStore;
import org.junit.jupiter.engine.execution.NamespaceAwareStore;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class DummyExtensionContext implements ExtensionContext {

    private final ExtensionValuesStore valuesStore;

    DummyExtensionContext() {
        valuesStore = new ExtensionValuesStore(null);
    }

    @Override
    public Store getStore(ExtensionContext.Namespace namespace) {
        return new NamespaceAwareStore(valuesStore, namespace);
    }

    @Override
    public Optional<ExtensionContext> getParent() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ExtensionContext getRoot() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getUniqueId() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getDisplayName() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Set<String> getTags() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Optional<AnnotatedElement> getElement() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Optional<Class<?>> getTestClass() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Optional<TestInstance.Lifecycle> getTestInstanceLifecycle() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Optional<Object> getTestInstance() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Optional<Method> getTestMethod() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Optional<Throwable> getExecutionException() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Optional<String> getConfigurationParameter(String key) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void publishReportEntry(Map<String, String> map) {
        throw new UnsupportedOperationException("Not implemented");
    }

}
