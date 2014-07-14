package hudson.plugins.sauce_ondemand;

import com.saucelabs.ci.sauceconnect.AbstractSauceTunnelManager;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

public class TunnelHolderTest {


    @Test
    public void constructor_should_store_parameters(){
        AbstractSauceTunnelManager tunnelManager = Mockito.mock(AbstractSauceTunnelManager.class);
        String username = RandomStringUtils.random(10);
        String options = "--tunnel-identifier " + RandomStringUtils.random(25);
        SauceOnDemandBuildWrapper.TunnelHolder holder = new SauceOnDemandBuildWrapper.TunnelHolder(tunnelManager, username, options);

        assertEquals(tunnelManager, holder.tunnelManager);
        assertEquals(username, holder.username);
        assertEquals(options, holder.options);
    }

}