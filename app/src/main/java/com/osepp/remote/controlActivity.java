package com.osepp.remote;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;


public class controlActivity extends AppCompatActivity {
    private JoyView joyLeft;
    private JoyView JoyRight;
    private MsgView msgView;
    private String btAddress;
    private Thread bt_thread;
    private boolean runing = true;
    private boolean dir_L = false;
    private boolean dir_R = false;
    private boolean dir_U = false;
    private boolean dir_D = false;
    private boolean fun_A = false;
    private boolean fun_B = false;
    private boolean fun_X = false;
    private boolean fun_Y = false;
    private int joyu = 0;
    private int joyv = 0;
    private int joyw = 0;
    private int joya = 0;
    private int mAccX = 0;
    private int mAccY = 0;
    private int mAccZ = 0;
    private SensorManager sm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*no way to exit app
        Window win=getWindow();
        WindowManager.LayoutParams params=win.getAttributes();
        params.systemUiVisibility= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        win.setAttributes(params);
        */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 设置全屏
        // ,
        // 屏幕长亮
        setContentView(R.layout.activity_control);

        btAddress = getIntent().getStringExtra("address");

        joyLeft = (JoyView) findViewById(R.id.joy_left);
        JoyRight = (JoyView) findViewById(R.id.joy_right);
        joyLeft.setJoyChangeListener(new JoyView.JoyChangeListener() {
            @Override
            public void report(float x, float y) {
                // TODO Auto-generated method stub
                joyu = constrain255(x * 255);
                joyv = constrain255(-y * 255);
            }
        });
        JoyRight.setJoyChangeListener(new JoyView.JoyChangeListener() {
            @Override
            public void report(float x, float y) {
                // TODO Auto-generated method stub
                joyw = constrain255(x * 255);
                joya = constrain255(-y * 255);
            }
        });
        DirButtonView db;
        db = (DirButtonView) findViewById(R.id.btn_left);
        db.setButtonChangeListener(new DirButtonView.JoyButtonChangeListener() {
            @Override
            public void report(boolean pressed) {
                dir_L = pressed;
            }
        });
        db = (DirButtonView) findViewById(R.id.btn_right);
        db.setButtonChangeListener(new DirButtonView.JoyButtonChangeListener() {
            @Override
            public void report(boolean pressed) {
                dir_R = pressed;
            }
        });
        db = (DirButtonView) findViewById(R.id.btn_up);
        db.setButtonChangeListener(new DirButtonView.JoyButtonChangeListener() {
            @Override
            public void report(boolean pressed) {
                dir_U = pressed;
            }
        });
        db = (DirButtonView) findViewById(R.id.btn_down);
        db.setButtonChangeListener(new DirButtonView.JoyButtonChangeListener() {
            @Override
            public void report(boolean pressed) {
                dir_D = pressed;
            }
        });

        db = (DirButtonView) findViewById(R.id.btn_a);
        db.setButtonChangeListener(new DirButtonView.JoyButtonChangeListener() {
            @Override
            public void report(boolean pressed) {
                fun_A = pressed;
            }
        });
        db = (DirButtonView) findViewById(R.id.btn_b);
        db.setButtonChangeListener(new DirButtonView.JoyButtonChangeListener() {
            @Override
            public void report(boolean pressed) {
                fun_B = pressed;
            }
        });
        db = (DirButtonView) findViewById(R.id.btn_x);
        db.setButtonChangeListener(new DirButtonView.JoyButtonChangeListener() {
            @Override
            public void report(boolean pressed) {
                fun_X = pressed;
            }
        });
        db = (DirButtonView) findViewById(R.id.btn_y);
        db.setButtonChangeListener(new DirButtonView.JoyButtonChangeListener() {
            @Override
            public void report(boolean pressed) {
                fun_Y = pressed;
            }
        });

        msgView = (MsgView) findViewById(R.id.msgbox);
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    final SensorEventListener myAccelerometerListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                int rotation=getWindowManager().getDefaultDisplay().getRotation();
                {
                    switch (rotation) {
                        case Surface.ROTATION_90:
                            mAccX = constrain255(sensorEvent.values[1]*25.5);
                            mAccY = constrain255(sensorEvent.values[0]*-25.5);
                            mAccZ = constrain255(sensorEvent.values[2]*25.5);
                            break;
                        case Surface.ROTATION_180:
                            mAccX = constrain255(sensorEvent.values[0]*25.5);
                            mAccY = constrain255(sensorEvent.values[1]*25.5);
                            mAccZ = constrain255(sensorEvent.values[2]*25.5);
                            break;
                        case Surface.ROTATION_270:
                            mAccX = constrain255(sensorEvent.values[1]*-25.5);
                            mAccY = constrain255(sensorEvent.values[0]*25.5);
                            mAccZ = constrain255(sensorEvent.values[2]*25.5);
                            break;
                        default:
                            mAccX = constrain255(sensorEvent.values[0]*-25.5);
                            mAccY = constrain255(sensorEvent.values[1]*-25.5);
                            mAccZ = constrain255(sensorEvent.values[2]*25.5);
                            break;
                    }
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private long mExitTime;
    public void exit() {
        if ((System.currentTimeMillis() - mExitTime) > 1000) {
            Toast.makeText(controlActivity.this, "one more back button to exit", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            exit();
            return true;
        }
        if(keyCode==KeyEvent.KEYCODE_MENU)
        {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onPause() {
        sm.unregisterListener(myAccelerometerListener);
        runing = false;
        bt_thread.interrupt();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        runing = true;
        bt_thread = new Thread(runbtconn);
        bt_thread.start();
    }

    @Override
    protected void onDestroy() {
        runing = false;
        super.onDestroy();
    }

    private static int constrain255(double v ) {
        return (int)((int)v > 512 ? 512 : ((int)v < -512 ? -512 : (int)v));
    }

    private Handler mMsgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String data = (String) msg.obj;
            msgView.appendText(data);
        }

    };

    Runnable runbtconn = new Runnable() {
        @Override
        public void run() {
            BluetoothAdapter btAdapter;
            BluetoothDevice btDev;
            BluetoothSocket btSocket = null;
            boolean loseConnect = false;
            while (runing) {
                try {
                    btAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (btAdapter == null) break;
                    btAdapter.cancelDiscovery();
                    btDev = btAdapter.getRemoteDevice(btAddress);
                    if (btDev == null) {
                        Thread.sleep(100);
                        continue;
                    }
                } catch (Exception e) {
                    btDev = null;
                    break;
                }

                try {
                    Method m = btDev.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                    btSocket = (BluetoothSocket) m.invoke(btDev, Integer.valueOf(1));
                    btSocket.connect();
                    if (btSocket.isConnected()) {
                        loseConnect = false;
                        Message msg = new Message();
                        msg.obj = "Connected to your robot\n";
                        mMsgHandler.sendMessage(msg);
                    }
                    InputStream ins = btSocket.getInputStream();
                    OutputStream ots = btSocket.getOutputStream();
                    while (runing) {
                        String s = "";
                        if (dir_L) s += 'L';
                        if (dir_R) s += 'R';
                        if (dir_U) s += 'U';
                        if (dir_D) s += 'D';
                        if (fun_A) s += 'A';
                        if (fun_B) s += 'B';
                        if (fun_X) s += 'X';
                        if (fun_Y) s += 'Y';

                        s += "u" + joyu + "v" + joyv + "w" + joyw + "a" + joya;
                        s += "x" + mAccX + "y" + mAccY+ "z" +mAccZ+ "\n";
                        byte[] bs = s.getBytes();
                        ots.write(bs);
                        ots.flush();
                        int rx = ins.available();
                        if (rx > 0) {
                            byte[] rd = new byte[rx];
                            ins.read(rd, 0, rx);
                            Message msg = new Message();
                            msg.obj = new String(rd);
                            mMsgHandler.sendMessage(msg);
                        }
                        Thread.sleep(40);
                    }
                } catch (Exception e) {
                    if (loseConnect == false) {
                        loseConnect = true;
                        Message msg = new Message();
                        msg.obj = "Lost your robot,reconnecting...\n";
                        mMsgHandler.sendMessage(msg);
                    }
                }
                try {
                    if (btSocket != null) btSocket.close();
                } catch (Exception e) {

                }
            }
        }
    };
}

