package ro.gs1.log4e2026.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ro.gs1.log4e2026.core.LogLevel;
import ro.gs1.log4e2026.preferences.PreferenceKeys;

/**
 * Wizard for adding a log statement at a specific position.
 * Allows the user to configure the log level and message.
 */
public class AddPositionWizard extends Wizard {

    private AddPositionPage positionPage;
    private LogLevel selectedLevel = LogLevel.DEBUG;
    private String message = "";

    public AddPositionWizard() {
        setWindowTitle("Add Log Statement");
        setNeedsProgressMonitor(false);
    }

    @Override
    public void addPages() {
        positionPage = new AddPositionPage();
        addPage(positionPage);
    }

    @Override
    public boolean performFinish() {
        selectedLevel = positionPage.getSelectedLevel();
        message = positionPage.getMessage();
        return true;
    }

    public LogLevel getSelectedLevel() {
        return selectedLevel;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Wizard page for configuring the log statement position.
     */
    private class AddPositionPage extends WizardPage implements PreferenceKeys {

        private Combo levelCombo;
        private Text messageText;

        private static final String[] LEVEL_NAMES = {
            "TRACE", "DEBUG", "INFO", "WARN", "ERROR"
        };

        protected AddPositionPage() {
            super("AddPositionPage");
            setTitle("Add Log Statement");
            setDescription("Configure the log level and message for the new log statement.");
        }

        @Override
        public void createControl(Composite parent) {
            Composite container = new Composite(parent, SWT.NONE);
            container.setLayout(new GridLayout(2, false));

            // Log level
            Label levelLabel = new Label(container, SWT.NONE);
            levelLabel.setText("Log Level:");

            levelCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
            levelCombo.setItems(LEVEL_NAMES);
            levelCombo.select(1); // Default to DEBUG
            levelCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            // Message
            Label msgLabel = new Label(container, SWT.NONE);
            msgLabel.setText("Message:");

            messageText = new Text(container, SWT.BORDER);
            messageText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            messageText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    validatePage();
                }
            });

            setControl(container);
            setPageComplete(true);
        }

        private void validatePage() {
            setPageComplete(true);
        }

        public LogLevel getSelectedLevel() {
            int idx = levelCombo.getSelectionIndex();
            return switch (idx) {
                case 0 -> LogLevel.TRACE;
                case 1 -> LogLevel.DEBUG;
                case 2 -> LogLevel.INFO;
                case 3 -> LogLevel.WARN;
                case 4 -> LogLevel.ERROR;
                default -> LogLevel.DEBUG;
            };
        }

        public String getMessage() {
            return messageText.getText();
        }
    }
}
