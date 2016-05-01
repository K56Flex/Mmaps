/*
 * Copyright 2016 Shen Zhang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dg.shenm233.mmaps.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.help.Tip;

import java.util.ArrayList;
import java.util.List;

import dg.shenm233.mmaps.util.AMapUtils;

public class RecentSearchTips implements Table {
    private static final String TABLE_NAME = "recent_search_tips";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DISTRICT = "district";
    private static final String COLUMN_ADCODE = "adcode";
    private static final String COLUMN_POI_ID = "poi_id";
    private static final String COLUMN_LATLONPOINT = "latlng_point";


    private static final String createTable = "CREATE TABLE " + TABLE_NAME + " ("
            + _ID + " INTEGER PRIMARY KEY,"
            + COLUMN_NAME + " TEXT,"
            + COLUMN_DISTRICT + " TEXT,"
            + COLUMN_ADCODE + " TEXT,"
            + COLUMN_POI_ID + " TEXT,"
            + COLUMN_LATLONPOINT + " TEXT"
            + ")";

    private static RecentSearchTips mInstance;

    public static RecentSearchTips getInstance() {
        if (mInstance == null) {
            throw new SQLiteAbortException("init table first!");
        }
        return mInstance;
    }

    static synchronized Table initTable() {
        if (mInstance == null) {
            mInstance = new RecentSearchTips();
        }
        return mInstance;
    }

    public void insertTip(Tip tip) {
        int _id = 0;
        LatLonPoint latLonPoint = tip.getPoint();
        synchronized (this) {
            SQLiteDatabase db = BaseDB.getInstance().getWritableDatabase();
            // 如果数据库中已存在该历史记录，就无需再次写入
            Cursor cursor = db.query(TABLE_NAME,
                    new String[]{COLUMN_NAME, COLUMN_POI_ID},
                    COLUMN_NAME + "=? AND " + COLUMN_POI_ID + "=?",
                    new String[]{tip.getName(), tip.getPoiID()},
                    null,
                    null,
                    null);
            if (cursor.moveToFirst()) {
                cursor.close();
                return;
            }
            cursor.close();

            Cursor cursor1;
            cursor1 = db.query(TABLE_NAME,
                    new String[]{_ID},
                    null,
                    null,
                    null,
                    null,
                    _ID + " DESC");
            if (cursor1.moveToFirst()) {
                _id = cursor1.getInt(0);
            }
            cursor1.close();

            ContentValues c = new ContentValues();
            c.put(_ID, ++_id);
            c.put(COLUMN_NAME, tip.getName());
            c.put(COLUMN_DISTRICT, tip.getDistrict());
            c.put(COLUMN_ADCODE, tip.getAdcode());
            c.put(COLUMN_POI_ID, tip.getPoiID());
            if (latLonPoint != null) {
                c.put(COLUMN_LATLONPOINT, AMapUtils.convertLatLonPointToString(latLonPoint));
            }
            db.insert(TABLE_NAME, null, c);
        }
    }

    public List<Tip> getRecentTips() {
        List<Tip> tipList = new ArrayList<>();
        synchronized (this) {
            SQLiteDatabase db = BaseDB.getInstance().getReadableDatabase();
            String[] columns = new String[]{
                    COLUMN_NAME,
                    COLUMN_DISTRICT,
                    COLUMN_ADCODE,
                    COLUMN_POI_ID,
                    COLUMN_LATLONPOINT
            };
            Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    Tip tip = new Tip();
                    tip.setName(cursor.getString(0));
                    tip.setDistrict(cursor.getString(1));
                    tip.setAdcode(cursor.getString(2));
                    tip.setID(cursor.getString(3));
                    String latLon = cursor.getString(4);
                    if (latLon != null) {
                        tip.setPostion(AMapUtils.convertToLatLonPoint(latLon));
                    }
                    tipList.add(tip);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return tipList;
    }

    public void clearRecent() {
        synchronized (this) {
            SQLiteDatabase db = BaseDB.getInstance().getWritableDatabase();
            db.delete(TABLE_NAME, null, null);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
