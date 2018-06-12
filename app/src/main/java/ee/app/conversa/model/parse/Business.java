package ee.app.conversa.model.parse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by edgargomez on 4/15/16.
 */
public class Business {

    private String displayName;
    private String conversaID;
    private String avatar;

    public Business() {}

    public String getDisplayName() {
        return displayName;
    }

    public String getConversaID() {
        return conversaID;
    }

    public String getAvatar() {
        return avatar;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("displayName", this.displayName);
        data.put("conversaID", this.conversaID);
        data.put("avatar", this.avatar);
        return data;
    }

}