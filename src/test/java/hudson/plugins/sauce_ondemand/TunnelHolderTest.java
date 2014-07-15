package hudson.plugins.sauce_ondemand;

import com.saucelabs.ci.sauceconnect.AbstractSauceTunnelManager;
import hudson.model.TaskListener;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TunnelHolderTest {

    @Test
    public void constructor_should_store_parameters(){
        SauceOnDemandBuildWrapper.TunnelManagerFactory tunnelManagerFactory = new SauceOnDemandBuildWrapper.TunnelManagerFactory(false);
        String username = anyUsername();
        String options = anyOptionsWithTunnelIdentifier();
        SauceOnDemandBuildWrapper.TunnelHolder holder = new SauceOnDemandBuildWrapper.TunnelHolder(tunnelManagerFactory, username, options);

        assertEquals(tunnelManagerFactory, holder.tunnelManagerFactory);
        assertEquals(username, holder.username);
        assertEquals(options, holder.options);
    }

    @Test
    public void close_should_invoke_closeTunnelsForPlan_on_tunnelManager() throws Exception {
        AbstractSauceTunnelManager tunnelManager = mock(AbstractSauceTunnelManager.class);
        SauceOnDemandBuildWrapper.TunnelManagerFactory tunnelManagerFactory = mock(SauceOnDemandBuildWrapper.TunnelManagerFactory.class);
        when(tunnelManagerFactory.getSauceTunnelManager()).thenReturn(tunnelManager);

        String username = anyUsername();
        String options = anyOptionsWithTunnelIdentifier();

        SauceOnDemandBuildWrapper.TunnelHolder holder = new SauceOnDemandBuildWrapper.TunnelHolder(tunnelManagerFactory, username, options);

        TaskListener taskListener = mock(TaskListener.class);

        PrintStream printStream = new PrintStream(System.out);
        when(taskListener.getLogger()).thenReturn(printStream);

        holder.close(taskListener);

        verify(tunnelManager).closeTunnelsForPlan(username, options, printStream);
    }

    private String anyOptionsWithTunnelIdentifier() {
        return "--tunnel-identifier " + RandomStringUtils.random(25);
    }

    private String anyUsername() {
        return RandomStringUtils.random(10);
    }

}