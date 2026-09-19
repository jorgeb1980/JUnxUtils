package unxutils.coreutils;

import org.junit.jupiter.api.Assertions;
import test.CaptureOutput;
import test.Sandbox;
import test.sandbox.SandboxTest;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class TestConcatenateFilesCommand {

    @SandboxTest
    public void testConcatenateFiles(Sandbox sandbox) throws IOException {
        var command = new ConcatenateFilesCommand();
        var lalala = sandbox.copyResource("cat/lalala.txt", "lalala.txt").toPath();
        var trololo = sandbox.copyResource("cat/trololo.txt", "trololo.txt").toPath();
        command.setFiles(List.of(lalala.toFile().getName(), trololo.toAbsolutePath().toString()));
        var ctx = CaptureOutput.captureOutput(() -> command.execute(sandbox.getSandbox().toPath()));
        Assertions.assertEquals(Files.readString(lalala) + Files.readString(trololo), ctx.out());
    }

}
