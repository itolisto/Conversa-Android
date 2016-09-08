package ee.app.conversa.events;

/**
 * Created by edgargomez on 9/6/16.
 */
public class RefreshEvent {

    private final boolean refresh;

    public RefreshEvent(boolean refresh) {
        this.refresh = refresh;
    }

    public boolean isRefresh() {
        return refresh;
    }

}
