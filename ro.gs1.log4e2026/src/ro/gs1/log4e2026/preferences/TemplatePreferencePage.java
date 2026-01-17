package ro.gs1.log4e2026.preferences;

import java.util.List;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.dialogs.TemplateDialog;
import ro.gs1.log4e2026.templates.Profile;
import ro.gs1.log4e2026.templates.ProfileManager;
import ro.gs1.log4e2026.templates.Profiles;

/**
 * Preference page for managing logger profiles/templates.
 * Allows selecting, editing, duplicating, renaming, and removing profiles.
 */
public class TemplatePreferencePage extends PreferencePage
        implements IWorkbenchPreferencePage, PreferenceKeys {

    private Combo profileCombo;
    private Button editButton;
    private Button duplicateButton;
    private Button renameButton;
    private Button removeButton;
    private Text previewText;

    private Profile currentProfile;
    private IPreferenceStore store;

    public TemplatePreferencePage() {
        super();
        setPreferenceStore(Log4e2026Plugin.getDefault().getPreferenceStore());
        setDescription("Logger Profile Management\n\n" +
            "Select and manage logger profiles for different logging frameworks.");
        store = getPreferenceStore();
    }

    @Override
    public void init(IWorkbench workbench) {
        initProfile();
    }

    private void initProfile() {
        ProfileManager manager = ProfileManager.getInstance();
        String profileName = store.getString(LOGGER_PROFILE);
        currentProfile = manager.getProfile(profileName);
        if (currentProfile == null) {
            currentProfile = manager.getFirstProfile();
        }
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(1, false));
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Profile selector with buttons
        createProfileSelector(container);

        // Profile management buttons
        createProfileButtons(container);

        // Preview area
        createPreview(container);

        // Load current settings
        loadProfiles();

        return container;
    }

    private void createProfileSelector(Composite parent) {
        Composite row = new Composite(parent, SWT.NONE);
        row.setLayout(new GridLayout(2, false));
        row.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label label = new Label(row, SWT.NONE);
        label.setText("Logger Profile:");

        profileCombo = new Combo(row, SWT.DROP_DOWN | SWT.READ_ONLY);
        profileCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        profileCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateProfile();
            }
        });
    }

    private void createProfileButtons(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText("Profile Management");
        group.setLayout(new GridLayout(4, true));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Edit button
        editButton = new Button(group, SWT.PUSH);
        editButton.setText("Edit...");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        editButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editProfile();
            }
        });

        // Duplicate button
        duplicateButton = new Button(group, SWT.PUSH);
        duplicateButton.setText("Duplicate...");
        duplicateButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        duplicateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                duplicateProfile();
            }
        });

        // Rename button
        renameButton = new Button(group, SWT.PUSH);
        renameButton.setText("Rename...");
        renameButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        renameButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                renameProfile();
            }
        });

        // Remove button
        removeButton = new Button(group, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        removeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                removeProfile();
            }
        });
    }

    private void createPreview(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText("Logger Declaration Preview");
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(GridData.FILL_BOTH));

        previewText = new Text(group, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.READ_ONLY | SWT.WRAP);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        previewText.setLayoutData(gd);
    }

    private void loadProfiles() {
        ProfileManager manager = ProfileManager.getInstance();
        Profiles profiles = manager.getProfiles();
        List<Profile> profileList = profiles.getProfileList();

        String[] titles = new String[profileList.size()];
        int selectedIndex = 0;

        for (int i = 0; i < profileList.size(); i++) {
            Profile p = profileList.get(i);
            titles[i] = p.getTitle() + (p.isBuiltIn() ? " (built-in)" : "");
            if (currentProfile != null && currentProfile.getName().equals(p.getName())) {
                selectedIndex = i;
            }
        }

        profileCombo.setItems(titles);
        if (profileList.size() > 0) {
            profileCombo.select(selectedIndex);
            currentProfile = profileList.get(selectedIndex);
        }

        updateButtons();
        updatePreview();
    }

    private void updateProfile() {
        int idx = profileCombo.getSelectionIndex();
        if (idx >= 0) {
            ProfileManager manager = ProfileManager.getInstance();
            Profiles profiles = manager.getProfiles();
            List<Profile> profileList = profiles.getProfileList();
            if (idx < profileList.size()) {
                currentProfile = profileList.get(idx);
                updateButtons();
                updatePreview();
            }
        }
    }

    private void updateButtons() {
        boolean isEditable = currentProfile != null && currentProfile.isEditable();

        // Edit is always available (shows read-only for built-in)
        if (editButton != null) {
            editButton.setText(isEditable ? "Edit..." : "View...");
        }

        // Duplicate is always available
        if (duplicateButton != null) {
            duplicateButton.setEnabled(currentProfile != null);
        }

        // Rename and Remove only for user profiles
        if (renameButton != null) {
            renameButton.setEnabled(isEditable);
        }
        if (removeButton != null) {
            removeButton.setEnabled(isEditable);
        }
    }

    private void updatePreview() {
        if (currentProfile == null || previewText == null) {
            return;
        }

        StringBuilder preview = new StringBuilder();
        preview.append("Profile: ").append(currentProfile.getTitle()).append("\n");
        preview.append("Type: ").append(currentProfile.isBuiltIn() ? "Built-in" : "User-defined").append("\n\n");

        String declaration = currentProfile.getString("LOGGER_DECLARATION");
        if (declaration != null) {
            preview.append("Declaration:\n");
            String previewDecl = declaration
                    .replace("${logger}", "logger")
                    .replace("${enclosing_type}", "MyClass");
            preview.append(previewDecl).append("\n\n");
        }

        String imports = currentProfile.getString("LOGGER_IMPORTS");
        if (imports != null) {
            preview.append("Imports:\n");
            preview.append(imports);
        }

        previewText.setText(preview.toString());
    }

    private void editProfile() {
        if (currentProfile == null) {
            return;
        }

        TemplateDialog dialog = new TemplateDialog(getShell(), currentProfile);
        if (dialog.open() == TemplateDialog.OK) {
            // Profile was modified
            ProfileManager.getInstance().storeProfiles();
            updatePreview();
        }
    }

    private void duplicateProfile() {
        if (currentProfile == null) {
            return;
        }

        Profiles profiles = ProfileManager.getInstance().getProfiles();

        // Generate unique name proposal
        String baseName = currentProfile.getTitle();
        String proposedName = baseName + " (copy)";
        int counter = 2;
        while (profiles.existsProfileWithTitle(proposedName)) {
            proposedName = baseName + " (copy " + counter + ")";
            counter++;
        }

        IInputValidator validator = newText -> {
            if (newText == null || newText.trim().isEmpty()) {
                return "Profile name cannot be empty";
            }
            if (profiles.existsProfileWithTitle(newText)) {
                return "A profile with this name already exists";
            }
            return null;
        };

        InputDialog dialog = new InputDialog(
                getShell(),
                "Duplicate Profile",
                "Enter a name for the new profile:",
                proposedName,
                validator);

        if (dialog.open() == InputDialog.OK) {
            String newTitle = dialog.getValue();
            Profile newProfile = ProfileManager.getInstance().duplicateProfile(currentProfile, newTitle);
            currentProfile = newProfile;
            loadProfiles();
        }
    }

    private void renameProfile() {
        if (currentProfile == null || currentProfile.isBuiltIn()) {
            return;
        }

        Profiles profiles = ProfileManager.getInstance().getProfiles();

        IInputValidator validator = newText -> {
            if (newText == null || newText.trim().isEmpty()) {
                return "Profile name cannot be empty";
            }
            if (!newText.equals(currentProfile.getTitle()) && profiles.existsProfileWithTitle(newText)) {
                return "A profile with this name already exists";
            }
            return null;
        };

        InputDialog dialog = new InputDialog(
                getShell(),
                "Rename Profile",
                "Enter a new name for the profile:",
                currentProfile.getTitle(),
                validator);

        if (dialog.open() == InputDialog.OK) {
            String newTitle = dialog.getValue();
            ProfileManager.getInstance().renameProfile(currentProfile, newTitle);
            loadProfiles();
        }
    }

    private void removeProfile() {
        if (currentProfile == null || currentProfile.isBuiltIn()) {
            return;
        }

        boolean confirmed = MessageDialog.openConfirm(
                getShell(),
                "Remove Profile",
                "Are you sure you want to remove the profile '" + currentProfile.getTitle() + "'?");

        if (confirmed) {
            ProfileManager.getInstance().removeProfile(currentProfile);
            currentProfile = ProfileManager.getInstance().getFirstProfile();
            loadProfiles();
        }
    }

    @Override
    public boolean performOk() {
        savePreferences();
        return super.performOk();
    }

    @Override
    protected void performApply() {
        savePreferences();
        super.performApply();
    }

    private void savePreferences() {
        if (currentProfile != null) {
            store.setValue(LOGGER_PROFILE, currentProfile.getName());
        }
        ProfileManager.getInstance().storeProfiles();
    }

    @Override
    protected void performDefaults() {
        currentProfile = ProfileManager.getInstance().getDefaultProfile();
        if (currentProfile == null) {
            currentProfile = ProfileManager.getInstance().getFirstProfile();
        }
        loadProfiles();
        super.performDefaults();
    }
}
