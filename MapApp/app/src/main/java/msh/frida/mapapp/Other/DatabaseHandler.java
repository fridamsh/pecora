package msh.frida.mapapp.Other;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import msh.frida.mapapp.Models.HikeModel;

/**
 * Created by Frida on 07/11/2017.
 */

// We need to write our own class to handle all database CRUD(Create, Read, Update and Delete) operations
public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "hikesManager";

    // Contacts table name
    private static final String TABLE_HIKES = "hikes";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_NAME = "name";
    private static final String KEY_PARTICIPANTS = "participants";
    private static final String KEY_WEATHER = "weather";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_START_DATE = "startdate";
    private static final String KEY_END_DATE = "enddate";
    private static final String KEY_MAP_FILE = "mapfile";
    // List<ObservationPoint> KEY_OBSERVATIONS
    // List<GeoPoint> KEY_TRACK

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        // This is where we need to write create table statements. This is called when the database is created.
        String CREATE_HIKES_TABLE = "CREATE TABLE " + TABLE_HIKES + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " TEXT,"
                + KEY_NAME + " TEXT,"
                + KEY_PARTICIPANTS + " TEXT,"
                + KEY_WEATHER + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_START_DATE + " TEXT,"
                + KEY_END_DATE + " TEXT,"
                + KEY_MAP_FILE + " TEXT" + ")";
        db.execSQL(CREATE_HIKES_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This method is called when the database is upgraded like modifying the table structure, adding constraints to database etc.

        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HIKES);

        // Create tables again
        onCreate(db);
    }

    // Adding new contact
    public void addHike(HikeModel hike) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, hike.getTitle());
        values.put(KEY_NAME, hike.getName());
        values.put(KEY_PARTICIPANTS, hike.getNumberOfParticipants());
        values.put(KEY_WEATHER, hike.getWeatherState());
        values.put(KEY_DESCRIPTION, hike.getDescription());
        values.put(KEY_START_DATE, hike.getDateStart());
        values.put(KEY_END_DATE, hike.getDateEnd());
        values.put(KEY_MAP_FILE, hike.getMapFileName());

        // Inserting row
        db.insert(TABLE_HIKES, null, values);
        db.close();

    }

    // Getting single contact
    public HikeModel getHike(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_HIKES,
                new String[] { KEY_ID, KEY_TITLE, KEY_NAME, KEY_PARTICIPANTS, KEY_WEATHER, KEY_DESCRIPTION, KEY_START_DATE, KEY_END_DATE, KEY_MAP_FILE },
                KEY_ID + "=?",
                new String[] { String.valueOf(id) },
                null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        HikeModel hike = new HikeModel(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), Integer.parseInt(cursor.getString(3)),
                cursor.getString(4), cursor.getString(5), Long.valueOf(cursor.getString(6)), Long.valueOf(cursor.getString(7)), cursor.getString(8));
        return hike;
    }

    // Getting All Contacts
    public List<HikeModel> getAllHikes() {
        List<HikeModel> hikeList = new ArrayList<HikeModel>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_HIKES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                HikeModel hike = new HikeModel();
                hike.setId(Integer.parseInt(cursor.getString(0)));
                hike.setTitle(cursor.getString(1));
                hike.setName(cursor.getString(2));
                hike.setNumberOfParticipants(Integer.parseInt(cursor.getString(3)));
                hike.setWeatherState(cursor.getString(4));
                hike.setDescription(cursor.getString(5));
                hike.setDateStart(Long.valueOf(cursor.getString(6)));
                hike.setDateEnd(Long.valueOf(cursor.getString(7)));
                hike.setMapFileName(cursor.getString(8));
                // Adding contact to list
                hikeList.add(hike);
            } while (cursor.moveToNext());
        }

        return hikeList;
    }

    // Getting contacts Count
    public int getHikesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_HIKES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

    // Updating single contact
    public int updateHike(HikeModel hike) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, hike.getTitle());
        values.put(KEY_NAME, hike.getName());
        values.put(KEY_PARTICIPANTS, hike.getNumberOfParticipants());
        values.put(KEY_WEATHER, hike.getWeatherState());
        values.put(KEY_DESCRIPTION, hike.getDescription());
        values.put(KEY_START_DATE, hike.getDateStart());
        values.put(KEY_END_DATE, hike.getDateEnd());
        values.put(KEY_MAP_FILE, hike.getMapFileName());

        // Updating row
        return db.update(TABLE_HIKES, values, KEY_ID + " = ?", new String[] { String.valueOf(hike.getId()) });
    }

    // Deleting single contact
    public void deleteHike(HikeModel hike) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HIKES, KEY_ID + " = ?", new String[] { String.valueOf(hike.getId()) });
        db.close();
    }

}
