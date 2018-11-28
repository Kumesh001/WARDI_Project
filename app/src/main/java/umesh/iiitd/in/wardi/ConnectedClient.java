package umesh.iiitd.in.wardi;

import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

public class ConnectedClient {

  /*  public static final String TAG="TESTER";

    private BluetoothSocket curBTSocket = null;

    ClientThread connectThread;
    DeviceConnectThread deviceConnectThread;

    public void connectAsClient() {
        showMessage("Connecting for online Bluetooth devices...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (deviceToConnect != null) {
                    if (connectThread != null) {
                        connectThread.cancel();
                        connectThread = null;
                    }
                    connectThread = new ClientThread();
                    if(uuid!=null)
                    {
                        Log.d(TAG,"From The Device UUID");
                        curBTSocket = connectThread.connect(mBluetoothAdapter, deviceToConnect, uuid,mHandler);
                    }else {
                        Log.d(TAG,"Using the Default UUID");
                        curBTSocket = connectThread.connect(mBluetoothAdapter, deviceToConnect, MY_UUID_SECURE,mHandler);
                    }
                    connectThread.start();
                    if(curBTSocket!=null)
                    {
                        runOnUiThread(new Runnable(){
                            public void run() {
                                selectedView.append("-Connected");
                                mDiscoveryButton.setVisibility(View.INVISIBLE);
                                mDisconnectButton.setVisibility(View.VISIBLE);
                            }
                        });
                        showMessage("Connected");
                        sendMessageToDevice("Hello Umesh");
                    }else{
                        Log.d(TAG,"Error");
                    }
                }
            }
        }).start();
    }


    public void sendMessageToDevice(String s) {
        deviceConnectThread = new DeviceConnectThread(curBTSocket,mHandler);
        deviceConnectThread.start();
        String message = s.trim();
        if (message.length() > 0) {
            byte[] send = message.getBytes();
            deviceConnectThread.write(send);
        }
    }

    public void showMessage(String message) {
        Snackbar snackbar = Snackbar
                .make(mCoordinateLayout, message, Snackbar.LENGTH_LONG);
        View view = snackbar.getView();
        view.setBackgroundColor(Color.GREEN);
        TextView textView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.BLACK);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
        params.gravity = Gravity.BOTTOM;
        view.setLayoutParams(params);
        snackbar.show();
    }

}*/
}
