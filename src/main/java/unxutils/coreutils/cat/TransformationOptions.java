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
        Boolean showNonPrinting
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
        return TransformationOptions
            .builder()
            .numberNonBlank(options.numberNonBlank())
            .showEnds(showEnds)
            .number(options.number())
            .squeezeBlank(options.squeezeBlank())
            .showTabs(showTabs)
            .showNonPrinting(showNonprinting)
            .build();
    }

}
