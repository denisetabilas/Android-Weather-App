package edu.deanza.cis53.weather.app.test;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import edu.deanza.cis53.weather.app.data.WeatherContract;
import edu.deanza.cis53.weather.app.data.WeatherDbHelper;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.Set;
import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class DbInstrumentedTest {
    public static String APP_TAG = "edu.deanza.cis53.weather.app";
    static final String TEST_LOCATION = "99705";
    static final String TEST_DATE = "20141205";

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals(APP_TAG, appContext.getPackageName());
    }
    @Test
    public void testCreateDb() throws Throwable{
        Log.d(APP_TAG,"testCreateDb");
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        appContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(
                appContext).getWritableDatabase();
        assertEquals(true,db.isOpen());
        db.close();

    }
    public static ContentValues createNorthPoleLocationValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, TEST_LOCATION);
        testValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, "North Pole");
        testValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, 64.7488);
        testValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, -147.353);

        return testValues;
    }
    // Create dummy weather data for test purpose
    static ContentValues createWeatherValues(long locationRowId){
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY,locationRowId);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT,"20141205");
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY,1.2);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE,1.3);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,75);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,65);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,"Asteroids");
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,5.5);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,321);

        return weatherValues;
    }


    // Compare row data in DB with inserted data
    static void validateCursor(Cursor valueCursor, ContentValues expectedValues){

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String,Object>> valuesSet = expectedValues.valueSet();
        for(Map.Entry<String,Object>entry:valuesSet){
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx ==-1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }

    // Test insert data
    @Test
    public void testInsertReadDb(){
        Log.d(APP_TAG,"testInsertReadDb");
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        //If there's an error in those massive SQL table creation Strings,
        //errors will be thrown here when you try to get a writable database.

        WeatherDbHelper dbHelper = new WeatherDbHelper(appContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = createNorthPoleLocationValues();
        long locationRowId;
        locationRowId= db.insert(WeatherContract.LocationEntry.TABLE_NAME,null,testValues);
        //Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(APP_TAG,"New row id: "+locationRowId);

        //Data's inserted. IN THEORY. Now pull some out to stare at it and verify it made
        //the round trip.
        //A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                WeatherContract.LocationEntry.TABLE_NAME, //Table to Query
                null,//all columns
                null,//Columns for the "where" clause
                null,//Values for the "where " clause
                null,//columns to group by
                null,//columns to filter by row groups
                null//sort order
        );
        validateCursor(cursor,testValues);
        //Fantastic. Now that we have a location, add some weather!
        ContentValues weatherValues = createWeatherValues(locationRowId);
        long weatherRowId = db.insert(WeatherContract.WeatherEntry.TABLE_NAME,null,weatherValues);
        assertTrue(weatherRowId != -1);

        //A cursor is your primary interface to the query results.
        Cursor weatherCursor = db.query(
                WeatherContract.WeatherEntry.TABLE_NAME,
                null,//Leaving "columns" null just returns all the columns.
                null,// cols for "where" clause
                null,// values for "where" clause
                null,//columns to group by
                null,// columns to filter by row groups
                null// sort order
        );
        validateCursor(weatherCursor,weatherValues);
        dbHelper.close();
    }
}

