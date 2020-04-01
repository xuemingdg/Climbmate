package com.hokming.climbmate;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.gyf.immersionbar.ImmersionBar;

import net.qiujuer.genius.blur.StackBlur;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements SensorEventListener{


    private static final String TAG = "MainActivity";
    private float[] r = new float[9];
    private float[] values = new float[3];
    private float[] gravity = null;
    private float[] geomagnetic = null;

    private SensorManager sensorManager;
    private CustomHandler customHandler = null;
    private float angle = 0.0F;
    private float pressure = 0.0F;
    private float altitude = 0.0F;

    @BindView(R.id.compass)
    ImageView compass;

    @BindView(R.id.pressure)
    TextView pressureTextView;

    @BindView(R.id.altitude)
    TextView altitudeTextView;

    @BindView(R.id.background_imagev)
    ImageView backgroundImageview;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initService();
        initUI();

    }

    private void initService() {
        customHandler = new CustomHandler();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //        new RxPermissions(this).request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
//                .compose(RxUtil.lifeCycle(this))
//                .subscribe(bool -> {
//                    if (bool) {
//                        SmartLocation.with(this).location().start(location -> Log.d("dxm", "onLocationUpdated: "+location.toString()));
//                    } else {
//                        Toast.makeText(this, "Please grant permissions", Toast.LENGTH_SHORT).show();
//                    }
//                });
    }

    private void initUI() {
        ButterKnife.bind(this);
        ImmersionBar.with(this).init();
        Bitmap bmp = BitmapFactory.decodeResource(getResources(),R.drawable.login_background );
        Bitmap result = StackBlur.blurNatively(bmp, 100 , false);
        bmp.recycle();
        backgroundImageview.setBackground(new BitmapDrawable(result));
    }


    private class CustomHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (gravity != null && geomagnetic != null) {
                        if (SensorManager.getRotationMatrix(r, null, gravity, geomagnetic)) {
                            SensorManager.getOrientation(r, values);
                            float degree = (float) ((360f - values[0] * 180f / Math.PI) % 360);
//                            Log.i(TAG, "angle from northpole:" + degree);
                            RotateAnimation ra = new RotateAnimation(angle, degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                            ra.setDuration(5);
                            ra.setFillAfter(true);
                            compass.startAnimation(ra);
                            angle = degree;
                        }
                    }
                    break;
                case 1:
                    pressureTextView.setText(pressure + " hPa");
                    altitudeTextView.setText(altitude + " m");
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Sensor acceleSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, acceleSensor, SensorManager.SENSOR_DELAY_NORMAL);
        Sensor magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_NORMAL);
        Sensor pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                gravity = event.values;
                customHandler.sendEmptyMessage(0);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                geomagnetic = event.values;
                customHandler.sendEmptyMessage(0);
                break;
            case Sensor.TYPE_PRESSURE:
                pressure = event.values[0];
                altitude = calculateAltitude(pressure);
                customHandler.sendEmptyMessage(1);
                break;
        }
    }

    private float calculateAltitude(float pressure) {
        float sp = 1013.25f; //standard pressure
        return (sp - pressure) * 100.0f / 12.7f;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        customHandler.removeMessages(0);
        customHandler.removeMessages(1);
        customHandler = null;
    }
}
