package io.d6e.vibrio.vibrio;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;

// Async callback for bluetooth scan (API < 21)
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class OldLeScanCallback implements BluetoothAdapter.LeScanCallback {
    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Log.i(MainActivity.TAG, "onLeScan: " + device.toString());
////                connectToDevice(device);
//            }
//        });
    }
}
