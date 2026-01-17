package ro.gs1.log4e2026.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ro.gs1.log4e2026.templates.LoggerTemplate;
import ro.gs1.log4e2026.templates.LoggerTemplates;

/**
 * Dialog for exchanging the logging framework.
 * Allows selection of source and target frameworks.
 */
public class ExchangeFrameworkDialog extends TitleAreaDialog {

    private Combo sourceFrameworkCombo;
    private Combo targetFrameworkCombo;
    private Text previewText;

    private String sourceFramework;
    private String targetFramework;

    private static final String[] FRAMEWORKS = {
        LoggerTemplates.SLF4J,
        LoggerTemplates.LOG4J2,
        LoggerTemplates.JUL
    };

    public ExchangeFrameworkDialog(Shell parentShell, String detectedFramework) {
        super(parentShell);
        this.sourceFramework = detectedFramework;
        this.targetFramework = LoggerTemplates.SLF4J;
        setHelpAvailable(false);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Exchange Logging Framework");
        setMessage("Select the target logging framework to convert to.");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);

        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        container.setLayout(new GridLayout(2, false));

        // Source framework
        Label sourceLabel = new Label(container, SWT.NONE);
        sourceLabel.setText("Current Framework:");

        sourceFrameworkCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        sourceFrameworkCombo.setItems(FRAMEWORKS);
        sourceFrameworkCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        selectFramework(sourceFrameworkCombo, sourceFramework);
        sourceFrameworkCombo.setEnabled(false); // Read-only, shows detected framework

        // Target framework
        Label targetLabel = new Label(container, SWT.NONE);
        targetLabel.setText("Target Framework:");

        targetFrameworkCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        targetFrameworkCombo.setItems(FRAMEWORKS);
        targetFrameworkCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        selectFramework(targetFrameworkCombo, targetFramework);
        targetFrameworkCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updatePreview();
            }
        });

        // Preview group
        Group previewGroup = new Group(container, SWT.NONE);
        previewGroup.setText("Preview");
        previewGroup.setLayout(new GridLayout(1, false));
        GridData previewGd = new GridData(GridData.FILL_BOTH);
        previewGd.horizontalSpan = 2;
        previewGroup.setLayoutData(previewGd);

        previewText = new Text(previewGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.READ_ONLY);
        GridData textGd = new GridData(GridData.FILL_BOTH);
        textGd.heightHint = 150;
        previewText.setLayoutData(textGd);

        updatePreview();

        return area;
    }

    private void selectFramework(Combo combo, String framework) {
        for (int i = 0; i < FRAMEWORKS.length; i++) {
            if (FRAMEWORKS[i].equals(framework)) {
                combo.select(i);
                return;
            }
        }
        combo.select(0);
    }

    private void updatePreview() {
        int idx = targetFrameworkCombo.getSelectionIndex();
        if (idx >= 0 && idx < FRAMEWORKS.length) {
            targetFramework = FRAMEWORKS[idx];
            LoggerTemplate template = LoggerTemplates.getTemplate(targetFramework);
            if (template != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("Logger Declaration:\n");
                sb.append(template.getDeclaration()).append("\n\n");
                sb.append("Imports:\n");
                for (String imp : template.getImports()) {
                    sb.append("import ").append(imp).append(";\n");
                }
                previewText.setText(sb.toString());
            }
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "Exchange", true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void okPressed() {
        int idx = targetFrameworkCombo.getSelectionIndex();
        if (idx >= 0 && idx < FRAMEWORKS.length) {
            targetFramework = FRAMEWORKS[idx];
        }
        super.okPressed();
    }

    public String getSourceFramework() {
        return sourceFramework;
    }

    public String getTargetFramework() {
        return targetFramework;
    }
}
