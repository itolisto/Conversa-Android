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
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ee.app.conversa.model.database.NotificationInformation;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.model.database.dbSearch;
import ee.app.conversa.model.parse.Account;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;

public class MySQLiteHelper {

    private static final String TAG = "MySQLiteHelper";
    private final Context context;
    private DatabaseHelper myDbHelper;

    private static final String DATABASE_NAME1 = "conversadb.db";
    private static final int DATABASE_VERSION = 2;

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
    private static final String sMessageFileId = "file_id";
    private static final String sMessageLongitude = "longitude";
    private static final String sMessageLatitude = "latitude";
    private static final String sMessageCreatedAt = "created_at";
    private static final String sMessageModifiedAt = "modified_at";
    private static final String sMessageReadAt = "read_at";
    private static final String sMessageMessageId = "message_id";
    private static final String sMessageWidth = "width";
    private static final String sMessageHeight = "height";
    private static final String sMessageDuration = "duration";
    private static final String sMessageBytes = "bytes";
    private static final String sMessageProgress = "progress";

    private static final String TABLE_MESSAGES_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_MESSAGES + "("
            + "\"" + COLUMN_ID + "\" INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "\"" + sMessageFromUserId + "\" CHAR(14) NOT NULL, "
            + "\"" + sMessageToUserId + "\" CHAR(14) NOT NULL, "
            + "\"" + sMessageType + "\" CHAR(1) NOT NULL, "
            + "\"" + sMessageDeliveryStatus + "\" CHAR(1) NOT NULL, "
            + "\"" + sMessageBody + "\" TEXT, "
            + "\"" + sMessageFileId + "\" TEXT, "
            + "\"" + sMessageLongitude + "\" REAL DEFAULT 0, "
            + "\"" + sMessageLatitude + "\" REAL DEFAULT 0, "
            + "\"" + sMessageCreatedAt + "\" INTEGER NOT NULL, "
            + "\"" + sMessageModifiedAt + "\" INTEGER NOT NULL DEFAULT '0', "
            + "\"" + sMessageReadAt + "\" INTEGER NOT NULL DEFAULT '0', "
            + "\"" + sMessageMessageId + "\" CHAR(14),"
            + "\"" + sMessageWidth + "\" INTEGER DEFAULT 0,"
            + "\"" + sMessageHeight + "\" INTEGER DEFAULT 0,"
            + "\"" + sMessageDuration + "\" INTEGER DEFAULT 0,"
            + "\"" + sMessageBytes + "\" INTEGER DEFAULT 0, "
            + "\"" + sMessageProgress + "\" INTEGER DEFAULT 0 );";
    private static final String tmIndex1 = "CREATE INDEX M_search on "  + TABLE_MESSAGES + "(" + sMessageFromUserId + ", " + sMessageToUserId + "); ";
    private static final String tmIndex2 = "CREATE UNIQUE INDEX IF NOT EXISTS C_messageId on "  + TABLE_MESSAGES + "(" + sMessageMessageId + ");";

    // CONTACTS
    private static final String sBusinessBusinessId = "businessId";
    private static final String sBusinessDisplayName = "displayName";
    private static final String sBusinessConversaId = "conversaId";
    private static final String sBusinessRecent = "recent";
    private static final String sBusinessAbout = "about";
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
            + "\"" + sBusinessAbout + "\" VARCHAR(255), "
            + "\"" + sBusinessComposingMessage + "\" VARCHAR(255), "
            + "\"" + sBusinessAvatarFile + "\" VARCHAR(355), "
            + "\"" + sBusinessBlocked + "\" CHAR(1) NOT NULL DEFAULT 'N', "
            + "\"" + sBusinessMuted + "\" CHAR(1) NOT NULL DEFAULT 'N', "
            + "\"" + sBusinessCreatedAt + "\" INTEGER NOT NULL );";
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
            + "\"" + sSearchAvatarUrl + "\" TEXT NOT NULL DEFAULT 0, "
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

    public SQLiteDatabase openDatabase() throws SQLException {
        return myDbHelper.getWritableDatabase();
    }

    public boolean deleteDatabase() {
        context.deleteDatabase(DATABASE_NAME1);
        return true;
    }

    /************************************************************/
    /*********************OPERATIONS METHODS*********************/
    /************************************************************/
    public dbBusiness saveContact(dbBusiness user) {
        ContentValues contact = new ContentValues();
        contact.put(sBusinessBusinessId, user.getBusinessId());
        contact.put(sBusinessDisplayName, user.getDisplayName());
        contact.put(sBusinessConversaId, user.getConversaId());
        contact.put(sBusinessRecent, user.getRecent());
        contact.put(sBusinessAbout, user.getAbout());
        contact.put(sBusinessComposingMessage, "");
        contact.put(sBusinessAvatarFile, user.getAvatarThumbFileId());
        contact.put(sBusinessBlocked, "N");
        contact.put(sBusinessMuted, "N");
        contact.put(sBusinessCreatedAt, user.getCreated());

        long result = openDatabase().insert(TABLE_CV_CONTACTS, null, contact);

        if (result > 0) {
            user.setId(result);
        }

        return user;
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
    public void deleteDataAssociatedWithUser(String ids, int total) {
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
        int i = 0;

        for (i = 0; i < avatars.size(); i++) {
            if (avatars.get(i) != null) {
                File file = new File(avatars.get(i));
                try {
                    file.delete();
                } catch (SecurityException e) {
                    Logger.error("deleteDataAssociated", e.getMessage());
                }
            }
        }

        // 132 is the min string. It happens when only one user is deleted
        StringBuilder objectIds = new StringBuilder(15);
        for (i = 0; i < business.size(); i++) {
            objectIds.append("\'");
            objectIds.append(business.get(i));
            objectIds.append("\'");
            if (i + 1 < business.size()) {
                objectIds.append(",");
            }
        }

        StringBuilder sb = new StringBuilder(132);
        // Delete data associated with messages
        sb.append("SELECT ");
        sb.append(sMessageFileId);
        sb.append(" FROM ");
        sb.append(TABLE_MESSAGES);
        sb.append(" WHERE ");
        sb.append(sMessageType);
        sb.append(" NOT IN (\'");
        sb.append(Const.kMessageTypeLocation);
        sb.append("\',\'");
        sb.append(Const.kMessageTypeText);
        sb.append("\')");
        sb.append(" AND (");
        sb.append(sMessageFromUserId);
        sb.append(" IN (");
        sb.append(objectIds);
        sb.append(")");
        sb.append(" OR ");
        sb.append(sMessageToUserId);
        sb.append(" IN (");
        sb.append(objectIds);
        sb.append(")");
        sb.append(")");

        query = sb.toString();

        cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToFirst();

        for (i = 0; i < cursor.getCount(); i++) {
            if (cursor.getString(0) != null) {
                File file = new File(cursor.getString(0));
                try {
                    file.delete();
                } catch (SecurityException e) {
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
        deleteDataAssociatedWithUser(args, customer.size());
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

    public boolean hasPendingMessages(String id) {
        Cursor cursor = openDatabase().query(TABLE_CV_CONTACTS, new String[] {"hasPendingMessages"}, COLUMN_ID + " = ?",new String[] { id },null,null,null);
        cursor.moveToFirst();
        int has = 1;

        while (!cursor.isAfterLast()) {
            has = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();

        return (has == 1);
    }

    public void setHasPendingMessages(String id, int status) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("hasPendingMessages", status);
        openDatabase().update(TABLE_CV_CONTACTS, contentValues, "_id = ? ", new String[]{id});
    }

    private dbBusiness cursorToUser(Cursor cursor) {
        dbBusiness contact = new dbBusiness();
        contact.setId(cursor.getLong(0));
        contact.setBusinessId(cursor.getString(1));
        contact.setDisplayName(cursor.getString(2));
        contact.setConversaId(cursor.getString(3));
        contact.setRecent(cursor.getLong(4));
        contact.setAbout(cursor.getString(5));
        contact.setComposingMessage(cursor.getString(6));
        contact.setAvatarThumbFileId(cursor.getString(7));
        boolean b = cursor.getString(8).contentEquals("Y");
        contact.setBlocked(b);
        b = cursor.getString(9).contentEquals("Y");
        contact.setMuted(b);
        contact.setCreated(cursor.getLong(10));
        return contact;
    }

    /* ******************************************* */
    /* ******************************************* */
    /* ******************************************* */

    public dbMessage saveMessage(dbMessage newMessage) {
        ContentValues message = new ContentValues();
        message.put(sMessageFromUserId, newMessage.getFromUserId());
        message.put(sMessageToUserId, newMessage.getToUserId());
        message.put(sMessageType, newMessage.getMessageType());
        message.put(sMessageDeliveryStatus, newMessage.getDeliveryStatus());
        message.put(sMessageBody, newMessage.getBody());
        message.put(sMessageFileId, newMessage.getFileId());
        message.put(sMessageLongitude, newMessage.getLongitude());
        message.put(sMessageLatitude, newMessage.getLatitude());
        message.put(sMessageCreatedAt, newMessage.getCreated());
        message.put(sMessageModifiedAt, newMessage.getModified());
        message.put(sMessageReadAt, newMessage.getReadAt());
        message.put(sMessageMessageId, newMessage.getMessageId());
        message.put(sMessageWidth, newMessage.getWidth());
        message.put(sMessageHeight, newMessage.getHeight());
        message.put(sMessageDuration, newMessage.getDuration());
        message.put(sMessageBytes, newMessage.getBytes());
        message.put(sMessageProgress, newMessage.getProgress());

        long id = openDatabase().insert(TABLE_MESSAGES, null, message);

        if(id > 0) {
            newMessage.setId(id);
        }

        return newMessage;
    }

    public int updateDeliveryStatus(long messageId, String status) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(sMessageDeliveryStatus, status);
        return openDatabase().update(TABLE_MESSAGES, contentValues, COLUMN_ID + " = ?", new String[]{Long.toString(messageId)});
    }

    public int messageCountForContact(String id) {
        String query = "SELECT COUNT(*) FROM " + TABLE_MESSAGES + " WHERE " + sMessageFromUserId + " = \'" + id + "\'";
        Cursor cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToFirst();
        int count = 0;

        while (!cursor.isAfterLast()) {
            count = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();

        return count;
    }

    public dbMessage getLastMessage(String id) {
        String fromId = Account.getCurrentUser().getObjectId();
        dbMessage message = null;
        String query = "SELECT m.* FROM "
                + TABLE_MESSAGES + " m"
                + " WHERE m." + sMessageFromUserId + " = \'" + id + "\' AND m." + sMessageToUserId + " = \'" + fromId + "\'"
                + " UNION ALL " +
                "SELECT p.* FROM "
                + TABLE_MESSAGES + " p"
                + " WHERE p." + sMessageFromUserId + " = \'" + fromId + "\' AND p." + sMessageToUserId + " = \'" + id + "\'"
                + " ORDER BY " + sMessageCreatedAt + " DESC LIMIT 1";

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

    public boolean hasUnreadMessagesOrNewMessages(String id) {
        String query = "SELECT COUNT(*) FROM " + TABLE_MESSAGES + " WHERE " + sMessageFromUserId + " = \'" + id + "\' AND " + sMessageReadAt + " = 0";
        Cursor cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToFirst();
        int count = 0;

        while (!cursor.isAfterLast()) {
            count = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();

        return (count > 0);
    }

    public int updateReadMessages(String id) {
        ContentValues contentValues = new ContentValues();
        long currentTimestamp = System.currentTimeMillis();
        contentValues.put(sMessageReadAt, currentTimestamp);
        String fromId = Account.getCurrentUser().getObjectId();
        int result1 = openDatabase().update(TABLE_MESSAGES, contentValues,
                "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)"
                + " OR "
                + "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)",
                new String[] {id, fromId, fromId, id} );
        return result1;
    }

    private int deleteAllMessagesById(String id) {
        String fromId = Account.getCurrentUser().getObjectId();
        int result = openDatabase().delete(TABLE_MESSAGES,
                "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)"
                + " OR "
                + "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)",
                new String[] {id, fromId, fromId, id});
        Logger.error("MySQLiteHelper", "A total of  " + result + " messages were deleted from internal database for contact " + id);
        return result;
    }

    public List<dbMessage> getMessagesByContact(String id, int count, int offset) {
        String fromId = Account.getCurrentUser().getObjectId();
        String query = "SELECT m.* FROM "
                + TABLE_MESSAGES + " m"
                + " WHERE m." + sMessageFromUserId + " = \'" + id + "\' AND m." + sMessageToUserId + " = \'" + fromId + "\'"
                + " UNION ALL " +
                "SELECT p.* FROM "
                + TABLE_MESSAGES + " p"
                + " WHERE p." + sMessageFromUserId + " = \'" + fromId + "\' AND p." + sMessageToUserId + " = \'" + id + "\'"
                + " ORDER BY " + sMessageCreatedAt + " DESC LIMIT " + count + " OFFSET " + offset;
        Cursor cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToFirst();
        ArrayList<dbMessage> messages = new ArrayList<>(cursor.getCount());

        while (!cursor.isAfterLast()) {
            dbMessage contact = cursorToMessage(cursor);
            messages.add(contact);
            cursor.moveToNext();
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
        message.setDeliveryStatus(cursor.getString(4));
        message.setBody(cursor.getString(5));
        message.setFileId(cursor.getString(6));
        message.setLongitude(cursor.getFloat(7));
        message.setLatitude(cursor.getFloat(8));
        message.setCreated(cursor.getLong(9));
        message.setModified(cursor.getLong(10));
        message.setReadAt(cursor.getLong(11));
        message.setMessageId(cursor.getString(12));
        message.setWidth(cursor.getInt(13));
        message.setHeight(cursor.getInt(14));
        message.setDuration(cursor.getInt(15));
        message.setBytes(cursor.getInt(16));
        message.setProgress(cursor.getInt(17));
        return message;
    }

    /* ******************************************* */
    /* ******************************************* */
    /* ******************************************* */

    public boolean addSearch(dbSearch search) {
        ContentValues searchContent = new ContentValues();
        long currentTimestamp = System.currentTimeMillis();

        if (DatabaseUtils.queryNumEntries(openDatabase(), TABLE_SEARCH) < 5) {
            // Create record
            searchContent.put(sSearchBusinessId, search.getBusinessId());
            searchContent.put(sSearchDisplayName, search.getDisplayName());
            searchContent.put(sSearchConversaId, search.getConversaId());
            searchContent.put(sSearchAvatarUrl, search.getAvatarUrl());
            searchContent.put(sSearchCreatedAt, currentTimestamp);

            return (openDatabase().insert(TABLE_SEARCH, null, searchContent) != -1);
        } else {
            // Update last record
            dbSearch recentSearch = null;
            String query = "SELECT MIN(" + sSearchCreatedAt + ") FROM " + TABLE_SEARCH;

            Cursor cursor = openDatabase().rawQuery(query, new String[]{});
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                recentSearch = cursorToSearch(cursor);
                cursor.moveToNext();
            }

            // make sure to close the cursor
            cursor.close();

            if (recentSearch != null) {
                searchContent.put(sSearchBusinessId, search.getBusinessId());
                searchContent.put(sSearchDisplayName, search.getDisplayName());
                searchContent.put(sSearchConversaId, search.getConversaId());
                searchContent.put(sSearchAvatarUrl, search.getAvatarUrl());
                searchContent.put(sSearchCreatedAt, currentTimestamp);
                return (openDatabase().update(TABLE_SEARCH, searchContent,
                        COLUMN_ID + " = ?", new String[] {String.valueOf(search.getID())} ) == 1);
            } else {
                return false;
            }
        }
    }

    public List<dbSearch> getRecentSearches() {
        String query = "SELECT * FROM " + TABLE_SEARCH + " ORDER BY " + sSearchCreatedAt + " ASC";
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

    public NotificationInformation getGroupInformation(String group_id) {
        String query = "SELECT " + COLUMN_ID + "," + sNotificationAndroidId + "," + sNotificationCount + " FROM " + TABLE_NOTIFICATION + " WHERE " + sNotificationGroup + " = \'" + group_id + "\'" + " LIMIT 1";
        Cursor cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToFirst();
        NotificationInformation information = new NotificationInformation(group_id);

        while (!cursor.isAfterLast()) {
            information.setNotificationId(cursor.getInt(0));
            information.setAndroidNotificationId(cursor.getLong(1));
            information.setCount(cursor.getInt(2));
            cursor.moveToNext();
        }

        cursor.close();
        return information;
    }

    public NotificationInformation incrementGroupCount(NotificationInformation information, boolean create) {
        if (create) {
            // Create record
            ContentValues record = new ContentValues();
            record.put(sNotificationAndroidId, information.getAndroidNotificationId());
            record.put(sNotificationGroup, information.getGroupId());
            record.put(sNotificationCount, 1);
            information.setNotificationId(openDatabase().insert(TABLE_NOTIFICATION, null, record));
        } else {
            // Update record
            openDatabase().execSQL(String.format(Locale.US, "UPDATE %s SET %s = (%s + 1) WHERE %s = %d;",
                    TABLE_NOTIFICATION, sNotificationCount, sNotificationCount, COLUMN_ID, information.getNotificationId()));
        }

        return information;
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
            super(context, DATABASE_NAME1, null, DATABASE_VERSION);
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
            Logger.error(TAG, "Upgrading database MESSAGES from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CV_CONTACTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEARCH);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATION);
            onCreate(db);
        }
    }
}