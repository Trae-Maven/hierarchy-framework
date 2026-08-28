package io.github.trae.hf;

import io.github.trae.di.InjectorApi;
import io.github.trae.utilities.UtilGeneric;

/**
 * A hierarchical component with typed access to its parent.
 *
 * <p>Nodes form the layers beneath a {@link Manager} and nest to any depth —
 * a Node's parent is either the owning Manager or another Node, so every
 * chain terminates at exactly one Manager within the same {@link Plugin}.</p>
 *
 * <p>Parent resolution is performed reflectively at runtime using
 * {@link UtilGeneric} to resolve the concrete type parameter, and
 * {@link InjectorApi} to retrieve the singleton instance from the dependency
 * injector.</p>
 *
 * <p>Position in the chain determines lifecycle order: a Node is initialized
 * after its parent and shut down before it, as ordered by
 * {@link io.github.trae.hf.comparators.HierarchyComparator}.</p>
 *
 * @param <P>      the plugin type this node belongs to
 * @param <Parent> the parent type, either a {@link Manager} or another Node
 */
public interface Node<P extends Plugin, Parent extends Frame<P>> extends Frame<P> {

    /**
     * Resolves and returns the parent this node belongs to.
     *
     * <p>The concrete parent type is resolved reflectively from the generic
     * type parameter at index 1 of {@link Node}, then retrieved from the
     * dependency injector. Walk further up the chain by calling
     * {@code getParent()} on the returned instance, or reach the root
     * directly with {@link Frame#getPlugin()}.</p>
     *
     * @return the parent instance
     * @throws IllegalStateException if the parent type cannot be resolved
     */
    @SuppressWarnings("unchecked")
    default Parent getParent() {
        final Class<?> parentType = UtilGeneric.getGenericParameter(this.getClass(), Node.class, 1);
        if (parentType == null) {
            throw new IllegalStateException("Could not resolve parent type for %s".formatted(this.getClass().getName()));
        }

        return (Parent) InjectorApi.get(parentType);
    }
}