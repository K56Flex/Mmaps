package dg.shenm233.mmaps.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;

import java.util.ArrayList;
import java.util.List;

import dg.shenm233.mmaps.util.AMapUtils;

public class FavoriteLocations implements Table {
    private static final String TABLE_NAME = "favorite_location";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DISTRICT = "district";
    private static final String COLUMN_ADCODE = "adcode";
    private static final String COLUMN_POI_ID = "poi_id";
    private static final String COLUMN_LATLONPOINT = "latlng_point";


    private static final String createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + _ID + " INTEGER PRIMARY KEY,"
            + COLUMN_NAME + " TEXT,"
            + COLUMN_DISTRICT + " TEXT,"
            + COLUMN_ADCODE + " TEXT,"
            + COLUMN_POI_ID + " TEXT,"
            + COLUMN_LATLONPOINT + " TEXT"
            + ")";

    private static FavoriteLocations mInstance;

    public static FavoriteLocations getInstance() {
        if (mInstance == null) {
            throw new SQLiteAbortException("init table first!");
        }
        return mInstance;
    }

    static synchronized Table initTable() {
        if (mInstance == null) {
            mInstance = new FavoriteLocations();
        }
        return mInstance;
    }

    public boolean save(PoiItem poi) {
        if (poi == null || exists(poi)) return false;
        synchronized (this) {
            SQLiteDatabase db = BaseDB.getInstance().getWritableDatabase();
            int _id = 0;
            Cursor cursor = db.query(TABLE_NAME,
                    new String[]{_ID},
                    null,
                    null,
                    null,
                    null,
                    _ID + " DESC");
            if (cursor.moveToFirst()) {
                _id = cursor.getInt(0);
            }
            cursor.close();

            ContentValues contentValues = new ContentValues();
            contentValues.put(_ID, ++_id);
            contentValues.put(COLUMN_NAME, poi.getTitle());
            contentValues.put(COLUMN_DISTRICT, poi.getAdName());
            contentValues.put(COLUMN_ADCODE, poi.getAdCode());
            contentValues.put(COLUMN_POI_ID, poi.getPoiId());
            contentValues.put(COLUMN_LATLONPOINT, AMapUtils.convertLatLonPointToString(poi.getLatLonPoint()));
            db.insert(TABLE_NAME, null, contentValues);

            db.close();
            return true;
        }
    }

    public boolean exists(PoiItem poi) {
        if (poi == null) return false;
        boolean result = false;
        synchronized (this) {
            SQLiteDatabase db = BaseDB.getInstance().getReadableDatabase();
            Cursor cursor = db.query(TABLE_NAME,
                    new String[]{COLUMN_LATLONPOINT},
                    COLUMN_LATLONPOINT + "=?",
                    new String[]{AMapUtils.convertLatLonPointToString(poi.getLatLonPoint())},
                    null,
                    null,
                    null);
            result = cursor.moveToFirst();
            cursor.close();
            db.close();
            return result;
        }
    }

    public boolean remove(PoiItem poi) {
        if (poi == null || poi.getLatLonPoint() == null) return false;
        synchronized (this) {
            SQLiteDatabase db = BaseDB.getInstance().getWritableDatabase();
            db.delete(TABLE_NAME,
                    COLUMN_LATLONPOINT + "=?",
                    new String[]{AMapUtils.convertLatLonPointToString(poi.getLatLonPoint())});
            db.close();
            return true;
        }
    }

    public List<PoiItem> getSavedList() {
        List<PoiItem> poiItemList = new ArrayList<>();
        synchronized (this) {
            SQLiteDatabase db = BaseDB.getInstance().getReadableDatabase();
            Cursor cursor = db.query(TABLE_NAME,
                    new String[]{COLUMN_NAME, COLUMN_DISTRICT, COLUMN_ADCODE, COLUMN_POI_ID, COLUMN_LATLONPOINT},
                    null,
                    null,
                    null,
                    null,
                    _ID + " DESC");

            if (!cursor.moveToFirst()) {
                cursor.close();
                db.close();
                return poiItemList;
            }
            do {
                String latLon = cursor.getString(4);
                LatLonPoint latLonPoint = null;
                if (latLon != null) {
                    latLonPoint = AMapUtils.convertToLatLonPoint(latLon);
                }
                PoiItem poi = new PoiItem(cursor.getString(3),
                        latLonPoint,
                        cursor.getString(0),
                        null);
                poi.setAdName(cursor.getString(1));
                poiItemList.add(poi);
            } while (cursor.moveToNext());
            cursor.close();
            db.close();
        }
        return poiItemList;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            onCreate(db);
        }
    }
}
