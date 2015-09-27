package io.d6e.vibrio.vibrio;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

// Async callback for bluetooth scan (API > 21)
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BleScanCallback extends ScanCallback {
    private String scanResults;

    public String getScanResults() {
        return scanResults;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
//        Log.i(MainActivity.TAG, "callbackType: " + String.valueOf(callbackType) + ", scan result: " + result.toString());
        scanResults = result.toString();
        BluetoothDevice btDevice = result.getDevice();
//        connectToDevice(btDevice);
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        ArrayList<String> resultList = new ArrayList<String>();
        for (ScanResult sr : results) {
            resultList.add(sr.toString());
        }
        scanResults = resultList.toString();
        Log.i(MainActivity.TAG, "Found several results: " + resultList.toString());
    }

    @Override
    public void onScanFailed(int errorCode) {
        Log.e(MainActivity.TAG, "Scan Failed: " + "Error Code: " + errorCode);
        scanResults = null;
    }
}
