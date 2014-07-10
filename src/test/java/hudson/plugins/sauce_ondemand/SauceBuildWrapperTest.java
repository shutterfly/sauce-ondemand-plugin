package hudson.plugins.sauce_ondemand;

import com.saucelabs.ci.sauceconnect.SauceConnectFourManager;
import com.saucelabs.ci.sauceconnect.SauceConnectTwoManager;
import com.saucelabs.ci.sauceconnect.SauceTunnelManager;
import com.saucelabs.hudson.HudsonSauceManagerFactory;
import com.saucelabs.saucerest.SauceREST;
import hudson.Launcher;
import hudson.model.*;
import hudson.model.queue.QueueTaskFuture;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.tasks.junit.TestDataPublisher;
import hudson.util.DescribableList;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

/**
 * @author Ross Rowe
 */
@Ignore("Ignore test because there is a missing dependency resulting in ClassNotFoundErrors for QueueItemAuthenticatorDescriptor and CryptoConfidentialKey")
public class SauceBuildWrapperTest {

    /**
     * JUnit rule which instantiates a local Jenkins instance with our plugin installed.
     */
    @Rule
    public transient JenkinsRule jenkinsRule = new JenkinsRule();

    /**
     * Dummy credentials to be used by the test.
     */
    private Credentials sauceCredentials = new Credentials("username", "access key");

    /**
     * Mockito spy of the Sauce REST instance, used to capture REST requests without sending them to Sauce Labs.
     */
    private JenkinsSauceREST spySauceRest;

    /**
     * Map of data sent over REST, keyed on Sauce Job id.
     */
    private HashMap<String, Map> restUpdates = new HashMap<String, Map>();

    private static final String DEFAULT_SESSION_ID = "0123345abc";

    public static final String DEFAULT_TEST_XML = "/hudson/plugins/sauce_ondemand/test-result.xml";

    private String currentSessionId = DEFAULT_SESSION_ID;

    private String currentTestResultFile = DEFAULT_TEST_XML;

    @Before
    public void setUp() throws Exception {

        JenkinsSauceREST sauceRest = new JenkinsSauceREST("username", "access key");
        //create a Mockito spy of the sauceREST instance, to capture REST updates sent by the tests
        spySauceRest = spy(sauceRest);
        restUpdates = new HashMap<String, Map>();
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                restUpdates.put((String) args[0], (Map) args[1]);
                return null;
            }
        }).when(spySauceRest).updateJobInfo(anyString(), any(HashMap.class));

        //store dummy implementations of Sauce Connect managers within Plexus container
        HudsonSauceManagerFactory.getInstance().start();
        SauceConnectFourManager sauceConnectFourManager = new SauceConnectFourManager() {
            @Override
            public Process openConnection(String username, String apiKey, int port, File sauceConnectJar, String options, String httpsProtocol, PrintStream printStream, Boolean verboseLogging) throws SauceConnectException {
                return null;
            }
        };

        SauceConnectTwoManager sauceConnectTwoManager = new SauceConnectTwoManager() {
            @Override
            public Process openConnection(String username, String apiKey, int port, File sauceConnectJar, String options, String httpsProtocol, PrintStream printStream, Boolean verboseLogging) throws SauceConnectException {
                return null;
            }
        };
        HudsonSauceManagerFactory.getInstance().getContainer().addComponent(sauceConnectFourManager, SauceConnectFourManager.class.getName());
        HudsonSauceManagerFactory.getInstance().getContainer().addComponent(sauceConnectTwoManager, SauceTunnelManager.class.getName());

    }

    /**
     * Verifies that environment variables are resolved for the Sauce Connect options.
     *
     * @throws Exception
     */
    @Test
    public void resolveVariables() throws Exception {
        SauceConnectFourManager sauceConnectFourManager = new SauceConnectFourManager() {
            @Override
            public Process openConnection(String username, String apiKey, int port, File sauceConnectJar, String options, String httpsProtocol, PrintStream printStream, Boolean verboseLogging) throws SauceConnectException {
                assertTrue("Variable not resolved", options.equals("-i 1"));
                return null;
            }
        };
        HudsonSauceManagerFactory.getInstance().getContainer().addComponent(sauceConnectFourManager, SauceConnectFourManager.class.getName());
        Credentials sauceCredentials = new Credentials("username", "access key");
        SeleniumInformation seleniumInformation = new SeleniumInformation("webDriver", null, null, null, null);
        SauceOnDemandBuildWrapper sauceBuildWrapper =
                new SauceOnDemandBuildWrapper(
                        sauceCredentials,
                        seleniumInformation,
                        null,
                        null,
                        null,
                        "-i ${BUILD_NUMBER}",
                        null,
                        true,
                        true,
                        false,
                        true);

        runFreestyleBuild(sauceBuildWrapper);


        //assert that the Sauce REST API was invoked for the Sauce job id
        assertNotNull(restUpdates.get(currentSessionId));
        //TODO verify that test results of build include Sauce results

    }

    /**
     * Simulates the handling of a Sauce Connect time out.
     *
     * @throws Exception
     */
    @Test
    public void sauceConnectTimeOut() throws Exception {
        SauceConnectFourManager sauceConnectFourManager = new SauceConnectFourManager() {
            @Override
            public Process openConnection(String username, String apiKey, int port, File sauceConnectJar, String options, String httpsProtocol, PrintStream printStream, Boolean verboseLogging) throws SauceConnectException {
                throw new SauceConnectDidNotStartException("Sauce Connect failed to start");
            }
        };
        HudsonSauceManagerFactory.getInstance().getContainer().addComponent(sauceConnectFourManager, SauceConnectFourManager.class.getName());
        Credentials sauceCredentials = new Credentials("username", "access key");
        SeleniumInformation seleniumInformation = new SeleniumInformation("webDriver", null, null, null, null);
        SauceOnDemandBuildWrapper sauceBuildWrapper =
                new SauceOnDemandBuildWrapper(
                        sauceCredentials,
                        seleniumInformation,
                        null,
                        null,
                        null,
                        null,
                        null,
                        true,
                        true,
                        false,
                        true);

        FreeStyleBuild build = runFreestyleBuild(sauceBuildWrapper);
        assertEquals(Result.FAILURE, build.getResult());

    }

    /**
     * Simulates the running of a build with Sauce Connect v4.
     *
     * @throws Exception thrown if an unexpected error occurs
     */
    @Test
    public void runSauceConnectVersion4() throws Exception {
        Credentials sauceCredentials = new Credentials("username", "access key");
        SeleniumInformation seleniumInformation = new SeleniumInformation("webDriver", null, null, Arrays.asList("androidandroid4.3."), null);
        SauceOnDemandBuildWrapper sauceBuildWrapper =
                new SauceOnDemandBuildWrapper(
                        sauceCredentials,
                        seleniumInformation,
                        null,
                        null,
                        null,
                        null,
                        null,
                        true,
                        true,
                        false,
                        true);

        runFreestyleBuild(sauceBuildWrapper);

        //assert that the Sauce REST API was invoked for the Sauce job id
        assertNotNull(restUpdates.get(currentSessionId));
        //TODO verify that test results of build include Sauce results
        //assert that mock SC stopped
//        verify(sauceConnectFourManager.closeTunnelsForPlan(anyString(), anyString(), any(PrintStream.class)));


    }

    /**
     * Simulates the running of a build with Sauce Connect v3.
     *
     * @throws Exception thrown if an unexpected error occurs
     */
    @Test
    public void runSauceConnectVersion3() throws Exception {

        SeleniumInformation seleniumInformation = new SeleniumInformation("webDriver", null, null, null, null);
        SauceOnDemandBuildWrapper sauceBuildWrapper =
                new SauceOnDemandBuildWrapper(
                        sauceCredentials,
                        seleniumInformation,
                        null,
                        null,
                        null,
                        null,
                        null,
                        true,
                        true,
                        true,
                        true);

        runFreestyleBuild(sauceBuildWrapper);

        //assert that the Sauce REST API was invoked for the Sauce job id
        assertNotNull(restUpdates.get(currentSessionId));
        //TODO verify that test results of build include Sauce results

    }

    /**
     * @throws Exception thrown if an unexpected error occurs
     */
    @Test
    public void multipleBrowsers() throws Exception {

        SeleniumInformation seleniumInformation = new SeleniumInformation("webDriver", null, null, Arrays.asList("", ""), null);
        SauceOnDemandBuildWrapper sauceBuildWrapper =
                new SauceOnDemandBuildWrapper(
                        sauceCredentials,
                        seleniumInformation,
                        null,
                        null,
                        null,
                        null,
                        null,
                        true,
                        true,
                        true,
                        true);

        SauceBuilder sauceBuilder = new SauceBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                //verify that SAUCE_ environment variables are populated
                Map<String, String> envVars = build.getEnvironment(listener);
                assertNotNull("Environment variable not found", envVars.get("SAUCE_ONDEMAND_BROWSERS"));
                return super.perform(build, launcher, listener);
            }
        };

        runFreestyleBuild(sauceBuildWrapper, sauceBuilder);

        //assert that the Sauce REST API was invoked for the Sauce job id
        assertNotNull(restUpdates.get(currentSessionId));
        //TODO verify that test results of build include Sauce results

    }

    private FreeStyleBuild runFreestyleBuild(SauceOnDemandBuildWrapper sauceBuildWrapper) throws Exception {
        return runFreestyleBuild(sauceBuildWrapper, new SauceBuilder());
    }

    private FreeStyleBuild runFreestyleBuild(SauceOnDemandBuildWrapper sauceBuildWrapper, TestBuilder builder) throws Exception {

        FreeStyleProject freeStyleProject = jenkinsRule.createFreeStyleProject();
        freeStyleProject.getBuildWrappersList().add(sauceBuildWrapper);
        freeStyleProject.getBuildersList().add(builder);
        SauceOnDemandReportPublisher publisher = new SauceOnDemandReportPublisher() {
            @Override
            protected SauceREST getSauceREST(SauceOnDemandBuildAction buildAction) {
                return spySauceRest;
            }
        };
        DescribableList<TestDataPublisher, Descriptor<TestDataPublisher>> testDataPublishers = new DescribableList<TestDataPublisher, Descriptor<TestDataPublisher>>(freeStyleProject);
        testDataPublishers.add(publisher);
        JUnitResultArchiver resultArchiver = new JUnitResultArchiver("*.xml", false, testDataPublishers);
        freeStyleProject.getPublishersList().add(resultArchiver);
        QueueTaskFuture<FreeStyleBuild> future = freeStyleProject.scheduleBuild2(0);

        FreeStyleBuild build = future.get(1, TimeUnit.MINUTES);
        assertNotNull(build);

        return build;


    }

    /**
     * Dummy builder which is run by the unit tests.
     */
    private class SauceBuilder extends TestBuilder implements Serializable {

        @Override
        public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
            //assert that mock SC started

            Map<String, String> envVars = build.getEnvironment(listener);

            assertEquals("Environment variable not found", sauceCredentials.getUsername(), envVars.get("SAUCE_USER_NAME"));
            assertEquals("Environment variable not found", sauceCredentials.getApiKey(), envVars.get("SAUCE_API_KEY"));

            File destination = new File(build.getWorkspace().getRemote(), "test.xml");
            FileUtils.copyURLToFile(getClass().getResource(currentTestResultFile), destination);
            return true;
        }
    }


}
