package ee.app.conversa.management;

/**
 * Created by edgargomez on 2/11/15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.model.Database.dBusiness;
import ee.app.conversa.model.Database.Location;
import ee.app.conversa.model.Database.Message;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;

public class MySQLiteHelper {

    private static final String TAG = "MySQLiteHelper";
    private final Context context;
    private DatabaseHelperMessages myDbHelperForMessages;
    private DatabaseHelperContacts myDbHelperForContacts;
    private DatabaseHelperLocation myDbHelperForLocation;
    private SQLiteDatabase myDb;

    private static final String DATABASE_NAME1 = "userMessagesdb.db";
    private static final String DATABASE_NAME2 = "userContactsdb.db";
    private static final String DATABASE_NAME4 = "userLocationdb.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MESSAGES = "message";
    private static final String TABLE_CV_CONTACTS = "cv_contact";
    private static final String TABLE_CV_LOCATIONS = "location";

    private static final String COLUMN_ID = "_id";

    // Database creation sql statement
    private static final String TABLE_MESSAGES_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_MESSAGES + "("
            + "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "\"from_user_id\" INTEGER NOT NULL, "
            + "\"to_user_id\" INTEGER NOT NULL, "
            + "\"from_user_type\" INTEGER NOT NULL, "
            + "\"body\" TEXT NOT NULL, "
            + "\"message_target_type\" VARCHAR(10) NOT NULL, "
            + "\"message_type\" VARCHAR(10) NOT NULL, "
            + "\"picture_file_id\" VARCHAR(255) NOT NULL, "
            + "\"longitude\" FLOAT NOT NULL, "
            + "\"latitude\" FLOAT NOT NULL, "
            + "\"created\" INTEGER NOT NULL, "
            + "\"modified\" INTEGER NOT NULL, "
            + "\"read_at\" INTEGER NOT NULL DEFAULT '0' ); ";
    private static final String tmIndex1 = "CREATE INDEX M_search on "  + TABLE_MESSAGES + "(from_user_id, from_user_type, to_user_id); ";
    private static final String tmIndex2 = "CREATE INDEX M_readAt on "  + TABLE_MESSAGES + "(from_user_id, from_user_type, read_at); ";


    // CONTACTS
    public static final String sBusinessId = "_id";
    public static final String sBusinessObjectId = "objectId";
    public static final String sBusinessBusinessId = "businessId";
    public static final String sBusinessDisplayName = "displayName";
    public static final String sBusinessConversaId = "conversaId";
    public static final String sBusinessRecent = "recent";
    public static final String sBusinessAbout = "about";
    public static final String sBusinessStatus = "statusMessage";
    public static final String sBusinessComposingMessage = "composingMessageString";
    public static final String sBusinessBlocked = "blocked";
    public static final String sBusinessMuted = "muted";

    private static final String TABLE_CONTACTS_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_CV_CONTACTS + "("
            + "\"" + sBusinessId + "\" INTEGER PRIMARY KEY, "
            + "\"" + sBusinessObjectId + "\" CHAR(10) NOT NULL, "
            + "\"" + sBusinessBusinessId + "\" CHAR(10) NOT NULL, "
            + "\"" + sBusinessDisplayName + "\" VARCHAR(180) NOT NULL, "
            + "\"" + sBusinessConversaId + "\" VARCHAR(255) NOT NULL, "
            + "\"" + sBusinessRecent + "\" INTEGER NOT NULL, "
            + "\"" + sBusinessAbout + "\" VARCHAR(255) NOT NULL, "
            + "\"" + sBusinessStatus + "\" VARCHAR(255) NOT NULL, "
            + "\"" + sBusinessComposingMessage + "\" VARCHAR(255) NOT NULL, "
            + "\"" + sBusinessBlocked + "\" CHAR(1) NOT NULL DEFAULT 'N', "
            + "\"" + sBusinessMuted + "\" CHAR(1) NOT NULL DEFAULT 'N' ); ";
    private static final String tcIndex1 = "CREATE INDEX C_objectId on "     + TABLE_CV_CONTACTS + "(" + sBusinessObjectId + "); ";
    private static final String tcIndex2 = "CREATE INDEX C_businessId on "  + TABLE_CV_CONTACTS + "(" + sBusinessBusinessId + ");";
    private static final String tcIndex3 = "CREATE INDEX C_displayName on "  + TABLE_CV_CONTACTS + "(" + sBusinessDisplayName + ");";
    private static final String tcIndex4 = "CREATE INDEX C_conversaId on "  + TABLE_CV_CONTACTS + "(" + sBusinessConversaId + ");";
    private static final String tcIndex5 = "CREATE INDEX C_recent on "  + TABLE_CV_CONTACTS + "(" + sBusinessRecent + ");";

    private static final String TABLE_LOCATIONS_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_CV_LOCATIONS + "("
            + "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "\"id_business\" INTEGER NOT NULL, "
            + "\"id_location\" INTEGER NOT NULL, "
            + "\"name\"        VARCHAR(25) NOT NULL, "
            + "\"address\"     VARCHAR(150) NOT NULL, "
            + "\"valid\"       TINYINT NOT NULL DEFAULT '1' ); ";
    private static final String tlIndex1 = "CREATE INDEX L_location on " + TABLE_CV_LOCATIONS + "(id_business,valid); ";

    /************************************************************/
    /*********************OPEN/CLOSE METHODS*********************/
    /************************************************************/

    public MySQLiteHelper(Context context) {
        this.context = context;
        myDbHelperForMessages = new DatabaseHelperMessages(context);
        myDbHelperForContacts = new DatabaseHelperContacts(context);
        myDbHelperForLocation = new DatabaseHelperLocation(context);

        openMessagesTable();
        closeMessagesTable();
        openContactsTable();
        closeContactsTable();
        openLocationTable();
        closeLocationTable();
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

    public MySQLiteHelper openLocationTable() throws SQLException {
        myDb = myDbHelperForLocation.getWritableDatabase();
        return this;
    }

    public void closeLocationTable() {
        if (myDbHelperForLocation != null) { myDbHelperForLocation.close(); }
    }

    public boolean deleteDatabase(){
        context.deleteDatabase(DATABASE_NAME1);
        context.deleteDatabase(DATABASE_NAME2);
        context.deleteDatabase(DATABASE_NAME4);
        return true;
    }

    /************************************************************/
    /*********************OPERATIONS METHODS*********************/
    /************************************************************/
    private dBusiness cursorToUser(Cursor cursor) {
        dBusiness contact = new dBusiness();
        contact.setId(cursor.getLong(0));
        contact.setObjectId(cursor.getString(1));
        contact.setBusinessId(cursor.getString(2));
        contact.setDisplayName(cursor.getString(3));
        contact.setConversaId(cursor.getString(4));
        contact.setRecent(cursor.getLong(5));
        contact.setAbout(cursor.getString(6));
        contact.setStatusMessage(cursor.getString(7));
        contact.setComposingMessage(cursor.getString(8));
        boolean b = cursor.getString(9).contentEquals("Y");
        contact.setBlocked(b);
        b = cursor.getString(10).contentEquals("Y");
        contact.setMuted(b);
        return contact;
    }

    public boolean addContact(dBusiness user) {
        ContentValues contact = new ContentValues();
        contact.put(sBusinessObjectId, user.getObjectId());
        contact.put(sBusinessBusinessId, user.getBusinessId());
        contact.put(sBusinessDisplayName, user.getDisplayName());
        contact.put(sBusinessConversaId, user.getConversaId());
        contact.put(sBusinessRecent, user.getRecent());
        contact.put(sBusinessAbout, user.getAbout());
        contact.put(sBusinessStatus, user.getStatusMessage());
        contact.put(sBusinessComposingMessage, user.getComposingMessage());
        contact.put(sBusinessBlocked, user.isBlocked());
        contact.put(sBusinessMuted, user.isMuted());

        openContactsTable();
        long result = myDb.insert(TABLE_CV_CONTACTS, null, contact);
        closeContactsTable();

        return result != -1;
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

    public boolean deleteContactById(String id) {
        openContactsTable();
        int result = myDb.delete(TABLE_CV_CONTACTS, sBusinessId + " = ? ", new String[]{id});
        closeContactsTable();
        if( result == 1 ) {
            deleteAllMessagesById(id);
            return true;
        }else {
            return false;
        }
    }

    public dBusiness isContact(String id) {
        openContactsTable();
        Cursor cursor = myDb.query(TABLE_CV_CONTACTS, null, sBusinessId + " = ?", new String[]{id}, null, null, null);
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

    public boolean callForMessages(String id) {
        openContactsTable();
        Cursor cursor = myDb.query(TABLE_CV_CONTACTS, new String[] {"stopCallingForMessages"}, sBusinessId + " = ?",new String[] { id },null,null,null);
        cursor.moveToFirst();
        int has = 0;

        while (!cursor.isAfterLast()) {
            has = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();
        closeContactsTable();

        return (has == 0);
    }

    public void setCallNoMore(String id) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("stopCallingForMessages", 1);
        openContactsTable();
        myDb.update(TABLE_CV_CONTACTS, contentValues, "_id = ? ", new String[]{id});
        closeContactsTable();
    }

    public boolean hasPendingMessages(String id) {
        openContactsTable();
        Cursor cursor = myDb.query(TABLE_CV_CONTACTS, new String[] {"hasPendingMessages"}, sBusinessId + " = ?",new String[] { id },null,null,null);
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

    public void updateAvatarFileId(String avatar, String id){
        ContentValues contentValues = new ContentValues();
        contentValues.put("avatar_thumb_file_id", avatar);
        openContactsTable();
        myDb.update(TABLE_CV_CONTACTS, contentValues, "_id = ? ", new String[]{id});
        closeContactsTable();
    }
    /* ******************************************* */
    /* ******************************************* */
    /* ******************************************* */
    public List<Location> getLocations(String business_id) {
        List<Location> locations = new ArrayList<>();
        String query = "SELECT _id, id_business, id_location, name, address FROM " + TABLE_CV_LOCATIONS
                       + " WHERE id_business = " + business_id + " AND valid = 1";
        openLocationTable();
        Cursor cursor = myDb.rawQuery(query, new String[]{});
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Location location = new Location();
            location.setId(String.valueOf(cursor.getInt(0)));
            location.setBusinessId(String.valueOf(cursor.getInt(1)));
            location.setLocationId(cursor.getInt(2));
            location.setName(cursor.getString(3));
            location.setAddress(cursor.getString(4));
            locations.add(location);
            cursor.moveToNext();
        }

        // make sure to close the cursor
        cursor.close();
        closeLocationTable();

        return locations;
    }

    public void addLocation(Location location) throws SQLiteConstraintException {
        Location prevlocation = null;
        String query = "SELECT _id, id_business, id_location, name, address FROM " + TABLE_CV_LOCATIONS
                + " WHERE id_business = " + location.getBusinessId() + " AND id_location = " + location.getLocationId();
        openLocationTable();
        Cursor cursor = myDb.rawQuery(query, new String[]{});
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            prevlocation = new Location();
            cursor.moveToNext();
        }

        // make sure to close the cursor
        cursor.close();

        ContentValues locat = new ContentValues();
        locat.put("id_business", location.getBusinessId());
        locat.put("id_location", location.getLocationId());
        locat.put("name", location.getName());
        locat.put("address", location.getAddress());
        if(prevlocation == null) {
            myDb.insert(TABLE_CV_LOCATIONS, null, locat);
        } else {
            locat.put("valid", 1);
            myDb.update(TABLE_CV_LOCATIONS, locat, "_id = ? AND valid = ?",
                    new String[]{location.getId(), String.valueOf(1)} );
        }

        closeLocationTable();
    }

    public boolean invalidLocation(String id, int valid) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("valid", valid);
        openLocationTable();
        myDb.update(TABLE_CV_LOCATIONS, contentValues, "_id = ? AND valid = ?",
                new String[]{id, String.valueOf(valid)});
        closeMessagesTable();
        return true;
    }

    public void addMessage(Message messages) throws SQLiteConstraintException {
        ContentValues message = new ContentValues();
        message.put("from_user_id", messages.getFromUserId());
        message.put("to_user_id", messages.getToUserId());
        message.put("from_user_type", messages.getType());
        message.put("body", messages.getBody());
        message.put("message_target_type", messages.getMessageTargetType());
        message.put("message_type", messages.getMessageType());
        message.put("picture_file_id", messages.getImageFileId());
        message.put("longitude", messages.getLongitude());
        message.put("latitude", messages.getLatitude());
        message.put("created", messages.getCreated());
        message.put("modified", messages.getModified());
        message.put("read_at", messages.getReadAt());

        openMessagesTable();
        myDb.insert(TABLE_MESSAGES, null, message);
        closeMessagesTable();
    }

    public int messageCountForContact(String id) {
        String query = "SELECT COUNT(*) FROM " + TABLE_MESSAGES + " WHERE from_user_id = " + id + " AND from_user_type = " + Const.C_TYPE_TO;
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
        String fromId = ConversaApp.getPreferences().getUserId();
        //if(fromId.isEmpty())
            //fromId = UsersManagement.getLoginUser().getId();

        Message message = null;
        openMessagesTable();
        String query = "SELECT m.* FROM "
                        + TABLE_MESSAGES + " m"
                        + " WHERE m.from_user_id = " + id + " AND m.from_user_type = " + Const.C_TYPE_TO + " AND m.to_user_id = " + fromId
                + " UNION " +
                        "SELECT p.* FROM "
                        + TABLE_MESSAGES + " p"
                        + " WHERE p.from_user_id = " + fromId + " AND p.from_user_type = " + Const.C_TYPE + " AND p.to_user_id = " + id
                        + " ORDER BY created DESC LIMIT 1";
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

    public boolean hasUnreadMessages(String id){
        String query = "SELECT COUNT(*) FROM " + TABLE_MESSAGES + " WHERE from_user_id = " + id + " AND from_user_type = " + Const.C_TYPE_TO + " AND read_at = 0";
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

        return (count > 0) ? true : false;
    }

    public boolean hasUnreadMessagesOrNewMessages(String id){
        String query = "SELECT COUNT(*) FROM " + TABLE_MESSAGES + " WHERE from_user_id = " + id + " AND from_user_type = " + Const.C_TYPE_TO + " AND read_at = 0";
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

        if(count == 0) {
            openContactsTable();
            cursor = myDb.query(TABLE_CV_CONTACTS, new String[] {"hasPendingMessages"}, COLUMN_ID + " = ?",new String[] { id },null,null,null);
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                count = cursor.getInt(0);
                cursor.moveToNext();
            }

            cursor.close();
            closeContactsTable();
        }

        return (count > 0);
    }

    public boolean updateReadMessages(String id) {
        ContentValues contentValues = new ContentValues();
        GregorianCalendar now = new GregorianCalendar();
        long currentTimestamp = now.getTimeInMillis() / 1000;
        contentValues.put("read_at", currentTimestamp);
        openMessagesTable();
        String fromId = ConversaApp.getPreferences().getUserId();
        //if(fromId.isEmpty())
            //fromId = UsersManagement.getLoginUser().getId();

        myDb.update(TABLE_MESSAGES, contentValues, "from_user_id = ? AND from_user_type = ? AND to_user_id = ?",
                new String[] { id,String.valueOf(Const.C_TYPE_TO), fromId} );
        closeMessagesTable();
        return true;
    }

    public boolean updateReadMessagesMe(String id) {
        ContentValues contentValues = new ContentValues();
        GregorianCalendar now = new GregorianCalendar();
        long currentTimestamp = now.getTimeInMillis() / 1000;
        contentValues.put("read_at", currentTimestamp);
        openMessagesTable();
        String fromId = ConversaApp.getPreferences().getUserId();
        //if(fromId.isEmpty())
            //fromId = UsersManagement.getLoginUser().getId();

        myDb.update(TABLE_MESSAGES, contentValues, "from_user_id = ? AND from_user_type = ? AND to_user_id = ?",
                new String[] { fromId, String.valueOf(Const.C_TYPE), id } );
        closeMessagesTable();
        return true;
    }

    private void deleteAllMessagesById(String id) {
        openMessagesTable();
        String fromId = ConversaApp.getPreferences().getUserId();
        //if(fromId.isEmpty())
        //    fromId = UsersManagement.getLoginUser().getId();

        int result1 = myDb.delete(TABLE_MESSAGES, "from_user_id = ? AND from_user_type = ? AND to_user_id = ?",
                new String[] { id, String.valueOf(Const.C_TYPE_TO), fromId });
        int result2 = myDb.delete(TABLE_MESSAGES, "from_user_id = ? AND from_user_type = ? AND to_user_id = ?",
                new String[]{fromId, String.valueOf(Const.C_TYPE), id});
        int result = result1 + result2;
        Logger.error("MySQLiteHelper", "A total of  " + result + " messages were deleted from internal database for contact " + id);
        closeMessagesTable();
    }

    public ArrayList<Message> getMessagesByContact(String id, int count, int offset) throws SQLException {
        String fromId = ConversaApp.getPreferences().getUserId();
        //if(fromId.isEmpty())
//            fromId = getUserId();

        ArrayList<Message> messages = new ArrayList<>();
        openMessagesTable();
        String query = "SELECT m.* FROM "
                        + TABLE_MESSAGES + " m"
                        + " WHERE m.from_user_id = " + id + " AND m.from_user_type = " + Const.C_TYPE_TO + " AND m.to_user_id = " + fromId
                        + " UNION " +
                        "SELECT p.* FROM "
                        + TABLE_MESSAGES + " p"
                        + " WHERE p.from_user_id = " + fromId + " AND p.from_user_type = " + Const.C_TYPE + " AND p.to_user_id = " + id
                        + " ORDER BY created DESC LIMIT " + count + " OFFSET " + offset;
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
        message.setFromUserId(Integer.toString(cursor.getInt(1)));
        message.setToUserId(Integer.toString(cursor.getInt(2)));
        message.setType(String.valueOf(cursor.getString(3)));
        message.setBody(cursor.getString(4));
        message.setMessageTargetType(cursor.getString(5));
        message.setMessageType(cursor.getString(6));
        message.setImageFileId(cursor.getString(7));
        message.setLongitude(Float.toString(cursor.getFloat(8)));
        message.setLatitude(Float.toString(cursor.getFloat(9)));
        String number = Integer.toString(cursor.getInt(10));
        message.setCreated(Long.valueOf(number));
        number = Integer.toString(cursor.getInt(11));
        message.setModified(Long.valueOf(number));
        message.setReadAt(cursor.getInt(12));

        return message;
    }
    /************************************************************/
    /*******************CREATE/UPGRADE METHODS*******************/
    /************************************************************/

    private static class DatabaseHelperMessages extends SQLiteOpenHelper {

        DatabaseHelperMessages(Context context) {
            super(context, DATABASE_NAME1, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLE_MESSAGES_CREATE);
            db.execSQL(tmIndex1);
            db.execSQL(tmIndex2);
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
            db.execSQL(tcIndex2);
            db.execSQL(tcIndex3);
            db.execSQL(tcIndex4);
            db.execSQL(tcIndex5);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Logger.error(TAG, "Upgrading database CONTACTS from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CV_CONTACTS);
            onCreate(db);
        }
    }

    private static class DatabaseHelperLocation extends SQLiteOpenHelper {

        DatabaseHelperLocation(Context context) {
            super(context, DATABASE_NAME4, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLE_LOCATIONS_CREATE);
            db.execSQL(tlIndex1);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Logger.error(TAG, "Upgrading database LOCATION from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CV_LOCATIONS);
            onCreate(db);
        }
    }
}