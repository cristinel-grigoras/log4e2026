package ro.gs1.log4e2026.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Wizard for previewing log statement changes before applying them.
 * Can be used for both single-file and multi-file operations.
 */
public class LoggerPreviewWizard extends Wizard {

    private PreviewPage previewPage;
    private List<ChangePreview> changes;
    private boolean confirmed = false;

    public LoggerPreviewWizard(List<ChangePreview> changes) {
        this.changes = changes != null ? changes : new ArrayList<>();
        setWindowTitle("Log4E - Preview Changes");
        setNeedsProgressMonitor(false);
    }

    @Override
    public void addPages() {
        previewPage = new PreviewPage();
        addPage(previewPage);
    }

    @Override
    public boolean performFinish() {
        confirmed = true;
        return true;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Represents a preview of a change to be made.
     */
    public static class ChangePreview {
        private final String fileName;
        private final String changeDescription;
        private final String before;
        private final String after;

        public ChangePreview(String fileName, String changeDescription, String before, String after) {
            this.fileName = fileName;
            this.changeDescription = changeDescription;
            this.before = before;
            this.after = after;
        }

        public String getFileName() {
            return fileName;
        }

        public String getChangeDescription() {
            return changeDescription;
        }

        public String getBefore() {
            return before;
        }

        public String getAfter() {
            return after;
        }
    }

    /**
     * Wizard page showing the preview of changes.
     */
    private class PreviewPage extends WizardPage {

        protected PreviewPage() {
            super("PreviewPage");
            setTitle("Preview Changes");
            setDescription("Review the changes that will be made to your code.");
        }

        @Override
        public void createControl(Composite parent) {
            Composite container = new Composite(parent, SWT.NONE);
            container.setLayout(new GridLayout(1, false));

            if (changes.isEmpty()) {
                Label noChangesLabel = new Label(container, SWT.NONE);
                noChangesLabel.setText("No changes to preview.");
            } else {
                // Summary
                Label summaryLabel = new Label(container, SWT.NONE);
                summaryLabel.setText("The following changes will be made (" + changes.size() + " total):");
                summaryLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

                // Preview text
                Text previewText = new Text(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
                GridData gd = new GridData(GridData.FILL_BOTH);
                gd.heightHint = 300;
                gd.widthHint = 500;
                previewText.setLayoutData(gd);

                StringBuilder sb = new StringBuilder();
                for (ChangePreview change : changes) {
                    sb.append("=== ").append(change.getFileName()).append(" ===\n");
                    sb.append(change.getChangeDescription()).append("\n\n");
                    if (change.getBefore() != null && !change.getBefore().isEmpty()) {
                        sb.append("Before:\n").append(change.getBefore()).append("\n\n");
                    }
                    if (change.getAfter() != null && !change.getAfter().isEmpty()) {
                        sb.append("After:\n").append(change.getAfter()).append("\n\n");
                    }
                    sb.append("---\n\n");
                }
                previewText.setText(sb.toString());
            }

            setControl(container);
            setPageComplete(true);
        }
    }
}
