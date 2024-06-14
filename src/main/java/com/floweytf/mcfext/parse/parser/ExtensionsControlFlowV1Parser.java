package com.floweytf.mcfext.parse.parser;

import com.floweytf.mcfext.execution.FunctionExecSource;
import com.floweytf.mcfext.parse.ControlFlowStatement;
import com.floweytf.mcfext.parse.ParseContext;
import com.floweytf.mcfext.parse.ast.ASTNode;
import com.floweytf.mcfext.parse.ast.BlockAST;
import com.floweytf.mcfext.parse.ast.cfv1.LoopAST;
import com.floweytf.mcfext.parse.ast.cfv1.RunAST;
import com.floweytf.mcfext.util.ExecuteCommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

import java.util.function.BiFunction;

public class ExtensionsControlFlowV1Parser {
    private static final CommandDispatcher<CommandSourceStack> DISPATCH = new CommandDispatcher<>();

    private static ASTNode parse(
        Parser parser, String text, int lineNo, String name, ParseContext context,
        BiFunction<BlockAST, ControlFlowStatement<CommandSourceStack>, ASTNode> constructor
    ) {
        parser.reader.next();
        final ControlFlowStatement<CommandSourceStack> statement;

        if (context.isMacro()) {
            statement = ControlFlowStatement.macro(DISPATCH, text, lineNo);
        } else {
            statement = ControlFlowStatement.plain(parser.parseCommand(
                DISPATCH, new StringReader(text),
                msg -> parser.context.reportErr(lineNo, "failed to parse '%s' statement: %s", name, msg)
            ).orElse(null));
        }

        return constructor.apply(
            new BlockAST(
                parser.parseBlock(
                    () -> parser.parseNextCommand(false, context.isSubroutine()), lineNo, "}",
                    "unclosed statement (missing '}')"
                )
            ),
            statement
        );
    }

    public static void init(CommandBuildContext access) {
        ExecuteCommandUtils.registerV1ControlFlow(DISPATCH, access, "run", context -> {
            final var source = (FunctionExecSource) context.getSource();
            source.getExecState().stack.peekSourceList().add(source);
            return 0;
        });

        ExecuteCommandUtils.registerV1ControlFlow(DISPATCH, access, "loop", context -> {
            final var source = (FunctionExecSource) context.getSource();
            source.getExecState().stack.peekSourceList().add(source);
            return 0;
        });

        Parser.register(
            "run",
            true,
            (p, text, lineNo, context) -> FeatureParseResult.ast(parse(p, text, lineNo, "run", context, RunAST::new)),
            features -> !features.isV2ControlFlow(),
            "'run' is disabled when control-flow-v2 is enabled (consider adding 'pragma disable cfv2')"
        );

        Parser.register(
            "loop",
            true,
            (p, text, lineNo, context) -> FeatureParseResult.ast(parse(p, text, lineNo, "loop", context, LoopAST::new)),
            features -> !features.isV2ControlFlow(),
            "'loop' is disabled when control-flow-v2' is enabled (consider adding 'pragma disable cfv2')"
        );

        // better error handling
        Parser.register("}", false, (p, text, lineNo, context) -> {
            p.context.reportErr(lineNo, "extraneous '}' (consider removing it)");
            p.reader.next(); // eat it
            return FeatureParseResult.parseNext();
        });
    }
}