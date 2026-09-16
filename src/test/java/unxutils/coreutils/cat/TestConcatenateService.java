package unxutils.coreutils.cat;

import org.junit.jupiter.api.Assertions;
import test.Sandbox;
import test.sandbox.SandboxTest;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class TestConcatenateService {

    @SandboxTest
    public void testSingleFile(Sandbox sandbox) throws Exception {
        var lalala = sandbox.copyResource("cat/lalala.txt", "lalala.txt");
        var service = new ConcatenateService(ConcatenateOptions.builder().build());

        var output = service.concatenate(sandbox.getSandbox().toPath(), List.of("lalala.txt"));
        Assertions.assertEquals(Files.readString(lalala.toPath()), output);
    }

    @SandboxTest
    public void testMultipleFiles(Sandbox sandbox) throws Exception {
        var lalala = sandbox.copyResource("cat/lalala.txt", "lalala.txt");
        var lerele = sandbox.copyResource("cat/lerele.txt", "lerele.txt");
        var trololo = sandbox.copyResource("cat/trololo.txt", "trololo.txt");
        var service = new ConcatenateService(ConcatenateOptions.builder().build());

        var output = service.concatenate(sandbox.getSandbox().toPath(), List.of("lalala.txt", "lerele.txt",  "trololo.txt"));
        Assertions.assertEquals(
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
        Assertions.assertEquals(
            Files.readString(lalala.toPath()) + FAKE_TEXT + Files.readString(lerele.toPath()),
            output
        );
    }
}
