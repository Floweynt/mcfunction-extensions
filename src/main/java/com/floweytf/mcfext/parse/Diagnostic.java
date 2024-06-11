package com.floweytf.mcfext.parse;

public record Diagnostic(DiagnosticLevel level, int line, String message) {
    public static final String ERR_BAD_BREAK = "'break' may only be used inside iterative control flow";
    public static final String ERR_BAD_CONTINUE = "'continue' may only be used inside iterative control flow";

}