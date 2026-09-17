package unxutils.coreutils.cat;

import lombok.Builder;

import java.util.List;

@Builder
public record TransformationOptions(
        Boolean numberNonBlank,
        Boolean showEnds,
        Boolean number,
        Boolean squeezeBlank,
        Boolean showTabs,
        Boolean showNonPrinting,
        Boolean readTextMode
) {

    public static TransformationOptions from(ConcatenateOptions options) {
        var showNonprinting =
            options.showAll()
            || options.showControlAndNumbers()
            || options.showControlAndTabs()
            || options.showNonprinting();
        var showTabs =
            options.showAll()
            || options.showControlAndTabs()
            || options.showTabs();
        var showEnds = options.showAll() || options.showEnds();
        // From the documentation:
        // However, cat reads in text mode if one of the options -bensAE is used or if cat is reading from standard input and
        // standard input is a terminal
        var readTextMode =
                options.showAll()
                || options.number()
                || options.numberNonBlank()
                || options.squeezeBlank()
                || options.showEnds();
        return TransformationOptions
            .builder()
            .numberNonBlank(options.numberNonBlank())
            .showEnds(showEnds)
            .number(options.number())
            .squeezeBlank(options.squeezeBlank())
            .showTabs(showTabs)
            .showNonPrinting(showNonprinting)
            .readTextMode(readTextMode)
            .build();
    }

}
