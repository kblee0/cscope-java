package io.cscope.java.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

/**
 * 자연 키(Natural Business Key) 생성기.
 *
 * <pre>
 * file_id   : com.home.service.UserService.java
 * class_id  : com.home.service.UserService
 * method_id : com.home.service.UserService.createUser(java.lang.String,int)
 * </pre>
 */
public final class SourceIdBuilder {

    /** 바인딩이 해석되지 않은 호출의 callee 클래스명. */
    public static final String UNRESOLVED_CLASS = "<unresolved>";

    private SourceIdBuilder() {
    }

    public static String fileId(String packageName, String fileName) {
        return isEmpty(packageName) ? fileName : packageName + "." + fileName;
    }

    public static String classId(String packageName, String simpleClassName) {
        return isEmpty(packageName) ? simpleClassName : packageName + "." + simpleClassName;
    }

    public static String methodId(String fullClassName, String methodName, List<String> parameterTypes) {
        return fullClassName + "." + methodName + "(" + String.join(",", parameterTypes) + ")";
    }

    /** 제네릭 소거(erasure) 기준의 풀 타입명. */
    public static String typeName(ITypeBinding type) {
        if (type == null) {
            return UNRESOLVED_CLASS;
        }
        ITypeBinding erasure = type.getErasure() != null ? type.getErasure() : type;
        String qualified = erasure.getQualifiedName();
        if (isEmpty(qualified)) {
            qualified = erasure.getBinaryName();
        }
        if (isEmpty(qualified)) {
            qualified = erasure.getName();
        }
        return isEmpty(qualified) ? UNRESOLVED_CLASS : qualified;
    }

    /** 바인딩 기반 파라미터 타입 목록(정확). */
    public static List<String> parameterTypeNames(IMethodBinding binding) {
        List<String> names = new ArrayList<>();
        for (ITypeBinding parameter : binding.getParameterTypes()) {
            names.add(typeName(parameter));
        }
        return names;
    }

    /** 바인딩이 없을 때의 AST 기반 파라미터 타입 목록(차선). */
    @SuppressWarnings("unchecked")
    public static List<String> parameterTypeNames(MethodDeclaration method) {
        List<String> names = new ArrayList<>();
        for (SingleVariableDeclaration parameter : (List<SingleVariableDeclaration>) method.parameters()) {
            ITypeBinding binding = parameter.getType().resolveBinding();
            names.add(binding != null ? typeName(binding) : parameter.getType().toString());
        }
        return names;
    }

    /** 바인딩이 없는 호출의 파라미터 자리표시자. */
    public static List<String> unknownParameterTypes(int argumentCount) {
        List<String> names = new ArrayList<>(argumentCount);
        for (int i = 0; i < argumentCount; i++) {
            names.add("?");
        }
        return names;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }
}
