package com.floweytf.mcfext.parse.parser;

import com.floweytf.mcfext.parse.ast.ReturnAST;
import com.floweytf.mcfext.util.ComponentUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import static com.floweytf.mcfext.util.CommandUtil.*;

public class BaseParser {
    private static final CommandDispatcher<Parser> PRAGMA_DISPATCH = new CommandDispatcher<>();

    public static void init() {
        final var PRAGMA_BAD_FEAT = exceptionType(s -> ComponentUtils.fLiteral("unknown pragma feature flag %s", s));

        PRAGMA_DISPATCH.register(lit("pragma", lit("enable", arg("flag", StringArgumentType.word(), (context) -> {
            final var value = StringArgumentType.getString(context, "flag");
            if (context.getSource().features.enable(StringArgumentType.getString(context, "flag"))) {
                throw PRAGMA_BAD_FEAT.create(value);
            }
            return 0;
        })), lit("disable", arg("flag", StringArgumentType.word(), (context) -> {
            final var value = StringArgumentType.getString(context, "flag");
            if (context.getSource().features.disable(StringArgumentType.getString(context, "flag"))) {
                throw PRAGMA_BAD_FEAT.create(value);
            }
            return 0;
        }))));

        Parser.register(
            "pragma",
            false,
            (parser, text, lineNo, context) -> {
                try {
                    PRAGMA_DISPATCH.execute(text, parser);
                } catch (CommandSyntaxException e) {
                    parser.context.reportErr(lineNo, "failed to parse pragma: %s", e.getMessage());
                }

                parser.reader.next(); // eat it
                return FeatureParseResult.parseNext();
            }
        );

        Parser.register(
            "return",
            false,
            (parser, text, lineNo, context) -> {
                if (!text.equals("return")) {
                    return FeatureParseResult.fallthrough();
                }

                parser.reader.next();
                return FeatureParseResult.ast(new ReturnAST());
            }
        );
    }
}
