package theappfoundry.lifestyles;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;
import static android.database.Cursor.FIELD_TYPE_FLOAT;
import static android.database.Cursor.FIELD_TYPE_INTEGER;
import static android.database.Cursor.FIELD_TYPE_STRING;

/**
 * Created by Ben on 6/14/2017.
 * Outerclass to act as interface to the innerclass database
 */

public class DatabaseAdapter {

    private MyDatabaseHelper myDatabaseHelper; // handler reference to inner class
    private static SQLiteDatabase db; // database reference available to entire class


    public DatabaseAdapter(Context context){

        // Get a reference to inner class, pass in context
        myDatabaseHelper = new MyDatabaseHelper(context); // instance of inner class
        db = myDatabaseHelper.getWritableDatabase(); // Open database when app starts

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
        contentValues.put(MyDatabaseHelper.TOPLEFTLAT, topLeftLat);
        contentValues.put(MyDatabaseHelper.TOPLEFTLONG, topLeftLong);
        contentValues.put(MyDatabaseHelper.TOPRIGHTLAT, topRightLat);
        contentValues.put(MyDatabaseHelper.TOPRIGHTLONG, topRightLong);
        contentValues.put(MyDatabaseHelper.BOTLEFTLAT, botLeftLat);
        contentValues.put(MyDatabaseHelper.BOTLEFTLONG, botLeftLong);
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
            db.execSQL(MyDatabaseHelper.DROP_TABLE);
        }catch(SQLException e){
            Log.d(TAG, "deleteTable: " + e);
        }
        Log.d(TAG, "deleteTable: Table Dropped");
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
        //////////////////////////////////////////////////////////////////////

        /////////////////// LOCATION_STATS_TBL ///////////////////////////////
        private static String TOTAL_TIME = "TOTAL_TIME";
        private static String CURRENT_DAY_TIME ="CURRENT_DAY_TIME"; // Time spent during current day


        private static String POLYGON_OPTIONS = FILL + "," + STROKE + "," + TOPLEFTLAT + ","
                + TOPLEFTLONG + "," + TOPRIGHTLAT + "," + TOPRIGHTLONG + "," + BOTLEFTLAT + "," +
                BOTLEFTLONG + "," + BOTRIGHTLAT + "," + BOTRIGHTLONG;
        private static String DROP_TABLE = "DROP TABLE "+RECT_GEOTABLE;
        private static String CREATE_GEOTABLE = "CREATE TABLE " +RECT_GEOTABLE+ " ("+LOCATION+"" +
                " TEXT PRIMARY KEY, "+FILL+" INTEGER, " +STROKE+ " INTEGER, "+BOTLEFTLAT+ " DOUBLE DEFAULT 0, "+BOTLEFTLONG+
                " DOUBLE DEFAULT 0, "+TOPLEFTLAT+ " DOUBLE DEFAULT 0, " +TOPLEFTLONG+ " DOUBLE DEFAULT 0, "+TOPRIGHTLAT+ " DOUBLE DEFAULT 0, "+TOPRIGHTLONG+
                " DOUBLE DEFAULT 0, "+BOTRIGHTLAT+ " DOUBLE DEFAULT 0, "
                +BOTRIGHTLONG+" DOUBLE DEFAULT 0);";



        // context passed in from OuterClass which is passed in from the activity
        // Third parameter specifies whether the column can be null.
        public MyDatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context; // Save the context being passed in
            Log.d(TAG, "MyDatabaseHelper: Constructor Called");
        }


        // Makes the table in the database, db object represents database that will created
        @Override
        public void onCreate(SQLiteDatabase db) {
            Toast.makeText(context, "ONCREATE", Toast.LENGTH_SHORT).show();
            try {
                db.execSQL(CREATE_GEOTABLE);
            }catch(SQLException e){
                Log.d(TAG, "onCreate: " + e);
            }
        }


        // Called when the database scheme gets upgraded or changed
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Toast.makeText(context, "ONUPGRADE", Toast.LENGTH_SHORT).show();
            try {
                db.execSQL(DROP_TABLE); // Drops table
                onCreate(db);
            }catch(SQLException e){
                Log.d(TAG, "onUpgrade: " + e);
            }



        }
    }







}