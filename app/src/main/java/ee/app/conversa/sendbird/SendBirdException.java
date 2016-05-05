package ee.app.conversa.sendbird;

/**
 * Created by edgargomez on 4/21/16.
 */
public class SendBirdException extends Exception{

    private static final long serialVersionUID = 1997753363232807009L;

    public SendBirdException() {
    }

    public SendBirdException(String message) {
        super(message);
    }

    public SendBirdException(Throwable cause) {
        super(cause);
    }

    public SendBirdException(String message, Throwable cause) {
        super(message, cause);
    }

}
