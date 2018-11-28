package umesh.iiitd.in.wardi;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;


public class ClientThread extends Thread {

    private BluetoothSocket BTSocket=null;
    public static final String TAG="ClientThread";

    public BluetoothSocket connect(BluetoothAdapter mAdapter, BluetoothDevice mBluetoothDevice, UUID uuid, Handler mHandler)
    {
        try{
            BTSocket=mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
        }catch (IOException e)
        {
            Log.d(TAG, "Could not create RFCOMM socket:" + e.toString());
            e.printStackTrace();
            return BTSocket;
        }

        if(mAdapter.isDiscovering())
        {
            mAdapter.cancelDiscovery();
        }
        try{
            BTSocket.connect();
        }catch (IOException e)
        {
            Log.d(TAG,"Could not Connect "+e.toString());
            try {
                BTSocket.close();
            } catch (IOException e1) {
                Log.d(TAG, "Could not close connection:" + e.toString());
                e1.printStackTrace();
                return BTSocket;
            }
        }
        byte[] bytes = mBluetoothDevice.getName().getBytes();
        byte[] buffer = new byte[1024];

        mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME, 0, -1, bytes)
                .sendToTarget();

        return BTSocket;
    }
    public boolean cancel(){
        if (BTSocket != null) {
            try {
                BTSocket.close();
            } catch (IOException e) {
                Log.d(TAG, "Could not close connection:" + e.toString());
                return false;
            }
        }
        return true;
    }
}
