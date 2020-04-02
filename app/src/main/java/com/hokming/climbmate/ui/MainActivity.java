package com.hokming.climbmate.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gyf.immersionbar.ImmersionBar;
import com.hokming.climbmate.R;
import com.hokming.climbmate.util.MySQLiteOpenHelper;

import net.qiujuer.genius.blur.StackBlur;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.nlopez.smartlocation.SmartLocation;

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
    private SQLiteDatabase db;
    private MySQLiteOpenHelper mySQLiteOpenHelper;
    private String loginUserName = "";
    private String name = "";

    @BindView(R.id.compass)
    ImageView compass;

    @BindView(R.id.pressure)
    TextView pressureTextView;

    @BindView(R.id.altitude)
    TextView altitudeTextView;

    @BindView(R.id.user_title)
    EditText nameTextView;

    @BindView(R.id.edit_btn)
    ImageView editBtn;

    @BindView(R.id.background_imagev)
    ImageView backgroundImageview;

    @BindView(R.id.panelview)
    CircularProgressView circularProgressView;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initService();
        initUI();
        getLoginUser();
    }

    private void getLoginUser() {
        try {
            loginUserName = getIntent().getExtras().getString(MySQLiteOpenHelper.USER_COLUMN_USERNAME);
            mySQLiteOpenHelper = new MySQLiteOpenHelper(getApplicationContext());
            db = mySQLiteOpenHelper.getWritableDatabase();
            Cursor cursor = mySQLiteOpenHelper.getUser(loginUserName, db);
            if(cursor.getCount()>0){
                cursor.moveToFirst();
                name = cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.USER_COLUMN_NAME));
                nameTextView.setText(name);
            }
        }catch (NullPointerException ex){
            ex.printStackTrace();
        }
    }

    private void initService() {
        customHandler = new CustomHandler();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    private void initUI() {
        ButterKnife.bind(this);
        ImmersionBar.with(this).init();
        Bitmap bmp = BitmapFactory.decodeResource(getResources(),R.drawable.login_background );
        Bitmap result = StackBlur.blurNatively(bmp, 100 , false);
        bmp.recycle();
        backgroundImageview.setBackground(new BitmapDrawable(result));
        nameTextView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event != null &&
                            event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (event == null || !event.isShiftPressed()) {
                    // the user is done typing.
                    editBtn.setVisibility(View.VISIBLE);
                    nameTextView.setEnabled(false);
                    name = nameTextView.getText().toString();
                    mySQLiteOpenHelper.updateUser(loginUserName, name);
                    return true; // consume.
                }
            }
            return false; // pass on to other listeners.
        }
        );
    }

    @OnClick(R.id.edit_btn)
    public void editNickname(){
        editBtn.setVisibility(View.GONE);
        nameTextView.setEnabled(true);
        nameTextView.setFocusable(true);
        nameTextView.setFocusableInTouchMode(true);
        nameTextView.requestFocus();
        nameTextView.findFocus();
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(nameTextView, InputMethodManager.SHOW_FORCED);
    }

    @OnClick(R.id.logout_btn)
    public void logout(){
        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.sp, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(MySQLiteOpenHelper.USER_COLUMN_USERNAME, "");
        editor.apply();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
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
        if(pressureSensor == null){
            Toast.makeText(this, "No Pressure Sensor Detected.", Toast.LENGTH_SHORT).show();
        }
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
        if(db!=null){
            db.close();
        }
        customHandler.removeMessages(0);
        customHandler.removeMessages(1);
        customHandler = null;
    }
}
