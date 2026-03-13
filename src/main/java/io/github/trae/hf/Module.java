package io.github.trae.hf;

import io.github.trae.di.InjectorApi;
import io.github.trae.utilities.UtilGeneric;

/**
 * A component that lives within a {@link Manager}, providing a logical
 * grouping of related functionality.
 *
 * <p>Modules sit between Managers and {@link SubModule SubModules} in the
 * hierarchy. They inherit plugin resolution from {@link Frame} and add
 * automatic manager resolution via {@link #getManager()}.</p>
 *
 * @param <P> the plugin type this module belongs to
 * @param <M> the manager type this module belongs to
 */
public interface Module<P extends Plugin, M extends Manager<P>> extends Frame<P> {

    /**
     * Resolves and returns the manager instance this module belongs to.
     *
     * <p>The concrete manager type is resolved reflectively from the generic
     * type parameter at index 1 of {@link Module}, then retrieved from the
     * dependency injector.</p>
     *
     * @return the manager instance
     * @throws IllegalStateException if the manager type cannot be resolved
     */
    @SuppressWarnings("unchecked")
    default M getManager() {
        final Class<?> managerClass = UtilGeneric.getGenericParameter(this.getClass(), Module.class, 1);
        if (managerClass == null) {
            throw new IllegalStateException("Could not resolve manager type for %s".formatted(this.getClass().getName()));
        }

        return (M) InjectorApi.get(managerClass);
    }
}