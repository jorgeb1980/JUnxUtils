package unxutils.coreutils.cat;

import test.Sandbox;
import test.sandbox.SandboxTest;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestConcatenateService {

    @SandboxTest
    public void testSingleFile(Sandbox sandbox) throws Exception {
        var lalala = sandbox.copyResource("cat/lalala.txt", "lalala.txt");
        var service = new ConcatenateService(ConcatenateOptions.builder().build());

        var output = service.concatenate(sandbox.getSandbox().toPath(), List.of("lalala.txt"));
        assertEquals(Files.readString(lalala.toPath()), output);
    }

    @SandboxTest
    public void testMultipleFiles(Sandbox sandbox) throws Exception {
        var lalala = sandbox.copyResource("cat/lalala.txt", "lalala.txt");
        var lerele = sandbox.copyResource("cat/lerele.txt", "lerele.txt");
        var trololo = sandbox.copyResource("cat/trololo.txt", "trololo.txt");
        var service = new ConcatenateService(ConcatenateOptions.builder().build());

        var output = service.concatenate(sandbox.getSandbox().toPath(), List.of("lalala.txt", "lerele.txt",  "trololo.txt"));
        assertEquals(
            Files.readString(lalala.toPath()) + Files.readString(lerele.toPath()) + Files.readString(trololo.toPath()),
            output
        );
    }

    @SandboxTest
    public void testWithStandardInput(Sandbox sandbox) throws Exception {
        var lalala = sandbox.copyResource("cat/lalala.txt", "lalala.txt");
        var FAKE_TEXT = "fakeStandardInput";
        var fakedStandardInput = new ByteArrayInputStream(FAKE_TEXT.getBytes(StandardCharsets.UTF_8));
        var lerele = sandbox.copyResource("cat/lerele.txt", "lerele.txt");
        var service = new ConcatenateService(ConcatenateOptions.builder().standardInput(fakedStandardInput).build());

        var output = service.concatenate(sandbox.getSandbox().toPath(), List.of("lalala.txt", "-", "lerele.txt"));
        assertEquals(
            Files.readString(lalala.toPath()) + FAKE_TEXT + Files.readString(lerele.toPath()),
            output
        );
    }

    @SandboxTest
    public void testWithGaps(Sandbox sandbox) throws Exception {
        var withGaps = sandbox.copyResource("cat/with_gaps.txt", "with_gaps.txt");
        var service = new ConcatenateService(ConcatenateOptions.builder().build());
        var output = service.concatenate(sandbox.getSandbox().toPath(), List.of("with_gaps.txt"));

        assertEquals(Files.readString(withGaps.toPath()), output);

        var serviceSqueezing = new ConcatenateService(ConcatenateOptions.builder().squeezeBlank(true).build());
        var outputSqueezing = serviceSqueezing.concatenate(sandbox.getSandbox().toPath(), List.of("with_gaps.txt"));
        var withGapsSqueezed = sandbox.copyResource("cat/with_gaps_squeezed.txt", "with_gaps_squeezed.txt");
        assertEquals(Files.readString(withGapsSqueezed.toPath()), outputSqueezing);
    }

    @SandboxTest
    public void testNumberLines(Sandbox sandbox) throws Exception {
        sandbox.copyResource("cat/with_gaps.txt", "with_gaps.txt");
        var service = new ConcatenateService(ConcatenateOptions.builder().number(true).build());
        var output = service.concatenate(sandbox.getSandbox().toPath(), List.of("with_gaps.txt"));

        var withGapsNumbered = sandbox.copyResource("cat/with_gaps_numbered.txt", "with_gaps_numbered.txt");
        assertEquals(Files.readString(withGapsNumbered.toPath()), output);

        var serviceNonBlank = new ConcatenateService(ConcatenateOptions.builder().number(true).numberNonBlank(true).build());
        var outputNonblank = serviceNonBlank.concatenate(sandbox.getSandbox().toPath(), List.of("with_gaps.txt"));

        var withGapsNumberedNonblank = sandbox.copyResource("cat/with_gaps_numbered_nonblank.txt", "with_gaps_numbered_nonblank.txt");
        assertEquals(Files.readString(withGapsNumberedNonblank.toPath()), outputNonblank);
    }

    @SandboxTest
    public void testTabs(Sandbox sandbox) throws Exception {
        sandbox.copyResource("cat/with_tabs.txt", "with_tabs.txt");
        var service = new ConcatenateService(ConcatenateOptions.builder().showTabs(true).build());
        var output = service.concatenate(sandbox.getSandbox().toPath(), List.of("with_tabs.txt"));

        var withTabsProcessed = sandbox.copyResource("cat/with_tabs_processed.txt", "with_tabs_processed.txt");
        assertEquals(Files.readString(withTabsProcessed.toPath()), output);
    }

    @SandboxTest
    public void testShowEnds(Sandbox sandbox) throws Exception {
        var service = new ConcatenateService(ConcatenateOptions.builder().showEnds(true).build());
        var lalala = sandbox.copyResource("cat/lalala.txt", "lalala.txt");
        assertEquals(Files.readString(lalala.toPath()), service.concatenate(sandbox.getSandbox().toPath(), List.of("lalala.txt")));
    }
}
