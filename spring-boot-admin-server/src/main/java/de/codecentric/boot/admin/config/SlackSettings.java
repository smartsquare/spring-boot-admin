package de.codecentric.boot.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties( prefix = "spring.boot.admin.notify.slack" )
public class SlackSettings {
    private String token;

    private String channel;

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
