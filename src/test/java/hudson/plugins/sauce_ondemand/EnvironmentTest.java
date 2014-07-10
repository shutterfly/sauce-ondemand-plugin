package hudson.plugins.sauce_ondemand;

import org.junit.Test;

import java.util.Map;

public class EnvironmentTest {

    @Test
    public void list_environment_vars() {
        System.out.println("Listing Environment Variables");
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            System.out.println(String.format("%s:%s", entry.getKey(), entry.getValue()));
        }
    }

    @Test
    public void list_system_properties() {
        System.out.println("Listing System Properties");

        System.out.println("user.home: " + System.getProperty("user.home"));

        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            System.out.println(String.format("%s:%s", entry.getKey(), entry.getValue()));
        }

    }
}
