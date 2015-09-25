package io.d6e.vibrio.vibrio;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    BluetoothManager  btManager;
    BluetoothAdapter btAdapter;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btManager = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void scanBLE(View view){
        String btMessage;

        if (btAdapter == null) { // if there's no bt adapter...
            btMessage = "There doesn't appear to be a bluetooth device...";
            Log.w("MainActivity", btMessage);
            return; // Nothing to do here!!
        }else{
            btAdapter.enable();
//            btMessage = btAdapter.getAddress();
        }
        BluetoothLeScanner btLeScanner = btAdapter.getBluetoothLeScanner();
        BleScanCallback scanCb = new BleScanCallback("scanning");
        Log.d("MainActivity", "About to scan...");
        btLeScanner.startScan(scanCb);
//        ArrayList<ScanResult> results = new ArrayList<ScanResult>();
//        scanCb.onBatchScanResults(results);
//        scanCb.onBatchScanResults(results);
//        scanCb.onBatchScanResults(results);

        while(scanCb.message == "scanning"){
            Log.d("MainActivity", "Scanning...");
        }
        Log.d("MainActivity", "Done scanning!");

        btMessage = scanCb.message;


        // Create the text view
        Intent intent = new Intent();
        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText("bluetooth info:\n"+btMessage);

        // Set the text view as the activity layout
        setContentView(textView);
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
