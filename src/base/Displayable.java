package base;

/**
 * Interface for entities that can describe themselves for display.
 * Demonstrates: Abstraction (interface), Polymorphism
 */
public interface Displayable {
    String   getDisplayName();
    String   getSummary();
    String[] getColumnHeaders();
}
