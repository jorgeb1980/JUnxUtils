package unxutils.coreutils.cat;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        try {
            if (file.equals("-")) return new String(options.standardInput().readAllBytes(), StandardCharsets.UTF_8);
            else return getContent(cwd.resolve(file).toAbsolutePath());
        }
        catch (IOException e) {
            return null;
        }
    }

    public String concatenate(Path cwd, List<String> files) {
        var linesStream = files.stream().map(
            file -> fileInput(cwd, file)
        ).filter(Objects::nonNull).collect(Collectors.joining()).lines();
        return linesStream.collect(Collectors.joining("\n"));
    }
}
