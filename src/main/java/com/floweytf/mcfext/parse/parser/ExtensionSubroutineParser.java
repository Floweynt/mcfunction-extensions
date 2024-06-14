package com.floweytf.mcfext.parse.parser;

import com.floweytf.mcfext.parse.ParseFeatureSet;
import com.floweytf.mcfext.parse.ast.BlockAST;
import com.floweytf.mcfext.parse.ast.subroutine.SubroutineCallAST;
import com.floweytf.mcfext.parse.ast.subroutine.SubroutineDefinitionAST;
import com.floweytf.mcfext.parse.ast.subroutine.SubroutineReturnAST;

public class ExtensionSubroutineParser {
    public static void init() {
        final var notEnabled = "subroutines are not enabled (consider adding 'pragma enable subroutine')";
        Parser.register(
            "subroutine",
            false,
            (parser, text, lineNo, context) -> {
                parser.reader.next();

                if (!context.isTopLevel()) {
                    parser.context.reportErr(lineNo, "subroutine definition is only allowed at the top level");
                }

                final var parts = text.split(" ");
                if (parts.length != 2 || parts[1].isEmpty()) {
                    parser.context.reportErr(lineNo, "bad subroutine definition, expected 'subroutine <identifier>'");

                    if (parts.length == 1 || parts[1].isEmpty()) {
                        return FeatureParseResult.ast(null);
                    }
                }

                return FeatureParseResult.ast(new SubroutineDefinitionAST(
                    new BlockAST(
                        parser.parseBlock(
                            () -> parser.parseNextCommand(false, true), lineNo, "end",
                            "unclosed subroutine definition (missing 'end')"
                        )
                    ),
                    parts[1],
                    lineNo
                ));
            },
            ParseFeatureSet::isSubroutines,
            notEnabled
        );

        Parser.register(
            "subroutine_return",
            false,
            (parser, text, lineNo, context) -> {
                parser.reader.next();

                if (!text.equals("subroutine_return")) {
                    parser.context.reportErr(lineNo, "subroutine_return takes no parameters");
                }

                if (!context.isSubroutine()) {
                    parser.context.reportErr(lineNo, "subroutine_return is not valid outside of a subroutine");
                }

                return FeatureParseResult.ast(new SubroutineReturnAST());
            },
            ParseFeatureSet::isSubroutines,
            notEnabled
        );


        Parser.register(
            "subroutine_call",
            false,
            (parser, text, lineNo, context) -> {
                parser.reader.next();

                final var parts = text.split(" ");

                if (parts.length != 2 || parts[1].isEmpty()) {
                    parser.context.reportErr(lineNo, "bad subroutine call, expected 'subroutine_call <identifier>'");

                    if (parts.length == 1 || parts[1].isEmpty()) {
                        return FeatureParseResult.ast(null);
                    }
                }

                return FeatureParseResult.ast(new SubroutineCallAST(parts[1], lineNo));
            },
            ParseFeatureSet::isSubroutines,
            notEnabled
        );
    }
}
