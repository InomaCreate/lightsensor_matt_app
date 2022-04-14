package com.example.lightsensor_mqtt_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mLight;
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        // 近接センサーを無効
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_LIGHT){
            Log.i(TAG,"Light!! value:"+event.values[0]);
            TextView textView = (TextView)findViewById(R.id.lightValueText);
            textView.setText(String.valueOf(event.values[0]));
        }

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // センサーの精度を変更するときに使う
    }

}