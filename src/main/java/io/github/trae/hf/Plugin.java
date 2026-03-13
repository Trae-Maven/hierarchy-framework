package io.github.trae.hf;

import io.github.trae.di.InjectorApi;
import io.github.trae.di.sorters.ComponentSorter;
import io.github.trae.hf.comparators.HierarchyComparator;
import io.github.trae.utilities.UtilString;

/**
 * The root of the hierarchy framework.
 *
 * <p>A Plugin represents an application or feature boundary that owns all
 * components beneath it. Managers, Modules, and SubModules all resolve
 * back to their owning Plugin instance via {@link Frame#getPlugin()}.</p>
 *
 * <p>Provides a human-readable name derived from the implementing class
 * name by expanding camelCase into spaced words.</p>
 */
public interface Plugin {

    /**
     * Returns a human-readable name for this plugin, derived from the
     * implementing class name by expanding camelCase into spaced words.
     *
     * @return the formatted name of this plugin
     */
    default String getPluginName() {
        return UtilString.unSlice(this.getClass().getSimpleName());
    }

    /**
     * Bootstraps the plugin by registering the {@link HierarchyComparator},
     * initializing the dependency injection container, and executing
     * {@link #onComponentInitialize(Object)} for each component belonging
     * to this plugin.
     *
     * <p>Components are initialized in hierarchy order: Managers first,
     * then Modules, then SubModules, grouped by their owning Manager.</p>
     */
    default void initializePlugin() {
        ComponentSorter.addComparator(new HierarchyComparator());

        InjectorApi.initialize(this.getClass());

        InjectorApi.executeCallback(this.getClass(), this::onComponentInitialize);
    }

    /**
     * Shuts down the plugin by executing {@link #onComponentShutdown(Object)}
     * for each component belonging to this plugin, then destroying the
     * container's components.
     *
     * <p>Components are shut down in reverse initialization order:
     * SubModules first, then Modules, then Managers.</p>
     */
    default void shutdownPlugin() {
        InjectorApi.executeCallback(this.getClass(), this::onComponentShutdown);

        InjectorApi.shutdown(this.getClass());
    }

    /**
     * Called for each component instance after the container has been
     * fully wired. Use this to register components with external systems
     * such as event buses, command handlers, or platform APIs.
     *
     * @param instance the component instance to register
     */
    void onComponentInitialize(final Object instance);

    /**
     * Called for each component instance before the container begins
     * destroying components. Use this to unregister components from
     * external systems such as event buses, command handlers, or
     * platform APIs.
     *
     * @param instance the component instance to unregister
     */
    void onComponentShutdown(final Object instance);
}