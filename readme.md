# Hierarchy-Framework

A lightweight hierarchical framework providing structured parent-child relationships between application components using generics.

Hierarchy-Framework defines a layered architecture where each component has typed access to its parent, allowing clean navigation through the hierarchy without casting. The framework works alongside the [Dependency Injector](https://github.com/Trae-Maven/dependency-injector) for automatic wiring and lifecycle management.

---

## Features

- Strongly-typed parent-child relationships enforced through generics
- Automatic parent resolution via reflection — no manual wiring required
- Plugin, Manager, Module, and SubModule hierarchy layers
- Human-readable component names derived from class names
- Lifecycle hooks for initialization and shutdown at every layer
- Platform integration callbacks for registering and unregistering components with external systems
- HierarchyComparator for deterministic initialization order — Managers first, then Modules, then SubModules, grouped by owning Manager
- Reverse-order shutdown — children are torn down before their parents
- Built on the Dependency Injector and Utilities libraries
- Designed for modern Java (Java 21+)

---

## Hierarchy
```
Plugin
  └─ Manager
       └─ Module
            └─ SubModule
```

Each layer has typed access to its parent:

| Component | Access |
|---|---|
| `Manager` | `getPlugin()` |
| `Module` | `getPlugin()`, `getManager()` |
| `SubModule` | `getPlugin()`, `getModule()` |

---

## Requirements

Dependency-Injector has no external runtime dependencies.

The following is only needed at compile time for annotation processing:
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.36</version>
    <scope>provided</scope>
</dependency>
```

---

## Built-in Dependencies

Hierarchy-Framework depends on the following libraries, which are included automatically through Maven:

- [Dependency Injector](https://github.com/Trae-Maven/dependency-injector) – Container management, classpath scanning, lifecycle callbacks, and component sorting.
- [Utilities](https://github.com/Trae-Maven/utilities) – Generic type resolution and string transformation utilities.

---

## Installation

Add the dependency to your Maven project:
```xml
<dependency>
    <groupId>io.github.trae</groupId>
    <artifactId>hierarchy-framework</artifactId>
    <version>0.0.1</version>
</dependency>
```

---

## Quick Start

### Defining the Plugin

Implement `Plugin` and provide platform-specific registration logic in the callbacks:
```java
@Application
public class SpigotPlugin extends JavaPlugin implements Plugin {

    @Override
    public void onEnable() {
        this.initializePlugin();
    }

    @Override
    public void onDisable() {
        this.shutdownPlugin();
    }

    @Override
    public void onComponentInitialize(final Object instance) {
        if (instance instanceof Listener listener) {
            Bukkit.getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    @Override
    public void onComponentShutdown(final Object instance) {
        if (instance instanceof Listener listener) {
            HandlerList.unregisterAll(listener);
        }
    }
}
```

### Defining a Manager

Managers are the direct children of a Plugin:
```java
@Service
public class ClientManager implements Manager<CorePlugin> {

    @PostConstruct
    public void onPostConstruct() {
        // called after all fields are injected
    }
}
```

### Defining a Module

Modules live within a Manager and have typed access to both their Manager and Plugin:
```java
@Component
public class ClientCommand implements Module<CorePlugin, ClientManager> {

    public void execute() {
        final CorePlugin plugin = this.getPlugin();
        final ClientManager manager = this.getManager();
    }
}
```

### Defining a SubModule

SubModules are the most granular components, living within a Module:
```java
@Component
public class RankSubCommand implements SubModule<CorePlugin, ClientCommand> {

    public void execute() {
        final CorePlugin plugin = this.getPlugin();
        final ClientManager manager = this.getModule().getManager();
        final ClientCommand module = this.getModule();
    }
}
```

### Initialization Order

The `HierarchyComparator` ensures components are initialized in hierarchy order, grouped by their owning Manager:
```
ClientManager              (Manager)
ClientCommand              (Module under ClientManager)
RankSubCommand             (SubModule under ClientCommand)
```

During shutdown, components are destroyed in the reverse order so that children are torn down before their parents.

### Multi-Application

Multiple plugins can share the same container. Declare upstream dependencies with `@Application(dependencies = ...)`:
```java
@Application
public class CorePlugin extends SpigotPlugin {
}

@Application(dependencies = CorePlugin.class)
public class FactionsPlugin extends SpigotPlugin {
}
```

Components in Factions can access Managers, Modules, and SubModules from Core via constructor or field injection.

---

## Interfaces

| Interface | Description |
|---|---|
| `Plugin` | Root application boundary with platform callbacks and DI bootstrapping |
| `Frame<P>` | Base interface providing plugin resolution, lifecycle hooks, and naming |
| `Manager<P>` | Top-level organizational component within a Plugin |
| `Module<P, M>` | Mid-level component within a Manager |
| `SubModule<P, M>` | Leaf-level component within a Module |
| `HierarchyComparator` | Sorts components by Manager group and hierarchy depth |
