package io.d6e.vibrio.vibrio;

import android.annotation.TargetApi;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.util.Log;

import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BleScanCallback extends ScanCallback {
    public String message;

    public BleScanCallback(String message){
        this.message = message;
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        super.onBatchScanResults(results);
        Log.d("BleScanCallback", "onBatchScanResults called!");
        message = "onBatchScanResults!";
    }

    @Override
    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
        Log.d("BleScanCallback", "onScanFailed called!");
        message = "onScanFailed!";
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        Log.d("BleScanCallback", "onScanREsult called!");
        message = "onScanResult!";
    }

}
