package io.cscope.java.parser;

import io.cscope.java.config.PackageFilter;
import io.cscope.java.dto.*;
import io.cscope.java.util.LineNumberUtils;
import io.cscope.java.util.SourceIdBuilder;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

public class ExtendedJdtMetricsVisitor extends ASTVisitor {
    private final String projectId;
    private final String filePath;
    private final String source;
    private final PackageFilter packageFilter;
    private CompilationUnit cu;
    
    private final FileMetricDto fileMetric;
    private final List<ClassMetricDto> classMetrics = new ArrayList<>();
    private final List<MethodMetricDto> methodMetrics = new ArrayList<>();
    private final List<CallGraphDto> callGraphs = new ArrayList<>();

    private TypeDeclaration currentClass;
    private MethodDeclaration currentMethod;
    private int callSeq = 0;

    public ExtendedJdtMetricsVisitor(String projectId, String filePath, String source, PackageFilter packageFilter) {
        this.projectId = projectId;
        this.filePath = filePath;
        this.source = source;
        this.packageFilter = packageFilter;
        
        // Initialize file metric with placeholder
        this.fileMetric = new FileMetricDto(null, projectId);
        this.fileMetric.filePath = filePath;
    }

    @Override
    public boolean visit(CompilationUnit node) {
        this.cu = node;
        PackageDeclaration pkg = node.getPackage();
        String packageName = (pkg != null) ? pkg.getName().getFullyQualifiedName() : "";
        
        if (!packageFilter.shouldAnalyze(packageName)) {
            return false;
        }

        String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);
        if (fileName.contains("/")) fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        
        fileMetric.fileId = SourceIdBuilder.buildFileId(packageName, fileName);
        fileMetric.packageName = packageName;
        fileMetric.fileName = fileName;
        fileMetric.totalLines = cu.getLineNumber(source.length());
        fileMetric.codeLoc = LineNumberUtils.calculateLoc(source, 0, source.length());
        
        // JDT provides comments separately
        fileMetric.commentLoc = node.getCommentList().size(); // Rough estimate
        
        return true;
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        ITypeBinding binding = node.resolveBinding();
        if (binding == null) return true;

        TypeDeclaration parentClass = currentClass;
        currentClass = node;

        String className = node.getName().getIdentifier();
        String fullClassName = binding.getBinaryName();
        if (fullClassName == null) fullClassName = binding.getQualifiedName();

        ClassMetricDto classDto = new ClassMetricDto(
                SourceIdBuilder.buildClassId(fileMetric.packageName, fullClassName),
                projectId,
                fileMetric.fileId
        );
        classDto.packageName = fileMetric.packageName;
        classDto.className = className;
        classDto.fullClassName = fullClassName;
        classDto.classType = node.isInterface() ? "INTERFACE" : (Modifier.isAbstract(node.getModifiers()) ? "ABSTRACT_CLASS" : "CLASS");
        classDto.startLine = LineNumberUtils.getStartLine(cu, node);
        classDto.endLine = LineNumberUtils.getEndLine(cu, node);
        classDto.loc = LineNumberUtils.calculateLoc(source, node.getStartPosition(), node.getLength());

        classMetrics.add(classDto);
        fileMetric.classCount++;

        return true;
    }

    @Override
    public void endVisit(TypeDeclaration node) {
        // Pop class if nested
        // This logic might need refinement for nested classes
    }

    @Override
    public boolean visit(EnumDeclaration node) {
        ITypeBinding binding = node.resolveBinding();
        if (binding == null) return true;

        ClassMetricDto classDto = new ClassMetricDto(
                SourceIdBuilder.buildClassId(fileMetric.packageName, binding.getQualifiedName()),
                projectId,
                fileMetric.fileId
        );
        classDto.packageName = fileMetric.packageName;
        classDto.className = node.getName().getIdentifier();
        classDto.fullClassName = binding.getQualifiedName();
        classDto.classType = "ENUM";
        classDto.startLine = LineNumberUtils.getStartLine(cu, node);
        classDto.endLine = LineNumberUtils.getEndLine(cu, node);
        classDto.loc = LineNumberUtils.calculateLoc(source, node.getStartPosition(), node.getLength());

        classMetrics.add(classDto);
        fileMetric.classCount++;
        return true;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        IMethodBinding binding = node.resolveBinding();
        if (binding == null) return true;

        currentMethod = node;
        callSeq = 0;

        String methodName = node.getName().getIdentifier();
        String signature = buildSignature(binding);
        String classId = SourceIdBuilder.buildClassId(fileMetric.packageName, binding.getDeclaringClass().getQualifiedName());
        String methodId = SourceIdBuilder.buildMethodId(classId, methodName, signature);

        MethodMetricDto methodDto = new MethodMetricDto(methodId, projectId, classId);
        methodDto.packageName = fileMetric.packageName;
        methodDto.className = binding.getDeclaringClass().getName();
        methodDto.methodName = methodName;
        methodDto.signature = signature;
        methodDto.startLine = LineNumberUtils.getStartLine(cu, node);
        methodDto.endLine = LineNumberUtils.getEndLine(cu, node);
        methodDto.loc = LineNumberUtils.calculateLoc(source, node.getStartPosition(), node.getLength());

        // Complexity
        CyclomaticComplexityVisitor ccVisitor = new CyclomaticComplexityVisitor();
        node.accept(ccVisitor);
        methodDto.cyclomaticComplexity = ccVisitor.getComplexity();

        methodMetrics.add(methodDto);
        fileMetric.methodCount++;
        
        // Update class method count
        for (ClassMetricDto c : classMetrics) {
            if (c.classId.equals(classId)) {
                c.methodCount++;
                break;
            }
        }

        return true;
    }

    @Override
    public boolean visit(MethodInvocation node) {
        if (currentMethod == null) return true;

        IMethodBinding callerBinding = currentMethod.resolveBinding();
        if (callerBinding == null) return true;

        IMethodBinding calleeBinding = node.resolveMethodBinding();
        
        String callerClassId = SourceIdBuilder.buildClassId(fileMetric.packageName, callerBinding.getDeclaringClass().getQualifiedName());
        String callerMethodId = SourceIdBuilder.buildMethodId(callerClassId, currentMethod.getName().getIdentifier(), buildSignature(callerBinding));

        callSeq++;
        CallGraphDto callDto = new CallGraphDto(callerMethodId, callSeq, projectId);
        callDto.callLine = cu.getLineNumber(node.getStartPosition());

        if (calleeBinding != null) {
            callDto.calleeClassName = calleeBinding.getDeclaringClass().getQualifiedName();
            callDto.calleeMethodName = calleeBinding.getName();
            callDto.calleeRawSignature = buildSignature(calleeBinding);
        } else {
            // Fallback if binding fails
            callDto.calleeMethodName = node.getName().getIdentifier();
            callDto.calleeRawSignature = "()"; // Unknown
        }

        callGraphs.add(callDto);
        return true;
    }

    private String buildSignature(IMethodBinding binding) {
        StringBuilder sb = new StringBuilder("(");
        ITypeBinding[] params = binding.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            sb.append(params[i].getQualifiedName());
            if (i < params.length - 1) sb.append(",");
        }
        sb.append(")");
        return sb.toString();
    }

    public FileMetricDto getFileMetric() { return fileMetric; }
    public List<ClassMetricDto> getClassMetrics() { return classMetrics; }
    public List<MethodMetricDto> getMethodMetrics() { return methodMetrics; }
    public List<CallGraphDto> getCallGraphs() { return callGraphs; }
}
