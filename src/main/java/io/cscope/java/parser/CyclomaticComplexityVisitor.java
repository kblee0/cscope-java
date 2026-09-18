package io.cscope.java.parser;

import org.eclipse.jdt.core.dom.*;

public class CyclomaticComplexityVisitor extends ASTVisitor {
    private int complexity = 1;

    public int getComplexity() {
        return complexity;
    }

    @Override
    public boolean visit(IfStatement node) {
        complexity++;
        return true;
    }

    @Override
    public boolean visit(ForStatement node) {
        complexity++;
        return true;
    }

    @Override
    public boolean visit(EnhancedForStatement node) {
        complexity++;
        return true;
    }

    @Override
    public boolean visit(WhileStatement node) {
        complexity++;
        return true;
    }

    @Override
    public boolean visit(DoStatement node) {
        complexity++;
        return true;
    }

    @Override
    public boolean visit(CatchClause node) {
        complexity++;
        return true;
    }

    @Override
    public boolean visit(ConditionalExpression node) {
        complexity++;
        return true;
    }

    @Override
    public boolean visit(SwitchCase node) {
        if (!node.isDefault()) {
            complexity++;
        }
        return true;
    }

    // InfixExpression can have logical operators like && and || which also increase complexity
    @Override
    public boolean visit(InfixExpression node) {
        InfixExpression.Operator op = node.getOperator();
        if (op == InfixExpression.Operator.CONDITIONAL_AND || op == InfixExpression.Operator.CONDITIONAL_OR) {
            complexity++;
        }
        return true;
    }
}
