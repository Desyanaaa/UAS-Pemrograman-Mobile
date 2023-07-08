package com.smartneasy.hargahponline;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.smartneasy.hargahponline.model.Handphone;
import com.smartneasy.hargahponline.server.AsyncInvokeURLTask;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FormHandphone extends AppCompatActivity {
    private EditText textNama, textHarga;
    private Handphone handphone;
    public static final String urlSubmit = "submit_phone.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_handphone);
        initView();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        handphone = new Handphone();
        if (getIntent().hasExtra("id")) {
            String id = getIntent().getStringExtra("id");
            String nama = getIntent().getStringExtra("nama");
            String harga = getIntent().getStringExtra("harga");
            textNama.setText(nama);
            textHarga.setText(harga);
            handphone.setId(Integer.valueOf(id));
        } else {
            handphone.setId(0);
        }
    }

    private void initView() {
        textNama = findViewById(R.id.add_new_nama);
        textHarga = findViewById(R.id.add_new_harga);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.form_handphone, menu);
        return true;
    }

    private void goToMainActivity() {
        Intent in = new Intent(getApplicationContext(), MainActivity.class);
        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(in);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                goToMainActivity();
                break;
            case R.id.option_menu_save:
                if (textHarga.getText().toString().trim().isEmpty() ||
                        textNama.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Nama dan Harga tidak boleh kosong", Toast.LENGTH_SHORT).show();
                } else {
                    sendData();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendData() {
        try {
            String nama = textNama.getText().toString();
            String harga = URLEncoder.encode(textHarga.getText().toString(), "utf-8");

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new FormBody.Builder()
                    .add("nama", nama)
                    .add("harga", harga)
                    .add("id", String.valueOf(handphone.getId()))
                    .build();

            Request request = new Request.Builder()
                    .url(urlSubmit)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Tidak Dapat Terkoneksi dengan Server", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseData = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handleResponse(responseData);
                        }
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleResponse(String response) {
        Log.d("TAG", "savedata: " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            String result = jsonObject.getString("result");

            if (result.equals("timeout") || result.trim().equalsIgnoreCase("Tidak dapat Terkoneksi ke Data Base")) {
                Toast.makeText(getBaseContext(), "Tidak Dapat Terkoneksi dengan Server", Toast.LENGTH_SHORT).show();
            } else {
                goToMainActivity();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
