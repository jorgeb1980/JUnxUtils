package unxutils.coreutils.cat;

import cli.LogUtils;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.logging.Level.FINER;

@AllArgsConstructor
public class ConcatenateService {
    private ConcatenateOptions options = null;

    private static String getContent(Path path) {
        try {
            return Files.readString(path);
        }
        catch (IOException e) {
            return null;
        }
    }

    private String fileInput(Path cwd, String file) {
        String ret = null;
        try(var input = options.standardInput()) {
            if (file.equals("-")) ret = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            else ret = getContent(cwd.resolve(file).toAbsolutePath());
        }
        catch (IOException ioe) {
            LogUtils.getDefaultLogger().log(FINER, "Error processing %s".formatted(file), ioe);
        }
        return ret;
    }

    public String concatenate(Path cwd, List<String> files) {
        var linesStream = files.stream().map(
            file -> fileInput(cwd, file)
        ).filter(Objects::nonNull).collect(Collectors.joining()).lines();
        return linesStream.collect(Collectors.joining("\n"));
    }
}
