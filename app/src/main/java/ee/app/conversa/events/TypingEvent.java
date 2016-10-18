package ee.app.conversa.events;

/**
 * Created by edgargomez on 10/3/16.
 */

public class TypingEvent {

    private final String from;
    private final boolean typing;

    public TypingEvent(String from, boolean typing) {
        this.from = from;
        this.typing = typing;
    }

    public String getFrom() {
        return from;
    }

    public boolean isTyping() {
        return typing;
    }

}
