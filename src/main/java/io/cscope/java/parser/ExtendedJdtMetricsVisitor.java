package io.cscope.java.parser;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import io.cscope.java.config.PackageFilter;
import io.cscope.java.dto.AnalysisResult;
import io.cscope.java.dto.CallEdge;
import io.cscope.java.dto.ClassMetric;
import io.cscope.java.dto.ClassType;
import io.cscope.java.dto.FileMetric;
import io.cscope.java.dto.MethodMetric;
import io.cscope.java.util.SourceIdBuilder;

/**
 * CompilationUnit 1개에서 class / method / call 정보를 수집한다.
 *
 * <p>수집 규칙
 * <ul>
 *   <li>익명/로컬 클래스의 메서드는 별도 method_metric 으로 만들지 않고, 그 안의 호출을 바깥(named) 메서드에 귀속시킨다.</li>
 *   <li>필드 초기화식/static 블록 안의 호출은 caller 메서드가 없으므로 수집하지 않는다.</li>
 *   <li>호출 수집 대상 노드: MethodInvocation, SuperMethodInvocation, ClassInstanceCreation</li>
 * </ul>
 */
public class ExtendedJdtMetricsVisitor extends ASTVisitor {

    private final CompilationUnit compilationUnit;
    private final PackageFilter filter;
    private final AnalysisResult result;
    private final String projectId;
    private final String filePackageName;
    private final String fileId;
    private final Deque<MethodContext> callerStack = new ArrayDeque<>();

    public ExtendedJdtMetricsVisitor(CompilationUnit compilationUnit, FileMetric fileMetric,
            PackageFilter filter, AnalysisResult result) {
        this.compilationUnit = compilationUnit;
        this.filter = filter;
        this.result = result;
        this.projectId = fileMetric.projectId;
        this.filePackageName = fileMetric.packageName;
        this.fileId = fileMetric.fileId;
    }

    // ---------------------------------------------------------------- 타입

    @Override
    public boolean visit(TypeDeclaration node) {
        ClassType classType;
        if (node.isInterface()) {
            classType = ClassType.INTERFACE;
        } else if (Modifier.isAbstract(node.getModifiers())) {
            classType = ClassType.ABSTRACT_CLASS;
        } else {
            classType = ClassType.CLASS;
        }
        addClass(node, node.getName().getIdentifier(), node.resolveBinding(), classType);
        return true;
    }

    @Override
    public boolean visit(EnumDeclaration node) {
        addClass(node, node.getName().getIdentifier(), node.resolveBinding(), ClassType.ENUM);
        return true;
    }

    @Override
    public boolean visit(AnnotationTypeDeclaration node) {
        addClass(node, node.getName().getIdentifier(), node.resolveBinding(), ClassType.INTERFACE);
        return true;
    }

    @Override
    public boolean visit(RecordDeclaration node) {
        addClass(node, node.getName().getIdentifier(), node.resolveBinding(), ClassType.CLASS);
        return true;
    }

    private void addClass(ASTNode node, String simpleName, ITypeBinding binding, ClassType classType) {
        String fullClassName = binding != null
                ? SourceIdBuilder.typeName(binding)
                : SourceIdBuilder.classId(enclosingTypePrefix(node), simpleName);

        ClassMetric metric = new ClassMetric();
        metric.classId = fullClassName;
        metric.projectId = projectId;
        metric.fileId = fileId;
        metric.packageName = packageNameOf(binding);
        metric.className = simpleName;
        metric.fullClassName = fullClassName;
        metric.classType = classType;
        metric.startLine = lineOf(node.getStartPosition());
        metric.endLine = lineOf(node.getStartPosition() + node.getLength() - 1);
        metric.loc = metric.endLine - metric.startLine + 1;
        result.classes.add(metric);
    }

    // -------------------------------------------------------------- 메서드

    @Override
    public boolean visit(MethodDeclaration node) {
        IMethodBinding binding = node.resolveBinding();
        ITypeBinding declaringClass = binding != null ? binding.getDeclaringClass() : null;
        if (declaringClass != null && (declaringClass.isAnonymous() || declaringClass.isLocal())) {
            // 익명/로컬 클래스 메서드: 호출은 바깥 메서드에 귀속시킨다.
            return true;
        }

        String fullClassName = declaringClass != null
                ? SourceIdBuilder.typeName(declaringClass)
                : enclosingTypeName(node);
        List<String> parameterTypes = binding != null
                ? SourceIdBuilder.parameterTypeNames(binding)
                : SourceIdBuilder.parameterTypeNames(node);
        String methodName = node.getName().getIdentifier();
        String methodId = SourceIdBuilder.methodId(fullClassName, methodName, parameterTypes);

        MethodMetric metric = new MethodMetric();
        metric.methodId = methodId;
        metric.projectId = projectId;
        metric.classId = fullClassName;
        metric.packageName = declaringClass != null ? packageNameOf(declaringClass) : filePackageName;
        metric.className = declaringClass != null ? declaringClass.getName() : simpleNameOf(fullClassName);
        metric.methodName = methodName;
        metric.signature = methodId;
        metric.startLine = lineOf(node.getStartPosition());
        metric.endLine = lineOf(node.getStartPosition() + node.getLength() - 1);
        metric.loc = metric.endLine - metric.startLine + 1;
        metric.cyclomaticComplexity = CyclomaticComplexityVisitor.compute(node);
        result.methods.add(metric);

        callerStack.push(new MethodContext(node, methodId));
        return true;
    }

    @Override
    public void endVisit(MethodDeclaration node) {
        MethodContext current = callerStack.peek();
        if (current != null && current.node == node) {
            callerStack.pop();
        }
    }

    // ---------------------------------------------------------------- 호출

    @Override
    public boolean visit(MethodInvocation node) {
        recordCall(node, node.resolveMethodBinding(), node.getName().getIdentifier(), node.arguments().size());
        return true;
    }

    @Override
    public boolean visit(SuperMethodInvocation node) {
        recordCall(node, node.resolveMethodBinding(), node.getName().getIdentifier(), node.arguments().size());
        return true;
    }

    @Override
    public boolean visit(ClassInstanceCreation node) {
        IMethodBinding binding = node.resolveConstructorBinding();
        String fallbackName = binding != null ? binding.getName() : node.getType().toString();
        recordCall(node, binding, fallbackName, node.arguments().size());
        return true;
    }

    private void recordCall(ASTNode node, IMethodBinding binding, String fallbackName, int argumentCount) {
        MethodContext caller = callerStack.peek();
        if (caller == null) {
            return; // 필드 초기화식 / static 블록 등 caller 메서드가 없는 호출
        }

        String calleeClassName;
        String calleeMethodName;
        String calleeRawSignature;
        boolean fromSource = false;

        if (binding != null) {
            IMethodBinding declaration = binding.getMethodDeclaration();
            ITypeBinding declaringClass = declaration.getDeclaringClass();
            calleeClassName = SourceIdBuilder.typeName(declaringClass);
            calleeMethodName = declaration.getName();
            calleeRawSignature = SourceIdBuilder.methodId(calleeClassName, calleeMethodName,
                    SourceIdBuilder.parameterTypeNames(declaration));
            fromSource = declaringClass != null && declaringClass.isFromSource();
        } else {
            calleeClassName = SourceIdBuilder.UNRESOLVED_CLASS;
            calleeMethodName = fallbackName;
            calleeRawSignature = SourceIdBuilder.methodId(calleeClassName, calleeMethodName,
                    SourceIdBuilder.unknownParameterTypes(argumentCount));
        }

        if (!filter.isCollectable(calleeClassName)) {
            return;
        }

        CallEdge edge = new CallEdge();
        edge.callerMethodId = caller.methodId;
        edge.callSeq = ++caller.callSeq;
        edge.projectId = projectId;
        edge.calleeClassName = calleeClassName;
        edge.calleeMethodName = calleeMethodName;
        edge.calleeRawSignature = calleeRawSignature;
        edge.calleeFromSource = fromSource;
        edge.callLine = lineOf(node.getStartPosition());
        result.calls.add(edge);
    }

    // ---------------------------------------------------------------- 보조

    private int lineOf(int position) {
        int line = compilationUnit.getLineNumber(position);
        return line > 0 ? line : 0;
    }

    private String packageNameOf(ITypeBinding binding) {
        if (binding == null || binding.getPackage() == null) {
            return filePackageName;
        }
        return binding.getPackage().getName();
    }

    /** 바인딩 실패 시 AST 부모를 거슬러 올라가 만든 풀 클래스명. */
    private String enclosingTypeName(ASTNode node) {
        ASTNode parent = node.getParent();
        while (parent != null && !(parent instanceof AbstractTypeDeclaration)) {
            parent = parent.getParent();
        }
        if (parent == null) {
            return SourceIdBuilder.classId(filePackageName, SourceIdBuilder.UNRESOLVED_CLASS);
        }
        AbstractTypeDeclaration type = (AbstractTypeDeclaration) parent;
        return SourceIdBuilder.classId(enclosingTypePrefix(type), type.getName().getIdentifier());
    }

    /** 중첩 타입이면 바깥 타입명까지 포함한 prefix, 아니면 패키지명. */
    private String enclosingTypePrefix(ASTNode node) {
        ASTNode parent = node.getParent();
        while (parent != null && !(parent instanceof AbstractTypeDeclaration)) {
            parent = parent.getParent();
        }
        if (parent == null) {
            return filePackageName;
        }
        AbstractTypeDeclaration outer = (AbstractTypeDeclaration) parent;
        return SourceIdBuilder.classId(enclosingTypePrefix(outer), outer.getName().getIdentifier());
    }

    private static String simpleNameOf(String fullClassName) {
        int index = fullClassName.lastIndexOf('.');
        return index < 0 ? fullClassName : fullClassName.substring(index + 1);
    }

    /** caller 메서드 컨텍스트. call_seq 는 메서드별 1부터 증가. */
    private static final class MethodContext {
        private final ASTNode node;
        private final String methodId;
        private int callSeq;

        private MethodContext(ASTNode node, String methodId) {
            this.node = node;
            this.methodId = methodId;
        }
    }
}
