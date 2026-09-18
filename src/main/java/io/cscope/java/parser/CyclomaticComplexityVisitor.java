package io.cscope.java.parser;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * 순환 복잡도 = 분기 노드 수 + 1.
 *
 * <p>대상 노드: If, For, EnhancedFor, While, Do, Catch, Conditional(?:), SwitchCase(default 제외)
 */
public final class CyclomaticComplexityVisitor extends ASTVisitor {

    private int decisionPoints;

    public static int compute(MethodDeclaration method) {
        CyclomaticComplexityVisitor visitor = new CyclomaticComplexityVisitor();
        if (method.getBody() != null) {
            method.getBody().accept(visitor);
        }
        return visitor.decisionPoints + 1;
    }

    @Override
    public boolean visit(IfStatement node) {
        decisionPoints++;
        return true;
    }

    @Override
    public boolean visit(ForStatement node) {
        decisionPoints++;
        return true;
    }

    @Override
    public boolean visit(EnhancedForStatement node) {
        decisionPoints++;
        return true;
    }

    @Override
    public boolean visit(WhileStatement node) {
        decisionPoints++;
        return true;
    }

    @Override
    public boolean visit(DoStatement node) {
        decisionPoints++;
        return true;
    }

    @Override
    public boolean visit(CatchClause node) {
        decisionPoints++;
        return true;
    }

    @Override
    public boolean visit(ConditionalExpression node) {
        decisionPoints++;
        return true;
    }

    @Override
    public boolean visit(SwitchCase node) {
        if (!node.isDefault()) {
            decisionPoints++;
        }
        return true;
    }
}
