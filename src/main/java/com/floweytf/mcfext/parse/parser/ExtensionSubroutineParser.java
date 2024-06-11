package com.floweytf.mcfext.parse.parser;

import com.floweytf.mcfext.parse.ParseFeatureSet;
import com.floweytf.mcfext.parse.ast.BlockAST;
import com.floweytf.mcfext.parse.ast.subroutine.SubroutineCallAST;
import com.floweytf.mcfext.parse.ast.subroutine.SubroutineDefinitionAST;
import com.floweytf.mcfext.parse.ast.subroutine.SubroutineReturnAST;

public class ExtensionSubroutineParser {
    private static final String ERR_UNCLOSED = "unclosed subroutine definition";
    private static final String ERR_NOT_TOP_LEVEL = "subroutine definition is only allowed at the top level";

    public static void init() {
        Parser.register(
            "subroutine",
            (parser, text, lineNo, isTopLevel, isInSubroutine) -> {
                parser.reader.next();

                if (!isTopLevel) {
                    parser.context.reportErr(lineNo, ERR_NOT_TOP_LEVEL);
                }

                final var parts = text.split(" ");
                if (parts.length != 2 || parts[1].isEmpty()) {
                    parser.context.reportErr(lineNo, "bad subroutine definition, expected 'subroutine <identifier>'");

                    if (parts.length == 1 || parts[1].isEmpty()) {
                        return Parser.Result.ast(null);
                    }
                }

                return Parser.Result.ast(new SubroutineDefinitionAST(
                    new BlockAST(
                        parser.parseBlock(
                            () -> parser.parseNextCommand(false, true), lineNo, "end",
                            ERR_UNCLOSED
                        )
                    ),
                    parts[1],
                    lineNo
                ));
            },
            ParseFeatureSet::isSubroutines,
            "subroutines are not enabled, use 'pragma enable subroutine' to enable this feature"
        );

        Parser.register(
            "subroutine_return",
            (parser, text, lineNo, isTopLevel, isInSubroutine) -> {
                parser.reader.next();

                if (!text.equals("subroutine_return")) {
                    parser.context.reportErr(lineNo, "subroutine_return takes no parameters");
                }

                if (!isInSubroutine) {
                    parser.context.reportErr(lineNo, "subroutine_return is not valid outside of a subroutine");
                }

                return Parser.Result.ast(new SubroutineReturnAST());
            },
            ParseFeatureSet::isSubroutines,
            "subroutines are not enabled, use 'pragma enable subroutine' to enable this feature"
        );


        Parser.register(
            "subroutine_call",
            (parser, text, lineNo, isTopLevel, isInSubroutine) -> {
                parser.reader.next();

                final var parts = text.split(" ");

                if (parts.length != 2 || parts[1].isEmpty()) {
                    parser.context.reportErr(lineNo, "bad subroutine definition, expected 'subroutine_call " +
                        "<identifier>'");

                    if (parts.length == 1 || parts[1].isEmpty()) {
                        return Parser.Result.ast(null);
                    }
                }

                return Parser.Result.ast(new SubroutineCallAST(parts[1], lineNo));
            },
            ParseFeatureSet::isSubroutines,
            "subroutines are not enabled, use 'pragma enable subroutine' to enable this feature"
        );
    }
}
