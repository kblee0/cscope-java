package io.cscope.java.parser;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

import io.cscope.java.config.AnalyzerConfig;

/** 바인딩 해석이 가능한 ASTParser 생성. */
public final class JdtParserFactory {

    private static final String COMPLIANCE = JavaCore.VERSION_17;

    private JdtParserFactory() {
    }

    public static ASTParser newParser(AnalyzerConfig config) {
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        parser.setStatementsRecovery(true);

        Map<String, String> compilerOptions = JavaCore.getOptions();
        JavaCore.setComplianceOptions(COMPLIANCE, compilerOptions);
        parser.setCompilerOptions(compilerOptions);

        // includeRunningVMBootclasspath = true : JDK 타입 바인딩 해석용
        parser.setEnvironment(config.getClasspathEntries(), config.getSourcepathEntries(), null, true);
        return parser;
    }
}
