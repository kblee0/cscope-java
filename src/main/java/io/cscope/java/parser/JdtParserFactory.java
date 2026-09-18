package io.cscope.java.parser;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

import java.util.Map;

public class JdtParserFactory {
    public static ASTParser createParser(String[] classpath, String[] sourcepath) {
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setBindingsRecovery(true);
        parser.setStatementsRecovery(true);

        Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_17, options);
        parser.setCompilerOptions(options);

        // Environment for binding resolution
        parser.setEnvironment(classpath, sourcepath, new String[]{"UTF-8"}, true);

        return parser;
    }
}
