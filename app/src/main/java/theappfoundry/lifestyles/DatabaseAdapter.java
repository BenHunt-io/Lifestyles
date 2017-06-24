package theappfoundry.lifestyles;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static android.content.ContentValues.TAG;
import static android.database.Cursor.FIELD_TYPE_FLOAT;
import static android.database.Cursor.FIELD_TYPE_INTEGER;
import static android.database.Cursor.FIELD_TYPE_STRING;

/**
 * Created by Ben on 6/14/2017.
 * Outerclass to act as interface to the innerclass database
 */

public class DatabaseAdapter{

    private MyDatabaseHelper myDatabaseHelper; // handler reference to inner class
    private static SQLiteDatabase db; // database reference available to entire class
    private Context context;


    public DatabaseAdapter(Context context){

        this.context = context;
        // Get a reference to inner class, pass in context
        myDatabaseHelper = new MyDatabaseHelper(context); // instance of inner class
        db = myDatabaseHelper.getWritableDatabase(); // Open database when app starts

//        db.execSQL("DROP TABLE " + myDatabaseHelper.RECT_GEOTABLE);
//        db.execSQL("DROP TABLE " + myDatabaseHelper.LOCATION_STATS_TBL);
//        db.execSQL("DROP TABLE " + myDatabaseHelper.LAST_LOCATION_TBL);
//        db.close();


    }

    /**
     * Inserts a row, default values for latLong ect.
     */
    public long insertLocation(String location, int fill, int stroke, double topLeftLat,
                               double topLeftLong, double topRightLat, double topRightLong,
                               double botLeftLat, double botLeftLong, double botRightLat,
                               double botRightLong){
        //db.execSQL(MyDatabaseHelper.CREATE_TABLE); // DB is already created need to recreate table
        Log.d(TAG, "insertData: Test");
        ContentValues contentValues = new ContentValues();
        contentValues.put(myDatabaseHelper.LOCATION, location);
        contentValues.put(myDatabaseHelper.FILL, fill);
        contentValues.put(myDatabaseHelper.STROKE, stroke);
        contentValues.put(MyDatabaseHelper.BOTLEFTLAT, topLeftLat);
        contentValues.put(MyDatabaseHelper.BOTLEFTLONG, topLeftLong);
        contentValues.put(MyDatabaseHelper.TOPLEFTLAT, topRightLat);
        contentValues.put(MyDatabaseHelper.TOPLEFTLONG, topRightLong);
        contentValues.put(MyDatabaseHelper.TOPRIGHTLAT, botLeftLat);
        contentValues.put(MyDatabaseHelper.TOPRIGHTLONG, botLeftLong);
        contentValues.put(MyDatabaseHelper.BOTRIGHTLAT, botRightLat);
        contentValues.put(MyDatabaseHelper.BOTRIGHTLONG, botRightLong);
        long result = db.insert(MyDatabaseHelper.RECT_GEOTABLE, null, contentValues);

        return result; // -1 if failed, otherwise returns index
    }

    /**
     * Deletes a table
     */
    public void deleteTable(){
        try {
            db.execSQL("DROP TABLE " + MyDatabaseHelper.RECT_GEOTABLE);
        }catch(SQLException e){
            Log.d(TAG, "deleteTable: " + e);
        }
        Log.d(TAG, "deleteTable: Table Dropped");
    }

    /**
     * Icon isn't set when the row is made. So we have to update the default value to the real value
     * @param location
     * @param id
     */
    public void updateIcon(String location, int id){
        db.execSQL("UPDATE " + myDatabaseHelper.RECT_GEOTABLE +" SET " + MyDatabaseHelper.ICON +
                "= " +id+ " WHERE " +myDatabaseHelper.LOCATION+ " = '"+ location +"';");

    }

    public String[] getAllLocations(){

        // Gets a cursor pointing to all Geofence location saved
        Cursor cursor = db.rawQuery("SELECT " + MyDatabaseHelper.LOCATION + " FROM " +
                MyDatabaseHelper.LOCATION_STATS_TBL, null);

        cursor.moveToFirst();

        String[] allLocations = new String[cursor.getCount()];

        int i = 0;
        // Store and return all the geofence locations from the subtable the cursor is pointing at
        do {
            allLocations[i] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.LOCATION));
            i++;
        }while(cursor.moveToNext());

        return allLocations;

    }


    //////////// WHEN I COME BACK I NEED TO LOOK at getting stroke/fill ////////////////
    public ArrayList<PolygonOptions> getPolygonRectOptions(){
        ArrayList<PolygonOptions> polygonOptions = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT "+MyDatabaseHelper.POLYGON_OPTIONS+
                " FROM "+MyDatabaseHelper.RECT_GEOTABLE,null);

        cursor.moveToFirst();
        if(!(cursor.getCount() >= 1))
            return null;
        int fill = 0;
        int stroke = 0;
        int columnCount = cursor.getColumnCount(); // retrieves columnCount
        Log.d(TAG, "getPolygonRectOptions: " + columnCount + "      "+ cursor.getString(0));
        LatLng[] latLngs = new LatLng[4];
        do {
            for (int i = 0, j = 0; i < columnCount; i++) {
                Log.d(TAG, "getPolygonRectOptions: ");
                switch(cursor.getColumnName(i)){
                    case MyDatabaseHelper.FILL:
                        fill = cursor.getInt(i);
                        break;
                    case MyDatabaseHelper.STROKE:
                        stroke = cursor.getInt(i);
                        break;
                }
                if (i >= 2) { // if fill & stroke have already been accounted for
                    latLngs[j] = new LatLng(cursor.getDouble(i), cursor.getDouble(i + 1));
                    i++; // Need to increment by two, since we are accessing two indexes at a time
                    j++;
                    Log.d(TAG, "getPolygonRectOptions: " + latLngs[j-1]);
                }
            }
            // Instantiates a new Polygon object and adds points to define a rectangle
            polygonOptions.add(new PolygonOptions().add(latLngs[0], latLngs[1], latLngs[2], latLngs[3])
                    .fillColor(fill)
                    .strokeColor(stroke));
            Log.d(TAG, "getPolygonRectOptions: made poly");




        }
        while(cursor.moveToNext());

        return polygonOptions;
    }


    /**
     * Each Location has a LatLng associated with it that represents the center of the geofence
     * @return HashMap of Location/LatLng Pairs for Geofence Locations
     */
    public HashMap<String,LatLng> getGeofenceLatLng(){

        HashMap<String, LatLng> geofenceLatLng = new HashMap<>();

        Cursor cursor = db.rawQuery("SELECT  " + MyDatabaseHelper.LOCATION +","+MyDatabaseHelper.LatLng
                + " FROM " + MyDatabaseHelper.RECT_GEOTABLE, null);

        int columnCount = cursor.getColumnCount();
        cursor.moveToFirst();

        double sumLat = 0;
        double sumLong = 0;

        if(cursor.getCount() > 0) {
            do {
                for (int i = 1; i < columnCount; i += 2) {
                    sumLat += cursor.getDouble(i);
                    sumLong += cursor.getDouble(i + 1);
                    Log.d(TAG, "getGeofenceLatLng: " + cursor.getDouble(i));
                    Log.d(TAG, "getGeofenceLatLng: " + cursor.getDouble(i+1));
                }
                LatLng latLng = new LatLng(sumLat / 4, sumLong / 4);
                String location = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.LOCATION));
                geofenceLatLng.put(location, latLng);

            } while (cursor.moveToNext());
        }


        return geofenceLatLng;
    }


    /**
     * Makes a query on the database on all the columns and returns a cursor. Cursor is then set to
     * the first Column. Loop through the columns and append the data to the stringbuffer depending
     * on the datatype. After an entire record/row has been processed, move cursor to the next row
     * Lastly, cast stringbuffer to string and toast it to the user
     */
    public void displayTable(){
        int type;
        float floatVal;
        String stringVal;
        int intVal;
        StringBuffer buffer = new StringBuffer(""); // If theres nothing to query .. empty string
        Cursor cursor = db.rawQuery("SELECT * FROM "+MyDatabaseHelper.RECT_GEOTABLE, null);
        cursor.moveToFirst(); // otherwise it starts at -1 index
        do{
            int count  = cursor.getColumnCount();
            for(int i = 0; i< count; i++) {
                type = cursor.getType(i);
                switch (type) {
                    case FIELD_TYPE_FLOAT: floatVal = cursor.getFloat(i);
                        buffer.append(" " + floatVal);
                        break;
                    case FIELD_TYPE_INTEGER: intVal = cursor.getInt(i);
                        buffer.append("   " + intVal);
                        break;
                    case FIELD_TYPE_STRING: stringVal = cursor.getString(i);
                        buffer.append(" " + stringVal);
                        break;
                    default: break;
                }
                if(i == count - 1)
                    buffer.append("\n");
            }
        }while(cursor.moveToNext());

        String table = buffer.toString();
        Toast.makeText(MyDatabaseHelper.context, table, Toast.LENGTH_LONG).show();
    }

    /**
     * Executes the sql statement defined in MyDatabaseHelper which creates a Table
     */
    public void addTable(){
        db.execSQL(MyDatabaseHelper.CREATE_GEOTABLE);
        Log.d(TAG, "addTable: ");

    }



    public void dropTable(){
        db.execSQL("DROP TABLE "+MyDatabaseHelper.RECT_GEOTABLE);
    }

    /**
     * Makes MarkerOptions which you add to a map to make a marker
     * @return googleMaps MarkerOptions
     */
    public ArrayList<MarkerOptions> getMarkerOptions(){

        ArrayList<MarkerOptions> markerOptions = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT "+ MyDatabaseHelper.LOCATION +"," + MyDatabaseHelper.ICON + "," +
            MyDatabaseHelper.LatLng + " FROM "+ MyDatabaseHelper.RECT_GEOTABLE, null);

        double sumLat = 0 , sumLong = 0;
        cursor.moveToFirst();
        int columnCount = cursor.getColumnCount();

        if(!(cursor.getCount() >= 1))
            return null;

        do{
            // Move to beginning of latlng cordinates in table
           // cursor.moveToPosition(cursor.getColumnIndex(MyDatabaseHelper.BOTLEFTLAT));
            Log.d(TAG, "getMarkerOptions:  " + cursor.getColumnNames());
            Log.d(TAG, "getMarkerOptions: " + cursor.getDouble(2));
            for(int i = 2; i < 10; i += 2){
                sumLat += cursor.getDouble(i);
                sumLong += cursor.getDouble(i+1);
            }
            LatLng latLng = new LatLng(sumLat/4, sumLong/4);

            Geofencing geofencing = new Geofencing();
            int iconID = cursor.getInt(cursor.getColumnIndex(MyDatabaseHelper.ICON));

            Drawable drawable = ContextCompat.getDrawable(context, iconID);
            BitmapDescriptor markerIcon = geofencing.getMarkerIconFromDrawable(drawable);

            markerOptions.add(new MarkerOptions()
                    .title(cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.LOCATION)))
                    .position(latLng)
                    .zIndex(1)
                    .icon(markerIcon));


        }while(cursor.moveToNext());



        return markerOptions;
    }

    /**
     * Inserts totalTime and currentDayTIme
     * @param  - total time recorded being spent at that location
     * @param  - total time recording being spent at that location for today only
     */
    public static void insertTime(String location){

        Log.d(TAG, "insertTime: inserted");
        ContentValues contentValues = new ContentValues();

        contentValues.put(MyDatabaseHelper.TOTAL_TIME, 0);
        contentValues.put(MyDatabaseHelper.CURRENT_DAY_TIME, 0);
        contentValues.put(MyDatabaseHelper.LOCATION, location);

//        Cursor cursor = db.rawQuery("SELECT " + MyDatabaseHelper.LOCATION + " FROM " +
//                MyDatabaseHelper.RECT_GEOTABLE + " WHERE " + MyDatabaseHelper.LOCATION +" = 'Away'",null);


            db.insert(MyDatabaseHelper.LOCATION_STATS_TBL, null, contentValues);
        Log.d(TAG, "insertTime: inserted: " + location);
    }


//    public static void insertLastLocation(String location){
//
//        Log.d(TAG, "insertTime: inserted");
//
//        // Make a row
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(MyDatabaseHelper.LOCATION, location);
//
//
//        db.insert(MyDatabaseHelper.LOCATION_STATS_TBL, null, contentValues);
//
//        Log.d(TAG, "insertTime: inserted: " + location);
//    }

    public void updateTime(int totalTime, int currentDayTime, String location){

        db.execSQL("UPDATE " + MyDatabaseHelper.LOCATION_STATS_TBL + " SET " +
                MyDatabaseHelper.TOTAL_TIME +" = " + totalTime + "," + MyDatabaseHelper.CURRENT_DAY_TIME
                + " = " + currentDayTime + " WHERE " + MyDatabaseHelper.LOCATION + " = '" + location + "'");
        return;
    }

    public void updateLastLocation(String location){

        // Deletes all rows
        Cursor cursor = db.rawQuery("SELECT * FROM " + MyDatabaseHelper.LAST_LOCATION_TBL,null);
        Log.d(TAG, "updateLastLocation: " + cursor.getCount());
        db.execSQL("DELETE FROM " + MyDatabaseHelper.LAST_LOCATION_TBL);

        ContentValues contentValues = new ContentValues();
        contentValues.put(MyDatabaseHelper.LOCATION, location);


        db.insert(MyDatabaseHelper.LAST_LOCATION_TBL, null, contentValues);

        Log.d(TAG, "insertTime: inserted: " + location);
    }


    public boolean rowExists(String locationRow){

        try {
            Cursor cursor = db.rawQuery("SELECT " + MyDatabaseHelper.LOCATION + " FROM " +
                    myDatabaseHelper.LOCATION_STATS_TBL + " WHERE " +
                    MyDatabaseHelper.LOCATION + " = '" + locationRow + "'", null);
            // Check to see if row exists.
            if(cursor.getCount() == 0) {
                cursor.close();
                Log.d(TAG, "rowExists: false ");
                return false;
            }
            else {
                Log.d(TAG, "rowExists: true ");
                cursor.close();
                return true;
            }
        }catch(SQLException e) {
            Log.d(TAG, "rowExists: " + e);
            return false;
        }





    }





    public HashMap<String, Integer> getTime(String location){

        Log.d(TAG, "getTime: " + location);
        HashMap<String, Integer> timeMap = new HashMap<>(); // for storing time returning two values
        // Get TotalTime, CurrentDayTime where the location is the location passed in
        Cursor cursor = db.rawQuery("SELECT " + MyDatabaseHelper.TOTAL_TIME + "," +
                MyDatabaseHelper.CURRENT_DAY_TIME + " FROM " + MyDatabaseHelper.LOCATION_STATS_TBL +
                " WHERE " + MyDatabaseHelper.LOCATION + " = '" + location + "'", null);


        Log.d(TAG, "getTime: " +cursor.getCount());
        cursor.moveToFirst(); // Cursor points to row -1 at first

        int totalTime = cursor.getInt(1);
        int currentDayTime = cursor.getInt(1);


        timeMap.put("totalTime", totalTime);
        timeMap.put("currentDayTime", currentDayTime);

        return timeMap;

    }

    public String getLastLocation(){

        Cursor cursor = db.rawQuery("SELECT * FROM " + MyDatabaseHelper.LAST_LOCATION_TBL, null);
        cursor.moveToFirst();

        String location = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.LOCATION));

        return location;

    }


    /**
     * Inner class (hidden from the activity) that is actually the database class bc it extends
     * SQLiteOpenHelper. Outerclass acts as an interface between activity and database. It has fxns
     * that access elements in the innerclass bc activity can't. Implements onUpgrade, onCreate
     */
    static class MyDatabaseHelper extends SQLiteOpenHelper{

        private static Context context;
        private static String DATABASE_NAME = "MY_DATABASE";
        private static int DATABASE_VERSION = 1;
        private static String UID = "_id";

        /////////////////Mutual Column////////////////////////////////////
        private static String LOCATION = "LOCATION"; // Primary Key

        /////////////////// RECT_GEOTABLE //////////////////////////////////
        private static String RECT_GEOTABLE = "RECT_GEOTABLE";
        private static final String FILL = "FILL";
        private static final String STROKE = "STROKE";
        private static String TOPLEFTLAT = "TopLeftLat";
        private static String TOPLEFTLONG = "TopLeftLong";
        private static String TOPRIGHTLAT = "TopRightLat";
        private static String TOPRIGHTLONG = "TopRightLong";
        private static String BOTLEFTLAT = "BotLeftLat";
        private static String BOTLEFTLONG = "BotLeftLong";
        private static String BOTRIGHTLAT = "BotRightLat";
        private static String BOTRIGHTLONG = "BotRightLong";
        private static String ICON = "ICON";
        //////////////////////////////////////////////////////////////////////

        /////////////////// LOCATION_STATS_TBL ///////////////////////////////
        // (Location is primary key)
        private static String LOCATION_STATS_TBL = "LOCATION_STATS_TBL";
        private static String TOTAL_TIME = "TOTAL_TIME"; // int (minutes)
        // int (seconds)
        private static String CURRENT_DAY_TIME ="CURRENT_DAY_TIME"; // Time spent during current day


        ///////////////////// LAST_LOCATION_TBL ///////////////////////////////
        // (Location is primary key and only row & column)
        private static String LAST_LOCATION_TBL = "LAST_LOCATION_TBL";




        private static String LatLng = BOTLEFTLAT + ","
                + BOTLEFTLONG + "," + TOPLEFTLAT + "," + TOPLEFTLONG + "," + TOPRIGHTLAT + "," +
                TOPRIGHTLONG + "," + BOTRIGHTLAT + "," + BOTRIGHTLONG;

        private static String POLYGON_OPTIONS = FILL + "," + STROKE + "," + LatLng;

        //private static String DROP_TABLE = "DROP TABLE ";
        private static String CREATE_GEOTABLE = "CREATE TABLE " +RECT_GEOTABLE+ " ("+LOCATION+"" +
                " VARCHAR(255) PRIMARY KEY, "+ICON+ " INTEGER DEFAULT 0, "+FILL+" INTEGER, " +STROKE+ " INTEGER, "+BOTLEFTLAT+ " DOUBLE DEFAULT 0, "+BOTLEFTLONG+
                " DOUBLE DEFAULT 0, "+TOPLEFTLAT+ " DOUBLE DEFAULT 0, " +TOPLEFTLONG+ " DOUBLE DEFAULT 0, "+TOPRIGHTLAT+ " DOUBLE DEFAULT 0, "+TOPRIGHTLONG+
                " DOUBLE DEFAULT 0, "+BOTRIGHTLAT+ " DOUBLE DEFAULT 0, "
                +BOTRIGHTLONG+" DOUBLE DEFAULT 0);";
        private static String CREATE_LOC_STATS_TBL = "CREATE TABLE " +LOCATION_STATS_TBL + "("
                +LOCATION+" VARCHAR(255) PRIMARY KEY,"+TOTAL_TIME+ " INTEGER DEFAULT 0,"
                +CURRENT_DAY_TIME+ " INTEGER DEFAULT 0);";
        private static String CREATE_LAST_LOCATION_TBL = "CREATE TABLE " +LAST_LOCATION_TBL + " ("
                +LOCATION+" VARCHAR(255))";

//        private static String GET_TIME_QUERY = "SELECT "+TOTAL_TIME+ ","+CURRENT_DAY_TIME+ " FROM " +
//                LOCATION_STATS_TBL;



        // context passed in from OuterClass which is passed in from the activity
        // Third parameter specifies whether the column can be null.
        public MyDatabaseHelper(Context context){
            super(context,DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context; // Save the context being passed in
            Log.d(TAG, "MyDatabaseHelper: Constructor Called");
        }


        // Makes the table in the database, db object represents database that will created
        @Override
        public void onCreate(SQLiteDatabase db) {
            Toast.makeText(context, "ONCREATE", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onCreate: SQLITEDATABASE");
            try {
                //Create the tables
                db.execSQL(CREATE_GEOTABLE);
                db.execSQL(CREATE_LOC_STATS_TBL);
                db.execSQL(CREATE_LAST_LOCATION_TBL);
                //insertLastLocation("DEFAULT"); // Insert blank default row for last location
                // so that updates can be made to that


            }catch(SQLException e){
                Log.d(TAG, "onCreate: " + e);
            }
        }


        // Called when the database scheme gets upgraded or changed
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//            Toast.makeText(context, "ONUPGRADE", Toast.LENGTH_SHORT).show();
//            try {
//                db.execSQL("DROP TABLE"+); // Drops table
//                onCreate(db);
//            }catch(SQLException e){
//                Log.d(TAG, "onUpgrade: " + e);
//            }



        }


    }







}