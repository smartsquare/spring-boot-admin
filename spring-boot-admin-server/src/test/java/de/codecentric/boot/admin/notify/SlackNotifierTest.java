package de.codecentric.boot.admin.notify;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.codecentric.boot.admin.config.SlackSettings;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class SlackNotifierTest {

    private static final String TOKEN = "TOKEN";

    private static final String CHANNEL = "CHANNEL";

    @Mock
    private RestTemplate template;

    @Mock
    private SlackSettings settings;

    @Mock
    private SpelExpressionParser spelExpressionParser;

    @Mock
    private Expression text;

    @Mock
    private ResponseEntity<String> response;

    @Captor
    private ArgumentCaptor<String> requestCaptor;

    private SlackNotifier notifier;

    private ClientApplicationStatusChangedEvent testEvent;

    private ObjectMapper jsonParser = new ObjectMapper();

    @Before
    public void setup() {
        when( settings.getToken() ).thenReturn( TOKEN );
        when( settings.getChannel() ).thenReturn( CHANNEL );
        when( spelExpressionParser.parseExpression( anyString(), any( ParserContext.class ) ) ).thenReturn( text );
        when( response.getBody() ).thenReturn( "{\"ok\":true}" );
        when( template.getForEntity( anyString(), eq( String.class ) ) ).thenReturn( response );
        notifier = new SlackNotifier( settings, template, spelExpressionParser, jsonParser );
        testEvent = new ClientApplicationStatusChangedEvent(
                Application.create( "App" ).withId( "-id-" ).withHealthUrl( "http://health" ).build(),
                StatusInfo.ofDown(), StatusInfo.ofUp() );
    }

    @Test
    public void should_use_correct_slack_settings() {
        notifier.onClientApplicationStatusChanged( testEvent );

        verify( template ).getForEntity( requestCaptor.capture(), eq( String.class ) );
        assertThat( requestCaptor.getValue(), allOf( containsString( TOKEN ), containsString( CHANNEL ) ) );
    }

    @Test( expected = SlackException.class )
    public void should_throw_exception_on_failure()
            throws Exception {
        when( response.getBody() ).thenReturn( "{\"ok\":false,\"error\":\"42\"}" );
        notifier.notify( testEvent );
    }

    @Test
    public void should_not_throw_exception_on_success()
            throws Exception {
        when( settings.getToken() ).thenReturn( "invalidToken" );
        notifier.notify( testEvent );
    }
}
