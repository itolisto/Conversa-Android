package ee.app.conversa.database;

/**
 * Created by edgargomez on 2/11/15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.UiThread;
import android.util.Log;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import ee.app.conversa.interfaces.OnContactTaskCompleted;
import ee.app.conversa.interfaces.OnMessageTaskCompleted;
import ee.app.conversa.model.Database.Message;
import ee.app.conversa.model.Database.dBusiness;
import ee.app.conversa.model.Parse.Account;
import ee.app.conversa.response.ContactResponse;
import ee.app.conversa.response.MessageResponse;
import ee.app.conversa.utils.Logger;

public class MySQLiteHelper {

    private OnMessageTaskCompleted messageListeners;
    private OnContactTaskCompleted contactListeners;

    private static final String TAG = "MySQLiteHelper";
    private final Context context;
    private DatabaseHelperMessages myDbHelperForMessages;
    private DatabaseHelperContacts myDbHelperForContacts;
    private SQLiteDatabase myDb;

    private static final String DATABASE_NAME1 = "userMessagesdb.db";
    private static final String DATABASE_NAME2 = "userContactsdb.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MESSAGES = "message";
    private static final String TABLE_CV_CONTACTS = "cv_contact";

    private static final String COLUMN_ID = "_id";

    // MESSAGE
    public static final String sMessageFromUserId = "from_user_id";
    public static final String sMessageToUserId = "to_user_id";
    public static final String sMessageType = "message_type";
    public static final String sMessageDeliveryStatus = "delivery_status";
    public static final String sMessageBody = "body";
    public static final String sMessageFileId = "file_id";
    public static final String sMessageLongitude = "longitude";
    public static final String sMessageLatitude = "latitude";
    public static final String sMessageCreatedAt = "created_at";
    public static final String sMessageModifiedAt = "modified_at";
    public static final String sMessageReadAt = "read_at";

    // Database creation sql statement
    private static final String TABLE_MESSAGES_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_MESSAGES + "("
            + "\"" + COLUMN_ID + "\" INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "\"" + sMessageFromUserId + "\" CHAR(10) NOT NULL, "
            + "\"" + sMessageToUserId + "\" CHAR(10) NOT NULL, "
            + "\"" + sMessageType + "\" CHAR(1) NOT NULL, "
            + "\"" + sMessageDeliveryStatus + "\" CHAR(1) NOT NULL, "
            + "\"" + sMessageBody + "\" TEXT NOT NULL, "
            + "\"" + sMessageFileId + "\" VARCHAR(255) NOT NULL, "
            + "\"" + sMessageLongitude + "\" FLOAT NOT NULL, "
            + "\"" + sMessageLatitude + "\" FLOAT NOT NULL, "
            + "\"" + sMessageCreatedAt + "\" INTEGER NOT NULL, "
            + "\"" + sMessageModifiedAt + "\" INTEGER NOT NULL, "
            + "\"" + sMessageReadAt + "\" INTEGER NOT NULL DEFAULT '0' );";
    private static final String tmIndex1 = "CREATE INDEX M_search on "  + TABLE_MESSAGES + "(" + sMessageFromUserId + ", " + sMessageToUserId + "); ";

    // CONTACTS
    public static final String sBusinessBusinessId = "businessId";
    public static final String sBusinessDisplayName = "displayName";
    public static final String sBusinessConversaId = "conversaId";
    public static final String sBusinessRecent = "recent";
    public static final String sBusinessAbout = "about";
    public static final String sBusinessStatus = "statusMessage";
    public static final String sBusinessComposingMessage = "composingMessageString";
    public static final String sBusinessAvatarFile = "avatar_file_url";
    public static final String sBusinessBlocked = "blocked";
    public static final String sBusinessMuted = "muted";
    public static final String sBusinessCreatedAt = "created_at";

    private static final String TABLE_CONTACTS_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_CV_CONTACTS + "("
            + "\"" + COLUMN_ID + "\" INTEGER PRIMARY KEY, "
            + "\"" + sBusinessBusinessId + "\" CHAR(10) NOT NULL, "
            + "\"" + sBusinessDisplayName + "\" VARCHAR(180) NOT NULL, "
            + "\"" + sBusinessConversaId + "\" VARCHAR(255) NOT NULL, "
            + "\"" + sBusinessRecent + "\" INTEGER NOT NULL, "
            + "\"" + sBusinessAbout + "\" VARCHAR(255) NOT NULL, "
            + "\"" + sBusinessStatus + "\" VARCHAR(255) NOT NULL, "
            + "\"" + sBusinessComposingMessage + "\" VARCHAR(255) NOT NULL, "
            + "\"" + sBusinessAvatarFile + "\" VARCHAR(355) NOT NULL, "
            + "\"" + sBusinessBlocked + "\" CHAR(1) NOT NULL DEFAULT 'N', "
            + "\"" + sBusinessMuted + "\" CHAR(1) NOT NULL DEFAULT 'N', "
            + "\"" + sBusinessCreatedAt + "\" INTEGER NOT NULL );";
    private static final String tcIndex1 = "CREATE UNIQUE INDEX IF NOT EXISTS C_businessId on "  + TABLE_CV_CONTACTS + "(" + sBusinessBusinessId + ");";

    /************************************************************/
    /*********************OPEN/CLOSE METHODS*********************/
    /************************************************************/

    public MySQLiteHelper(Context context) {
        this.context = context;
        myDbHelperForMessages = new DatabaseHelperMessages(context);
        myDbHelperForContacts = new DatabaseHelperContacts(context);

        messageListeners = null;
        contactListeners = null;

        openMessagesTable();
        closeMessagesTable();
        openContactsTable();
        closeContactsTable();
    }

    public MySQLiteHelper openMessagesTable() throws SQLException {
        myDb = myDbHelperForMessages.getWritableDatabase();
        return this;
    }

    public void closeMessagesTable() {
        if (myDbHelperForMessages != null) { myDbHelperForMessages.close(); }
    }

    public MySQLiteHelper openContactsTable() throws SQLException {
        myDb = myDbHelperForContacts.getWritableDatabase();
        return this;
    }

    public void closeContactsTable() {
        if (myDbHelperForContacts != null) { myDbHelperForContacts.close(); }
    }

    public boolean deleteDatabase() {
        context.deleteDatabase(DATABASE_NAME1);
        context.deleteDatabase(DATABASE_NAME2);
        return true;
    }

    /************************************************************/
    /*********************OPERATIONS METHODS*********************/
    /************************************************************/
    public dBusiness saveContact(dBusiness user) {
        ContentValues contact = new ContentValues();
        contact.put(sBusinessBusinessId, user.getBusinessId());
        contact.put(sBusinessDisplayName, user.getDisplayName());
        contact.put(sBusinessConversaId, user.getConversaId());
        contact.put(sBusinessRecent, user.getRecent());
        contact.put(sBusinessAbout, user.getAbout());
        contact.put(sBusinessStatus, user.getStatusMessage());
        contact.put(sBusinessComposingMessage, "");
        contact.put(sBusinessAvatarFile, user.getAvatarThumbFileId());
        contact.put(sBusinessBlocked, "N");
        contact.put(sBusinessMuted, "N");
        contact.put(sBusinessCreatedAt, user.getCreated());

        openContactsTable();
        long result = myDb.insert(TABLE_CV_CONTACTS, null, contact);
        closeContactsTable();

        if (result > 0) {
            user.setId(result);
        }

        return user;
    }

    public List<dBusiness> getAllContacts() {
        List<dBusiness> contacts = new ArrayList<>();

        openContactsTable();
        Cursor cursor = myDb.query(TABLE_CV_CONTACTS,null,null,null,null,null, sBusinessRecent + " DESC");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            dBusiness contact = cursorToUser(cursor);
            contacts.add(contact);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        closeContactsTable();

        return contacts;
    }

    public dBusiness deleteContactById(dBusiness customer) {
        String id = Long.toString(customer.getId());
        openContactsTable();
        int result = myDb.delete(TABLE_CV_CONTACTS, COLUMN_ID + " = ? ", new String[]{id});
        closeContactsTable();

        if( result == 1 ) {
            deleteAllMessagesById(id);
        }

        return customer;
    }

    public dBusiness isContact(String businessId) {
        openContactsTable();
        Cursor cursor = myDb.query(TABLE_CV_CONTACTS, null, sBusinessBusinessId + " = ?", new String[]{businessId}, null, null, null);
        cursor.moveToFirst();
        dBusiness contact = null;

        while (!cursor.isAfterLast()) {
            contact = cursorToUser(cursor);
            cursor.moveToNext();
        }

        cursor.close();
        closeContactsTable();

        return contact;
    }

    public boolean hasPendingMessages(String id) {
        openContactsTable();
        Cursor cursor = myDb.query(TABLE_CV_CONTACTS, new String[] {"hasPendingMessages"}, COLUMN_ID + " = ?",new String[] { id },null,null,null);
        cursor.moveToFirst();
        int has = 1;

        while (!cursor.isAfterLast()) {
            has = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();
        closeContactsTable();

        return (has == 1);
    }

    public void setHasPendingMessages(String id, int status) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("hasPendingMessages", status);
        openContactsTable();
        myDb.update(TABLE_CV_CONTACTS, contentValues, "_id = ? ", new String[]{id});
        closeContactsTable();
    }

    private dBusiness cursorToUser(Cursor cursor) {
        dBusiness contact = new dBusiness();
        contact.setId(cursor.getLong(0));
        contact.setBusinessId(cursor.getString(1));
        contact.setDisplayName(cursor.getString(2));
        contact.setConversaId(cursor.getString(3));
        contact.setRecent(cursor.getLong(4));
        contact.setAbout(cursor.getString(5));
        contact.setStatusMessage(cursor.getString(6));
        contact.setComposingMessage(cursor.getString(7));
        contact.setAvatarThumbFileId(cursor.getString(8));
        boolean b = cursor.getString(9).contentEquals("Y");
        contact.setBlocked(b);
        b = cursor.getString(10).contentEquals("Y");
        contact.setMuted(b);
        contact.setCreated(cursor.getLong(11));
        return contact;
    }

    /* ******************************************* */
    /* ******************************************* */
    /* ******************************************* */

    public Message saveMessage(Message newMessage) {
        ContentValues message = new ContentValues();
        message.put(sMessageFromUserId, newMessage.getFromUserId());
        message.put(sMessageToUserId, newMessage.getToUserId());
        message.put(sMessageType, newMessage.getMessageType());
        message.put(sMessageDeliveryStatus, newMessage.getDeliveryStatus());
        message.put(sMessageBody, newMessage.getBody());
        message.put(sMessageFileId, newMessage.getImageFileId());
        message.put(sMessageLongitude, newMessage.getLongitude());
        message.put(sMessageLatitude, newMessage.getLatitude());
        message.put(sMessageCreatedAt, newMessage.getCreated());
        message.put(sMessageModifiedAt, newMessage.getModified());
        message.put(sMessageReadAt, newMessage.getReadAt());

        openMessagesTable();
        long id = myDb.insert(TABLE_MESSAGES, null, message);
        closeMessagesTable();

        if(id > 0) {
            newMessage.setId(id);
        }

        return newMessage;
    }

    public int updateDeliveryStatus(long messageId, String status) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(sMessageDeliveryStatus, status);
        openMessagesTable();
        int result = myDb.update(TABLE_MESSAGES, contentValues, COLUMN_ID + " = ?", new String[]{Long.toString(messageId)});
        closeMessagesTable();
        return result;
    }

    public int messageCountForContact(String id) {
        String query = "SELECT COUNT(*) FROM " + TABLE_MESSAGES + " WHERE " + sMessageFromUserId + " = \'" + id + "\'";
        openMessagesTable();
        Cursor cursor = myDb.rawQuery(query, new String[]{});
        cursor.moveToFirst();
        int count = 0;

        while (!cursor.isAfterLast()) {
            count = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();
        closeMessagesTable();

        return count;
    }

    public Message getLastMessage(String id) {
        String fromId = Account.getCurrentUser().getObjectId();
        Message message = null;
        openMessagesTable();
        String query = "SELECT m.* FROM "
                + TABLE_MESSAGES + " m"
                + " WHERE m." + sMessageFromUserId + " = \'" + id + "\' AND m." + sMessageToUserId + " = \'" + fromId + "\'"
                + " UNION ALL " +
                "SELECT p.* FROM "
                + TABLE_MESSAGES + " p"
                + " WHERE p." + sMessageFromUserId + " = \'" + fromId + "\' AND p." + sMessageToUserId + " = \'" + id + "\'"
                + " ORDER BY " + sMessageCreatedAt + " DESC LIMIT 1";

        Cursor cursor = myDb.rawQuery(query, new String[]{});
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            message = cursorToMessage(cursor);
            cursor.moveToNext();
        }

        // make sure to close the cursor
        cursor.close();
        closeMessagesTable();

        return message;
    }

    public boolean hasUnreadMessagesOrNewMessages(String id) {
        String query = "SELECT COUNT(*) FROM " + TABLE_MESSAGES + " WHERE " + sMessageFromUserId + " = \'" + id + "\' AND " + sMessageReadAt + " = 0";
        openMessagesTable();
        Cursor cursor = myDb.rawQuery(query, new String[]{});
        cursor.moveToFirst();
        int count = 0;

        while (!cursor.isAfterLast()) {
            count = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();
        closeMessagesTable();

        return (count > 0);
    }

    public int updateReadMessages(String id) {
        ContentValues contentValues = new ContentValues();
        GregorianCalendar now = new GregorianCalendar();
        long currentTimestamp = now.getTimeInMillis() / 1000;
        contentValues.put("read_at", currentTimestamp);
        openMessagesTable();
        String fromId = Account.getCurrentUser().getObjectId();
        int result1 = myDb.update(TABLE_MESSAGES, contentValues, sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?",
                new String[] {id, fromId} );
        closeMessagesTable();
        //return result;
        openMessagesTable();
        int result2 = myDb.update(TABLE_MESSAGES, contentValues, "" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?",
                new String[] { fromId, id } );
        closeMessagesTable();
        return result1 + result2;
    }

    private int deleteAllMessagesById(String id) {
        openMessagesTable();
        String fromId = Account.getCurrentUser().getObjectId();
        int result1 = myDb.delete(TABLE_MESSAGES, "" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?",
                new String[] { id, fromId });
        int result2 = myDb.delete(TABLE_MESSAGES, "" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?",
                new String[]{fromId, id});
        int result = result1 + result2;
        Logger.error("MySQLiteHelper", "A total of  " + result + " messages were deleted from internal database for contact " + id);
        closeMessagesTable();
        return result;
    }

    public List<Message> getMessagesByContact(String id, int count, int offset) throws SQLException {
        String fromId = Account.getCurrentUser().getObjectId();
        ArrayList<Message> messages = new ArrayList<>();
        openMessagesTable();
        String query = "SELECT m.* FROM "
                + TABLE_MESSAGES + " m"
                + " WHERE m." + sMessageFromUserId + " = \'" + id + "\' AND m." + sMessageToUserId + " = \'" + fromId + "\'"
                + " UNION ALL " +
                "SELECT p.* FROM "
                + TABLE_MESSAGES + " p"
                + " WHERE p." + sMessageFromUserId + " = \'" + fromId + "\' AND p." + sMessageToUserId + " = \'" + id + "\'"
                + " ORDER BY " + sMessageCreatedAt + " DESC LIMIT " + count + " OFFSET " + (offset * count);
        Cursor cursor = myDb.rawQuery(query, new String[]{});
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Message contact = cursorToMessage(cursor);
            messages.add(contact);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        closeMessagesTable();
        return messages;
    }

    private Message cursorToMessage(Cursor cursor) {
        Message message = new Message();
        message.setId(cursor.getLong(0));
        message.setFromUserId(cursor.getString(1));
        message.setToUserId(cursor.getString(2));
        message.setMessageType(cursor.getString(3));
        message.setDeliveryStatus(cursor.getString(4));
        message.setBody(cursor.getString(5));
        message.setImageFileId(cursor.getString(6));
        message.setLongitude(cursor.getFloat(7));
        message.setLatitude(cursor.getFloat(8));
        message.setCreated(cursor.getLong(9));
        message.setModified(cursor.getLong(10));
        message.setReadAt(cursor.getLong(11));
        return message;
    }

    /************************************************************/
    /*******************CREATE/UPGRADE METHODS*******************/
    /************************************************************/

    public void setMessageListener(OnMessageTaskCompleted listener) {
        messageListeners = listener;
    }

    public void removeMessageListener () {
        messageListeners = null;
    }

    @UiThread
    public void notifyMessageListeners(MessageResponse response) {
        switch (response.getActionCode()) {
            case Message.ACTION_MESSAGE_SAVE:
                if (messageListeners != null) {
                    messageListeners.MessageSent(response);
                }
                break;
            case Message.ACTION_MESSAGE_UPDATE:
                if (messageListeners != null) {
                    messageListeners.MessageUpdated(response);
                }
                break;
            case Message.ACTION_MESSAGE_DELETE:
                if (messageListeners != null) {
                    messageListeners.MessageDeleted(response);
                }
                break;
            case Message.ACTION_MESSAGE_RETRIEVE_ALL:
                if (messageListeners != null) {
                    messageListeners.MessagesGetAll(response);
                }
                break;
            default:
                Log.e(TAG, "notifyMessageListeners: " + response.getActionCode() + "\nObjeto puede ser null: " + response);
                break;
        }
    }

    public void setContactListener(OnContactTaskCompleted listener) {
        contactListeners = listener;
    }

    public void removeContactListener () {
        contactListeners = null;
    }

    @UiThread
    public void notifyContactListeners(ContactResponse response) {
        switch (response.getActionCode()) {
            case dBusiness.ACTION_MESSAGE_SAVE:
                if (contactListeners != null) {
                    contactListeners.ContactAdded(response);
                }
                break;
            case dBusiness.ACTION_MESSAGE_UPDATE:
                if (contactListeners != null) {
                    contactListeners.ContactUpdated(response);
                }
                break;
            case dBusiness.ACTION_MESSAGE_DELETE:
                if (contactListeners != null) {
                    contactListeners.ContactDeleted(response);
                }
                break;
            case dBusiness.ACTION_MESSAGE_RETRIEVE_ALL:
                if (contactListeners != null) {
                    contactListeners.ContactGetAll(response);
                }
                break;
            default:
                Log.e(TAG, "notifyContactListeners: " + response.getActionCode() + "\nObjeto puede ser null: " + response);
                break;
        }
    }

    private static class DatabaseHelperMessages extends SQLiteOpenHelper {

        DatabaseHelperMessages(Context context) {
            super(context, DATABASE_NAME1, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLE_MESSAGES_CREATE);
            db.execSQL(tmIndex1);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Logger.error(TAG, "Upgrading database MESSAGES from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
            onCreate(db);
        }
    }

    private static class DatabaseHelperContacts extends SQLiteOpenHelper {

        DatabaseHelperContacts(Context context) {
            super(context, DATABASE_NAME2, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLE_CONTACTS_CREATE);
            db.execSQL(tcIndex1);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Logger.error(TAG, "Upgrading database CONTACTS from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CV_CONTACTS);
            onCreate(db);
        }
    }
}