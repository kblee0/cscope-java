package io.cscope.java.util;

/**
 * 파일 단위 라인 계측.
 *
 * <p>문자열/문자 리터럴 내부의 {@code //}, {@code /*} 는 주석으로 보지 않는다.
 * 코드와 주석이 한 줄에 같이 있으면 code_loc, comment_loc 양쪽에 모두 계수한다.
 */
public final class LineNumberUtils {

    private LineNumberUtils() {
    }

    public static LineCounts count(String source) {
        String[] lines = source.split("\r\n|\r|\n", -1);
        int lineCount = lines.length;
        if (lineCount > 1 && lines[lineCount - 1].isEmpty()) {
            lineCount--; // 파일 끝 개행으로 생긴 빈 원소는 제외
        }

        int code = 0;
        int comment = 0;
        boolean inBlockComment = false;

        for (int index = 0; index < lineCount; index++) {
            String line = lines[index];
            boolean hasCode = false;
            boolean hasComment = false;
            int i = 0;

            while (i < line.length()) {
                char c = line.charAt(i);
                if (inBlockComment) {
                    hasComment = true;
                    if (c == '*' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
                        inBlockComment = false;
                        i += 2;
                    } else {
                        i++;
                    }
                    continue;
                }
                if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
                    hasComment = true;
                    break;
                }
                if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
                    hasComment = true;
                    inBlockComment = true;
                    i += 2;
                    continue;
                }
                if (c == '"' || c == '\'') {
                    hasCode = true;
                    i = skipLiteral(line, i, c);
                    continue;
                }
                if (!Character.isWhitespace(c)) {
                    hasCode = true;
                }
                i++;
            }

            if (hasCode) {
                code++;
            }
            if (hasComment) {
                comment++;
            }
        }
        return new LineCounts(lineCount, code, comment);
    }

    private static int skipLiteral(String line, int start, char quote) {
        int i = start + 1;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (c == '\\') {
                i += 2;
                continue;
            }
            if (c == quote) {
                return i + 1;
            }
            i++;
        }
        return i;
    }

    /** 라인 계측 결과. */
    public static final class LineCounts {
        public final int totalLines;
        public final int codeLoc;
        public final int commentLoc;

        LineCounts(int totalLines, int codeLoc, int commentLoc) {
            this.totalLines = totalLines;
            this.codeLoc = codeLoc;
            this.commentLoc = commentLoc;
        }
    }
}
