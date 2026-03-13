package io.github.trae.hf;

import io.github.trae.di.InjectorApi;
import io.github.trae.utilities.UtilGeneric;
import io.github.trae.utilities.UtilString;

/**
 * The base interface for all hierarchical components within the framework.
 *
 * <p>Every component in the hierarchy (Manager, Module, SubModule) extends Frame,
 * which provides automatic plugin resolution, lifecycle hooks, and a human-readable
 * name derived from the implementing class.</p>
 *
 * <p>Plugin resolution is performed reflectively at runtime using {@link UtilGeneric}
 * to resolve the concrete type parameter, and {@link InjectorApi} to retrieve the
 * singleton instance from the dependency injector.</p>
 *
 * @param <P> the plugin type this frame belongs to
 */
public interface Frame<P extends Plugin> {

    /**
     * Returns a human-readable name for this frame, derived from the
     * implementing class name by expanding camelCase into spaced words.
     *
     * @return the formatted name of this frame
     */
    default String getFrameName() {
        return UtilString.unSlice(this.getClass().getSimpleName());
    }

    /**
     * Resolves and returns the plugin instance this frame belongs to.
     *
     * <p>The concrete plugin type is resolved reflectively from the generic
     * type parameter at index 0 of {@link Frame}, then retrieved from the
     * dependency injector.</p>
     *
     * @return the plugin instance
     * @throws IllegalStateException if the plugin type cannot be resolved
     */
    @SuppressWarnings("unchecked")
    default P getPlugin() {
        final Class<?> pluginClass = UtilGeneric.getGenericParameter(this.getClass(), Frame.class, 0);
        if (pluginClass == null) {
            throw new IllegalStateException("Could not resolve plugin type for %s".formatted(this.getClass().getName()));
        }

        return (P) InjectorApi.get(pluginClass);
    }
}