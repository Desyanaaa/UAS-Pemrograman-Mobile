package com.smartneasy.hargahponline;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.smartneasy.hargahponline.adapter.ListAdapterHandphone;
import com.smartneasy.hargahponline.model.Handphone;
import com.smartneasy.hargahponline.server.AsyncInvokeURLTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private static final String TAG = "MainActivity";
    private ListView listView;
    private ActionMode actionMode;
    private ActionMode.Callback amCallback;
    private List<Handphone> listhp;
    private ListAdapterHandphone adapter;
    private Handphone selectedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listview_main);
        amCallback = new ActionMode.Callback() {
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                actionMode = null;
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                getMenuInflater().inflate(R.menu.activity_main_action, menu);
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_menu_edit:
                        showUpdateForm();
                        break;
                    case R.id.action_menu_delete:
                        delete();
                        break;
                }
                mode.finish();
                return false;
            }
        };

        listhp = new ArrayList<>();
        loadDataHP();
    }

    private void showUpdateForm() {
        Intent in = new Intent(getApplicationContext(), FormHandphone.class);
        in.putExtra("id", selectedList.getId().toString());
        in.putExtra("nama", selectedList.getNama());
        in.putExtra("harga", selectedList.getHarga());
        startActivity(in);
    }

    private void delete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete " + selectedList.getNama() + " ?");
        builder.setTitle("Delete");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                listhp.remove(selectedList);
                Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.setIcon(android.R.drawable.ic_menu_delete);
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.option_menu_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        if (searchView == null) {
            MenuItemCompat.setShowAsAction(searchItem,
                    MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_ALWAYS);
            MenuItemCompat.setActionView(searchItem, searchView = new SearchView(MainActivity.this));
        }

        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) searchView.findViewById(id);
        textView.setTextColor(Color.WHITE);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("nama");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_menu_new:
                Intent in = new Intent(getApplicationContext(), FormHandphone.class);
                startActivity(in);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void processResponse(String response) {
        try {
            JSONObject jsonObj = new JSONObject(response);
            JSONArray jsonArray = jsonObj.getJSONArray("handphone");
            Log.d(TAG, "data length: " + jsonArray.length());

            Handphone handphone = null;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                handphone = new Handphone();
                handphone.setId(obj.getInt("id"));
                handphone.setNama(obj.getString("nama"));
                handphone.setHarga(obj.getString("harga"));
                listhp.add(handphone);
            }
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private void populateListView() {
        adapter = new ListAdapterHandphone(getApplicationContext(), listhp);
        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View v, int pos, long id) {
                if (actionMode != null) {
                    return false;
                }

                actionMode = startActionMode(amCallback);
                v.setSelected(true);
                selectedList = (Handphone) adapter.getItem(pos);
                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int pos, long id) {
                selectedList = (Handphone) adapter.getItem(pos);
                Intent in = new Intent(getApplicationContext(), DetailHandphone.class);
                in.putExtra("id", selectedList.getId().toString());
                in.putExtra("nama", selectedList.getNama());
                in.putExtra("harga", selectedList.getHarga());
                startActivity(in);
            }
        });
    }

    public void loadDataHP() {
        try {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("list_phone.php")
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
                            processResponse(responseData);
                            populateListView();
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteData() {
        try {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("/select_all.php")
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
                            processResponse(responseData);
                            populateListView();
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
