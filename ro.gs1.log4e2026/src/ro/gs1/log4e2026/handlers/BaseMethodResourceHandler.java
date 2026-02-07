package ro.gs1.log4e2026.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.operations.LoggingOperation;
import ro.gs1.log4e2026.operations.OperationContext;

/**
 * Abstract base class for method-level operations invoked from Package Explorer
 * when a method node is selected in an expanded .java file tree.
 */
public abstract class BaseMethodResourceHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);

        if (!(selection instanceof IStructuredSelection)) {
            return null;
        }

        IStructuredSelection structuredSelection = (IStructuredSelection) selection;
        Object element = structuredSelection.getFirstElement();

        if (!(element instanceof IMethod)) {
            return null;
        }

        IMethod iMethod = (IMethod) element;
        ICompilationUnit cu = iMethod.getCompilationUnit();
        if (cu == null) {
            return null;
        }

        try {
            // Get source and create document
            String source = cu.getSource();
            Document document = new Document(source);

            // Parse AST
            ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
            parser.setSource(cu);
            parser.setResolveBindings(true);
            CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);

            // Find the MethodDeclaration using IMethod's source range
            ISourceRange sourceRange = iMethod.getSourceRange();
            ASTNode node = NodeFinder.perform(astRoot, sourceRange.getOffset(), sourceRange.getLength());
            MethodDeclaration methodDecl = findMethodDeclaration(node);

            if (methodDecl == null || methodDecl.getBody() == null) {
                Log4e2026Plugin.logWarning("Could not find method declaration for: " + iMethod.getElementName());
                return null;
            }

            // Build OperationContext
            OperationContext context = new OperationContext();
            context.setCompilationUnit(cu);
            context.setAstRoot(astRoot);
            context.setDocument(document);
            context.setSelectedMethod(methodDecl);
            context.setSelectionOffset(sourceRange.getOffset());
            context.setSelectionLength(sourceRange.getLength());

            // Delegate to subclass
            processMethod(context);

            // Save changes back to the compilation unit
            cu.getBuffer().setContents(document.get());
            cu.save(null, true);

        } catch (Exception e) {
            Log4e2026Plugin.logError("Failed to execute method resource operation", e);
            throw new ExecutionException("Failed to execute method resource operation", e);
        }

        return null;
    }

    /**
     * Finds the MethodDeclaration AST node from the given node.
     */
    private MethodDeclaration findMethodDeclaration(ASTNode node) {
        while (node != null) {
            if (node instanceof MethodDeclaration) {
                return (MethodDeclaration) node;
            }
            node = node.getParent();
        }
        return null;
    }

    /**
     * Creates a new LoggingOperation for the given context.
     */
    protected LoggingOperation createOperation(OperationContext context) {
        return new LoggingOperation(context);
    }

    /**
     * Process the selected method. Subclasses implement this to perform their
     * specific logging operation.
     */
    protected abstract void processMethod(OperationContext context) throws Exception;
}
