package io.github.trae.hf.comparators;

import io.github.trae.di.sorters.comparators.ComponentComparator;
import io.github.trae.hf.Manager;
import io.github.trae.hf.Node;
import io.github.trae.utilities.UtilGeneric;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orders components so that each {@link Manager} is initialized before the
 * {@link Node Nodes} beneath it.
 *
 * <p>Components are grouped by their owning Manager, then ordered by depth
 * within that group, then by class name. Types whose parent chain does not
 * terminate at a Manager sort last, preserving a deterministic order for
 * components that sit outside the hierarchy entirely.</p>
 *
 * <p>Each type's parent chain is resolved reflectively once and cached, so a
 * sort resolves every class a single time regardless of how many comparisons
 * the underlying algorithm performs.</p>
 *
 * <p>Registered with the container by {@link io.github.trae.hf.Plugin#initializePlugin()};
 * shutdown order is the reverse of the order produced here, so children are
 * always torn down before their parents.</p>
 */
public class HierarchyComparator implements ComponentComparator {

    /**
     * Cache of resolved parent chains, keyed by the component type.
     *
     * <p>Each value runs from the component itself through its parents up to
     * and including its owning {@link Manager}, or is empty when the chain
     * does not terminate at a Manager. Populated lazily during comparison and
     * concurrent because sorting may be performed off the main thread.</p>
     */
    private final Map<Class<?>, List<Class<?>>> chains = new ConcurrentHashMap<>();

    /**
     * Compares two component types by their position in the hierarchy.
     *
     * <p>Ordering is applied in three stages: owning Manager name, then depth
     * beneath that Manager, then the component's own class name. The final
     * stage guarantees a total order, so the result is consistent even for
     * unrelated types or distinct Managers that share a simple name.</p>
     *
     * @param a the first component type
     * @param b the second component type
     * @return a negative integer, zero, or a positive integer as {@code a}
     * initializes before, at the same position as, or after {@code b}
     */
    @Override
    public int compare(final Class<?> a, final Class<?> b) {
        final List<Class<?>> left = this.chains.computeIfAbsent(a, this::resolveChain);
        final List<Class<?>> right = this.chains.computeIfAbsent(b, this::resolveChain);

        final int managerCompare = this.compareManagers(left, right);

        if (managerCompare != 0) {
            return managerCompare;
        }

        final int depthCompare = Integer.compare(left.size(), right.size());

        if (depthCompare != 0) {
            return depthCompare;
        }

        return a.getName().compareTo(b.getName());
    }

    /**
     * Compares two resolved chains by the name of the Manager they terminate at.
     *
     * <p>An empty chain represents a type that does not resolve to a Manager
     * and always sorts after one that does; two empty chains are equal and
     * fall through to the remaining stages of {@link #compare(Class, Class)}.</p>
     *
     * @param left  the first resolved chain
     * @param right the second resolved chain
     * @return a negative integer, zero, or a positive integer as the left
     * Manager sorts before, equal to, or after the right Manager
     */
    private int compareManagers(final List<Class<?>> left, final List<Class<?>> right) {
        if (left.isEmpty() || right.isEmpty()) {
            return Boolean.compare(left.isEmpty(), right.isEmpty());
        }

        return left.getLast().getName().compareTo(right.getLast().getName());
    }

    /**
     * Walks the parent chain of a component type up to its owning Manager.
     *
     * <p>Each step resolves the type declared at index 1 of {@link Node}; the
     * walk stops at the first {@link Manager}, whose type becomes the last
     * element of the returned chain. The chain's size therefore doubles as the
     * component's depth, counting the Manager itself as one.</p>
     *
     * <p>An empty list is returned when the chain leaves the hierarchy, when a
     * generic parameter cannot be resolved, or when a type is revisited, which
     * guards against a cyclic or self-referential parent declaration.</p>
     *
     * @param type the component type to resolve
     * @return the chain from {@code type} to its owning Manager, or an empty
     * list if it does not resolve to one
     */
    private List<Class<?>> resolveChain(final Class<?> type) {
        final List<Class<?>> chain = new ArrayList<>();

        Class<?> current = type;

        while (current != null && !chain.contains(current)) {
            chain.add(current);

            if (Manager.class.isAssignableFrom(current)) {
                return chain;
            }

            if (!Node.class.isAssignableFrom(current)) {
                return List.of();
            }

            current = UtilGeneric.getGenericParameter(current, Node.class, 1);
        }

        return List.of();
    }
}