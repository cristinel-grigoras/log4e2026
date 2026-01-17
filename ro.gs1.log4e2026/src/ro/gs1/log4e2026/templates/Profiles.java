package ro.gs1.log4e2026.templates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Collection of logger profiles.
 * Manages built-in and user-defined profiles.
 */
public class Profiles implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_PROFILE = "SLF4J";

    private String version;
    private final List<Profile> profileList = new ArrayList<>();
    private boolean nameChanged = false;

    /**
     * Add all profiles from another Profiles collection.
     * @return true if any names were changed to avoid duplicates
     */
    public boolean addProfiles(Profiles otherProfiles) {
        if (otherProfiles == null) {
            return false;
        }
        boolean changed = this.isNameChanged() || otherProfiles.isNameChanged();
        Iterator<Profile> iterator = otherProfiles.getProfiles();
        while (iterator.hasNext()) {
            Profile profile = iterator.next();
            changed = this.addProfile(profile) || changed;
        }
        this.setNameChanged(changed);
        return changed;
    }

    /**
     * Add a profile to the collection.
     * If a profile with the same name exists, generates a new unique title.
     * @return true if the profile title was changed
     */
    public boolean addProfile(Profile profile) {
        boolean changed = false;
        if (this.getProfile(profile.getName()) != null) {
            if (profile.getName() == null) {
                profile.setName(Profile.generateName());
            }
            // Generate unique title if needed
            String newTitle = computeUniqueTitle(profile.getTitle());
            if (!newTitle.equals(profile.getTitle())) {
                profile.setTitle(newTitle);
                changed = true;
            }
        }
        this.profileList.add(profile);
        return changed;
    }

    /**
     * Compute a unique title by appending a number if needed.
     */
    private String computeUniqueTitle(String title) {
        if (!existsProfileWithTitle(title)) {
            return title;
        }
        int counter = 1;
        String newTitle;
        do {
            newTitle = title + " (" + counter + ")";
            counter++;
        } while (existsProfileWithTitle(newTitle));
        return newTitle;
    }

    /**
     * Get a profile by its internal name.
     */
    public Profile getProfile(String name) {
        if (name == null || this.profileList == null) {
            return null;
        }
        for (Profile profile : this.profileList) {
            if (profile.getName() != null && profile.getName().equalsIgnoreCase(name)) {
                return profile;
            }
        }
        return null;
    }

    /**
     * Get a profile by its display title.
     */
    public Profile getProfileByTitle(String title) {
        if (title == null || this.profileList == null) {
            return null;
        }
        for (Profile profile : this.profileList) {
            if (profile.getTitle() != null && profile.getTitle().equalsIgnoreCase(title)) {
                return profile;
            }
        }
        return null;
    }

    /**
     * Get the first profile in the collection.
     */
    public Profile getFirstProfile() {
        if (this.profileList == null || this.profileList.isEmpty()) {
            return null;
        }
        return this.profileList.get(0);
    }

    /**
     * Get the default profile.
     */
    public Profile getDefaultProfile() {
        return this.getProfileByTitle(DEFAULT_PROFILE);
    }

    /**
     * Check if a profile with the given name exists.
     */
    public boolean existsProfile(String name) {
        return this.getProfile(name) != null;
    }

    /**
     * Check if a profile with the given title exists.
     */
    public boolean existsProfileWithTitle(String title) {
        if (this.profileList == null) {
            return false;
        }
        for (Profile profile : this.profileList) {
            if (profile.getTitle() != null && profile.getTitle().equalsIgnoreCase(title)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove a profile from the collection.
     */
    public void remove(Profile profile) {
        this.profileList.remove(profile);
    }

    /**
     * Reset the name changed flag.
     */
    public void reset() {
        this.nameChanged = false;
    }

    /**
     * Get the number of profiles.
     */
    public int getSize() {
        return this.profileList.size();
    }

    /**
     * Sort profiles by title.
     */
    public void sort() {
        if (this.profileList == null) {
            return;
        }
        Collections.sort(this.profileList);
    }

    /**
     * Get an iterator over all profiles.
     */
    public Iterator<Profile> getProfiles() {
        return this.profileList.iterator();
    }

    /**
     * Get profiles as a list.
     */
    public List<Profile> getProfileList() {
        return new ArrayList<>(this.profileList);
    }

    /**
     * Get only user-defined (editable) profiles.
     */
    public List<Profile> getUserProfiles() {
        List<Profile> userProfiles = new ArrayList<>();
        for (Profile profile : this.profileList) {
            if (profile.isEditable()) {
                userProfiles.add(profile);
            }
        }
        return userProfiles;
    }

    /**
     * Get only built-in profiles.
     */
    public List<Profile> getBuiltInProfiles() {
        List<Profile> builtInProfiles = new ArrayList<>();
        for (Profile profile : this.profileList) {
            if (profile.isBuiltIn()) {
                builtInProfiles.add(profile);
            }
        }
        return builtInProfiles;
    }

    /**
     * Get profile titles as array (for combo boxes).
     */
    public String[] getProfileTitles() {
        String[] titles = new String[this.profileList.size()];
        for (int i = 0; i < this.profileList.size(); i++) {
            titles[i] = this.profileList.get(i).getTitle();
        }
        return titles;
    }

    // --- Getters and Setters ---

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isNameChanged() {
        return this.nameChanged;
    }

    public void setNameChanged(boolean nameChanged) {
        this.nameChanged = nameChanged;
    }

    @Override
    public String toString() {
        return "Profiles[size=" + getSize() + ", version=" + version + "]";
    }
}
