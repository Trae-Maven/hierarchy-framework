package io.github.trae.hf;

import io.github.trae.di.InjectorApi;
import io.github.trae.di.sorters.ComponentSorter;
import io.github.trae.hf.comparators.HierarchyComparator;
import io.github.trae.utilities.UtilString;

/**
 * The root of the hierarchy framework.
 *
 * <p>A Plugin represents an application or feature boundary that owns all
 * components beneath it. Managers and Nodes all resolve back to their owning
 * Plugin instance via {@link Frame#getPlugin()}.</p>
 *
 * <p>Provides a human-readable name derived from the simple name of the
 * implementing class.</p>
 */
public interface Plugin {

    /**
     * Returns a human-readable name for this plugin, derived from the simple
     * name of the implementing class by splitting it into spaced words at each
     * word boundary — {@code CorePlugin} becomes {@code Core Plugin}.
     *
     * <p>The name comes from {@link Class#getSimpleName()}, so a nested class
     * contributes only its own name without the enclosing type, and an
     * anonymous class yields an empty string.</p>
     *
     * @return the formatted name of this plugin
     */
    default String getPluginName() {
        return UtilString.unSlice(this.getClass().getSimpleName());
    }

    /**
     * Bootstraps the plugin by registering a {@link HierarchyComparator},
     * initializing the dependency injection container, and executing
     * {@link #onComponentInitialize(Object)} for each component belonging
     * to this plugin.
     *
     * <p>Components are initialized in hierarchy order: each {@link Manager}
     * first, then the {@link Node Nodes} beneath it in ascending depth,
     * grouped by their owning Manager.</p>
     */
    default void initializePlugin() {
        ComponentSorter.addComparator(new HierarchyComparator());

        InjectorApi.initialize(this);

        InjectorApi.executeCallback(this.getClass(), this::onComponentInitialize);
    }

    /**
     * Shuts down the plugin by executing {@link #onComponentShutdown(Object)}
     * for each component belonging to this plugin, then destroying the
     * container's components via {@link InjectorApi#shutdown(Object)}.
     *
     * <p>Components are shut down in reverse initialization order: the
     * deepest Nodes first, then their parents, then the owning Manager.</p>
     */
    default void shutdownPlugin() {
        InjectorApi.executeCallback(this.getClass(), this::onComponentShutdown);

        InjectorApi.shutdown(this);
    }

    /**
     * Called for each component instance after the container has been
     * fully wired. If the instance is a {@link Frame}, its
     * {@link Frame#initializeFrame()} method is invoked by default.
     *
     * <p>Override to register components with external systems such as
     * event buses, command handlers, or platform APIs. Call
     * {@code super.onComponentInitialize(instance)} to preserve the
     * Frame lifecycle hook.</p>
     *
     * @param instance the component instance to register
     */
    default void onComponentInitialize(final Object instance) {
        if (instance instanceof final Frame<?> frame) {
            frame.initializeFrame();
        }
    }

    /**
     * Called for each component instance before the container begins
     * destroying components. If the instance is a {@link Frame}, its
     * {@link Frame#shutdownFrame()} method is invoked by default.
     *
     * <p>Override to unregister components from external systems such as
     * event buses, command handlers, or platform APIs. Call
     * {@code super.onComponentShutdown(instance)} to preserve the
     * Frame lifecycle hook.</p>
     *
     * @param instance the component instance to unregister
     */
    default void onComponentShutdown(final Object instance) {
        if (instance instanceof final Frame<?> frame) {
            frame.shutdownFrame();
        }
    }
}