package de.codecentric.boot.admin.notify;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;

/**
 * @author Lukas Taake Created on 10.12.15.
 */
public class SlackNotifier
    extends AbstractNotifier {

    private final static String DEFAULT_TEXT =
        "#{application.name} (#{application.id})\nstatus changed from *#{from.status}* to *#{to.status}*\n\n#{application.healthUrl}";

    private static final String SLACK_MSG = "https://slack.com/api/chat.postMessage";

    private String token;

    private String channel;

    private final RestTemplate template;

    private final SpelExpressionParser spelExpressionParser;

    private final Expression text;

    private final ObjectMapper jsonParser;

    public SlackNotifier( RestTemplate template, SpelExpressionParser spelExpressionParser,
                          ObjectMapper jsonParser ) {
        this.template = template;
        this.spelExpressionParser = spelExpressionParser;
        this.jsonParser = jsonParser;

        this.text = spelExpressionParser.parseExpression( DEFAULT_TEXT, ParserContext.TEMPLATE_EXPRESSION );
    }

    @Override
    protected void notify( final ClientApplicationStatusChangedEvent event )
        throws Exception {
        EvaluationContext context = new StandardEvaluationContext( event );
        StringBuilder requestParams = new StringBuilder( "?token=" + token );
        requestParams.append( "&channel=" + channel );
        requestParams.append( "&text=" );
        requestParams.append( text.getValue( context, String.class ) );

        ResponseEntity<String> response = template.getForEntity( SLACK_MSG + requestParams, String.class );

        JsonNode jsonNode = jsonParser.readTree( response.getBody() );

        if ( !jsonNode.get( "ok" ).asBoolean() ) {
            throw new SlackException( jsonNode.get( "error" ).asText() );
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken( String token ) {
        this.token = token;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel( String channel ) {
        this.channel = channel;
    }
}
