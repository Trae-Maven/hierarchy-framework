package io.github.trae.hf.comparators;

import io.github.trae.di.sorters.comparators.ComponentComparator;
import io.github.trae.hf.Manager;
import io.github.trae.hf.Module;
import io.github.trae.hf.SubModule;
import io.github.trae.utilities.UtilGeneric;

/**
 * Sorts components by their position in the hierarchy framework.
 *
 * <p>Components are first grouped by their owning {@link Manager},
 * then ordered by hierarchy depth within each group: Managers first,
 * followed by Modules, then SubModules.</p>
 *
 * <p>Components that do not participate in the hierarchy framework
 * are treated as neutral and retain their existing order.</p>
 */
public class HierarchyComparator implements ComponentComparator {

    /**
     * Compares two component classes by their position in the hierarchy.
     *
     * <p>Sorting is performed in three phases:</p>
     * <ol>
     *   <li>Group by owning Manager — components under different Managers
     *       are ordered alphabetically by the Manager's class name.
     *       Non-hierarchy components sort after hierarchy components.</li>
     *   <li>Sort by depth within the same Manager group — Managers (0)
     *       before Modules (1) before SubModules (2).</li>
     *   <li>Alphabetical tiebreaker — components at the same depth under
     *       the same Manager are ordered by their fully qualified class name.</li>
     * </ol>
     *
     * @param a the first component class
     * @param b the second component class
     * @return a negative integer, zero, or positive integer as the first
     * component should be initialized before, at the same time as,
     * or after the second
     */
    @Override
    public int compare(final Class<?> a, final Class<?> b) {
        final Class<?> leftManager = this.resolveManager(a);
        final Class<?> rightManager = this.resolveManager(b);

        if (leftManager != rightManager) {
            if (leftManager == null) {
                return 1;
            }
            if (rightManager == null) {
                return -1;
            }

            return leftManager.getName().compareTo(rightManager.getName());
        }

        final int depthCompare = Integer.compare(getDepth(a), getDepth(b));

        if (depthCompare != 0) {
            return depthCompare;
        }

        return a.getName().compareTo(b.getName());
    }

    /**
     * Resolves the owning {@link Manager} class for the given component.
     *
     * <p>If the component is a Manager, it returns itself. If the component
     * is a Module, it resolves the Manager from the generic parameter. If the
     * component is a SubModule, it resolves the Module first, then resolves
     * the Manager from that Module's generic parameter.</p>
     *
     * @param type the component class to resolve
     * @return the owning Manager class, or {@code null} if the component
     * is not part of the hierarchy
     */
    private Class<?> resolveManager(final Class<?> type) {
        if (Manager.class.isAssignableFrom(type)) {
            return type;
        }

        if (Module.class.isAssignableFrom(type)) {
            return UtilGeneric.getGenericParameter(type, Module.class, 1);
        }

        if (SubModule.class.isAssignableFrom(type)) {
            final Class<?> moduleClass = UtilGeneric.getGenericParameter(type, SubModule.class, 1);
            if (moduleClass != null) {
                return UtilGeneric.getGenericParameter(moduleClass, Module.class, 1);
            }
        }

        return null;
    }

    /**
     * Returns the hierarchy depth for the given class.
     *
     * <p>Lower values are initialized first:</p>
     * <ul>
     *   <li>0 — {@link Manager}</li>
     *   <li>1 — {@link Module}</li>
     *   <li>2 — {@link SubModule}</li>
     *   <li>3 — any other component</li>
     * </ul>
     *
     * @param type the component class to evaluate
     * @return the hierarchy depth
     */
    private int getDepth(final Class<?> type) {
        if (Manager.class.isAssignableFrom(type)) {
            return 0;
        }

        if (Module.class.isAssignableFrom(type)) {
            return 1;
        }

        if (SubModule.class.isAssignableFrom(type)) {
            return 2;
        }

        return 3;
    }
}