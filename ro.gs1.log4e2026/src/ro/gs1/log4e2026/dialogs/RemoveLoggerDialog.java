package ro.gs1.log4e2026.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for confirming removal of log statements.
 * Provides options for what to remove.
 */
public class RemoveLoggerDialog extends TitleAreaDialog {

    private Button removeDeclarationCheck;
    private Button removeImportsCheck;
    private Button removeStatementsCheck;
    private Button removeCommentsCheck;

    private boolean removeDeclaration = true;
    private boolean removeImports = true;
    private boolean removeStatements = true;
    private boolean removeComments = false;

    private int logStatementCount;
    private String scopeDescription;

    public RemoveLoggerDialog(Shell parentShell, int logStatementCount, String scopeDescription) {
        super(parentShell);
        this.logStatementCount = logStatementCount;
        this.scopeDescription = scopeDescription;
        setHelpAvailable(false);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Remove Logging");
        setMessage("Configure what logging elements to remove from " + scopeDescription);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);

        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        container.setLayout(new GridLayout(1, false));

        // Summary
        Label summaryLabel = new Label(container, SWT.WRAP);
        summaryLabel.setText("Found " + logStatementCount + " log statement(s) in " + scopeDescription + ".");
        summaryLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Options group
        Group optionsGroup = new Group(container, SWT.NONE);
        optionsGroup.setText("Remove Options");
        optionsGroup.setLayout(new GridLayout(1, false));
        optionsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeStatementsCheck = new Button(optionsGroup, SWT.CHECK);
        removeStatementsCheck.setText("Remove log statements");
        removeStatementsCheck.setSelection(removeStatements);
        removeStatementsCheck.setToolTipText("Remove all log.debug(), log.info(), etc. statements");

        removeDeclarationCheck = new Button(optionsGroup, SWT.CHECK);
        removeDeclarationCheck.setText("Remove logger declaration");
        removeDeclarationCheck.setSelection(removeDeclaration);
        removeDeclarationCheck.setToolTipText("Remove the logger field declaration");

        removeImportsCheck = new Button(optionsGroup, SWT.CHECK);
        removeImportsCheck.setText("Remove logger imports");
        removeImportsCheck.setSelection(removeImports);
        removeImportsCheck.setToolTipText("Remove import statements for the logging framework");

        removeCommentsCheck = new Button(optionsGroup, SWT.CHECK);
        removeCommentsCheck.setText("Remove logger comments");
        removeCommentsCheck.setSelection(removeComments);
        removeCommentsCheck.setToolTipText("Remove comments above the logger declaration");

        return area;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "Remove", true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void okPressed() {
        removeDeclaration = removeDeclarationCheck.getSelection();
        removeImports = removeImportsCheck.getSelection();
        removeStatements = removeStatementsCheck.getSelection();
        removeComments = removeCommentsCheck.getSelection();
        super.okPressed();
    }

    public boolean isRemoveDeclaration() {
        return removeDeclaration;
    }

    public boolean isRemoveImports() {
        return removeImports;
    }

    public boolean isRemoveStatements() {
        return removeStatements;
    }

    public boolean isRemoveComments() {
        return removeComments;
    }
}
