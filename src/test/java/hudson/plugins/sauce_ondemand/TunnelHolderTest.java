package hudson.plugins.sauce_ondemand;

import com.saucelabs.ci.sauceconnect.AbstractSauceTunnelManager;
import hudson.model.TaskListener;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.PrintStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TunnelHolderTest {


    @Test
    public void constructor_should_store_parameters(){
        AbstractSauceTunnelManager tunnelManager = Mockito.mock(AbstractSauceTunnelManager.class);
        String username = anyUsername();
        String options = anyOptionsWithTunnelIdentifier();
        SauceOnDemandBuildWrapper.TunnelHolder holder = new SauceOnDemandBuildWrapper.TunnelHolder(tunnelManager, username, options);

        assertEquals(tunnelManager, holder.tunnelManager);
        assertEquals(username, holder.username);
        assertEquals(options, holder.options);
    }

    @Test
    public void close_should_invoke_closeTunnelsForPlan_on_tunnelManager(){
        AbstractSauceTunnelManager tunnelManager = Mockito.mock(AbstractSauceTunnelManager.class);
        String username = anyUsername();
        String options = anyOptionsWithTunnelIdentifier();

        SauceOnDemandBuildWrapper.TunnelHolder holder = new SauceOnDemandBuildWrapper.TunnelHolder(tunnelManager, username, options);

        TaskListener taskListener = Mockito.mock(TaskListener.class);

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