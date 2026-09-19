package unxutils.coreutils.cat;

import cli.LogUtils;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.logging.Level.FINER;

@AllArgsConstructor
public class ConcatenateService {
    private ConcatenateOptions options;

    private static String readFileContent(Path path) {
        try {
            return Files.readString(path);
        }
        catch (IOException e) {
            return null;
        }
    }

    private String readStreamContent(Path cwd, String file) {
        String ret = null;
        try(var input = options.standardInput()) {
            if (file.equals("-")) ret = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            else ret = readFileContent(cwd.resolve(file).toAbsolutePath());
        }
        catch (IOException ioe) {
            LogUtils.getDefaultLogger().log(FINER, "Error processing %s".formatted(file), ioe);
        }
        return ret;
    }

    private Stream<String> getLinesRespectingNewLineAtTheEnd(String s) {
        try (var reader = new BufferedReader(new StringReader(s))) {
            var lines = Arrays.asList(reader.readAllAsString().split(System.lineSeparator(), -1));
            return lines.stream();
        }
        catch (IOException ioe) {
            return Stream.empty();
        }
    }

    public String concatenate(Path cwd, List<String> files) {
        var linesStream = getLinesRespectingNewLineAtTheEnd(
            files.stream().map(
                file -> readStreamContent(cwd, file)
            ).filter(Objects::nonNull).collect(Collectors.joining())
        );
        // Apply output transformations - squeeze options together since many are actually aliases or
        //  aggregators for the rest
        var conversions = TransformationOptions.from(options);
        // Do we want to squeeze blanks?
        if (conversions.squeezeBlank()) {
            var lines = new java.util.ArrayList<>(linesStream.toList());
            if (lines.size() > 1) {
                int i = 1;
                while (i < lines.size())
                    if (lines.get(i).isEmpty() && lines.get(i - 1).isEmpty())
                        lines.remove(i);
                    else i++;
            }
            linesStream = lines.stream();
        }
        if (conversions.showEnds()) linesStream = linesStream.map(this::showEnds);
        if (conversions.showTabs()) linesStream = linesStream.map(this::showTabs);
        if (conversions.number() || conversions.numberNonBlank()) {
            var lineNumberHelper = new LineNumberHelper(conversions);
            linesStream = linesStream.map(lineNumberHelper::decorate);
        }
        return linesStream.collect(Collectors.joining(System.lineSeparator()));
    }

    private String showEnds(String line) {
        return line + "^M$";
    }

    private String showTabs(String line) {
        return line.replace("\t", "^I");
    }

    @RequiredArgsConstructor
    private static class LineNumberHelper {
        private int lineNumber = 1;
        private final TransformationOptions options;

        public String decorate(String line) {
            return (options.squeezeBlank() && line.isEmpty())
                ? line
                : String.format("%6d%s", lineNumber++, line.trim().isEmpty() ? "" : "\t" + line);
        }
    }
}
