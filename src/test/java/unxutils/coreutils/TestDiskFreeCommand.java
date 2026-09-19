package unxutils.coreutils;

import org.junit.jupiter.api.Assertions;
import test.Sandbox;
import test.sandbox.SandboxTest;

public class TestDiskFreeCommand {

    private void testCommand(Sandbox sandbox, FreeDiskSpaceCommand command) throws Exception {
        int ret = command.execute(sandbox.getSandbox().toPath());
        Assertions.assertEquals(0, ret);
    }

    @SandboxTest
    public void testDiskFreeCommandVanilla(Sandbox sandbox) throws Exception {
        testCommand(sandbox, new FreeDiskSpaceCommand());
    }

    @SandboxTest
    public void testDiskFreeCommandHumanReadable(Sandbox sandbox) throws Exception {
        var command = new FreeDiskSpaceCommand();
        command.setHumanReadable(true);
        testCommand(sandbox, command);
    }

    @SandboxTest
    public void testDiskFreeCommandHumanWithPrintType(Sandbox sandbox) throws Exception {
        var command = new FreeDiskSpaceCommand();
        command.setPrintType(true);
        testCommand(sandbox, command);
    }

    @SandboxTest
    public void testDiskFreeCommandHumanWithAllOptions(Sandbox sandbox) throws Exception {
        var command = new FreeDiskSpaceCommand();
        command.setPrintType(true);
        command.setHumanReadable(true);
        testCommand(sandbox, command);
    }
}
