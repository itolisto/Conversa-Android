package ee.app.conversa.model.database;

/**
 * Created by edgargomez on 9/20/16.
 */

public class dbSearch {

    private final long _ID;
    private final String mBusinessId;
    private final String mDisplayName;
    private final String mConversaId;
    private final String mAvatarUrl;

    public dbSearch(long _ID, String mBusinessId, String mDisplayName, String mConversaId,
                    String mAvatarUrl){
        this._ID = _ID;
        this.mBusinessId = mBusinessId;
        this.mDisplayName = mDisplayName;
        this.mConversaId = mConversaId;
        this.mAvatarUrl = mAvatarUrl;
    }

    public String getBusinessId() {
        return mBusinessId;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public String getConversaId() {
        return mConversaId;
    }

    public String getAvatarUrl() {
        return mAvatarUrl;
    }

}
