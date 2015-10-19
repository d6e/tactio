package io.d6e.vibrio.vibrio;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@TargetApi(21)
public class BluetoothActivity extends AppCompatActivity {
    public static final String TAG = "Vibrio";
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private BluetoothGatt cmGatt;
    private BleScanCallback bleScanCallback;
    private OldLeScanCallback oldLeScanCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Created!!!");
        setContentView(R.layout.activity_bluetooth);

        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported", Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        bleScanCallback = new BleScanCallback();
        oldLeScanCallback = new OldLeScanCallback();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Resumed!!!");
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
                filters = new ArrayList<ScanFilter>();
            }
            scanLeDevice(true); // start scan
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "Paused!!!");
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false); // stop scan
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "Stopped!!!");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Destroyed!!!");
        if (mGatt != null) {
            mGatt.close();
            mGatt = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "Result!!!");
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void onRefreshButton(View view){
//        scanLeDevice(true); // start scan
        TextView tv = (TextView)findViewById(R.id.text);
//        tv.setText("this string is set ynamically from java code");
        ArrayList<ScanResult> results = bleScanCallback.getScanResults();
//        tv.setText(results.toString());
        BluetoothDevice btDevice = results.get(0).getDevice();
        Log.i(TAG, "Bonded state: " + btDevice.getBondState());
        connectToDevice(btDevice);
        Log.i(TAG, "!!! Get services: " + mGatt.getServices());
//        ArrayList<BluetoothGattService> gattServs = (ArrayList<BluetoothGattService>) mGatt.getServices();
//        Log.i(TAG, "GATservices: "+ gattServs.toString());

//        UUID uuid = gattServs.get(0).getUuid();
//        if (uuid != null){
//            Log.i(TAG, "UUID: " + uuid.toString());
//        } else {
//            Log.i(TAG, "UUID: null" + null);
//        }
//        BluetoothSocket btSocket = null;
//        try {
//            btSocket = btDevice.createInsecureRfcommSocketToServiceRecord(uuid);
//            OutputStream out = btSocket.getOutputStream();
//            out.write(Integer.parseInt("hello world"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }



//        mGatt.beginReliableWrite();
//        mGatt.executeReliableWrite();

        tv.setText("Connected to: " + btDevice.getName());
//        Log.i(TAG, "Discovered services: " + mGatt.);
//        mGatt.readRemoteRssi(); // Will give you the remote signal strength
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            Log.i(TAG, "Enable scanning...");
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(oldLeScanCallback);
                    } else {
                        mLEScanner.stopScan(bleScanCallback);
                    }
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(oldLeScanCallback);
            } else {
                mLEScanner.startScan(filters, settings, bleScanCallback);
            }
        } else {
            Log.i(TAG, "Disable scanning...");
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(oldLeScanCallback);
            } else {
                mLEScanner.stopScan(bleScanCallback);
            }
        }
    }

    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            mGatt = device.connectGatt(this, true, gattCallback);
            Log.i(TAG, "Connecting... " + mGatt.connect());

//            Log.i(TAG, "Create bond... " + device.createBond());
//            Log.i(TAG, "Bonded state: " + device.getBondState());
            scanLeDevice(false);// will stop after first device detection
        }
    }

    // Used for connecting to the device
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG, "onConnectionStateChange: " + "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i(TAG, "gattCallback: " + "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e(TAG, "gattCallback: " + "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e(TAG, "gattCallback: " + "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i(TAG, "Status: " + status);
            Log.i(TAG, "onServicesDiscovered: " + services.toString());
            cmGatt = gatt;
            gatt.readCharacteristic(services.get(1).getCharacteristics().get(0));
//            gatt.beginReliableWrite()
            Log.i(TAG, "beginReliableWrite(): "+gatt.beginReliableWrite());
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i(TAG, "onCharacteristicRead: " + characteristic.toString());
            gatt.disconnect();
        }
    };
}


