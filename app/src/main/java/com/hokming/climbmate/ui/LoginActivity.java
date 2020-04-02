package com.hokming.climbmate.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.gyf.immersionbar.ImmersionBar;
import com.hokming.climbmate.R;
import com.hokming.climbmate.util.MySQLiteOpenHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.username_edit)
    TextInputEditText usernameEditText;

    @BindView(R.id.password_edit)
    TextInputEditText passwordEditText;

    @BindView(R.id.loginbtn)
    Button loginButton;

    private static final String TAG = "LoginActivity";
    public static final String sp = "LOGIN_USER";
    private boolean usernameFinishEdit = false;
    private boolean passwordFinishEdit = false;
    private SQLiteDatabase db;
    private MySQLiteOpenHelper mySQLiteOpenHelper;
    private SharedPreferences sharedPreferences = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (preLoad(savedInstanceState)) {
            return;
        }
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        ImmersionBar.with(this).init();
        initEdittextListeners();
    }

    private boolean preLoad(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences(sp, MODE_PRIVATE);
        String username=sharedPreferences.getString(MySQLiteOpenHelper.USER_COLUMN_USERNAME, "");
        if(!username.isEmpty()){
            goToMain(username);
            return true;
        }
        return false;
    }

    @OnClick(R.id.loginbtn)
    public void login(){
        String usernameInput = usernameEditText.getEditableText().toString();
        String passwordInput = passwordEditText.getEditableText().toString();
        if(!usernameInput.isEmpty() && !passwordInput.isEmpty()){
            mySQLiteOpenHelper = new MySQLiteOpenHelper(getApplicationContext());
            db = mySQLiteOpenHelper.getWritableDatabase();
            Cursor cursor = mySQLiteOpenHelper.getUser(usernameInput, db);
            if(cursor.getCount()>0){
                cursor.moveToFirst();
                //TODO: encrypt password
                String password = cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.USER_COLUMN_PASSWORD));
                if(!password.equals(passwordInput)){
                    Toast.makeText(this, "Incorrect password!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            else{
                mySQLiteOpenHelper.insertUser(usernameInput, passwordInput , 0, db);
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(MySQLiteOpenHelper.USER_COLUMN_USERNAME, usernameInput);
            editor.apply();
            goToMain(usernameInput);
        }
        else{
            Toast.makeText(this, "Username or password is empty!", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToMain(String usernameInput) {
        Intent intent = new Intent(this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(MySQLiteOpenHelper.USER_COLUMN_USERNAME, usernameInput);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    private void initEdittextListeners() {
        usernameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});
        passwordEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty()){
                    usernameFinishEdit = true;
                }
                if(usernameFinishEdit && passwordFinishEdit){
                    loginButton.setEnabled(true);
                }
            }
        });
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty()){
                    passwordFinishEdit = true;
                }
                if(usernameFinishEdit && passwordFinishEdit){
                    loginButton.setEnabled(true);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(db!=null){
            db.close();
        }
    }
}
