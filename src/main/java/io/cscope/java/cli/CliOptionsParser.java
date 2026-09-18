package io.cscope.java.cli;

/** CLI 옵션 파싱 전담. */
public final class CliOptionsParser {

    public static final String USAGE =
            "Usage: java -jar cscope-java.jar -p <project id> -s <path> [-l <jar_dir_or_file>] "
            + "[-o <output path>] [-i <pkg1,pkg2>] [-e <pkg1,pkg2>]\n"
            + "\n"
            + "  -p  project_id (필수). 예: my-app\n"
            + "  -s  분석 대상 소스 루트 디렉터리 (필수). file_path 의 기준 경로가 된다.\n"
            + "  -l  의존 라이브러리 Jar 디렉터리 또는 Jar 파일 (선택). 바인딩 해석 정확도 향상.\n"
            + "  -o  CSV 출력 디렉터리 (선택, 기본 ./output)\n"
            + "  -i  수집 대상 패키지 prefix 목록, 콤마 구분 (선택)\n"
            + "  -e  제외 패키지 prefix 목록, 콤마 구분 (선택)\n"
            + "  -h  도움말\n";

    private CliOptionsParser() {
    }

    public static CliOptions parse(String[] args) {
        CliOptions options = new CliOptions();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-h":
                case "--help":
                    options.help = true;
                    return options;
                case "-p":
                    options.projectId = requireValue(args, ++i, "-p");
                    break;
                case "-s":
                    options.sourcePath = requireValue(args, ++i, "-s");
                    break;
                case "-l":
                    options.libPath = requireValue(args, ++i, "-l");
                    break;
                case "-o":
                    options.outputPath = requireValue(args, ++i, "-o");
                    break;
                case "-i":
                    addCsv(options.includePrefixes, requireValue(args, ++i, "-i"));
                    break;
                case "-e":
                    addCsv(options.excludePrefixes, requireValue(args, ++i, "-e"));
                    break;
                default:
                    throw new IllegalArgumentException("알 수 없는 옵션: " + arg);
            }
        }
        if (options.projectId == null || options.projectId.isBlank()) {
            throw new IllegalArgumentException("-p <project id> 는 필수입니다.");
        }
        if (options.sourcePath == null || options.sourcePath.isBlank()) {
            throw new IllegalArgumentException("-s <path> 는 필수입니다.");
        }
        return options;
    }

    private static String requireValue(String[] args, int index, String option) {
        if (index >= args.length) {
            throw new IllegalArgumentException(option + " 옵션의 값이 없습니다.");
        }
        return args[index];
    }

    private static void addCsv(java.util.List<String> target, String value) {
        for (String token : value.split(",")) {
            String trimmed = token.trim();
            if (!trimmed.isEmpty()) {
                target.add(trimmed);
            }
        }
    }
}
