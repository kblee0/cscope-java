package io.cscope.java.cli;

import java.util.Arrays;
import java.util.List;

public class CliOptions {
    public String projectId;
    public String sourcePath;
    public String outputPath = "./output";
    public String libPath; // Path to JARs or libraries
    public List<String> includePackages;
    public List<String> excludePackages;

    public void printHelp() {
        System.out.println("Usage: java -jar cscope-java.jar --project <id> --source <path> [--lib <jar_dir_or_file>] [--output <path>] [--include <pkg1,pkg2>] [--exclude <pkg1,pkg2>]");
    }
}
