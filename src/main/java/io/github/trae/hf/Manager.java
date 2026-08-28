package io.github.trae.hf;

/**
 * A top-level organizational component within a {@link Plugin}.
 *
 * <p>Managers are the direct children of a Plugin and act as the root of a
 * {@link Node} chain — every Node resolves back to exactly one Manager, which
 * is initialized before any of its Nodes and shut down after them.</p>
 *
 * <p>Managers inherit plugin resolution and lifecycle hooks from
 * {@link Frame}.</p>
 *
 * @param <P> the plugin type this manager belongs to
 */
public interface Manager<P extends Plugin> extends Frame<P> {
}