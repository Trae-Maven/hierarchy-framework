package io.github.trae.hf;

/**
 * A top-level organizational component within a {@link Plugin}.
 *
 * <p>Managers serve as the direct children of a Plugin and act as
 * containers for related {@link Module Modules}. They inherit plugin
 * resolution and lifecycle hooks from {@link Frame}.</p>
 *
 * @param <P> the plugin type this manager belongs to
 */
public interface Manager<P extends Plugin> extends Frame<P> {
}