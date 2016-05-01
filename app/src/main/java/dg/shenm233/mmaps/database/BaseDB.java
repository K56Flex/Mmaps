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

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import dg.shenm233.mmaps.MainApplication;

public class BaseDB extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "mmaps.db";

    private static Table[] TABLES = new Table[]{
            RecentSearchTips.initTable()
    };

    private static BaseDB mInstance;

    private BaseDB() {
        super(MainApplication.getAppContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized BaseDB getInstance() {
        if (mInstance == null) {
            mInstance = new BaseDB();
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("BaseDB", "[onCreate] start to create tables.");
        for (Table table : TABLES) {
            table.onCreate(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("BaseDB", "[onUpgrade] upgrade database.");
        for (Table table : TABLES) {
            table.onUpgrade(db, oldVersion, newVersion);
        }
    }
}
