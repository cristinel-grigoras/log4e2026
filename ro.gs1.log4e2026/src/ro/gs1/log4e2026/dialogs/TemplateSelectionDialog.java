package ro.gs1.log4e2026.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ro.gs1.log4e2026.templates.LoggerTemplate;
import ro.gs1.log4e2026.templates.LoggerTemplates;

/**
 * Dialog for selecting a logger template.
 * Shows available templates with preview.
 */
public class TemplateSelectionDialog extends TitleAreaDialog {

    private ListViewer templateListViewer;
    private Text previewText;

    private String selectedTemplate;

    private static final String[] TEMPLATES = {
        LoggerTemplates.SLF4J,
        LoggerTemplates.LOG4J2,
        LoggerTemplates.JUL
    };

    public TemplateSelectionDialog(Shell parentShell) {
        super(parentShell);
        this.selectedTemplate = LoggerTemplates.SLF4J;
        setHelpAvailable(false);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Select Logger Template");
        setMessage("Choose a logging framework template for your project.");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);

        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        container.setLayout(new GridLayout(2, true));

        // Template list
        Group listGroup = new Group(container, SWT.NONE);
        listGroup.setText("Available Templates");
        listGroup.setLayout(new GridLayout(1, false));
        listGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

        templateListViewer = new ListViewer(listGroup, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        templateListViewer.getList().setLayoutData(new GridData(GridData.FILL_BOTH));
        templateListViewer.setContentProvider(ArrayContentProvider.getInstance());
        templateListViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                String name = (String) element;
                LoggerTemplate template = LoggerTemplates.getTemplate(name);
                return template != null ? template.getName() : name;
            }
        });
        templateListViewer.setInput(TEMPLATES);
        templateListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                if (!selection.isEmpty()) {
                    selectedTemplate = (String) selection.getFirstElement();
                    updatePreview();
                }
            }
        });

        // Preview
        Group previewGroup = new Group(container, SWT.NONE);
        previewGroup.setText("Template Preview");
        previewGroup.setLayout(new GridLayout(1, false));
        previewGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

        previewText = new Text(previewGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY);
        previewText.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Description
        Label descLabel = new Label(container, SWT.WRAP);
        descLabel.setText("Select a template to use for logger declarations in your Java files.");
        GridData descGd = new GridData(GridData.FILL_HORIZONTAL);
        descGd.horizontalSpan = 2;
        descLabel.setLayoutData(descGd);

        // Select first template
        templateListViewer.setSelection(new StructuredSelection(selectedTemplate));
        updatePreview();

        return area;
    }

    private void updatePreview() {
        LoggerTemplate template = LoggerTemplates.getTemplate(selectedTemplate);
        if (template != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Framework: ").append(template.getName()).append("\n\n");
            sb.append("Declaration:\n");
            sb.append(template.getDeclaration()).append("\n\n");
            sb.append("Imports:\n");
            for (String imp : template.getImports()) {
                sb.append("import ").append(imp).append(";\n");
            }
            previewText.setText(sb.toString());
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "Select", true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void okPressed() {
        IStructuredSelection selection = (IStructuredSelection) templateListViewer.getSelection();
        if (!selection.isEmpty()) {
            selectedTemplate = (String) selection.getFirstElement();
        }
        super.okPressed();
    }

    public String getSelectedTemplate() {
        return selectedTemplate;
    }
}
