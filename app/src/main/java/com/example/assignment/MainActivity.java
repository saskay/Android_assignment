package com.example.assignment;

import androidx.appcompat.app.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyStore;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Button login_btn = (Button) findViewById(R.id.login_btn);
        Button btn_register = (Button) findViewById(R.id.btn_register);
        final EditText password = (EditText) findViewById(R.id.password_value);
        final EditText login_id = (EditText) findViewById((R.id.login_id));
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), login_id.getText(), Toast.LENGTH_SHORT).show();
                Login(login_id.getText().toString(), password.getText().toString());
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent register = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(register);
            }});
    }

    private void Login(String login_id, String password){
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("login_id", login_id);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        Request request = new Request.Builder().url("https://androidkbackend.herokuapp.com/user/login").post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("IOException", e.getMessage());
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Fail", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseString = response.body().string();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject result = new JSONObject(responseString);
                            if(result.getInt("user_id") > 0) {
                                Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_LONG).show();
                                startPrimaryActivity(result.getInt("user_id"), result.getString("login_id"));
                            }else{
                                Toast.makeText(getApplicationContext(), "Login fail, Please try again!", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void startPrimaryActivity(int user_id, String login_id) {
        SharedPreferences auth = getSharedPreferences("auth", MODE_PRIVATE);
        auth.edit().putString("login_status", "true");
        auth.edit().putString("user_id", String.valueOf(user_id));
        auth.edit().putString("login_id", login_id);

        Intent intent = new Intent(this, PrimaryActivity.class);
        startActivity(intent);
        finish();
    }
}
