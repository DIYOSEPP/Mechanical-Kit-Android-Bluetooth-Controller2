package com.osepp.remote;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.lang.reflect.Method;

import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.content.IntentFilter;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.widget.AdapterView.OnItemClickListener;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.Manifest;
import android.content.pm.PackageManager;

public class btSelectorActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;

    cbtAdapter adapter;

    private Set<BluetoothDevice> pairedDevices;
    private ListView lv;
    private List<cbtDevice> list;
    private boolean continueDiscover;

    private void runController(String  btAddress){
        Intent intent = new Intent();
        intent.setClass(btSelectorActivity.this, controlActivity.class);
        intent.putExtra("address",btAddress);
        startActivity(intent);
    }

    private void startDiscovery(){
        continueDiscover=true;
        if (bluetoothAdapter.isDiscovering())return;
        for(cbtDevice b:list)b.founded=false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (bluetoothAdapter.isDiscovering())bluetoothAdapter.cancelDiscovery();
                bluetoothAdapter.startDiscovery();
            }
        }).start();
    }
    private void stopDiscovery(){
        continueDiscover=false;
        if(bluetoothAdapter!=null)bluetoothAdapter.cancelDiscovery();
    }
    private void fillBoundList()
    {
        if(bluetoothAdapter==null)return;
        Set<BluetoothDevice> devices =bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bt:devices) {
            cbtDevice d=null;
            for(int i=0;i<list.size();i++){
                cbtDevice t=list.get(i);
                if(t.mAddress.equals(bt.getAddress())){
                    d=t;
                    break;
                }
            }
            if(d==null){
                d=new cbtDevice(bt.getName(),bt.getAddress());
                d.mBonded=bt.getBondState()==BluetoothDevice.BOND_BONDED;
                d.founded=false;
                d.rssi=0;
                list.add(d);
            }
        }
    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                cbtDevice d=null;
                int i=0;
                for(i=0;i<list.size();i++){
                    d=list.get(i);
                    if(d.mAddress.equals(device.getAddress())){
                        d.rssi=rssi;
                        d.founded=true;
                        d.mName=device.getName();
                        d.mBonded=device.getBondState()==BluetoothDevice.BOND_BONDED;
                        break;
                    }
                }
                if(i>=list.size()){
                    d=new cbtDevice(device.getName(),device.getAddress());
                    d.mBonded=device.getBondState()==BluetoothDevice.BOND_BONDED;
                    d.rssi=rssi;
                    d.founded=true;
                    list.add(d);
                }
                adapter.notifyDataSetChanged();
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                for(cbtDevice b:list)if(b.founded==false)b.rssi=0;

                adapter.notifyDataSetChanged();
                if(continueDiscover)startDiscovery();
            }else if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                int cur_state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,0);
                if(cur_state==BluetoothAdapter.STATE_ON){
                    fillBoundList();
                    startDiscovery();
                }
            }else if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:
                        //Log.d("BlueToothTestActivity", "正在配对......");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        runController(device.getAddress());
                        //Log.d("BlueToothTestActivity", "完成配对");
                        break;
                    case BluetoothDevice.BOND_NONE:
                        //Log.d("BlueToothTestActivity", "取消配对");
                    default:
                        break;
                }
                startDiscovery();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT>=23)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 10);   }
        }
        setContentView(R.layout.activity_btselector);
        lv = (ListView) findViewById(R.id.ListView1);
        list=new ArrayList<cbtDevice>();
        adapter=new cbtAdapter(this,0,list);

        lv.setAdapter(adapter);

        IntentFilter mFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, mFilter);
        mFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mReceiver, mFilter);
        mFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, mFilter);
        mFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, mFilter);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),
                    "BlueTooth not found!",
                    Toast.LENGTH_SHORT).show();
        } else {

        }
        lv.setOnItemClickListener(btItemClick);
    };
    private OnItemClickListener btItemClick=new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            cbtDevice d=list.get(position);
            BluetoothDevice btDev=null;
            if(d!=null){
                btDev=bluetoothAdapter.getRemoteDevice(d.mAddress);
            }
            if(btDev!=null){
                stopDiscovery();
                if(btDev.getBondState()!=BluetoothDevice.BOND_BONDED){
                    try {
                        Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                        createBondMethod.invoke(btDev);
                    }catch (Exception e){

                    }
                }else{
                    runController(btDev.getAddress());
                }
            }
        }
    };
    @Override
    protected void onResume() {
        super.onResume();

        if(bluetoothAdapter!=null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOn, 0);
            } else {
                fillBoundList();
                startDiscovery();
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        stopDiscovery();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}

class cbtDevice{
    public String mName;
    public String mAddress;
    public boolean mBonded;
    public int rssi;
    public boolean founded;
    public cbtDevice(String name,String address){
        this.mName=name;
        this.mAddress=address;
        this.founded=true;
        this.rssi=0;
        this.mBonded=false;
    }
}

class cbtAdapter extends ArrayAdapter<cbtDevice>{
    private List<cbtDevice> objects;
    private Context context;

    public cbtAdapter(Context context, int resourceId, List<cbtDevice> objects) {
        super(context, resourceId, objects);
        this.objects=objects;
        this.context=context;
    }
    private static class ViewHolder
    {
        TextView name;
        TextView address;
        TextView content;
    }
    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView==null)
        {
            viewHolder=new ViewHolder();
            LayoutInflater mInflater=LayoutInflater.from(context);
            convertView = mInflater.inflate(R.layout.item, null);
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            viewHolder.address = (TextView) convertView.findViewById(R.id.address);
            viewHolder.content = (TextView) convertView.findViewById(R.id.content);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        cbtDevice btd = objects.get(position);
        if(null!=btd)
        {
            int textColor;
            if(btd.rssi<0)textColor=Color.GREEN;else textColor=Color.BLACK;

            viewHolder.name.setTextColor(textColor);
            viewHolder.address.setTextColor(textColor);
            viewHolder.content.setTextColor(textColor);
            if(btd.mBonded){
                viewHolder.name.setText(btd.mName+ " (Paired)");
            }
            else{
                viewHolder.name.setText(btd.mName);
            }
            viewHolder.address.setText(btd.mAddress);

            if(btd.rssi<0) {
                double p = (Math.abs(btd.rssi) - 65) / (10 * 2.0);
                p = Math.pow(10, p);
                viewHolder.content.setText(String.format("Rssi:%d, Dis:%.2fm", btd.rssi, p));
            }else{
                viewHolder.content.setText("Rssi:--, Dis:--");
            }
        }
        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}