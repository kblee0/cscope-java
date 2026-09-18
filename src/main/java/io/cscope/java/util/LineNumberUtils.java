package io.cscope.java.util;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class LineNumberUtils {
    public static int getStartLine(CompilationUnit cu, ASTNode node) {
        return cu.getLineNumber(node.getStartPosition());
    }

    public static int getEndLine(CompilationUnit cu, ASTNode node) {
        return cu.getLineNumber(node.getStartPosition() + node.getLength());
    }

    public static int calculateLoc(String source, int startPosition, int length) {
        if (source == null) return 0;
        String nodeSource = source.substring(startPosition, Math.min(startPosition + length, source.length()));
        String[] lines = nodeSource.split("\r\n|\r|\n");
        int count = 0;
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                count++;
            }
        }
        return count;
    }
}
