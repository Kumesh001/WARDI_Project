package umesh.iiitd.in.wardi;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.gson.Gson;

import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import umesh.iiitd.in.wardi.Utilities.NetworkUtils;


public class MainActivity extends AppCompatActivity {

    public static final String TAG="TESTER";

    //public  BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT  =1;
    private int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> mDeviceNames;
    private ArrayList<String> mDeviceMacAddress;
    private ListView mListView;
    private Button mDiscoveryButton;
    private Button mStopButton;
    private BluetoothAdapter mBluetoothAdapter;
    private Context mContext;
    private CoordinatorLayout mCoordinateLayout;

    private Map<String,BluetoothDevice> deviceMap=new HashMap<>();

    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private UUID uuid;

    // Other Variables used for Bluetooth Connection
    private BluetoothSocket curBTSocket = null;

    ClientThread connectThread;
    BluetoothDevice deviceToConnect;
    private Button mDisconnectButton;

    TextView selectedView;

    DeviceConnectThread deviceConnectThread;

    private List<Integer> maneuver=new ArrayList<>();
    private Map<Integer,int[]> latLong=new HashMap<>();


//    private Gson jsonObject;

     private float[] source=new float[1];
     private float[] destination=new float[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext=getApplicationContext();
        mListView=findViewById(R.id.listView);
        mDiscoveryButton=findViewById(R.id.discoveryButton);
        mStopButton=findViewById(R.id.stop);
        mCoordinateLayout=findViewById(R.id.myCoordinatorLayout);
        mDisconnectButton=findViewById(R.id.disconnect);

        mDeviceNames=new ArrayList<>();
        mDeviceMacAddress=new ArrayList<>();

        uuid=null;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter.isDiscovering()) {
            Toast.makeText(mContext, "Cancelling the Discovery", Toast.LENGTH_SHORT).show();
            mBluetoothAdapter.cancelDiscovery();
        }
        if(curBTSocket!=null)
        {
            disconnect();
        }

        // Checking whether the device support Bluetooth Functionality of not
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Bluetooth not supported!",Toast.LENGTH_SHORT).show();
        }

        //Register The broadcast Receiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        Set<BluetoothDevice> pairedDevices=mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            Log.d(TAG,"ALready Paired Devices");
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                mDeviceMacAddress.add(deviceHardwareAddress);
                mDeviceNames.add(deviceName);
                deviceMap.put(deviceName,device);
                Log.d(TAG,"Device Name: "+deviceName);
                Log.d(TAG,"Device Mac Address: "+deviceHardwareAddress);
            }
            Log.d(TAG,"------------------------------------");
        }

        mAdapter=new ArrayAdapter<String>(this,R.layout.sample_list_item,mDeviceNames);
        mListView.setAdapter(mAdapter);

        mDiscoveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBluetoothStatus();
                // Now Start the Discovery of new Devices with their names and mac address;
                //Log.d(TAG,"Inside the  Discovery Method");
                Toast.makeText(mContext,"Discovery Started",Toast.LENGTH_SHORT).show();
                mBluetoothAdapter.startDiscovery();
                mDiscoveryButton.setVisibility(View.INVISIBLE);
                mStopButton.setVisibility(View.VISIBLE);
            }
        });

        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter.isDiscovering()) {
                    //Log.d(TAG,"Cancelling the Discovery");
                    mBluetoothAdapter.cancelDiscovery();
                }else{
                    Toast.makeText(mContext,"Discovering Already Off",Toast.LENGTH_SHORT).show();
                }

                mStopButton.setVisibility(View.INVISIBLE);
                mDiscoveryButton.setVisibility(View.VISIBLE);
            }
        });


        mDisconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
                mDiscoveryButton.setVisibility(View.VISIBLE);
                mDisconnectButton.setVisibility(View.INVISIBLE);
            }
        });

        // Item Click Listener
       mListView.setOnItemClickListener(new OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               String Name= (String) parent.getItemAtPosition(position);
               Toast.makeText(MainActivity.this,"Item Clicked : "+Name,Toast.LENGTH_SHORT).show();

               deviceToConnect=deviceMap.get(Name);

               selectedView=view.findViewById(R.id.label);
               try {
                   ParcelUuid[] uuidNew=deviceToConnect.getUuids();
                   if(uuidNew!=null) {
                       ParcelUuid tempID=uuidNew[0];
                       uuid=UUID.fromString(tempID.toString());

                       /*for(ParcelUuid checkID:uuidNew)
                           Log.d(TAG, "Temp Device : " + deviceToConnect + " Its UUID : " + checkID.getUuid().toString());*/
                   }else{
                       Log.d(TAG,"UUID Is null");
                   }
               } catch (Exception e) {
                   Log.d(TAG,"In the Exception Part");
                   e.printStackTrace();
               }

               // Fragment Transaction to load the Buttons Fragment which connect to the device and allow us to exchange the data
               FragmentManager fm=getSupportFragmentManager();
               OnConnectFragment FragmentObject=new OnConnectFragment();

               FragmentTransaction fmTrans=fm.beginTransaction();
               fmTrans.add(R.id.frameLayout,FragmentObject);
               fmTrans.commit();

               connectAsClient();
           }
       });

        Snackbar.make(mCoordinateLayout,R.string.not_connected,Snackbar.LENGTH_SHORT).show();

        populateData();
    }


    public void populateData()
    {
        int[] temp=new int[]{
                1,2,3,1,2,3,3,2,2,1,1,3,1
        };

        int[] latLon=new int[]{
                1002,12312
        };

        for(int i=0;i<temp.length;i++)
        {
            maneuver.add(temp[i]);
            latLong.put(temp[i],latLon);
        }
    }

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

    private void disconnect() {
        Log.i(TAG,"Disconnecting!");

        if (curBTSocket != null) {
            try {
                curBTSocket.close();
            } catch (IOException e) {

            }
        }
        selectedView.setText(deviceToConnect.getName());
        deviceToConnect=null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_help)
        {
            Toast.makeText(this,"Help Called",Toast.LENGTH_SHORT).show();
            sendMessageToDevice("Help");

            // Waiting for the reply from the device

            /*String srcAddress=getAddress(source[0],source[1]);
            String desAddress=getAddress(destination[0],destination[1]);*/
            URL url=generateUrl("HI","BYe");
            new HTTPResponse().execute(url);
        }
        return super.onOptionsItemSelected(item);
    }

    private class HTTPResponse extends AsyncTask<URL, Void, String> {


        @Override
        protected String doInBackground(URL... urls) {
            URL searchUrl = urls[0];
            String SearchResults = null;
            try {
                JSONObject jsonObj = null;
                SearchResults= NetworkUtils.getResponseFromHttpUrl(searchUrl);
                try
                {
                    Log.d(TAG,"Printing data---------------------------------");
                    List<JSONObject> sampleList = (List<JSONObject>) jsonObj.getJSONArray("routes");
                    Log.d(TAG,"After the Declaration");
                    if(sampleList!=null)
                    {
                        Log.d(TAG,"Inside the if Condition");
                        for(JSONObject i:sampleList)
                        {
                            Log.d(TAG,"i: "+i);
                        }
                    }
                    Log.d(TAG,"After the condition---------------------------------");
                }
                catch(Exception e)
                {
                    System.out.println(e);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return SearchResults;
        }

        @Override
        protected void onPostExecute(String s) {

            if(s!=null)
            {
            }

            super.onPostExecute(s);
        }
    }

    public URL generateUrl(String src,String des){

        String tempSrc="Kondli";
        String tempDes="IIIT,Delhi";

        URL Google_direction_api=NetworkUtils.buildUrl(tempSrc,tempDes);
        return Google_direction_api;
    }

    private String getAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                result.append(address.getLocality()).append("\n");
                result.append(address.getCountryName());
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();
    }



    void CheckBluetoothStatus()
    {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // Only ask for these permissions on runtime when running Android 6.0 or higher
            switch (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                case PackageManager.PERMISSION_DENIED:
                    ((TextView) new AlertDialog.Builder(this)
                            .setTitle("Runtime Permissions up ahead")
                            .setMessage(Html.fromHtml("<p>To find nearby bluetooth devices please click \"Allow\" on the runtime permissions popup.</p>" +
                                    "<p>For more info see <a href=\"http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id\">here</a>.</p>"))
                            .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                                    }
                                }
                            })
                            .show()
                            .findViewById(android.R.id.message))
                            .setMovementMethod(LinkMovementMethod.getInstance());       // Make the link clickable. Needs to be called after show(), in order to generate hyperlinks
                    break;
                case PackageManager.PERMISSION_GRANTED:
                    break;
            }
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                ParcelUuid[] deviceUUID=device.getUuids();
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                deviceMap.put(deviceName,device);
                Log.i(TAG,"Device Found : "+deviceName);
                Log.i(TAG,"Device Mac Address : "+deviceHardwareAddress);
                Log.i(TAG,"Device UUID: "+deviceUUID);

                mDeviceNames.add(deviceName);
                mDeviceMacAddress.add(deviceHardwareAddress);
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (curBTSocket != null) {
            try {
                curBTSocket.close();
            } catch (IOException e) {
            }
        }

        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {

            byte[] buf = (byte[]) msg.obj;

            switch (msg.what) {

                case Constants.MESSAGE_WRITE:
                    // construct a string from the buffer
                    String writeMessage = new String(buf);
                    Log.i(TAG, "Write Message : " + writeMessage);
                    showMessage("Message Sent : " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(buf, 0, msg.arg1);
                    Log.i(TAG, "readMessage : " + readMessage);
                    showMessage("Message Received : " + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    String mConnectedDeviceName = new String(buf);
                    showMessage("Connected to " + mConnectedDeviceName);
                    //linSendMessage.setVisibility(View.VISIBLE);
                    //sendMessageToDevice();
                    break;
                case Constants.MESSAGE_SERVER_CONNECTED:
                    showMessage("CONNECTED");
                    Log.i(TAG, "Connected...");
                    //linSendMessage.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };
}
