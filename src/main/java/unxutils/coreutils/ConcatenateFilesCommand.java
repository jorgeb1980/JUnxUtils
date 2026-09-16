package unxutils.coreutils;

import cli.annotations.Command;
import cli.annotations.OptionalArgs;
import cli.annotations.Parameter;
import cli.annotations.Run;
import lombok.Setter;
import unxutils.coreutils.cat.ConcatenateOptions;
import unxutils.coreutils.cat.ConcatenateService;

import java.nio.file.Path;
import java.util.List;

/**
 * <b>Program documentation</b><br>
 <pre>
 {@code
 cat copies each file (‘-’ means standard input), or standard input if none are given, to standard output. Synopsis:

 cat [option]... [file]...

 The program accepts the following options. Also see Common options.

 ‘-A’
 ‘--show-all’
 Equivalent to -vET.

 ‘-b’
 ‘--number-nonblank’
 Number all nonempty output lines, starting with 1.

 ‘-e’
 Equivalent to -vE.

 ‘-E’
 ‘--show-ends’
 Display a ‘$’ after the end of each line. The \r\n combination is shown as ‘^M$’.

 ‘-n’
 ‘--number’
 Number all output lines, starting with 1. This option is ignored if -b is in effect.

 ‘-s’
 ‘--squeeze-blank’
 Suppress repeated adjacent blank lines; output just one empty line instead of several.

 ‘-t’
 Equivalent to -vT.

 ‘-T’
 ‘--show-tabs’
 Display TAB characters as ‘^I’.

 ‘-u’
 Ignored; for POSIX compatibility.

 ‘-v’
 ‘--show-nonprinting’
 Display control characters except for LFD and TAB using ‘^’ notation and precede characters that have the high bit set with ‘M-’.

 On systems like MS-DOS that distinguish between text and binary files, cat normally reads and writes in binary mode.
 However, cat reads in text mode if one of the options -bensAE is used or if cat is reading from standard input and
 standard input is a terminal. Similarly, cat writes in text mode if one of the options -bensAE is used or if standard
 output is a terminal.

 An exit status of zero indicates success, and a nonzero value indicates failure.
 }
 </pre>
 */

@Setter
@Command(command="cat", description="Concatenate FILE(s) to standard output.")
public class ConcatenateFilesCommand {

    // Command parameters
    @Setter
    @Parameter(name = "A", longName = "show-all", description = "Equivalent to -vET.")
    private Boolean showAll;

    @Setter
    @Parameter(name = "b", longName = "--number-nonblank", description = "Number all nonempty output lines, starting with 1.")
    private Boolean numberNonBlank;

    @Setter
    @Parameter(name = "e", description = "Equivalent to -vE.")
    private Boolean showControlAndNumbers;

    @Setter
    @Parameter(name = "E", longName = "show-ends", description = "Display a ‘$’ after the end of each line. The \\r\\n combination is shown as ‘^M$’.")
    private Boolean showEnds;

    @Setter
    @Parameter(name = "n", longName = "number", description = "Number all output lines, starting with 1. This option is ignored if -b is in effect.")
    private Boolean number;

    @Setter
    @Parameter(name = "s", longName = "squeeze-blank", description = "Suppress repeated adjacent blank lines; output just one empty line instead of several.")
    private Boolean squeezeBlank;

    @Setter
    @Parameter(name = "t", description = "Equivalent to -vT.")
    private Boolean showControlAndTabs;

    @Setter
    @Parameter(name = "T", longName = "show-tabs", description = "Display TAB characters as ‘^I’.")
    private Boolean showTabs;

    @Setter
    @Parameter(name = "u", description = "Ignored; for POSIX compatibility.")
    private Boolean ignoredParameter;

    @Setter
    @Parameter(name = "v", longName = "show-nonprinting", description = "Display control characters except for LFD and TAB using ‘^’ notation and precede characters that have the high bit set with ‘M-’.")
    private Boolean showNonprinting;

    @Setter
    @OptionalArgs(name = "FILE")
    private List<String> files;

    @Run
    // Entry point for cat
    public int execute(Path cwd) throws Exception {
        // Stream of lines concatenating all the input
        var output =
            new ConcatenateService(
                ConcatenateOptions
                    .builder()
                    .showAll(showAll)
                    .numberNonBlank(numberNonBlank)
                    .showControlAndNumbers(showControlAndNumbers)
                    .showEnds(showEnds)
                    .number(number)
                    .squeezeBlank(squeezeBlank)
                    .showControlAndTabs(showControlAndTabs)
                    .showTabs(showTabs)
                    .ignoredParameter(ignoredParameter)
                    .showNonprinting(showNonprinting)
                    .standardInput(System.in)
                    .build()
            ).concatenate(cwd, files);
        System.out.print(output);
        return 0;
    }

}
