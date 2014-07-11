package hudson.plugins.sauce_ondemand;

import com.saucelabs.ci.sauceconnect.SauceConnectFourManager;
import com.saucelabs.ci.sauceconnect.SauceConnectTwoManager;
import com.saucelabs.ci.sauceconnect.SauceTunnelManager;
import com.saucelabs.hudson.HudsonSauceManagerFactory;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.util.OneShotEvent;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Ross Rowe
 */
public class SauceBuildWrapperTest {

    /**
     * JUnit rule which instantiates a local Jenkins instance with our plugin installed.
     */
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    //@Mock
    public SauceConnectFourManager sauceConnectFourManager;

    //@Mock
    public SauceConnectTwoManager sauceConnectTwoManager;

    private Credentials sauceCredentials = new Credentials("username", "access key");

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);
        HudsonSauceManagerFactory.getInstance().start();
        sauceConnectFourManager = new SauceConnectFourManager() {
            @Override
            public Process openConnection(String username, String apiKey, int port, File sauceConnectJar, String options, String httpsProtocol, PrintStream printStream, Boolean verboseLogging) throws SauceConnectException {
                return null;
            }
        };

        sauceConnectTwoManager = new SauceConnectTwoManager() {
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
    /*@Test
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
                        true,
                        false);

        runFreestyleBuild(sauceBuildWrapper);


        //assert that the Sauce REST API was invoked for the Sauce job id
        assertNotNull(restUpdates.get(currentSessionId));
        //TODO verify that test results of build include Sauce results

    }*/


    /**
     * Simulates the handling of a Sauce Connect time out.
     *
     * @throws Exception
     */
    @Test(expected=IOException.class)
    @Ignore
    public void sauceConnectTimeOut() throws Exception {
        sauceConnectFourManager = new SauceConnectFourManager() {
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
                        true,
                        false);

        runFreestyleBuild(sauceBuildWrapper);

    }

    /**
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
                        true,
                        false);

        runFreestyleBuild(sauceBuildWrapper);


        //assert that mock SC stopped
//        verify(sauceConnectFourManager.closeTunnelsForPlan(anyString(), anyString(), any(PrintStream.class)));


    }

    /**
     * @throws Exception thrown if an unexpected error occurs
     */
    /*@Test
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
                        true,
                        false);

        runFreestyleBuild(sauceBuildWrapper);

        //assert that the Sauce REST API was invoked for the Sauce job id
        assertNotNull(restUpdates.get(currentSessionId));
        //TODO verify that test results of build include Sauce results

    }*/

    /**
     * @throws Exception thrown if an unexpected error occurs
     */
/*
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
                        true,
                        false);

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

        //assert that mock SC stopped
        //        verify(sauceConnectFourManager.closeTunnelsForPlan(anyString(), anyString(), any(PrintStream.class)));


    }
*/

    private void runFreestyleBuild(SauceOnDemandBuildWrapper sauceBuildWrapper) throws IOException, InterruptedException {
        FreeStyleProject freeStyleProject = jenkinsRule.createFreeStyleProject();
        freeStyleProject.getBuildWrappersList().add(sauceBuildWrapper);
        final OneShotEvent buildStarted = new OneShotEvent();
        freeStyleProject.getBuildersList().add(new TestBuilder() {

            @Override
            public boolean perform(AbstractBuild<?, ?> abstractBuild, Launcher launcher, BuildListener buildListener) throws InterruptedException, IOException {
                //assert that mock SC started

                assertEquals("Environment variable not found", sauceCredentials.getUsername(), abstractBuild.getEnvVars().get("SAUCE_USER_NAME"));
                assertEquals("Environment variable not found", sauceCredentials.getApiKey(), abstractBuild.getEnvVars().get("SAUCE_API_KEY"));
                buildStarted.signal();
                return true;
            }
        });

        freeStyleProject.scheduleBuild2(0);
        buildStarted.block();
    }


}
