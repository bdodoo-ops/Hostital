package base;

import util.FileUtil;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generic abstract manager for all entity collections.
 * Demonstrates: Abstraction, Generics (Collections), Polymorphism
 *
 * @param <T> The entity type managed by this class
 */
public abstract class Manager<T extends Entity> {

    protected final List<T> items = new ArrayList<>();
    protected final String  fileName;

    public Manager(String fileName) {
        this.fileName = fileName;
        loadFromFile();
    }

    /** Deserialize one file line to an entity. */
    public abstract T      fromFileString(String line);

    /** ID prefix, e.g. "P" → "P0001". */
    public abstract String getIdPrefix();

    // ── CRUD ─────────────────────────────────────────────────────────────────

    public void add(T item) {
        item.validate();           // Throws if invalid
        items.add(item);
        saveToFile();
    }

    public boolean update(T updated) {
        updated.validate();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equalsIgnoreCase(updated.getId())) {
                items.set(i, updated);
                saveToFile();
                return true;
            }
        }
        return false;
    }

    public boolean remove(String id) {
        boolean removed = items.removeIf(e -> e.getId().equalsIgnoreCase(id));
        if (removed) saveToFile();
        return removed;
    }

    public T findById(String id) {
        return items.stream()
                    .filter(e -> e.getId().equalsIgnoreCase(id))
                    .findFirst().orElse(null);
    }

    public List<T> getAll() { return Collections.unmodifiableList(items); }

    /** Case-insensitive search across the whole file-string of each entity. */
    public List<T> search(String query) {
        if (query == null || query.isBlank()) return getAll();
        String q = query.toLowerCase();
        return items.stream()
                    .filter(e -> e.toFileString().toLowerCase().contains(q))
                    .collect(Collectors.toList());
    }

    public int count() { return items.size(); }

    /** Generate next sequential ID, e.g. "P0003". */
    public String nextId() {
        return getIdPrefix() + String.format("%04d", items.size() + 1);
    }

    public void refresh() { loadFromFile(); }

    // ── File I/O ──────────────────────────────────────────────────────────────

    protected void loadFromFile() {
        items.clear();
        for (String line : FileUtil.readLines(fileName)) {
            try {
                T entity = fromFileString(line);
                if (entity != null) items.add(entity);
            } catch (Exception ignored) { /* skip malformed lines */ }
        }
    }

    protected void saveToFile() {
        List<String> lines = new ArrayList<>();
        for (T e : items) lines.add(e.toFileString());
        FileUtil.writeLines(fileName, lines);
    }
}
