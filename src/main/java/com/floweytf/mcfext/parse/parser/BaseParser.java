package com.floweytf.mcfext.parse.parser;

import com.floweytf.mcfext.parse.ast.ReturnAST;
import com.floweytf.mcfext.util.ComponentUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import static com.floweytf.mcfext.util.CommandUtil.*;

public class BaseParser {
    public static final String ERR_PRAGMA = "failed to parse pragma: %s";
    public static final String ERR_PRAGMA_BAD_FEAT = "unknown pragma feature flag %s";
    private static final CommandDispatcher<Parser> PRAGMA_DISPATCH = new CommandDispatcher<>();

    public static void init() {
        final var PRAGMA_BAD_FEAT = exceptionType(s -> ComponentUtils.fLiteral(ERR_PRAGMA_BAD_FEAT, s));

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

        Parser.register("pragma", (parser, text, lineNo) -> {
            try {
                PRAGMA_DISPATCH.execute(text, parser);
            } catch (CommandSyntaxException e) {
                parser.context.reportErr(lineNo, ERR_PRAGMA, e.getMessage());
            }

            parser.reader.next(); // eat it
            return Parser.Result.parseNext();
        });

        Parser.register("return", (parser, text, lineNo) -> {
            if (!text.equals("return")) {
                return Parser.Result.fallthrough();
            }

            parser.reader.next();
            return Parser.Result.ast(new ReturnAST());
        });
    }
}
