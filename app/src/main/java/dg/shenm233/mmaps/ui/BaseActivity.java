package dg.shenm233.mmaps.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.amap.api.maps.MapsInitializer;

import dg.shenm233.mmaps.util.OffLineMapUtils;

public class BaseActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle saveInstanceStat) {
        super.onCreate(saveInstanceStat);
        String sdFilesDir = OffLineMapUtils.getSdFilesDir(this.getApplicationContext());
        if (!sdFilesDir.isEmpty()) {
            MapsInitializer.sdcardDir = sdFilesDir;
        } else {
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
