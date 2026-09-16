package unxutils.coreutils.cat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

public class ConcatenateService {
    private static Stream<String> getLines(Path path) {
        try {
            return Files.lines(path);
        }
        catch (IOException e) {
            return Stream.empty();
        }
    }

    private Stream<String> fileInput(Path cwd, String file) {
        if (file.equals("-")) return new Scanner(
                System.in,
                StandardCharsets.UTF_8
        ).findAll(".+").map(MatchResult::group);
        else {
            var filePath = cwd.resolve(file).toAbsolutePath();
            return getLines(filePath);
        }
    }

    public void concatenate(Path cwd, List<String> files) {
        var linesStream = files.stream().map(file -> fileInput(cwd, file)).reduce(Stream::concat).orElse(Stream.empty());
        linesStream.forEach(System.out::println);
    }
}
