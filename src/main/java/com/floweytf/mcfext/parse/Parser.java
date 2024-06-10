package com.floweytf.mcfext.parse;

import com.floweytf.mcfext.codegen.CodeGenerator;
import com.floweytf.mcfext.codegen.DebugCodeGenerator;
import com.floweytf.mcfext.execution.ControlFlowEvalSource;
import com.floweytf.mcfext.parse.ast.ASTNode;
import com.floweytf.mcfext.parse.ast.BlockAST;
import com.floweytf.mcfext.parse.ast.CommandAST;
import com.floweytf.mcfext.parse.ast.TopLevelAST;
import com.floweytf.mcfext.parse.ast.cfv1.LoopAST;
import com.floweytf.mcfext.parse.ast.cfv1.RunAST;
import com.floweytf.mcfext.util.ComponentUtils;
import com.floweytf.mcfext.util.ExecuteCommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.execution.tasks.BuildContexts;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.floweytf.mcfext.util.CommandUtil.*;

public class Parser {
    private static final Logger LOGGER = LogManager.getLogger("FunctionParser");

    private final ParseContext context;
    private final CommandLineReader reader;
    private final CommandSourceStack dummySource;
    private final CommandDispatcher<CommandSourceStack> dispatcher;
    private final ParseFeatureSet features = new ParseFeatureSet();

    private static final CommandDispatcher<Parser> PRAGMA_DISPATCH = new CommandDispatcher<>();
    private static final CommandDispatcher<CommandSourceStack> CONTROL_FLOW_V1 = new CommandDispatcher<>();

    public Optional<UnboundEntryAction<CommandSourceStack>> parseCommand(CommandDispatcher<CommandSourceStack> dispatcher, StringReader reader, Consumer<String> onError) {
        final var parseResults = dispatcher.parse(reader, dummySource);

        try {
            Commands.validateParseResults(parseResults);
        } catch (CommandSyntaxException e) {
            onError.accept(e.getMessage());
            return Optional.empty();
        }

        final var chain = ContextChain.tryFlatten(parseResults.getContext().build(reader.getString()));

        if (chain.isEmpty()) {
            onError.accept(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parseResults.getReader()).getMessage());
            return Optional.empty();
        }

        return chain.map(x -> new BuildContexts.Unbound<>(reader.getString(), x));
    }

    private void parsePragma(String line, int lineNo) {
        try {
            PRAGMA_DISPATCH.execute(line, this);
        } catch (CommandSyntaxException e) {
            context.reportErr(lineNo, Diagnostic.ERR_PRAGMA, e.getMessage());
        }
    }

    private ASTNode parseControlFlowV1(String text, int lineNo, String type) {
        final var res = parseCommand(
            CONTROL_FLOW_V1, new StringReader(text),
            msg -> context.reportErr(lineNo, Diagnostic.ERR_CF_V1_PARSE, type, msg)
        );

        reader.next();

        final var body = new ArrayList<ASTNode>();

        while (true) {
            if (reader.curr().equals("}")) {
                reader.next();
                break;
            }

            final var ast = parseNextCommand();
            if (ast == null) {
                context.reportErr(lineNo, Diagnostic.ERR_UNCLOSED, type);
                break;
            }

            body.add(ast);
        }

        return type.equals("run") ? new RunAST(new BlockAST(body), res.orElse(null)) :
            new LoopAST(new BlockAST(body), res.orElse(null));
    }

    @Nullable
    private ASTNode parseNextCommand() {
        while (reader.present()) {
            final var line = reader.curr();
            final var lineNo = reader.lineNumber();
            final var lineReader = new StringReader(line);
            final var firstStr = lineReader.readUnquotedString();

            if (firstStr.equals("pragma")) {
                parsePragma(line, lineNo);
                reader.next();
                continue;
            }

            if (features.isV2ControlFlow()) {
                throw new NotImplementedException();
            } else {
                switch (firstStr) {
                case "run":
                case "loop":
                    return parseControlFlowV1(line, lineNo, firstStr);
                }
            }

            final var res = parseCommand(
                dispatcher, new StringReader(line),
                msg -> context.reportErr(lineNo, Diagnostic.ERR_PARSE_CMD, msg)
            );

            if (res.isEmpty()) {
                // Special error handling for '}'
                reader.next();
                if (line.equals("}")) {
                    context.reportErr(lineNo, Diagnostic.ERR_CF_V1_EXTRA_CLOSE);
                } else {
                    context.reportErr(lineNo, Diagnostic.ERR_BAD_CMD, line);
                }
            } else {
                reader.next();
                return new CommandAST(res.get());
            }
        }

        return null;
    }

    private ASTNode parseTopLevel() {
        final var children = new ArrayList<ASTNode>();
        while (reader.present()) {
            children.add(parseNextCommand());
        }

        return new TopLevelAST(new BlockAST(children));
    }

    // Dispatcher initialization
    private static void initPragma() {
        final var PRAGMA_BAD_FEAT = exceptionType(s -> ComponentUtils.fLiteral(Diagnostic.ERR_PRAGMA_BAD_FEAT, s));

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
    }

    private static void initCFv1(CommandBuildContext access) {
        ExecuteCommandUtils.registerV1ControlFlow(CONTROL_FLOW_V1, access, "run", context -> {
            final var source = (ControlFlowEvalSource) context.getSource();
            source.getExecState().stack.peekSourceList().add(context.getSource());
            return 0;
        });

        ExecuteCommandUtils.registerV1ControlFlow(CONTROL_FLOW_V1, access, "loop", context -> {
            final var source = (ControlFlowEvalSource) context.getSource();
            source.getExecState().stack.peekSourceList().add(context.getSource());
            return 0;
        });
    }

    public static void init(CommandBuildContext access) {
        initPragma();
        initCFv1(access);
    }

    private Parser(ParseContext context, List<String> lines, CommandSourceStack dummySource,
                   CommandDispatcher<CommandSourceStack> dispatcher) {
        this.context = context;
        this.dispatcher = dispatcher;
        this.reader = CommandLineReader.fromLines(context, lines);
        this.dummySource = dummySource;
    }

    /**
     * Parses a command function with extended syntax capabilities.
     *
     * @param dispatcher  The instance of minecraft's dispatcher.
     * @param dummySource A dummy command source.
     * @param lines       The source code.
     * @param id          The resource location of the function.
     * @return The parsed command function, or null if error.
     */
    @Nullable
    public static CommandFunction<CommandSourceStack> parseLine(
        CommandDispatcher<CommandSourceStack> dispatcher,
        CommandSourceStack dummySource, List<String> lines, ResourceLocation id) {
        final var context = new ParseContext();
        final var parser = new Parser(context, lines, dummySource, dispatcher);
        final var ast = parser.parseTopLevel();

        if (context.dumpErrors(2, LOGGER, id, lines)) {
            return null;
        }

        if (parser.features.isDebugDump()) {
            final var codegen = new DebugCodeGenerator<CommandSourceStack>();
            ast.emit(codegen);
            final var res = codegen.define(id);
            LOGGER.info("AST dump: \n{}\nCodegen dump: \n{}\n----", ast.dump(), codegen.dumpDisassembly());
            return res;
        } else {
            final var codegen = new CodeGenerator<CommandSourceStack>();
            ast.emit(codegen);
            return codegen.define(id);
        }
    }
}
