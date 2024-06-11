package com.floweytf.mcfext.parse.parser;

import com.floweytf.mcfext.codegen.CodeGenerator;
import com.floweytf.mcfext.codegen.DebugCodeGenerator;
import com.floweytf.mcfext.parse.CommandLineReader;
import com.floweytf.mcfext.parse.ParseContext;
import com.floweytf.mcfext.parse.ParseFeatureSet;
import com.floweytf.mcfext.parse.ast.*;
import com.floweytf.mcfext.parse.ast.subroutine.SubroutineDefinitionAST;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.execution.tasks.BuildContexts;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Parser {
    static class Result {
        private enum Action {
            RETURN,
            CONTINUE,
            FALLTHROUGH
        }

        private final Action action;
        @Nullable
        private final ASTNode value;

        private Result(Action action, @Nullable ASTNode value) {
            this.action = action;
            this.value = value;
        }

        public static Result parseNext() {
            return new Result(Action.CONTINUE, null);
        }

        public static Result fallthrough() {
            return new Result(Action.FALLTHROUGH, null);
        }

        public static Result ast(ASTNode node) {
            return new Result(Action.RETURN, node);
        }
    }

    interface FullParseHandler {
        Result doParse(Parser parser, String text, int lineNo, boolean isTopLevel, boolean isInSubroutine);
    }

    interface SimpleParseHandler {
        Result doParse(Parser parser, String text, int lineNo);
    }

    private record FeatureHandlerEntry(FullParseHandler handler, Predicate<ParseFeatureSet> enablePred,
                                       String message) {

    }

    private static final Logger LOGGER = LogManager.getLogger("FunctionParser");
    private static final Map<String, FeatureHandlerEntry> FEATURE_HANDLER = new HashMap<>();

    public static final String ERR_PARSE_CMD = "failed to parse command: %s";
    public static final String ERR_BAD_CMD = "unknown or invalid command '%s' (use '#' not '//' for comments)";

    static void register(String name, FullParseHandler handler, Predicate<ParseFeatureSet> enablePred, String error) {
        FEATURE_HANDLER.put(name, new FeatureHandlerEntry(handler, enablePred, error));
    }

    static void register(String name, SimpleParseHandler handler, Predicate<ParseFeatureSet> enablePred, String error) {
        register(name, (parser, text, lineNo, _0, _1) -> handler.doParse(parser, text, lineNo), enablePred, error);
    }

    static void register(String name, FullParseHandler handler) {
        register(name, handler, f -> true, null);
    }

    static void register(String name, SimpleParseHandler handler) {
        register(name, handler, f -> true, null);
    }

    public static void init(CommandBuildContext access) {
        BaseParser.init();
        ExtensionsControlFlowV1Parser.init(access);
        ExtensionSubroutineParser.init();
    }

    final ParseContext context;
    final CommandLineReader reader;
    final CommandSourceStack dummySource;
    final CommandDispatcher<CommandSourceStack> dispatcher;
    final ParseFeatureSet features = new ParseFeatureSet();

    Optional<UnboundEntryAction<CommandSourceStack>> parseCommand(CommandDispatcher<CommandSourceStack> dispatcher,
                                                                  StringReader reader, Consumer<String> onError) {
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

    List<ASTNode> parseBlock(Supplier<ASTNode> parseOne, int startLineNo, String terminator, String onUnterminated) {
        int prevIndex = -1;

        final var body = new ArrayList<ASTNode>();

        while (reader.present()) {
            if (reader.index() == prevIndex) {
                throw new IllegalStateException("internal error: parser failed to advance");
            }
            prevIndex = reader.index();

            if (reader.curr().equals(terminator)) {
                reader.next();
                return body;
            }

            final var ast = parseOne.get();


            if (ast != null) {
                body.add(ast);
            }
        }

        context.reportErr(startLineNo, onUnterminated);
        return body;
    }

    @Nullable
    ASTNode parseNextCommand(boolean isTopLevel, boolean isInSubroutine) {
        int prevIndex = -1;

        while (reader.present()) {
            if (reader.index() == prevIndex) {
                throw new IllegalStateException("internal error: parser failed to advance");
            }
            prevIndex = reader.index();

            final var text = reader.curr();
            final var lineNo = reader.lineNumber();
            final var parts = text.split(" ", 2);

            final var entry = FEATURE_HANDLER.get(parts[0]);

            // handle features
            if (entry != null) {
                if (!entry.enablePred.test(features)) {
                    context.reportWarn(lineNo, entry.message);
                } else {
                    final var res = entry.handler.doParse(this, text, lineNo, isTopLevel, isInSubroutine);
                    switch (res.action) {
                    case RETURN:
                        return res.value;
                    case CONTINUE:
                        continue;
                    case FALLTHROUGH:
                    }
                }
            }

            // handle commands
            final var res = parseCommand(
                dispatcher, new StringReader(text),
                msg -> context.reportErr(lineNo, ERR_PARSE_CMD, msg)
            );

            reader.next();

            if (res.isEmpty()) {
                context.reportErr(lineNo, ERR_BAD_CMD, text);
            } else {
                return new CommandAST(res.get());
            }
        }

        return null;
    }

    private ASTNode parseTopLevel() {
        final var children = new ArrayList<ASTNode>();
        final var subroutine = new ArrayList<SubroutineDefinitionAST>();

        while (reader.present()) {
            final var ast = parseNextCommand(true, false);
            if (ast == null)
                continue;

            if (ast instanceof SubroutineDefinitionAST subroutineDefinitionAST) {
                subroutine.add(subroutineDefinitionAST);
            } else {
                children.add(ast);
            }
        }

        return new TopLevelAST(new BlockAST(children), subroutine);
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
    public static CommandFunction<CommandSourceStack> compileFunction(
        CommandDispatcher<CommandSourceStack> dispatcher,
        CommandSourceStack dummySource, List<String> lines, ResourceLocation id) {
        final var parseCtx = new ParseContext();
        final var parser = new Parser(parseCtx, lines, dummySource, dispatcher);
        final var ast = parser.parseTopLevel();

        if (parseCtx.dumpErrors(2, LOGGER, id, lines)) {
            return null;
        }

        final var context = new CodegenContext();

        if (parser.features.isDebugDump()) {
            final var codegen = new DebugCodeGenerator();
            ast.emit(parseCtx, context, codegen);
            final var res = codegen.define(id);
            LOGGER.info("AST dump: \n{}\nCodegen dump: \n{}\n----", ast.dump(), codegen.dumpDisassembly());
            return res;
        } else {
            final var codegen = new CodeGenerator();
            ast.emit(parseCtx, context, codegen);
            return codegen.define(id);
        }
    }
}
