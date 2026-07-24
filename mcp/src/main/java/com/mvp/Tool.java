package com.mvp;


import java.util.Objects;

public abstract class Tool {

    private final String name;
    private final String description;

    protected Tool(String name, String description) {
        this.name = Objects.requireNonNull(name, "Tool name cannot be null");
        this.description = Objects.requireNonNull(description, "Tool description cannot be null");
    }

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        return description;
    }

    /**
     * Executes the tool with the supplied input.
     *
     * @param input Tool input. Concrete implementations decide the expected type.
     * @return Tool execution result.
     */
    public abstract Object execute(Object input);
}
