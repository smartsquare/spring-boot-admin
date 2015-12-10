package de.codecentric.boot.admin.notify;

import de.codecentric.boot.admin.config.EnableAdminServer;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Lukas Taake
 *         Created on 10.12.15.
 */
@RunWith( SpringJUnit4ClassRunner.class )
@SpringApplicationConfiguration( classes = SlackNotifierIntegrationTest.TestAdminApplication.class )
@WebIntegrationTest( { "server.port=0", "spring.cloud.config.enabled=false" } )
@TestPropertySource( properties = { "spring.boot.admin.notify.slack.token=TEST_TOKEN",
        "spring.boot.admin.notify.slack.channel=C03713LCG" } )
@Ignore
public class SlackNotifierIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    private SlackNotifier notifier;

    @Before
    public void setup() {
        notifier = applicationContext.getBean( SlackNotifier.class );
    }

    @Test
    public void test() {
        notifier.onClientApplicationStatusChanged( new ClientApplicationStatusChangedEvent(
                Application.create( "App" ).withId( "-id-" ).withHealthUrl( "http://health" ).build(),
                StatusInfo.ofDown(), StatusInfo.ofUp() ) );
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableAdminServer
    public static class TestAdminApplication {
    }
}
