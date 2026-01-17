package ro.gs1.log4e2026.wizard;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for displaying logger preview wizards.
 */
public class LoggerWizardDialog extends WizardDialog {

    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;

    public LoggerWizardDialog(Shell parentShell, IWizard wizard) {
        super(parentShell, wizard);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
    }

    @Override
    protected Point getInitialSize() {
        return new Point(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    protected void createButtonsForButtonBar(org.eclipse.swt.widgets.Composite parent) {
        super.createButtonsForButtonBar(parent);
        // Rename Finish to Apply
        getButton(IDialogConstants.FINISH_ID).setText("Apply Changes");
    }

    /**
     * Opens a preview wizard dialog and returns true if the user clicks Apply.
     *
     * @param shell the parent shell
     * @param changeElement the change to preview
     * @param operationTitle the title of the operation
     * @return true if the user wants to apply the changes, false otherwise
     */
    public static boolean openPreview(Shell shell, ChangeElement changeElement, String operationTitle) {
        if (changeElement == null || !changeElement.hasChanges()) {
            return true; // No changes, proceed directly
        }

        LoggerPreviewWizard wizard = new LoggerPreviewWizard(changeElement, operationTitle);
        LoggerWizardDialog dialog = new LoggerWizardDialog(shell, wizard);
        return dialog.open() == WizardDialog.OK;
    }
}
