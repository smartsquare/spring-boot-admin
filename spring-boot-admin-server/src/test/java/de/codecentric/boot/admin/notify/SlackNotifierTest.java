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
    private ObjectMapper jsonParser;

    @Captor
    private ArgumentCaptor<String> requestCaptor;

    private SlackNotifier notifier;

    @Before
    public void setup() {
        when( settings.getToken() ).thenReturn( TOKEN );
        when( settings.getChannel() ).thenReturn( CHANNEL );
        when( spelExpressionParser.parseExpression( anyString(), any( ParserContext.class ) ) ).thenReturn( text );
        notifier = new SlackNotifier( settings, template, spelExpressionParser, jsonParser );
    }

    @Test
    public void should_use_correct_slack_settings() {
        notifier.onClientApplicationStatusChanged( new ClientApplicationStatusChangedEvent(
                Application.create( "App" ).withId( "-id-" ).withHealthUrl( "http://health" ).build(),
                StatusInfo.ofDown(), StatusInfo.ofUp() ) );

        verify( template ).getForEntity( requestCaptor.capture(), eq( String.class ) );
        assertThat( requestCaptor.getValue(), allOf( containsString( TOKEN ), containsString( CHANNEL ) ) );
    }
}
