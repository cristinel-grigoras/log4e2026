package ro.gs1.log4e2026.wizard;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * Represents a change to be previewed in the wizard.
 * Contains the old and new content for comparison.
 */
public class ChangeElement {

    private ICompilationUnit compilationUnit;
    private String oldContents;
    private String newContents;
    private String fileName;

    public ChangeElement(ICompilationUnit cu, String oldContents, String newContents) {
        this.compilationUnit = cu;
        this.oldContents = oldContents;
        this.newContents = newContents;
        this.fileName = cu != null ? cu.getElementName() : "Unknown";
    }

    public ICompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    public String getOldContents() {
        return oldContents;
    }

    public String getNewContents() {
        return newContents;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean hasChanges() {
        if (oldContents == null || newContents == null) {
            return false;
        }
        return !oldContents.equals(newContents);
    }

    @Override
    public String toString() {
        return fileName;
    }
}
