package ro.gs1.log4e2026.wizard;

import org.eclipse.jface.wizard.Wizard;

/**
 * Wizard that shows a preview of logging changes before applying them.
 */
public class LoggerPreviewWizard extends Wizard {

    private ChangeElement changeElement;
    private PreviewWizardPage previewPage;
    private String operationTitle;

    public LoggerPreviewWizard(ChangeElement changeElement, String operationTitle) {
        this.changeElement = changeElement;
        this.operationTitle = operationTitle;
        setWindowTitle("Log4E - " + operationTitle);
        setNeedsProgressMonitor(false);
    }

    @Override
    public void addPages() {
        previewPage = new PreviewWizardPage("preview", changeElement);
        previewPage.setTitle(operationTitle + " - Preview");
        previewPage.setDescription("Review the changes that will be applied.");
        addPage(previewPage);
    }

    @Override
    public boolean performFinish() {
        return true;
    }

    @Override
    public boolean canFinish() {
        return changeElement != null && changeElement.hasChanges();
    }

    public ChangeElement getChangeElement() {
        return changeElement;
    }
}
