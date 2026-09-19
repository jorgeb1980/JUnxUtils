package unxutils.coreutils.cat;

import lombok.Builder;

import java.io.InputStream;

import static java.lang.Boolean.FALSE;

@Builder
public record ConcatenateOptions(
    Boolean showAll,
    Boolean numberNonBlank,
    Boolean showControlAndEnds,
    Boolean showEnds,
    Boolean number,
    Boolean squeezeBlank,
    Boolean showControlAndTabs,
    Boolean showTabs,
    Boolean ignoredParameter,
    Boolean showNonprinting,
    InputStream standardInput
) {
    public ConcatenateOptions {
        if (showAll == null) showAll = FALSE;
        if (numberNonBlank == null) numberNonBlank = FALSE;
        if (showControlAndEnds == null) showControlAndEnds = FALSE;
        if (showEnds == null) showEnds = FALSE;
        if (number == null) number = FALSE;
        if (squeezeBlank == null) squeezeBlank = FALSE;
        if (showControlAndTabs == null) showControlAndTabs = FALSE;
        if (showTabs == null) showTabs = FALSE;
        if (ignoredParameter == null) ignoredParameter = FALSE;
        if (showNonprinting == null) showNonprinting = FALSE;
    }
}
