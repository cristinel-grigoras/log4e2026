package ro.gs1.log4e2026.jdt;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;

/**
 * Utility class for Java model operations.
 */
public final class JavaModelUtil {

    private JavaModelUtil() {
    }

    /**
     * Finds a field by name in a type.
     */
    public static IField findField(IType type, String fieldName) throws JavaModelException {
        if (type == null || fieldName == null) {
            return null;
        }
        for (IField field : type.getFields()) {
            if (field.getElementName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    /**
     * Checks if a field exists in a type.
     */
    public static boolean fieldExists(IType type, String fieldName) throws JavaModelException {
        return findField(type, fieldName) != null;
    }

    /**
     * Finds a method by name in a type.
     */
    public static IMethod findMethod(IType type, String methodName) throws JavaModelException {
        if (type == null || methodName == null) {
            return null;
        }
        for (IMethod method : type.getMethods()) {
            if (method.getElementName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Gets the number of methods in a compilation unit.
     */
    public static int getMethodCount(ICompilationUnit cu) throws JavaModelException {
        if (cu == null) {
            return 0;
        }
        IType primaryType = cu.findPrimaryType();
        if (primaryType == null) {
            return 0;
        }
        return primaryType.getMethods().length;
    }

    /**
     * Gets the source path of a compilation unit.
     * @param truncate if true, returns only the last part of the path
     */
    public static String getSourcePath(ICompilationUnit cu, boolean truncate) {
        if (cu == null) {
            return "";
        }
        String path = cu.getPath().toString();
        if (truncate && path.length() > 50) {
            return "..." + path.substring(path.length() - 47);
        }
        return path;
    }

    /**
     * Gets the fully qualified name of a type.
     */
    public static String getFullyQualifiedName(IType type) {
        if (type == null) {
            return null;
        }
        return type.getFullyQualifiedName();
    }

    /**
     * Gets the package name of a compilation unit.
     */
    public static String getPackageName(ICompilationUnit cu) throws JavaModelException {
        if (cu == null) {
            return "";
        }
        IPackageFragment pkg = (IPackageFragment) cu.getParent();
        if (pkg == null) {
            return "";
        }
        return pkg.getElementName();
    }

    /**
     * Checks if a compilation unit is in the default package.
     */
    public static boolean isInDefaultPackage(ICompilationUnit cu) throws JavaModelException {
        String packageName = getPackageName(cu);
        return packageName == null || packageName.isEmpty();
    }

    /**
     * Gets the simple name of a compilation unit (without .java extension).
     */
    public static String getSimpleName(ICompilationUnit cu) {
        if (cu == null) {
            return null;
        }
        String name = cu.getElementName();
        if (name.endsWith(".java")) {
            return name.substring(0, name.length() - 5);
        }
        return name;
    }

    /**
     * Finds a token position in source code.
     * @return int[] containing [startPosition, endPosition] or null if not found
     */
    public static int[] getTokenNamePosition(char[] source, int tokenName) throws InvalidInputException {
        return getTokenNamePosition(source, tokenName, 0, source.length - 1);
    }

    /**
     * Finds a token position in source code within a range.
     * @return int[] containing [startPosition, endPosition] or null if not found
     */
    public static int[] getTokenNamePosition(char[] source, int tokenName, int start, int end)
            throws InvalidInputException {
        IScanner scanner = ToolFactory.createScanner(true, true, true, true);
        scanner.setSource(source);
        scanner.resetTo(start, end);

        int token;
        while ((token = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
            if (token == tokenName) {
                return new int[] {
                    scanner.getCurrentTokenStartPosition(),
                    scanner.getCurrentTokenEndPosition()
                };
            }
        }
        return null;
    }

    /**
     * Checks if a package fragment root is a source folder.
     */
    public static boolean isSourceFolder(IPackageFragmentRoot root) throws JavaModelException {
        if (root == null) {
            return false;
        }
        return root.getKind() == IPackageFragmentRoot.K_SOURCE;
    }

    /**
     * Checks if a compilation unit is writable.
     */
    public static boolean isWritable(ICompilationUnit cu) {
        if (cu == null) {
            return false;
        }
        return !cu.isReadOnly();
    }

    /**
     * Gets the primary type of a compilation unit.
     */
    public static IType getPrimaryType(ICompilationUnit cu) throws JavaModelException {
        if (cu == null) {
            return null;
        }
        return cu.findPrimaryType();
    }

    /**
     * Checks if the compilation unit has errors.
     */
    public static boolean hasCompilationErrors(ICompilationUnit cu) {
        // This would require checking markers, simplified for now
        return false;
    }
}
