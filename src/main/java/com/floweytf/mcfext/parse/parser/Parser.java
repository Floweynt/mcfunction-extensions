package com.floweytf.mcfext.parse.parser;

import com.floweytf.mcfext.codegen.CodeGenerator;
import com.floweytf.mcfext.codegen.DebugCodeGenerator;
import com.floweytf.mcfext.parse.CommandLineReader;
import com.floweytf.mcfext.parse.Diagnostics;
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
    interface ParseHandler {
        FeatureParseResult doParse(Parser parser, String text, int lineNo, ParseContext context);
    }

    private record FeatureHandlerEntry(ParseHandler handler, boolean recvMacro, Predicate<ParseFeatureSet> enablePred,
                                       String notEnabledWarning) {
    }

    private static final Logger LOGGER = LogManager.getLogger("FunctionParser");
    private static final Map<String, FeatureHandlerEntry> FEATURE_HANDLER = new HashMap<>();

    static void register(
        String name, boolean recvMacro, ParseHandler handler, Predicate<ParseFeatureSet> enablePred, String error
    ) {
        FEATURE_HANDLER.put(name, new FeatureHandlerEntry(handler, recvMacro, enablePred, error));
    }

    static void register(String name, boolean recvMacro, ParseHandler handler) {
        register(name, recvMacro, handler, f -> true, null);
    }

    public static void init(CommandBuildContext access) {
        BaseParser.init();
        ExtensionsControlFlowV1Parser.init(access);
        ExtensionSubroutineParser.init();
    }

    final Diagnostics context;
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
        var prevIndex = -1;

        while (reader.present()) {
            // Rather than leak memory by infinite loop, we check if the parser has eaten the next token
            // This forces the parser to terminate in linear bounded time (context-sensitive grammar)
            if (reader.index() == prevIndex) {
                throw new IllegalStateException("internal error: parser failed to advance");
            }
            prevIndex = reader.index();

            // Stupid stuff
            final var line = reader.curr();
            final var isMacro = line.startsWith("$");
            final var text = isMacro ? line.substring(1) : line;
            final var lineNo = reader.lineNumber();

            // Generate diagnostic for empty macro statements
            if (isMacro && text.length() == 1) {
                reader.next();
                context.reportErr(lineNo, "empty macro statement is not allowed");
            }

            final var parts = text.split(" ", 2);
            final var entry = FEATURE_HANDLER.get(parts[0]);

            // Handle feature parsers
            if (entry != null && (!isMacro || entry.recvMacro())) {
                if (!entry.enablePred.test(features)) {
                    context.reportWarn(lineNo, entry.notEnabledWarning);
                } else {
                    final var res = entry.handler.doParse(
                        this, text, lineNo,
                        new ParseContext(isMacro, isTopLevel, isInSubroutine)
                    );

                    switch (res.action()) {
                    case RETURN:
                        return res.value();
                    case CONTINUE:
                        continue;
                    case FALLTHROUGH:
                    }
                }
            }

            // Handle vanilla
            reader.next(); // Eat the current line
            if (isMacro) {
                // Handle vanilla macros
                return new MacroAST(lineNo, text);
            } else {
                // Handle vanilla commands
                final var res = parseCommand(
                    dispatcher, new StringReader(text),
                    msg -> context.reportErr(lineNo, "failed to parse command: '%s'", msg)
                );

                if (res.isPresent()) {
                    return new CommandAST(res.get());
                }
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

    private Parser(Diagnostics context, List<String> lines, CommandSourceStack dummySource,
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
        final var diagnostics = new Diagnostics();
        final var parser = new Parser(diagnostics, lines, dummySource, dispatcher);
        final var ast = parser.parseTopLevel();

        if (diagnostics.hasError()) {
            diagnostics.dumpErrors(2, LOGGER, id, lines);
            return null;
        }

        final var context = new CodegenContext();
        final var shouldDebugDump = parser.features.isDebugDump();

        final CodeGenerator<CommandSourceStack> codegen = shouldDebugDump ? new DebugCodeGenerator<>() :
            new CodeGenerator<>();

        ast.emit(diagnostics, context, codegen);

        if (shouldDebugDump) {
            LOGGER.info("AST dump: \n{}\nCodegen dump: \n{}\n----", ast.dump(), codegen.dumpDisassembly());
        }

        diagnostics.dumpErrors(2, LOGGER, id, lines);

        if (diagnostics.hasError()) {
            return null;
        }

        return codegen.define(id);
    }
}
