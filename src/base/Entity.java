package base;

/**
 * Abstract base class for all entities in the system.
 * Demonstrates: Abstraction, Encapsulation
 */
public abstract class Entity {
    protected String id;

    public Entity(String id) {
        this.id = id;
    }

    public String getId()        { return id; }
    public void   setId(String id) { this.id = id; }

    /** Serialize this entity to a pipe-delimited string for file storage. */
    public abstract String   toFileString();

    /** Return a row suitable for JTable display. */
    public abstract String[] toTableRow();

    /**
     * Validate all fields. Throws IllegalArgumentException with a user-friendly
     * message when a field is invalid.
     */
    public abstract void validate() throws IllegalArgumentException;

    @Override
    public String toString() { return id; }
}
