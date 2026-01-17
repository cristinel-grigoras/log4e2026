package ro.gs1.log4e2026.templates;

import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a logger profile with all settings for a logging framework.
 * Profiles can be built-in (read-only) or user-defined (editable).
 */
public class Profile implements Comparable<Profile>, Serializable {
    private static final long serialVersionUID = 1L;

    public static final String ID_BUILTIN = "BuiltIn";
    public static final String ID_USER = "User";

    private static final Collator COLLATOR = Collator.getInstance();

    private String id;           // "BuiltIn" or "User"
    private String name;         // Unique internal name (UUID for user profiles)
    private String title;        // Display title
    private int version = 1;
    private final Map<String, Object> settings = new HashMap<>();

    /**
     * Create a new empty profile.
     */
    public Profile() {
        this.name = generateName();
        this.id = ID_USER;
    }

    /**
     * Create a profile with the given ID and title.
     */
    public Profile(String id, String title) {
        this.id = id;
        this.name = generateName();
        this.title = title;
    }

    /**
     * Generate a unique profile name.
     */
    public static String generateName() {
        return UUID.randomUUID().toString();
    }

    // --- Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        if (title == null) {
            return name;
        }
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public boolean isBuiltIn() {
        return ID_BUILTIN.equals(id);
    }

    public boolean isEditable() {
        return !isBuiltIn();
    }

    // --- Settings Access ---

    public void put(String key, Object value) {
        settings.put(key, value);
    }

    public Object get(String key) {
        return settings.get(key);
    }

    public String getString(String key) {
        Object value = settings.get(key);
        return value != null ? value.toString() : null;
    }

    public String getString(String key, String defaultValue) {
        String value = getString(key);
        return value != null ? value : defaultValue;
    }

    public boolean getBoolean(String key) {
        Object value = settings.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return false;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = settings.get(key);
        if (value == null) {
            return defaultValue;
        }
        return getBoolean(key);
    }

    @SuppressWarnings("unchecked")
    public List<String> getList(String key) {
        Object value = settings.get(key);
        if (value instanceof List) {
            return (List<String>) value;
        }
        return new ArrayList<>();
    }

    public String[] getStringArray(String key) {
        List<String> list = getList(key);
        return list.toArray(new String[0]);
    }

    public void addToList(String key, String value) {
        List<String> list = getList(key);
        if (list.isEmpty()) {
            list = new ArrayList<>();
            settings.put(key, list);
        }
        list.add(value);
    }

    // --- Clone ---

    /**
     * Create a clone of this profile with a new ID and title.
     */
    public Profile getClone(String newId, String newTitle) {
        Profile clone = new Profile();
        clone.id = newId;
        clone.title = newTitle;
        clone.name = generateName();
        clone.version = this.version;
        clone.settings.putAll(this.settings);
        return clone;
    }

    // --- Comparable ---

    @Override
    public int compareTo(Profile other) {
        if (other == null) {
            return 1;
        }
        String thisTitle = getTitle();
        String otherTitle = other.getTitle();
        if (thisTitle == null) {
            return otherTitle == null ? 0 : -1;
        }
        if (otherTitle == null) {
            return 1;
        }
        return COLLATOR.compare(thisTitle, otherTitle);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Profile other = (Profile) obj;
        return name != null && name.equalsIgnoreCase(other.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.toLowerCase().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Profile[" + title + " (" + id + ")]";
    }
}
