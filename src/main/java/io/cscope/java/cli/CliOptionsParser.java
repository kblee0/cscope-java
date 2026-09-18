package io.cscope.java.cli;

import java.util.Arrays;

public class CliOptionsParser {
    public CliOptions parse(String[] args) {
        CliOptions options = new CliOptions();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--project":
                    options.projectId = args[++i];
                    break;
                case "--source":
                    options.sourcePath = args[++i];
                    break;
                case "--lib":
                    options.libPath = args[++i];
                    break;
                case "--output":
                    options.outputPath = args[++i];
                    break;
                case "--include":
                    options.includePackages = Arrays.asList(args[++i].split(","));
                    break;
                case "--exclude":
                    options.excludePackages = Arrays.asList(args[++i].split(","));
                    break;
            }
        }
        return options;
    }
}
