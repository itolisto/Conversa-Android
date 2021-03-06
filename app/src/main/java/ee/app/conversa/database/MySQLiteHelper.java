package ee.app.conversa.database;

/**
 * Created by edgargomez on 2/11/15.
 *
 * Database is never closed as described in this answer:
 * http://stackoverflow.com/a/7739454/5349296
 *
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.WorkerThread;
import ee.app.conversa.ConversaApp;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.model.database.dbNotificationInformation;
import ee.app.conversa.model.database.dbSearch;
import ee.app.conversa.model.nChatItem;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;
import ee.app.conversa.utils.Utils;

public class MySQLiteHelper {
    
    private final Context context;
    private DatabaseHelper myDbHelper;

    private static final String DATABASE_NAME = "conversadb.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MESSAGES = "message";
    private static final String TABLE_CV_CONTACTS = "cv_contact";
    private static final String TABLE_NOTIFICATION = "notification";
    private static final String TABLE_SEARCH = "search";

    private static final String COLUMN_ID = "_id";

    // MESSAGE
    private static final String sMessageFromUserId = "from_user_id";
    private static final String sMessageToUserId = "to_user_id";
    private static final String sMessageType = "message_type";
    private static final String sMessageDeliveryStatus = "delivery_status";
    private static final String sMessageBody = "body";
    private static final String sMessageLocalUrl = "local_url";
    private static final String sMessageRemoteUrl = "remote_url";
    private static final String sMessageLongitude = "longitude";
    private static final String sMessageLatitude = "latitude";
    private static final String sMessageCreatedAt = "created_at";
    private static final String sMessageViewAt = "view_at";
    private static final String sMessageReadAt = "read_at";
    private static final String sMessageMessageId = "message_id";
    private static final String sMessageWidth = "width";
    private static final String sMessageHeight = "height";
    private static final String sMessageDuration = "duration";
    private static final String sMessageBytes = "bytes";
    private static final String sMessageProgress = "progress";
    private static final String sMessageConversa = "conversa_agent";

    private static final String TABLE_MESSAGES_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_MESSAGES + "("
            + "\"" + COLUMN_ID + "\" INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "\"" + sMessageFromUserId + "\" CHAR(14) NOT NULL, "
            + "\"" + sMessageToUserId + "\" CHAR(14) NOT NULL, "
            + "\"" + sMessageType + "\" CHAR(1) NOT NULL, "
            + "\"" + sMessageDeliveryStatus + "\" INTEGER NOT NULL, "
            + "\"" + sMessageBody + "\" TEXT, "
            + "\"" + sMessageLocalUrl + "\" TEXT, "
            + "\"" + sMessageRemoteUrl + "\" TEXT, "
            + "\"" + sMessageLongitude + "\" REAL DEFAULT 0, "
            + "\"" + sMessageLatitude + "\" REAL DEFAULT 0, "
            + "\"" + sMessageCreatedAt + "\" INTEGER NOT NULL, "
            + "\"" + sMessageViewAt + "\" INTEGER NOT NULL DEFAULT 0, "
            + "\"" + sMessageReadAt + "\" INTEGER NOT NULL DEFAULT 0, "
            + "\"" + sMessageMessageId + "\" CHAR(20), "
            + "\"" + sMessageWidth + "\" INTEGER DEFAULT 0, "
            + "\"" + sMessageHeight + "\" INTEGER DEFAULT 0, "
            + "\"" + sMessageDuration + "\" INTEGER DEFAULT 0, "
            + "\"" + sMessageBytes + "\" INTEGER DEFAULT 0, "
            + "\"" + sMessageProgress + "\" INTEGER DEFAULT 0, "
            + "\"" + sMessageConversa + "\" CHAR(1) DEFAULT 'N');";

    private static final String tmIndex1 = "CREATE INDEX M_search on "  + TABLE_MESSAGES + "(" + sMessageFromUserId + ", " + sMessageToUserId + "); ";
    private static final String tmIndex2 = "CREATE UNIQUE INDEX IF NOT EXISTS C_messageId on "  + TABLE_MESSAGES + "(" + sMessageMessageId + ");";

    // CONTACTS
    private static final String sBusinessBusinessId = "businessId";
    private static final String sBusinessDisplayName = "displayName";
    private static final String sBusinessConversaId = "conversaId";
    private static final String sBusinessRecent = "recent";
    private static final String sBusinessComposingMessage = "composingMessageString";
    private static final String sBusinessAvatarFile = "avatar_file_url";
    private static final String sBusinessBlocked = "blocked";
    private static final String sBusinessMuted = "muted";
    private static final String sBusinessCreatedAt = "created_at";

    private static final String TABLE_CONTACTS_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_CV_CONTACTS + "("
            + "\"" + COLUMN_ID + "\" INTEGER PRIMARY KEY, "
            + "\"" + sBusinessBusinessId + "\" CHAR(14) NOT NULL, "
            + "\"" + sBusinessDisplayName + "\" VARCHAR(180) NOT NULL, "
            + "\"" + sBusinessConversaId + "\" VARCHAR(255) NOT NULL, "
            + "\"" + sBusinessRecent + "\" INTEGER, "
            + "\"" + sBusinessComposingMessage + "\" VARCHAR(255), "
            + "\"" + sBusinessAvatarFile + "\" VARCHAR(355), "
            + "\"" + sBusinessBlocked + "\" CHAR(1) NOT NULL DEFAULT 'N', "
            + "\"" + sBusinessMuted + "\" CHAR(1) NOT NULL DEFAULT 'N', "
            + "\"" + sBusinessCreatedAt + "\" INTEGER NOT NULL);";

    private static final String tcIndex1 = "CREATE UNIQUE INDEX IF NOT EXISTS C_businessId on "  + TABLE_CV_CONTACTS + "(" + sBusinessBusinessId + ");";

    // SEARCHES
    private static final String sSearchBusinessId = "business_id";
    private static final String sSearchDisplayName = "display_name";
    private static final String sSearchConversaId = "conversa_id";
    private static final String sSearchAvatarUrl = "avatar_url";
    private static final String sSearchCreatedAt = "created_at";

    private static final String TABLE_SEARCH_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_SEARCH + "("
            + "\"" + COLUMN_ID + "\" INTEGER PRIMARY KEY, "
            + "\"" + sSearchBusinessId + "\" TEXT NOT NULL, "
            + "\"" + sSearchDisplayName + "\" TEXT NOT NULL, "
            + "\"" + sSearchConversaId + "\" TEXT NOT NULL, "
            + "\"" + sSearchAvatarUrl + "\" TEXT DEFAULT 0, "
            + "\"" + sSearchCreatedAt + "\" INTEGER NOT NULL );";

    // NOTIFICATIONS
    private static final String sNotificationAndroidId = "android_id";
    private static final String sNotificationGroup = "group_id";
    private static final String sNotificationCount = "count";

    private static final String TABLE_NOTIFICATION_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NOTIFICATION + "("
            + "\"" + COLUMN_ID + "\" INTEGER PRIMARY KEY, "
            + "\"" + sNotificationAndroidId + "\" INTEGER NOT NULL, "
            + "\"" + sNotificationGroup + "\" TEXT NOT NULL, "
            + "\"" + sNotificationCount + "\" INTEGER NOT NULL DEFAULT 0);";

    // TRIGGERS
    private static final String NEW_MESSAGE_TRIGGER = "new_message_trigger";
    private static final String newMessageTrigger = "CREATE TRIGGER IF NOT EXISTS " + NEW_MESSAGE_TRIGGER
            + " AFTER INSERT"
            + " ON " + TABLE_MESSAGES
            + " BEGIN "
            + " update " + TABLE_CV_CONTACTS + " set " + sBusinessRecent + " = new." + sMessageCreatedAt
            + " where " + sBusinessBusinessId + " = new." + sMessageFromUserId
            + " or " + sBusinessBusinessId + " = new." + sMessageToUserId + ";"
            + " END;";

    private static final String DELETE_USER_TRIGGER = "delete_user_trigger";
    private static final String deleteUserTrigger = "CREATE TRIGGER IF NOT EXISTS " + DELETE_USER_TRIGGER
            + " AFTER DELETE"
            + " ON " + TABLE_CV_CONTACTS
            + " FOR EACH ROW"
            + " BEGIN "
            + " delete from " + TABLE_MESSAGES + " where " + sMessageFromUserId + " = old." + sBusinessBusinessId
            + " or " + sMessageToUserId + " = old." + sBusinessBusinessId + ";"
            + " END;";

    /************************************************************/
    /**********************DATABASE METHODS**********************/
    /************************************************************/

    public MySQLiteHelper(Context context) {
        this.context = context;
        myDbHelper = new DatabaseHelper(context);
        openDatabase();
    }

    private SQLiteDatabase openDatabase() throws SQLException {
        return myDbHelper.getWritableDatabase();
    }

    public boolean deleteDatabase() {
        deleteAllData();
        return context.deleteDatabase(DATABASE_NAME);
    }

    public void refreshDbHelper() {
        myDbHelper = new DatabaseHelper(context);
    }

    /************************************************************/
    /*********************OPERATIONS METHODS*********************/
    /************************************************************/
    public void saveContact(dbBusiness user) {
        ContentValues contact = new ContentValues();
        contact.put(sBusinessBusinessId, user.getBusinessId());
        contact.put(sBusinessDisplayName, user.getDisplayName());
        contact.put(sBusinessConversaId, user.getConversaId());
        contact.put(sBusinessRecent, user.getRecent());
        contact.put(sBusinessComposingMessage, "");
        contact.put(sBusinessAvatarFile, user.getAvatarThumbFileId());
        contact.put(sBusinessBlocked, "N");
        contact.put(sBusinessMuted, "N");
        contact.put(sBusinessCreatedAt, user.getCreated());

        long result = openDatabase().insert(TABLE_CV_CONTACTS, null, contact);

        if (result > 0) {
            user.setId(result);
        }
    }

    public synchronized int updateContactAvatar(long contactId, String avatarUrl) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(sBusinessAvatarFile, avatarUrl);
        return openDatabase().update(TABLE_CV_CONTACTS, contentValues, COLUMN_ID + " = ?", new String[]{Long.toString(contactId)});
    }

    public List<dbBusiness> getAllContacts() {
        List<dbBusiness> contacts = new ArrayList<>();

        Cursor cursor = openDatabase().query(TABLE_CV_CONTACTS,null,null,null,null,null, sBusinessRecent + " DESC");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            dbBusiness contact = cursorToUser(cursor);
            contacts.add(contact);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();

        return contacts;
    }

    @WorkerThread
    private void deleteAllData() {
        // TODO: if any new folders are added they should be included here
        try {
            File path1 = Utils.getMediaDirectory(context, "images");
            File path2 = Utils.getMediaDirectory(context, "avatars");

            if (path1.exists()) {
                String[] fileNames = path1.list();
                for (String fileName : fileNames) {
                    if (!fileName.equals("lib")) {
                        Utils.deleteFile(new File(path1, fileName));
                    }
                }
            }

            if (path2.exists()) {
                String[] fileNames = path2.list();
                for (String fileName : fileNames) {
                    if (!fileName.equals("lib")) {
                        Utils.deleteFile(new File(path2, fileName));
                    }
                }
            }
        } catch (Exception e) {
            Logger.error("deleteAllData", e.getMessage());
        }
    }

    @WorkerThread
    public void deleteAllDataAssociatedWithUser(String ids, int total) {
        String query = "SELECT " + sBusinessAvatarFile + "," + sBusinessBusinessId + " FROM "
                + TABLE_CV_CONTACTS + " WHERE " + COLUMN_ID + " IN (" + ids + ")";

        Cursor cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToFirst();

        List<String> avatars = new ArrayList<>(total);
        List<String> business = new ArrayList<>(total);

        while (!cursor.isAfterLast()) {
            avatars.add(cursor.getString(0));
            business.add(cursor.getString(1));
            cursor.moveToNext();
        }

        cursor.close();
        int i;

        for (i = 0; i < avatars.size(); i++) {
            if (avatars.get(i) != null) {
                File file = new File(avatars.get(i));
                try {
                    file.delete();
                } catch (Exception e) {
                    Logger.error("deleteDataAssociated", e.getMessage());
                }
            }
        }

        // 15 is the min string. It happens when only one user is deleted
        StringBuilder objectIds = new StringBuilder(15);
        for (i = 0; i < business.size(); i++) {
            objectIds.append("\'");
            objectIds.append(business.get(i));
            objectIds.append("\'");
            if (i + 1 < business.size()) {
                objectIds.append(",");
            }
        }

        deleteAllDataAssociatedToMessagesWithUser(objectIds.toString());
    }

    @WorkerThread
    public void deleteAllDataAssociatedToMessagesWithUser(String objectIds) {
        String query = "SELECT " + sMessageLocalUrl + " FROM " + TABLE_MESSAGES + " WHERE " +
                sMessageType + " NOT IN (\'" + Const.kMessageTypeLocation + "\',\'" +
                Const.kMessageTypeText + "\')" + " AND (" + sMessageFromUserId + " IN (" +
                objectIds + ")" + " OR " + sMessageToUserId + " IN (" + objectIds + ")" + ")";

        Cursor cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.getString(0) != null) {
                File file = new File(cursor.getString(0));
                try {
                    file.delete();
                } catch (Exception e) {
                    Logger.error("deleteDataAssociated", e.getMessage());
                }
            }
        }

        cursor.close();
    }

    @WorkerThread
    public void deleteContactsById(List<String> customer) {
        String args = TextUtils.join(",", customer);
        // Delete avatars/images/videos/audios associated with contact list
        deleteAllDataAssociatedWithUser(args, customer.size());
        openDatabase().execSQL(String.format("DELETE FROM " + TABLE_CV_CONTACTS
                + " WHERE " + COLUMN_ID + " IN (%s);", args));
    }

    public dbBusiness isContact(String businessId) {
        Cursor cursor = openDatabase().query(TABLE_CV_CONTACTS, null, sBusinessBusinessId + " = ?", new String[]{businessId}, null, null, null);
        cursor.moveToFirst();
        dbBusiness contact = null;

        while (!cursor.isAfterLast()) {
            contact = cursorToUser(cursor);
            cursor.moveToNext();
        }

        cursor.close();

        return contact;
    }

    public dbBusiness getContactById(long id) {
        Cursor cursor = openDatabase().query(TABLE_CV_CONTACTS, null, COLUMN_ID + " = ?",
                new String[]{Long.toString(id)}, null, null, null);
        cursor.moveToFirst();
        dbBusiness contact = null;

        while (!cursor.isAfterLast()) {
            contact = cursorToUser(cursor);
            cursor.moveToNext();
        }

        cursor.close();

        return contact;
    }

    private dbBusiness cursorToUser(Cursor cursor) {
        dbBusiness contact = new dbBusiness();
        contact.setId(cursor.getLong(0));
        contact.setBusinessId(cursor.getString(1));
        contact.setDisplayName(cursor.getString(2));
        contact.setConversaId(cursor.getString(3));
        contact.setRecent(cursor.getLong(4));
        contact.setComposingMessage(cursor.getString(5));
        contact.setAvatarThumbFileId(cursor.getString(6));
        contact.setBlocked(cursor.getString(7).contentEquals("Y"));
        contact.setMuted(cursor.getString(8).contentEquals("Y"));
        contact.setCreated(cursor.getLong(9));
        return contact;
    }

    /* ******************************************* */
    /* ******************************************* */
    /* ******************************************* */

    public void saveMessage(dbMessage newMessage) {
        ContentValues message = new ContentValues();
        message.put(sMessageFromUserId, newMessage.getFromUserId());
        message.put(sMessageToUserId, newMessage.getToUserId());
        message.put(sMessageType, newMessage.getMessageType());
        message.put(sMessageDeliveryStatus, newMessage.getDeliveryStatus());
        message.put(sMessageBody, newMessage.getBody());
        message.put(sMessageLocalUrl, newMessage.getLocalUrl());
        message.put(sMessageRemoteUrl, newMessage.getRemoteUrl());
        message.put(sMessageLongitude, newMessage.getLongitude());
        message.put(sMessageLatitude, newMessage.getLatitude());
        message.put(sMessageCreatedAt, newMessage.getCreated());
        message.put(sMessageViewAt, newMessage.getViewAt());
        message.put(sMessageReadAt, newMessage.getReadAt());
        message.put(sMessageMessageId, newMessage.getMessageId());
        message.put(sMessageWidth, newMessage.getWidth());
        message.put(sMessageHeight, newMessage.getHeight());
        message.put(sMessageDuration, newMessage.getDuration());
        message.put(sMessageBytes, newMessage.getBytes());
        message.put(sMessageProgress, newMessage.getProgress());
        message.put(sMessageConversa, newMessage.getConversaAgent() ? "Y" : "N");

        long id = openDatabase().insert(TABLE_MESSAGES, null, message);

        if(id > 0) {
            newMessage.setId(id);
        }
    }

    // insert data using transaction and prepared statement
    public void insertFast(int insertCount) {

        // you can use INSERT only
        String sql = "INSERT OR REPLACE INTO " + "test" + " ( name, description ) VALUES ( ?, ? )";

        SQLiteDatabase db = openDatabase();

        /*
         * According to the docs http://developer.andwroid.com/reference/android/database/sqlite/SQLiteDatabase.html
         * Writers should use beginTransactionNonExclusive() or beginTransactionWithListenerNonExclusive(SQLiteTransactionListener)
         * to start a transaction. Non-exclusive mode allows database file to be in readable by other threads executing queries.
         */
        db.beginTransactionNonExclusive();
        // db.beginTransaction();

        SQLiteStatement stmt = db.compileStatement(sql);

        for(int x=1; x<=insertCount; x++){

            stmt.bindString(1, "Name # " + x);
            stmt.bindString(2, "Description # " + x);

            stmt.execute();
            stmt.clearBindings();

        }

        db.setTransactionSuccessful();
        db.endTransaction();

        db.close();
    }

    public int updateDeliveryStatus(long messageId, int status) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(sMessageDeliveryStatus, status);
        return openDatabase().update(TABLE_MESSAGES, contentValues, COLUMN_ID + " = ?", new String[]{Long.toString(messageId)});
    }

    public synchronized int updateLocalUrl(long messageId, String url) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(sMessageLocalUrl, url);
        return openDatabase().update(TABLE_MESSAGES, contentValues, COLUMN_ID + " = ?", new String[]{Long.toString(messageId)});
    }

    public nChatItem getLastMessageAndUnredCount(String fromId) {
        String id = ConversaApp.getInstance(context).getPreferences().getAccountCustomerId();

        String query = "SELECT *, " +
                "(" +
                "SELECT COUNT(*) FROM " +
                TABLE_MESSAGES +
                " WHERE " +
                sMessageFromUserId +
                " = \'" +
                fromId +
                "\' AND " +
                sMessageViewAt +
                " = 0) FROM (" +
                "SELECT * FROM " +
                TABLE_MESSAGES +
                " WHERE " +
                sMessageFromUserId +
                " = \'" +
                fromId +
                "\' AND " +
                sMessageToUserId +
                " = \'" +
                id +
                "\'" +
                " OR " +
                sMessageFromUserId +
                " = \'" +
                id +
                "\' AND " +
                sMessageToUserId +
                " = \'" +
                fromId +
                "\'" +
                " ORDER BY " +
                sMessageCreatedAt +
                " DESC LIMIT 1 " +
                ")";

        Cursor cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToFirst();
        dbMessage message = null;
        int count = 0;

        while (!cursor.isAfterLast()) {
            message = cursorToMessage(cursor);
            // TODO: If a field is added/removed from database, change position
            count = cursor.getInt(20);
            cursor.moveToNext();
        }

        // make sure to close the cursor
        cursor.close();

        return new nChatItem(message, (count > 0));
    }

    public int updateViewMessages(String id) {
        ContentValues contentValues = new ContentValues();
        long currentTimestamp = System.currentTimeMillis();
        contentValues.put(sMessageViewAt, currentTimestamp);
        String fromId = ConversaApp.getInstance(context).getPreferences().getAccountCustomerId();
        return openDatabase().update(TABLE_MESSAGES, contentValues,
                "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)"
                + " OR "
                + "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)",
                new String[] {id, fromId, fromId, id} );
    }

    public int updateReadMessages(String id) {
        ContentValues contentValues = new ContentValues();
        long currentTimestamp = System.currentTimeMillis();
        contentValues.put(sMessageReadAt, currentTimestamp);
        String fromId = ConversaApp.getInstance(context).getPreferences().getAccountCustomerId();
        return openDatabase().update(TABLE_MESSAGES, contentValues,
                "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)"
                        + " OR "
                        + "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)",
                new String[] {id, fromId, fromId, id} );
    }

    public int deleteAllMessagesById(String id) {
        deleteAllDataAssociatedToMessagesWithUser("\'" + id + "\'");
        String fromId = ConversaApp.getInstance(context).getPreferences().getAccountCustomerId();
        int result = openDatabase().delete(TABLE_MESSAGES,
                "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)"
                + " OR "
                + "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)",
                new String[] {id, fromId, fromId, id});
        Logger.error("MySQLiteHelper", "A total of  " + result + " messages were deleted from internal database for contact " + id);
        return result;
    }

    public List<dbMessage> getMessagesByContact(String id, int count, int offset) {
        String fromId = ConversaApp.getInstance(context).getPreferences().getAccountCustomerId();
        String query = "SELECT m.* FROM "
                + TABLE_MESSAGES + " m"
                + " WHERE m." + sMessageFromUserId + " = \'" + id + "\' AND m." + sMessageToUserId + " = \'" + fromId + "\'"
                + " UNION ALL " +
                "SELECT p.* FROM "
                + TABLE_MESSAGES + " p"
                + " WHERE p." + sMessageFromUserId + " = \'" + fromId + "\' AND p." + sMessageToUserId + " = \'" + id + "\'"
                + " ORDER BY " + sMessageCreatedAt + " DESC LIMIT " + count + " OFFSET " + offset;
        Cursor cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToLast();
        ArrayList<dbMessage> messages = new ArrayList<>(cursor.getCount());

        while (!cursor.isBeforeFirst()) {
            dbMessage contact = cursorToMessage(cursor);
            messages.add(contact);
            cursor.moveToPrevious();
        }
        // make sure to close the cursor
        cursor.close();
        return messages;
    }

    public dbMessage getMessageById(long id) {
        dbMessage message = null;
        String query = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + COLUMN_ID + " = " + id;

        Cursor cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            message = cursorToMessage(cursor);
            cursor.moveToNext();
        }

        // make sure to close the cursor
        cursor.close();
        return message;
    }

    private dbMessage cursorToMessage(Cursor cursor) {
        dbMessage message = new dbMessage();
        message.setId(cursor.getLong(0));
        message.setFromUserId(cursor.getString(1));
        message.setToUserId(cursor.getString(2));
        message.setMessageType(cursor.getString(3));
        message.setDeliveryStatus(cursor.getInt(4));
        message.setBody(cursor.getString(5));
        message.setLocalUrl(cursor.getString(6));
        message.setRemoteUrl(cursor.getString(7));
        message.setLongitude(cursor.getFloat(8));
        message.setLatitude(cursor.getFloat(9));
        message.setCreated(cursor.getLong(10));
        message.setViewAt(cursor.getLong(11));
        message.setReadAt(cursor.getLong(12));
        message.setMessageId(cursor.getString(13));
        message.setWidth(cursor.getInt(14));
        message.setHeight(cursor.getInt(15));
        message.setDuration(cursor.getInt(16));
        message.setBytes(cursor.getLong(17));
        message.setProgress(cursor.getInt(18));
        message.setConversaAgent(cursor.getString(19).equals("Y") ? 'Y' : 'N');
        return message;
    }

    /* ******************************************* */
    /* ******************************************* */
    /* ******************************************* */

    public boolean addSearch(dbSearch search) {
        ContentValues searchContent = new ContentValues();
        long currentTimestamp = System.currentTimeMillis();

        if (DatabaseUtils.queryNumEntries(openDatabase(), TABLE_SEARCH) < 5) {
            String query = "SELECT " + COLUMN_ID + " FROM " + TABLE_SEARCH
                    + " WHERE " + sSearchBusinessId + " = \'" + search.getBusinessId() + "\'";

            Cursor cursor = openDatabase().rawQuery(query, new String[]{});
            cursor.moveToFirst();
            long exists = -1;

            while (!cursor.isAfterLast()) {
                if (!cursor.isNull(0)) {
                    exists = cursor.getLong(0);
                }
                cursor.moveToNext();
            }

            // make sure to close the cursor
            cursor.close();

            if (exists != -1) {
                searchContent.put(sSearchBusinessId, search.getBusinessId());
                searchContent.put(sSearchDisplayName, search.getDisplayName());
                searchContent.put(sSearchConversaId, search.getConversaId());
                searchContent.put(sSearchAvatarUrl, search.getAvatarUrl());
                searchContent.put(sSearchCreatedAt, currentTimestamp);
                return (openDatabase().update(TABLE_SEARCH, searchContent,
                        COLUMN_ID + " = ?", new String[] {String.valueOf(exists)}) == 1);
            } else {
                // Create record
                searchContent.put(sSearchBusinessId, search.getBusinessId());
                searchContent.put(sSearchDisplayName, search.getDisplayName());
                searchContent.put(sSearchConversaId, search.getConversaId());
                searchContent.put(sSearchAvatarUrl, search.getAvatarUrl());
                searchContent.put(sSearchCreatedAt, currentTimestamp);
                return (openDatabase().insert(TABLE_SEARCH, null, searchContent) != -1);
            }
        } else {
            // Update last record
            String query = "SELECT (SELECT " + COLUMN_ID + " FROM " + TABLE_SEARCH + " WHERE " +
                    sSearchCreatedAt + " = (SELECT MIN(" + sSearchCreatedAt + ") FROM " +
                    TABLE_SEARCH + ")), (SELECT " + COLUMN_ID + " FROM " + TABLE_SEARCH +
                    " WHERE " + sSearchBusinessId + " = \'" + search.getBusinessId() + "\')";

            Cursor cursor = openDatabase().rawQuery(query, new String[]{});
            cursor.moveToFirst();
            long id = -1;
            long exists = -1;

            while (!cursor.isAfterLast()) {
                id = cursor.getLong(0);
                if (!cursor.isNull(1)) {
                    exists = cursor.getLong(1);
                }
                cursor.moveToNext();
            }

            // make sure to close the cursor
            cursor.close();

            if (exists != -1) {
                searchContent.put(sSearchCreatedAt, currentTimestamp);
                return (openDatabase().update(TABLE_SEARCH, searchContent,
                        COLUMN_ID + " = ?", new String[] {String.valueOf(exists)}) == 1);
            } else if (id != -1) {
                searchContent.put(sSearchBusinessId, search.getBusinessId());
                searchContent.put(sSearchDisplayName, search.getDisplayName());
                searchContent.put(sSearchConversaId, search.getConversaId());
                searchContent.put(sSearchAvatarUrl, search.getAvatarUrl());
                searchContent.put(sSearchCreatedAt, currentTimestamp);
                return (openDatabase().update(TABLE_SEARCH, searchContent,
                        COLUMN_ID + " = ?", new String[] {String.valueOf(id)}) == 1);
            } else {
                return false;
            }
        }
    }

    public List<dbSearch> getRecentSearches() {
        String query = "SELECT * FROM " + TABLE_SEARCH + " ORDER BY " + sSearchCreatedAt + " DESC";
        Cursor cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToFirst();
        ArrayList<dbSearch> searches = new ArrayList<>(cursor.getCount());

        while (!cursor.isAfterLast()) {
            dbSearch contact = cursorToSearch(cursor);
            searches.add(contact);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return searches;
    }

    public void clearRecentSearches() {
        openDatabase().delete(TABLE_SEARCH, null, null);
    }

    private dbSearch cursorToSearch(Cursor cursor) {
        dbSearch search = new dbSearch(
                cursor.getLong(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4)
        );
        return search;
    }

    /* ******************************************* */
    /* ******************************************* */
    /* ******************************************* */

    public dbNotificationInformation getGroupInformation(String group_id) {
        String query = "SELECT " + COLUMN_ID + "," + sNotificationAndroidId + "," + sNotificationCount + " FROM " + TABLE_NOTIFICATION + " WHERE " + sNotificationGroup + " = \'" + group_id + "\'" + " LIMIT 1";
        Cursor cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToFirst();
        dbNotificationInformation information = new dbNotificationInformation(group_id);

        while (!cursor.isAfterLast()) {
            information.setNotificationId(cursor.getInt(0));
            information.setAndroidNotificationId(cursor.getLong(1));
            information.setCount(cursor.getInt(2));
            cursor.moveToNext();
        }

        cursor.close();
        return information;
    }

    public void incrementGroupCount(dbNotificationInformation information, boolean create) {
        if (create) {
            // Create record
            ContentValues record = new ContentValues();
            record.put(sNotificationAndroidId, information.getAndroidNotificationId());
            record.put(sNotificationGroup, information.getGroupId());
            record.put(sNotificationCount, 1);
            information.setNotificationId(openDatabase().insert(TABLE_NOTIFICATION, null, record));
        } else {
            // Update record
            information.setCount(information.getCount() + 1);
            openDatabase().execSQL(String.format(Locale.US, "UPDATE %s SET %s = (%s + 1) WHERE %s = %d;",
                    TABLE_NOTIFICATION, sNotificationCount, sNotificationCount, COLUMN_ID, information.getNotificationId()));
        }
    }

    public void resetGroupCount(long notificationId) {
        openDatabase().execSQL(String.format(Locale.US, "UPDATE %s SET %s = 0 WHERE %s = %d;",
                TABLE_NOTIFICATION, sNotificationCount, COLUMN_ID, notificationId));
    }

    public void resetAllCounts() {
        openDatabase().execSQL(String.format("UPDATE %s SET %s = 0;",
                TABLE_NOTIFICATION, sNotificationCount));
    }

    /************************************************************/
    /*******************CREATE/UPGRADE METHODS*******************/
    /************************************************************/

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLE_MESSAGES_CREATE);
            db.execSQL(tmIndex1);
            db.execSQL(tmIndex2);
            db.execSQL(TABLE_CONTACTS_CREATE);
            db.execSQL(tcIndex1);
            db.execSQL(TABLE_SEARCH_CREATE);
            db.execSQL(TABLE_NOTIFICATION_CREATE);
            db.execSQL(newMessageTrigger);
            db.execSQL(deleteUserTrigger);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Logger.error("onUpgrade", "Upgrading database MESSAGES from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CV_CONTACTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEARCH);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATION);
            onCreate(db);
        }
    }
}