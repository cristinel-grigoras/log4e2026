package ro.gs1.log4e2026.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.templates.LoggerTemplate;
import ro.gs1.log4e2026.templates.LoggerTemplates;

/**
 * Preference page for managing logging framework profiles.
 * Allows selection between SLF4J, Log4j2, and JUL frameworks.
 */
public class ProfilePreferencesPage extends PreferencePage
        implements IWorkbenchPreferencePage, PreferenceKeys {

    private ListViewer profileListViewer;
    private Text profileNameText;
    private Text loggerTypeText;
    private Text loggerImportsText;
    private Text loggerInitializerText;
    private Button setDefaultButton;

    private IPreferenceStore store;
    private String selectedProfile;

    public ProfilePreferencesPage() {
        super();
        setPreferenceStore(Log4e2026Plugin.getDefault().getPreferenceStore());
        setDescription("Logging Framework Profiles\n\n" +
            "Select and configure the logging framework to use.");
        store = getPreferenceStore();
    }

    @Override
    public void init(IWorkbench workbench) {
        // Nothing to initialize
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(2, false));
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Left side - Profile list
        createProfileList(container);

        // Right side - Profile details
        createProfileDetails(container);

        // Load current selection
        loadPreferences();

        return container;
    }

    private void createProfileList(Composite parent) {
        Group listGroup = new Group(parent, SWT.NONE);
        listGroup.setText("Available Profiles");
        listGroup.setLayout(new GridLayout(1, false));
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 150;
        listGroup.setLayoutData(gd);

        profileListViewer = new ListViewer(listGroup, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        profileListViewer.getList().setLayoutData(new GridData(GridData.FILL_BOTH));
        profileListViewer.setContentProvider(ArrayContentProvider.getInstance());
        profileListViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return element.toString();
            }
        });

        // Add available profiles
        profileListViewer.setInput(new String[] {
            LoggerTemplates.SLF4J,
            LoggerTemplates.LOG4J2,
            LoggerTemplates.JUL
        });

        profileListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                if (!selection.isEmpty()) {
                    selectedProfile = (String) selection.getFirstElement();
                    updateProfileDetails();
                }
            }
        });

        // Set Default button
        setDefaultButton = new Button(listGroup, SWT.PUSH);
        setDefaultButton.setText("Set as Default");
        setDefaultButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        setDefaultButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (selectedProfile != null) {
                    store.setValue(LOGGER_DEFAULT, selectedProfile);
                    store.setValue(LOGGER_PROFILE, selectedProfile);
                    updateProfileDetails();
                }
            }
        });
    }

    private void createProfileDetails(Composite parent) {
        Group detailsGroup = new Group(parent, SWT.NONE);
        detailsGroup.setText("Profile Details");
        detailsGroup.setLayout(new GridLayout(2, false));
        detailsGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Profile name (read-only)
        Label nameLabel = new Label(detailsGroup, SWT.NONE);
        nameLabel.setText("Profile Name:");
        profileNameText = new Text(detailsGroup, SWT.BORDER | SWT.READ_ONLY);
        profileNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Logger type
        Label typeLabel = new Label(detailsGroup, SWT.NONE);
        typeLabel.setText("Logger Type:");
        loggerTypeText = new Text(detailsGroup, SWT.BORDER | SWT.READ_ONLY);
        loggerTypeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Logger imports
        Label importsLabel = new Label(detailsGroup, SWT.NONE);
        importsLabel.setText("Imports:");
        GridData labelGd = new GridData(SWT.LEFT, SWT.TOP, false, false);
        importsLabel.setLayoutData(labelGd);

        loggerImportsText = new Text(detailsGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.READ_ONLY);
        GridData importsGd = new GridData(GridData.FILL_HORIZONTAL);
        importsGd.heightHint = 60;
        loggerImportsText.setLayoutData(importsGd);

        // Logger initializer
        Label initLabel = new Label(detailsGroup, SWT.NONE);
        initLabel.setText("Declaration:");
        GridData initLabelGd = new GridData(SWT.LEFT, SWT.TOP, false, false);
        initLabel.setLayoutData(initLabelGd);

        loggerInitializerText = new Text(detailsGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY);
        GridData initGd = new GridData(GridData.FILL_BOTH);
        initGd.heightHint = 100;
        loggerInitializerText.setLayoutData(initGd);

        // Default indicator
        Label defaultLabel = new Label(detailsGroup, SWT.NONE);
        defaultLabel.setText("Status:");
        GridData defaultLabelGd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        defaultLabelGd.horizontalSpan = 2;
        defaultLabel.setLayoutData(defaultLabelGd);
    }

    private void updateProfileDetails() {
        if (selectedProfile == null) {
            clearProfileDetails();
            return;
        }

        LoggerTemplate template = LoggerTemplates.getTemplate(selectedProfile);
        if (template == null) {
            clearProfileDetails();
            return;
        }

        profileNameText.setText(template.getName());
        loggerTypeText.setText(template.getLoggerType());

        StringBuilder imports = new StringBuilder();
        for (String imp : template.getImports()) {
            if (imports.length() > 0) {
                imports.append("\n");
            }
            imports.append(imp);
        }
        loggerImportsText.setText(imports.toString());

        loggerInitializerText.setText(template.getDeclaration());

        // Update default button state
        String currentDefault = store.getString(LOGGER_DEFAULT);
        setDefaultButton.setEnabled(!selectedProfile.equals(currentDefault));
    }

    private void clearProfileDetails() {
        profileNameText.setText("");
        loggerTypeText.setText("");
        loggerImportsText.setText("");
        loggerInitializerText.setText("");
    }

    private void loadPreferences() {
        String currentProfile = store.getString(LOGGER_PROFILE);
        if (currentProfile == null || currentProfile.isEmpty()) {
            currentProfile = LoggerTemplates.SLF4J;
        }

        selectedProfile = currentProfile;
        profileListViewer.setSelection(new StructuredSelection(currentProfile), true);
        updateProfileDetails();
    }

    @Override
    public boolean performOk() {
        if (selectedProfile != null) {
            store.setValue(LOGGER_PROFILE, selectedProfile);
        }
        return super.performOk();
    }

    @Override
    protected void performApply() {
        if (selectedProfile != null) {
            store.setValue(LOGGER_PROFILE, selectedProfile);
        }
        super.performApply();
    }

    @Override
    protected void performDefaults() {
        selectedProfile = store.getDefaultString(LOGGER_PROFILE);
        if (selectedProfile == null || selectedProfile.isEmpty()) {
            selectedProfile = LoggerTemplates.SLF4J;
        }
        profileListViewer.setSelection(new StructuredSelection(selectedProfile), true);
        updateProfileDetails();
        super.performDefaults();
    }
}
