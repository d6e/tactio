package io.d6e.vibrio.vibrio;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

// Async callback for bluetooth scan (API > 21)
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BleScanCallback extends ScanCallback {
    private ArrayList<ScanResult> scanResults;
    private BluetoothGatt mGatt;

    public ArrayList<ScanResult> getScanResults() {
        return scanResults;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        scanResults = new ArrayList<ScanResult>();
        scanResults.add(result);
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        scanResults = (ArrayList<ScanResult>) results;
        Log.i(MainActivity.TAG, "Found several results: " + results.toString());
    }

    @Override
    public void onScanFailed(int errorCode) {
        Log.e(MainActivity.TAG, "Scan Failed: " + "Error Code: " + errorCode);
        scanResults = null;
    }
}
