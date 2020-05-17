package com.example.assignment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLOutput;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {
    private EditText login_id;
    private EditText password;
    private EditText re_password;
    private Button btn_reg;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        login_id = findViewById(R.id.reg_login_id);
        password = findViewById(R.id.reg_password);
        re_password = findViewById(R.id.reg_repassword);
        btn_reg = findViewById(R.id.btn_register);

        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckPassword(password.getText().toString(), re_password.getText().toString());
            }
        });
    }

    public void CheckPassword (String password, String re_password) {
        if(!password.equals(re_password)){
            System.out.println("password: " + password);
            System.out.println("re-password: " + re_password);
            Toast.makeText(RegisterActivity.this, "Password not match. Please try again!", Toast.LENGTH_LONG).show();
        } else{

            Register(login_id.getText().toString(), password);
        }
    }

    private void Register(String login_id, String password) {
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
        Request request = new Request.Builder().url("https://androidkbackend.herokuapp.com/user/register").post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("IOException", e.getMessage());
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RegisterActivity.this, "Register fail, maybe due to network problem.", Toast.LENGTH_SHORT).show();
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
                            switch (result.getInt("status")) {
                                case 400:
                                    Toast.makeText(getApplicationContext(), "Login ID already in use. Try another Login ID.", Toast.LENGTH_LONG).show();
                                    return;
                                case 201:
                                    Toast.makeText(getApplicationContext(), "Register Successful.", Toast.LENGTH_LONG).show();
                                    finish();
                                    return;
                                default:
                                    Toast.makeText(getApplicationContext(), "Unknown Error.", Toast.LENGTH_LONG).show();
                                    return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}
