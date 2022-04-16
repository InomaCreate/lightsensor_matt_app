package com.example.lightsensor_mqtt_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import io.moquette.BrokerConstants;
import io.moquette.broker.config.MemoryConfig;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mLight;
    private String TAG = "MainActivity";
    io.moquette.broker.Server broker;
    String IPAddress=null;
    private MqttAndroidClient mqttAndroidClient;
    private int LightValue;
    private Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(mqttAndroidClient == null)
                    return;

                try {
                    Log.i(TAG,"publish!!");
                    mqttAndroidClient.publish("sensor/light", String.valueOf(LightValue).getBytes(), 0, true);
                } catch (MqttPersistenceException e) {
                    Log.d(TAG,e.toString());
                } catch (MqttException e) {
                    Log.d(TAG,e.toString());
                }

            }
        },1000,1000);

        // Broker起動処理
        try {
            broker = new io.moquette.broker.Server();

            MemoryConfig memoryConfig = new MemoryConfig(new Properties());
            memoryConfig.setProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, this.getFilesDir() + BrokerConstants.DEFAULT_MOQUETTE_STORE_H2_DB_FILENAME);

            broker.startServer(memoryConfig);
            Log.i(TAG,"startServer");

        } catch (IOException e){
            e.printStackTrace();
        }

        // 自分のIPアドレスを取得する
        try {
            for (NetworkInterface n : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress addr : Collections.list(n.getInetAddresses())) {
                    if(addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        Log.i(TAG,"IP ADDRESS:"+addr.getHostAddress());
                        IPAddress = addr.getHostAddress();
                        TextView textView = (TextView)findViewById(R.id.textIP);
                        textView.setText(IPAddress);
                    }
                }
            }
        } catch (SocketException e){
            Log.e(TAG,"IP address not get!!");
            e.printStackTrace();
        }

        // クライアントブローカー接続
        if(IPAddress!=null) {
            mqttAndroidClient = new MqttAndroidClient(this, "tcp://" + IPAddress + ":1883", "test-android") {
                @Override
                public void onReceive(Context context, Intent intent) {
                    super.onReceive(context, intent);
                }
            };
            try {
                MqttConnectOptions options = new MqttConnectOptions();
                mqttAndroidClient.connect(options, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken iMqttToken) {
                        Log.d(TAG, "onSuccess");
                    }

                    @Override
                    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                        Log.d(TAG, "onFailure");
                    }
                });

            } catch (MqttException e) {
                Log.d(TAG, e.toString());
            }
        }


        // センサーオブジェクトを取得
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // 照度センサーのオブジェクトを取得
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);


    }

    @Override
    protected void onResume(){
        super.onResume();

        // センサーのリスナー登録
        mSensorManager.registerListener(this,mLight,SensorManager.SENSOR_DELAY_NORMAL);

    }
    @Override
    protected void onPause() {
        super.onPause();
        // センサーを無効
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_LIGHT){
            Log.i(TAG,"Light!! value:"+event.values[0]);
            TextView textView = (TextView)findViewById(R.id.lightValueText);
            textView.setText(String.valueOf(event.values[0]));
            LightValue = (int)event.values[0];
        }

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // センサーの精度を変更するときに使う
    }

}