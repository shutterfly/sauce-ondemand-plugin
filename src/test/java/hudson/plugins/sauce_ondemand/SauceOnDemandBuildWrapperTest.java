package hudson.plugins.sauce_ondemand;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class SauceOnDemandBuildWrapperTest {

    private final Random random = new Random();

    @Test
    public void constructor_should_store_parameters(){

        for(int i = 0; i < 5; i++){
            Credentials credentials = new Credentials("username", "api-key");
            SeleniumInformation seleniumInformation = null;
            String seleniumHost = anyString();
            String seleniumPort = anyString();
            String protocol = anyString();
            String options = anyString();
            String startingURL = anyString();
            boolean enableSauceConnect = anyBoolean();
            boolean launchSauceConnectOnSlave = anyBoolean();
            boolean useOldSauceConnect = anyBoolean();
            boolean verboseLogging = anyBoolean();
            boolean useGeneratedTunnelIdentifier = anyBoolean();
            SauceOnDemandBuildWrapper wrapper = new SauceOnDemandBuildWrapper(
                credentials,
                seleniumInformation,
                seleniumHost,
                seleniumPort,
                protocol,
                options,
                startingURL,
                enableSauceConnect,
                launchSauceConnectOnSlave,
                useOldSauceConnect,
                verboseLogging,
                useGeneratedTunnelIdentifier
            );


            assertEquals(credentials, wrapper.getCredentials());
            assertEquals(seleniumInformation, wrapper.getSeleniumInformation());
            assertEquals(seleniumHost, wrapper.getSeleniumHost());
            assertEquals(seleniumPort, wrapper.getSeleniumPort());
            assertEquals(options, wrapper.getOptions());
            assertEquals(startingURL, wrapper.getStartingURL());

            assertEquals(enableSauceConnect, wrapper.isEnableSauceConnect());
            assertEquals(launchSauceConnectOnSlave, wrapper.isLaunchSauceConnectOnSlave());
            assertEquals(useOldSauceConnect, wrapper.isUseOldSauceConnect());
            assertEquals(verboseLogging, wrapper.isVerboseLogging());
            assertEquals(useGeneratedTunnelIdentifier, wrapper.isUseGeneratedTunnelIdentifier());
        }

    }

    private boolean anyBoolean() {
        return random.nextBoolean();
    }

    private String anyString(){
        return "str-" + random.nextInt();
    }

}