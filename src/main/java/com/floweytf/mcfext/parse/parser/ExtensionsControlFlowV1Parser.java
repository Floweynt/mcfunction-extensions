package com.floweytf.mcfext.parse.parser;

import com.floweytf.mcfext.execution.FunctionExecSource;
import com.floweytf.mcfext.parse.ast.ASTNode;
import com.floweytf.mcfext.parse.ast.BlockAST;
import com.floweytf.mcfext.parse.ast.cfv1.LoopAST;
import com.floweytf.mcfext.parse.ast.cfv1.RunAST;
import com.floweytf.mcfext.util.ExecuteCommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.execution.UnboundEntryAction;

import java.util.function.BiFunction;

public class ExtensionsControlFlowV1Parser {
    private static final String ERR_UNCLOSED = "unclosed statement";
    private static final String ERR_CFV1_PARSE = "failed to parse '%s' control flow statement: %s";
    private static final String ERR_DISABLED = "'run' and 'loop' statements not enabled when 'control flow v2' is " +
        "enabled, consider disabling it with 'pragma disable cfv2'";
    private static final String ERR_EXTRA_CLOSING = "extraneous '}' (consider removing it)";

    private static final CommandDispatcher<CommandSourceStack> DISPATCH = new CommandDispatcher<>();

    private static ASTNode parse(
        Parser parser, String text, int lineNo, String name, boolean isFunction,
        BiFunction<BlockAST, UnboundEntryAction<CommandSourceStack>, ASTNode> constructor
    ) {
        final var res = parser.parseCommand(
            DISPATCH, new StringReader(text),
            msg -> parser.context.reportErr(lineNo, ERR_CFV1_PARSE, name, msg)
        );

        parser.reader.next();

        return constructor.apply(
            new BlockAST(
                parser.parseBlock(
                    () -> parser.parseNextCommand(false, isFunction), lineNo, "}",
                    ERR_UNCLOSED
                )
            ),
            res.orElse(null)
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
            (p, text, lineNo, _0, b) -> Parser.Result.ast(parse(p, text, lineNo, "run", b, RunAST::new)),
            features -> !features.isV2ControlFlow(),
            ERR_DISABLED
        );

        Parser.register(
            "loop",
            (p, text, lineNo, _0, b) -> Parser.Result.ast(parse(p, text, lineNo, "loop", b, LoopAST::new)),
            features -> !features.isV2ControlFlow(),
            ERR_DISABLED
        );

        // better error handling
        Parser.register("}", (p, text, lineNo) -> {
            p.context.reportErr(lineNo, ERR_EXTRA_CLOSING);
            p.reader.next(); // eat it
            return Parser.Result.parseNext();
        });
    }
}