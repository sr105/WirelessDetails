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

    private TextView mTextView;

    private boolean mDoNtpExample = true;
    private boolean mDoWifiExample = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.textView);
        final String text = "Build Time: " + new ExtraAppInfo(this).buildTime().toString() + "\n";
        mTextView.setText(text);
        if (mDoNtpExample)
            addNtpTimeToTextView();
        else
            addWifiInfoToTextView();
    }

    public void addNtpTimeToTextView() {
        AsyncTask<String, Void, Date> task = new AsyncTask<String, Void, Date>() {
            @Override
            protected Date doInBackground(String[] params) {
                return SntpClient.getTime(params[0], 20000);
            }

            @Override
            protected void onPostExecute(Date date) {
                String text = mTextView.getText().toString();
                mTextView.setText(text + "NTP Time: " + date + "\n");
                addWifiInfoToTextView();
            }
        };
        task.execute("2.android.pool.ntp.org");
    }

    public void addWifiInfoToTextView() {
        if (!mDoWifiExample)
            return;
        String text = mTextView.getText().toString();
        WifiUtils.sContext = this;
        WifiConfigurationWrapper wifi = WifiUtils.getCurrentWifiConfig();
        String wifiText = "\n" + WifiUtils.logWifi(wifi, "Current");
        mTextView.setText(text + wifiText);
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
