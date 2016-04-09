package dg.shenm233.mmaps.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.WebView;

import dg.shenm233.mmaps.R;

public class LicenseActivity extends Activity {
    private WebView mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        mWebView = (WebView) findViewById(R.id.webview);
    }

    @Override
    public void onStart() {
        super.onStart();
        mWebView.loadUrl("file:///android_asset/license.html");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WebView webView = mWebView;
        ((ViewGroup) webView.getParent()).removeView(webView);
        webView.destroy();
    }
}
