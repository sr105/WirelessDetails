package com.sr105.wirelessdetails;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.sr105.ntp.SntpClient;
import com.sr105.wifi.WifiConfigurationWrapper;
import com.sr105.wifi.WifiUtils;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private TextView mBuildTimeTextView;
    private TextView mNtpTimeTextView;
    private TextView mWifiDetailsTextView;

    private boolean mDoNtpExample = true;
    private boolean mDoWifiExample = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBuildTimeTextView = (TextView) findViewById(R.id.buildTime);
        final String text = "Build Time: " + new ExtraAppInfo(this).buildTime().toString() + "\n";
        mBuildTimeTextView.setText(text);

        mNtpTimeTextView = (TextView) findViewById(R.id.ntpTime);
        mWifiDetailsTextView = (TextView) findViewById(R.id.wifiDetails);

        addNtpTimeToTextView();
        addWifiInfoToTextView();
    }

    public void addNtpTimeToTextView() {
        if (!mDoNtpExample)
            return;
        RecurringAndroidTask.run(mGetNtpTimeRunnable, 1000);
    }

    private final Runnable mGetNtpTimeRunnable = new Runnable() {
        @Override
        public void run() {
            AsyncTask<String, Void, Date> task = new AsyncTask<String, Void, Date>() {
                @Override
                protected Date doInBackground(String[] params) {
                    return SntpClient.getTime(params[0], 20000);
                }

                @Override
                protected void onPostExecute(Date date) {
                    mNtpTimeTextView.setText("NTP Time: " + date + "\n");
                }
            };
            task.execute("2.android.pool.ntp.org");
        }
    };

    public void addWifiInfoToTextView() {
        if (!mDoWifiExample)
            return;
        WifiUtils.sContext = this;
        WifiConfigurationWrapper wifi = WifiUtils.getCurrentWifiConfig();
        mWifiDetailsTextView.setText(WifiUtils.logWifi(wifi, "Current"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
