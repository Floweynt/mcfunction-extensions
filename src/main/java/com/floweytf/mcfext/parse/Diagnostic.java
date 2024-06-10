package com.floweytf.mcfext.parse;

public record Diagnostic(DiagnosticLevel level, int line, String message) {
    public static final String ERR_PRAGMA = "failed to parse pragma: %s";
    public static final String ERR_PRAGMA_BAD_FEAT = "unknown pragma feature flag %s";
    public static final String ERR_PARSE_CMD = "failed to parse command: %s";
    public static final String ERR_UNCLOSED = "unclosed '%s' statement";
    public static final String ERR_LINE_CONT = "line continuation at end-of-file";
    public static final String ERR_BAD_CMD = "unknown or invalid command '%s' (use '#' not '//' for comments)";
    public static final String ERR_FORWARD_SLASH = "unknown or invalid command '%s' (do you mean '%s'?)";
    public static final String ERR_CF_V1_PARSE = "failed to parse '%s' command: %s";
    public static final String ERR_CF_V1_EXTRA_CLOSE = "extraneous '}', consider removing it";
}