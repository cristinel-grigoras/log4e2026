package ro.gs1.log4e2026.jdt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * Utility class for AST manipulation operations.
 */
public final class ASTUtil {

    private ASTUtil() {
    }

    /**
     * Parses a compilation unit.
     */
    public static CompilationUnit parseCompilationUnit(ICompilationUnit unit) {
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setSource(unit);
        parser.setResolveBindings(true);
        return (CompilationUnit) parser.createAST(null);
    }

    /**
     * Parses a compilation unit from source characters.
     */
    public static CompilationUnit parseCompilationUnit(char[] source) {
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setSource(source);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        return (CompilationUnit) parser.createAST(null);
    }

    /**
     * Gets the parent TypeDeclaration of a node.
     */
    public static TypeDeclaration getParentTypeDeclaration(ASTNode node) {
        return (TypeDeclaration) getParent(node, ASTNode.TYPE_DECLARATION);
    }

    /**
     * Gets the parent PackageDeclaration of a node.
     */
    public static PackageDeclaration getParentPackageDeclaration(ASTNode node) {
        return (PackageDeclaration) getParent(node, ASTNode.PACKAGE_DECLARATION);
    }

    /**
     * Gets the parent CatchClause of a node.
     */
    public static CatchClause getParentCatchClause(ASTNode node) {
        return (CatchClause) getParent(node, ASTNode.CATCH_CLAUSE);
    }

    /**
     * Gets the parent IfStatement of a node.
     */
    public static IfStatement getParentIfStatement(ASTNode node) {
        return (IfStatement) getParent(node, ASTNode.IF_STATEMENT);
    }

    /**
     * Gets the current block containing the node.
     */
    public static Block getCurrentBlock(ASTNode node) {
        if (node instanceof Block) {
            return (Block) node;
        }
        return getParentBlock(node);
    }

    /**
     * Gets the parent Block of a node.
     */
    public static Block getParentBlock(ASTNode node) {
        return (Block) getParent(node, ASTNode.BLOCK);
    }

    /**
     * Gets the parent MethodDeclaration of a node.
     */
    public static MethodDeclaration getParentMethodDeclaration(ASTNode node) {
        return (MethodDeclaration) getParent(node, ASTNode.METHOD_DECLARATION);
    }

    /**
     * Gets the parent VariableDeclarationStatement of a node.
     */
    public static VariableDeclarationStatement getParentVariableDeclarationStatement(ASTNode node) {
        return (VariableDeclarationStatement) getParent(node, ASTNode.VARIABLE_DECLARATION_STATEMENT);
    }

    /**
     * Gets the parent node of a specific type.
     */
    public static ASTNode getParent(ASTNode node, int nodeType) {
        if (node == null) {
            return null;
        }
        ASTNode parent = node.getParent();
        while (parent != null) {
            if (parent.getNodeType() == nodeType) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /**
     * Checks if a catch block is empty.
     */
    public static boolean isEmptyCatchBlock(CatchClause catchClause) {
        if (catchClause == null) {
            return false;
        }
        Block body = catchClause.getBody();
        return body == null || body.statements().isEmpty();
    }

    /**
     * Checks if a return statement is empty (void return).
     */
    public static boolean isEmptyReturnStatement(ReturnStatement returnStatement) {
        if (returnStatement == null) {
            return false;
        }
        return returnStatement.getExpression() == null;
    }

    /**
     * Gets the last statement in a method.
     */
    public static Statement getLastStatement(MethodDeclaration methodDeclaration) {
        if (methodDeclaration == null || methodDeclaration.getBody() == null) {
            return null;
        }
        List<?> statements = methodDeclaration.getBody().statements();
        if (statements.isEmpty()) {
            return null;
        }
        return (Statement) statements.get(statements.size() - 1);
    }

    /**
     * Checks if a block contains a return statement.
     */
    public static boolean isReturnStatementInBlock(Block block) {
        if (block == null) {
            return false;
        }
        for (Object stmt : block.statements()) {
            if (stmt instanceof ReturnStatement) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a type is void.
     */
    public static boolean isVoidType(Type type) {
        if (type == null) {
            return true;
        }
        return "void".equals(getName(type));
    }

    /**
     * Checks if a type declaration is an inner class.
     */
    public static boolean isInnerClass(TypeDeclaration typeDeclaration) {
        if (typeDeclaration == null) {
            return false;
        }
        ASTNode parent = typeDeclaration.getParent();
        return parent instanceof TypeDeclaration;
    }

    /**
     * Gets the name of a Type.
     */
    public static String getName(Type type) {
        if (type == null) {
            return null;
        }
        if (type instanceof SimpleType) {
            return getName(((SimpleType) type).getName());
        }
        if (type instanceof ParameterizedType) {
            return getName(((ParameterizedType) type).getType());
        }
        return type.toString();
    }

    /**
     * Gets the name from a Name node.
     */
    public static String getName(Name name) {
        if (name == null) {
            return null;
        }
        if (name instanceof SimpleName) {
            return ((SimpleName) name).getIdentifier();
        }
        if (name instanceof QualifiedName) {
            return ((QualifiedName) name).getName().getIdentifier();
        }
        return name.toString();
    }

    /**
     * Gets the name of a TypeDeclaration.
     */
    public static String getName(TypeDeclaration typeDeclaration) {
        if (typeDeclaration == null) {
            return null;
        }
        return typeDeclaration.getName().getIdentifier();
    }

    /**
     * Gets the name of a MethodDeclaration.
     */
    public static String getName(MethodDeclaration methodDeclaration) {
        if (methodDeclaration == null) {
            return null;
        }
        return methodDeclaration.getName().getIdentifier();
    }

    /**
     * Gets the name of a MethodInvocation.
     */
    public static String getName(MethodInvocation methodInvocation) {
        if (methodInvocation == null) {
            return null;
        }
        return methodInvocation.getName().getIdentifier();
    }

    /**
     * Gets the name of a SingleVariableDeclaration.
     */
    public static String getName(SingleVariableDeclaration singleVariableDeclaration) {
        if (singleVariableDeclaration == null) {
            return null;
        }
        return singleVariableDeclaration.getName().getIdentifier();
    }

    /**
     * Gets the name of a VariableDeclarationFragment.
     */
    public static String getName(VariableDeclarationFragment fragment) {
        if (fragment == null) {
            return null;
        }
        return fragment.getName().getIdentifier();
    }

    /**
     * Gets the type of a SingleVariableDeclaration as a string.
     */
    public static String getType(SingleVariableDeclaration svd) {
        if (svd == null) {
            return null;
        }
        Type type = svd.getType();
        StringBuilder sb = new StringBuilder(getName(type));
        for (int i = 0; i < svd.getExtraDimensions(); i++) {
            sb.append("[]");
        }
        if (svd.isVarargs()) {
            sb.append("...");
        }
        return sb.toString();
    }

    /**
     * Gets the name of a ClassInstanceCreation.
     */
    public static String getName(ClassInstanceCreation cic) {
        if (cic == null) {
            return null;
        }
        return getName(cic.getType());
    }

    /**
     * Gets the name of a PackageDeclaration.
     */
    public static String getName(PackageDeclaration packageDeclaration) {
        if (packageDeclaration == null) {
            return null;
        }
        return packageDeclaration.getName().getFullyQualifiedName();
    }

    /**
     * Gets the name of an ImportDeclaration.
     */
    public static String getName(ImportDeclaration importDeclaration) {
        if (importDeclaration == null) {
            return null;
        }
        return importDeclaration.getName().getFullyQualifiedName();
    }

    /**
     * Gets the method invocation from a statement.
     */
    public static MethodInvocation getMethodInvocationFrom(Statement statement) {
        if (statement == null) {
            return null;
        }
        if (statement instanceof org.eclipse.jdt.core.dom.ExpressionStatement) {
            Expression expr = ((org.eclipse.jdt.core.dom.ExpressionStatement) statement).getExpression();
            if (expr instanceof MethodInvocation) {
                return (MethodInvocation) expr;
            }
        }
        return null;
    }

    /**
     * Checks if a method is a getter.
     */
    public static boolean isGetter(MethodDeclaration methodDeclaration) {
        if (methodDeclaration == null) {
            return false;
        }
        String name = getName(methodDeclaration);
        if (name == null) {
            return false;
        }
        boolean startsWithGet = name.startsWith("get") && name.length() > 3;
        boolean startsWithIs = name.startsWith("is") && name.length() > 2;
        if (!startsWithGet && !startsWithIs) {
            return false;
        }
        // Check no parameters
        if (!methodDeclaration.parameters().isEmpty()) {
            return false;
        }
        // Check not void
        Type returnType = methodDeclaration.getReturnType2();
        return returnType != null && !isVoidType(returnType);
    }

    /**
     * Checks if a method is a setter.
     */
    public static boolean isSetter(MethodDeclaration methodDeclaration) {
        if (methodDeclaration == null) {
            return false;
        }
        String name = getName(methodDeclaration);
        if (name == null || !name.startsWith("set") || name.length() <= 3) {
            return false;
        }
        // Check exactly one parameter
        if (methodDeclaration.parameters().size() != 1) {
            return false;
        }
        // Check void return
        Type returnType = methodDeclaration.getReturnType2();
        return returnType == null || isVoidType(returnType);
    }

    /**
     * Checks if a method is toString().
     */
    public static boolean isToStringMethod(MethodDeclaration methodDeclaration) {
        if (methodDeclaration == null) {
            return false;
        }
        return "toString".equals(getName(methodDeclaration))
                && methodDeclaration.parameters().isEmpty();
    }

    /**
     * Checks if a method is hashCode().
     */
    public static boolean isHashCodeMethod(MethodDeclaration methodDeclaration) {
        if (methodDeclaration == null) {
            return false;
        }
        return "hashCode".equals(getName(methodDeclaration))
                && methodDeclaration.parameters().isEmpty();
    }

    /**
     * Checks if a method is equals().
     */
    public static boolean isEqualsMethod(MethodDeclaration methodDeclaration) {
        if (methodDeclaration == null) {
            return false;
        }
        return "equals".equals(getName(methodDeclaration))
                && methodDeclaration.parameters().size() == 1;
    }

    /**
     * Checks if an import declaration exists in the compilation unit.
     */
    public static boolean isImportDeclarationInCompilationUnit(CompilationUnit cu, String importName) {
        if (cu == null || importName == null) {
            return false;
        }
        for (Object imp : cu.imports()) {
            ImportDeclaration importDecl = (ImportDeclaration) imp;
            if (importName.equals(getName(importDecl))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a variable declaration exists in a type.
     */
    public static VariableDeclarationFragment findVariableInType(String variableName, TypeDeclaration typeDeclaration) {
        if (typeDeclaration == null || variableName == null) {
            return null;
        }
        for (FieldDeclaration field : typeDeclaration.getFields()) {
            for (Object fragment : field.fragments()) {
                VariableDeclarationFragment vdf = (VariableDeclarationFragment) fragment;
                if (variableName.equals(getName(vdf))) {
                    return vdf;
                }
            }
        }
        return null;
    }

    /**
     * Identifies method parameters as type-name pairs.
     * @return array of [types[], names[]]
     */
    @SuppressWarnings("unchecked")
    public static String[][] identifyParameters(List<?> methodParameterList) {
        if (methodParameterList == null || methodParameterList.isEmpty()) {
            return new String[][] { new String[0], new String[0] };
        }
        String[] types = new String[methodParameterList.size()];
        String[] names = new String[methodParameterList.size()];
        int i = 0;
        for (Object param : methodParameterList) {
            SingleVariableDeclaration svd = (SingleVariableDeclaration) param;
            types[i] = getType(svd);
            names[i] = getName(svd);
            i++;
        }
        return new String[][] { types, names };
    }

    /**
     * Finds all methods in a compilation unit.
     */
    public static List<MethodDeclaration> findAllMethods(CompilationUnit cu) {
        List<MethodDeclaration> methods = new ArrayList<>();
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration node) {
                methods.add(node);
                return true;
            }
        });
        return methods;
    }

    /**
     * Finds the method containing a specific offset position.
     */
    public static MethodDeclaration findMethodAtOffset(CompilationUnit cu, int offset) {
        final MethodDeclaration[] result = new MethodDeclaration[1];
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration node) {
                int start = node.getStartPosition();
                int end = start + node.getLength();
                if (offset >= start && offset < end) {
                    result[0] = node;
                }
                return true;
            }
        });
        return result[0];
    }

    /**
     * Finds all catch clauses in a method.
     */
    public static List<CatchClause> findCatchClauses(MethodDeclaration method) {
        List<CatchClause> catches = new ArrayList<>();
        if (method == null || method.getBody() == null) {
            return catches;
        }
        method.accept(new ASTVisitor() {
            @Override
            public boolean visit(CatchClause node) {
                catches.add(node);
                return true;
            }
        });
        return catches;
    }

    /**
     * Gets the exception variable name from a catch clause.
     */
    public static String getExceptionName(CatchClause catchClause) {
        if (catchClause == null) {
            return null;
        }
        SingleVariableDeclaration exception = catchClause.getException();
        return getName(exception);
    }

    /**
     * Gets the exception type name from a catch clause.
     */
    public static String getExceptionType(CatchClause catchClause) {
        if (catchClause == null) {
            return null;
        }
        SingleVariableDeclaration exception = catchClause.getException();
        return getType(exception);
    }

    /**
     * Finds the method declaration containing the selection.
     * The selection must be inside the method body.
     *
     * @param cu the compilation unit
     * @param selectionOffset the selection start offset
     * @param selectionLength the selection length
     * @return the method declaration or null if not inside a method body
     */
    public static MethodDeclaration findSelectedMethod(CompilationUnit cu, int selectionOffset, int selectionLength) {
        final MethodDeclaration[] result = new MethodDeclaration[1];
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration methodDeclaration) {
                if (methodDeclaration == null) {
                    return true;
                }
                Block body = methodDeclaration.getBody();
                if (body != null) {
                    int start = methodDeclaration.getStartPosition();
                    int end = body.getStartPosition() + body.getLength();
                    if (start <= selectionOffset && selectionOffset + selectionLength <= end) {
                        result[0] = methodDeclaration;
                    }
                }
                return true;
            }
        });
        return result[0];
    }

    /**
     * Checks if the cursor is at a valid position for inserting a log statement.
     * The position must be inside a block and not in the middle of a statement.
     *
     * @param cu the compilation unit
     * @param selectionOffset the selection start offset
     * @param selectionLength the selection length
     * @return true if position is valid for insertion
     */
    public static boolean isValidInsertPosition(CompilationUnit cu, int selectionOffset, int selectionLength) {
        int selectedPosition = selectionOffset + selectionLength;

        // Find the innermost block containing the position
        Block mostInnerBlock = findMostInnerBlock(cu, selectedPosition);
        if (mostInnerBlock == null) {
            return false;
        }

        // Check if we're not in the middle of a statement
        for (Object obj : mostInnerBlock.statements()) {
            Statement statement = (Statement) obj;
            int start = statement.getStartPosition();
            int end = start + statement.getLength();
            // If position is strictly inside a statement (not at boundaries), it's invalid
            if (selectedPosition > start && selectedPosition < end) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds the innermost block containing the given position.
     */
    public static Block findMostInnerBlock(CompilationUnit cu, int position) {
        final Block[] result = new Block[1];
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(Block block) {
                int start = block.getStartPosition();
                int end = start + block.getLength();
                if (start <= position && position < end) {
                    result[0] = block;
                    return true; // Continue to find inner blocks
                }
                return false; // No need to visit children outside position
            }
        });
        return result[0];
    }

    /**
     * Checks if cursor is inside a method body (for menu enablement).
     */
    public static boolean isCursorInMethod(CompilationUnit cu, int offset) {
        return findSelectedMethod(cu, offset, 0) != null;
    }

    /**
     * Checks if cursor is on or near a variable (for Log Variable menu enablement).
     */
    public static boolean isCursorOnVariable(CompilationUnit cu, int offset) {
        ASTNode node = findNodeAtOffset(cu, offset);
        if (node == null) {
            return false;
        }
        // Check if it's a simple name (variable reference)
        if (node instanceof SimpleName) {
            return true;
        }
        // Check parent for variable declaration
        ASTNode parent = node.getParent();
        return parent instanceof VariableDeclarationFragment
            || parent instanceof SingleVariableDeclaration;
    }

    /**
     * Finds the AST node at a specific offset.
     */
    public static ASTNode findNodeAtOffset(CompilationUnit cu, int offset) {
        final ASTNode[] result = new ASTNode[1];
        cu.accept(new ASTVisitor() {
            @Override
            public void preVisit(ASTNode node) {
                int start = node.getStartPosition();
                int end = start + node.getLength();
                if (offset >= start && offset <= end) {
                    // Keep the smallest/most specific node
                    if (result[0] == null || node.getLength() < result[0].getLength()) {
                        result[0] = node;
                    }
                }
            }
        });
        return result[0];
    }
}
