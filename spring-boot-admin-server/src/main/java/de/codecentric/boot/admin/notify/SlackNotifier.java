package de.codecentric.boot.admin.notify;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

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

    private String username = "Spring Boot Admin";

    private String defaultIcon = ":cop:";

    private Map<String, String> icons;

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
        requestParams.append( "&channel=" ).append( channel );
        requestParams.append( "&username=" ).append( username );
        requestParams.append( "&icon_emoji=" ).append( defaultIcon );
        requestParams.append( "&text=" ).append( text.getValue( context, String.class ) );

        // check for emoji_icon mapping for new status
        String newStatus = event.getTo().getStatus();
        if ( icons != null && icons.containsKey( newStatus ) ) {
            requestParams.append( "&icon_emoji=" ).append( icons.get( newStatus ) );
        }

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

    public String getUsername() {
        return username;
    }

    public void setUsername( String username ) {
        this.username = username;
    }

    public Map<String, String> getIcons() {
        return icons;
    }

    public void setIcons( Map<String, String> icons ) {
        this.icons = icons;
    }

    public String getDefaultIcon() {
        return defaultIcon;
    }

    public void setDefaultIcon(String defaultIcon) {
        this.defaultIcon = defaultIcon;
    }
}
