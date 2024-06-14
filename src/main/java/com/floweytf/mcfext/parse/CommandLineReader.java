package com.floweytf.mcfext.parse;

import com.mojang.brigadier.StringReader;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.commands.functions.CommandFunction;

import java.util.ArrayList;
import java.util.List;

public class CommandLineReader {
    public static final String ERR_LINE_CONT = "line continuation at end-of-file";
    public static final String ERR_FORWARD_SLASH = "unknown or invalid command '%s' (do you mean '%s'?)";
    public static final String ERR_BAD_COMMENT = "'//' is not allowed, use '#' for comment";

    private final List<String> entries;
    private final IntArrayList entryLineNumbers;
    private int index;

    private CommandLineReader(List<String> entries, IntArrayList entryLineNumbers) {
        this.entries = entries;
        this.entryLineNumbers = entryLineNumbers;
        index = 0;
    }

    public static CommandLineReader fromLines(Diagnostics context, List<String> lines) {
        final var entries = new ArrayList<String>();
        final var entryLineNumbers = new IntArrayList();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            final int lineNo = i;
            String cmd;

            // Read the next line, handling \ at the end of the line if present
            if (CommandFunction.shouldConcatenateNextLine(line)) {
                StringBuilder reader = new StringBuilder(line);
                do {
                    if (++i == lines.size()) {
                        context.reportErr(i, ERR_LINE_CONT);
                        break;
                    }

                    reader.deleteCharAt(reader.length() - 1);
                    String string2 = lines.get(i).trim();
                    reader.append(string2);
                } while (CommandFunction.shouldConcatenateNextLine(reader));
                cmd = reader.toString();
            } else {
                cmd = line;
            }

            final var reader = new StringReader(cmd.trim());

            if (!reader.canRead() || reader.peek() == '#') {
                continue;
            }

            // better syntax error handling for lines starting with '/'
            if (reader.peek() == '/') {
                reader.skip();
                if (reader.peek() == '/') {
                    context.reportErr(i, ERR_BAD_COMMENT, cmd);
                    continue;
                }

                context.reportErr(i, ERR_FORWARD_SLASH, cmd, reader.readUnquotedString());
                continue;
            }


            entries.add(line);
            entryLineNumbers.add(lineNo);
        }

        return new CommandLineReader(entries, entryLineNumbers);
    }

    public String curr() {
        return entries.get(index);
    }

    public int lineNumber() {
        return entryLineNumbers.getInt(index);
    }

    public int index() {
        return index;
    }

    public void next() {
        index++;
    }

    public boolean present() {
        return index < entries.size();
    }
}
