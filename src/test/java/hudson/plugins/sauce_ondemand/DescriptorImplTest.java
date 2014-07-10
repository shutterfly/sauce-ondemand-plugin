package hudson.plugins.sauce_ondemand;

import hudson.util.FormValidation;
import org.junit.Test;

import java.io.File;
import java.util.Random;

import static org.junit.Assert.*;

@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class DescriptorImplTest {

    private final Random random = new Random(System.currentTimeMillis());

    @Test
    public void doCheckSauceConnectDirectory_should_return_ok_when_working_directory_is_blank()
        throws Exception {
        PluginImpl.DescriptorImpl descriptor = new PluginImpl.DescriptorImpl();

        assertEquals(FormValidation.ok(), descriptor.doCheckSauceConnectDirectory(""));
    }

    @Test
    public void doCheckSauceConnectDirectory_should_return_ok_when_working_directory_is_writable()
        throws Exception {
        File tempDir = getTempDirectory();

        assertTrue(tempDir.isDirectory());
        assertTrue(tempDir.canWrite());

        PluginImpl.DescriptorImpl descriptor = new PluginImpl.DescriptorImpl();
        FormValidation validationResult = descriptor.doCheckSauceConnectDirectory(tempDir.getCanonicalPath());

        assertEquals(FormValidation.ok(), validationResult);
    }

    private File getTempDirectory() {
        String tempDirLocation = System.getProperty("java.io.tmpdir");
        return new File(tempDirLocation);
    }

    @Test
    public void doCheckSauceConnectDirectory_should_return_error_when_directory_does_not_exist() throws Exception {

        PluginImpl.DescriptorImpl descriptor = new PluginImpl.DescriptorImpl();
        File tempDir = getTempDirectory();

        String nonExistentDirLocation = tempDir.getCanonicalPath() + File.pathSeparator + "should-not-exist-" + random.nextInt();
        FormValidation validationResult = descriptor.doCheckSauceConnectDirectory(nonExistentDirLocation);

        assertEquals(FormValidation.Kind.ERROR, validationResult.kind);
        String message = validationResult.getMessage();
        assertTrue(message.startsWith(PluginImpl.DescriptorImpl.BASE_MSG_INVALID_WORKING_DIR));
        assertTrue(message.contains(nonExistentDirLocation));
        assertTrue(message.contains("is not a directory"));
    }

    @Test
    public void doCheckSauceConnectDirectory_should_return_error_when_directory_is_not_writable()
        throws Exception {

        if ("root".equals(System.getProperty("user.name"))) {
            System.out.println("warning, skipping writable-directory validation test " +
                "because root can always write to the directory");
        } else {
            PluginImpl.DescriptorImpl descriptor = new PluginImpl.DescriptorImpl();
            File tempDir = getTempDirectory();

            String newDirLocation = tempDir.getCanonicalPath() + File.pathSeparator + "should-not-exist-" + random.nextInt();
            File newDir = new File(newDirLocation);
            assertTrue("could not create dir: " + newDirLocation, newDir.mkdir());
            assertTrue("could not make dir read-only: " + newDirLocation, newDir.setWritable(false));

            assertTrue(newDir.isDirectory());
            assertFalse(newDir.canWrite()); //canWrite will always return true as root

            FormValidation validationResult = descriptor.doCheckSauceConnectDirectory(newDirLocation);

            assertEquals(FormValidation.Kind.ERROR, validationResult.kind);
            String message = validationResult.getMessage();
            assertTrue(message.startsWith(PluginImpl.DescriptorImpl.BASE_MSG_INVALID_WORKING_DIR));
            assertTrue(message.contains(newDirLocation));
            assertTrue(message, message.contains("is not writable"));
        }
    }
}