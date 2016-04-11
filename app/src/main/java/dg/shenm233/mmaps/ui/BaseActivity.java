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

package dg.shenm233.mmaps.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.amap.api.maps.MapsInitializer;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle saveInstanceStat) {
        super.onCreate(saveInstanceStat);
        if (MapsInitializer.sdcardDir.isEmpty()) {
            Toast.makeText(this, "error", Toast.LENGTH_SHORT)
                    .show();
            finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
