package ro.gs1.log4e2026.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * Wizard page that shows a side-by-side preview of old vs new content.
 */
public class PreviewWizardPage extends WizardPage {

    private ChangeElement changeElement;
    private StyledText oldText;
    private StyledText newText;
    private Font monoFont;

    public PreviewWizardPage(String pageName, ChangeElement changeElement) {
        super(pageName);
        this.changeElement = changeElement;
        setTitle("Preview Changes");
        setDescription("Review the changes that will be applied to " +
                (changeElement != null ? changeElement.getFileName() : "the file"));
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(1, false));
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Create monospace font for code display
        FontData[] fontData = Display.getCurrent().getSystemFont().getFontData();
        monoFont = new Font(Display.getCurrent(), "Monospace", fontData[0].getHeight(), SWT.NORMAL);

        // Info label
        Label infoLabel = new Label(container, SWT.NONE);
        if (changeElement != null && changeElement.hasChanges()) {
            infoLabel.setText("The following changes will be applied:");
        } else {
            infoLabel.setText("No changes detected.");
        }
        infoLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Create sash form for side-by-side view
        SashForm sashForm = new SashForm(container, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Old content group
        Group oldGroup = new Group(sashForm, SWT.NONE);
        oldGroup.setText("Before");
        oldGroup.setLayout(new GridLayout(1, false));

        oldText = new StyledText(oldGroup, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
        oldText.setLayoutData(new GridData(GridData.FILL_BOTH));
        oldText.setFont(monoFont);
        oldText.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND));

        // New content group
        Group newGroup = new Group(sashForm, SWT.NONE);
        newGroup.setText("After");
        newGroup.setLayout(new GridLayout(1, false));

        newText = new StyledText(newGroup, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
        newText.setLayoutData(new GridData(GridData.FILL_BOTH));
        newText.setFont(monoFont);
        newText.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

        // Set equal weights
        sashForm.setWeights(new int[]{50, 50});

        // Populate content
        if (changeElement != null) {
            String oldContent = changeElement.getOldContents();
            String newContent = changeElement.getNewContents();

            oldText.setText(oldContent != null ? oldContent : "");
            newText.setText(newContent != null ? newContent : "");
        }

        setControl(container);
    }

    public void setChangeElement(ChangeElement changeElement) {
        this.changeElement = changeElement;
        if (oldText != null && newText != null && changeElement != null) {
            oldText.setText(changeElement.getOldContents() != null ?
                    changeElement.getOldContents() : "");
            newText.setText(changeElement.getNewContents() != null ?
                    changeElement.getNewContents() : "");
        }
    }

    public ChangeElement getChangeElement() {
        return changeElement;
    }

    @Override
    public void dispose() {
        if (monoFont != null && !monoFont.isDisposed()) {
            monoFont.dispose();
        }
        super.dispose();
    }
}
