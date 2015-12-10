package de.codecentric.boot.admin.notify;

import java.io.IOException;

/**
 * @author Lukas Taake
 *         Created on 10.12.15.
 */
public class SlackException
        extends IOException {

    public SlackException( String msg ) {
        super( msg );
    }
}
