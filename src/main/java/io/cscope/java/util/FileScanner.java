package io.cscope.java.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** 소스/의존 Jar 수집 유틸. */
public final class FileScanner {

    private FileScanner() {
    }

    public static List<Path> findJavaFiles(Path root) throws IOException {
        try (Stream<Path> stream = Files.walk(root)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .sorted(Comparator.comparing(Path::toString))
                    .collect(Collectors.toList());
        }
    }

    /** 디렉터리면 하위 *.jar 전체, 파일이면 그 파일 자체를 반환한다. */
    public static List<Path> findJarFiles(Path libPath) throws IOException {
        if (Files.isRegularFile(libPath)) {
            return List.of(libPath);
        }
        try (Stream<Path> stream = Files.walk(libPath)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .sorted(Comparator.comparing(Path::toString))
                    .collect(Collectors.toList());
        }
    }
}
