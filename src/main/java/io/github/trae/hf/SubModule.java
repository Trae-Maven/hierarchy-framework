package io.github.trae.hf;

import io.github.trae.di.InjectorApi;
import io.github.trae.utilities.UtilGeneric;

/**
 * The most granular component in the hierarchy, living within a {@link Module}.
 *
 * <p>SubModules represent leaf-level functionality such as individual commands,
 * listeners, or handlers. They inherit plugin resolution from {@link Frame}
 * and add automatic module resolution via {@link #getModule()}.</p>
 *
 * @param <P> the plugin type this sub-module belongs to
 * @param <M> the module type this sub-module belongs to
 */
public interface SubModule<P extends Plugin, M extends Module<P, ?>> extends Frame<P> {

    /**
     * Resolves and returns the module instance this sub-module belongs to.
     *
     * <p>The concrete module type is resolved reflectively from the generic
     * type parameter at index 1 of {@link SubModule}, then retrieved from
     * the dependency injector.</p>
     *
     * @return the module instance
     * @throws IllegalStateException if the module type cannot be resolved
     */
    @SuppressWarnings("unchecked")
    default M getModule() {
        final Class<?> moduleClass = UtilGeneric.getGenericParameter(this.getClass(), SubModule.class, 1);
        if (moduleClass == null) {
            throw new IllegalStateException("Could not resolve module type for %s".formatted(this.getClass().getName()));
        }

        return (M) InjectorApi.get(moduleClass);
    }
}