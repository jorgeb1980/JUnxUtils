package unxutils.coreutils.cat;

import lombok.Builder;

import java.io.InputStream;

@Builder
public record ConcatenateOptions(
    Boolean showAll,
    Boolean numberNonBlank,
    Boolean showControlAndNumbers,
    Boolean showEnds,
    Boolean number,
    Boolean squeezeBlank,
    Boolean showControlAndTabs,
    Boolean showTabs,
    Boolean ignoredParameter,
    Boolean showNonprinting,
    InputStream standardInput
) {
}
