package com.example.axant.library;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CatalogActivity extends AppCompatActivity {
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        String url = getString(R.string.base_url) + "/books";

        JsonArrayRequest jsObjRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        ArrayList books= new ArrayList<>();

                        Log.d("Response",response.toString());
                        for (int i = 0; i < response.length(); i++) {
                            try {

                                JSONObject book = response.getJSONObject(i);
                                books.add(new Book(book.getInt("id"), book.getString("title"), book.getString("author"),
                                        book.getString("publishing"), book.getInt("year"), book.getString("status")));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        listView=(ListView)findViewById(R.id.catalogList);

                        CatalogAdapter adapter= new CatalogAdapter(books,getApplicationContext(), CatalogActivity.this);

                        listView.setAdapter(adapter);

                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley", "Error: " + error.toString());
                        String message = "Error";
                        if (error instanceof NoConnectionError) {
                            message = "Impossibile connettersi al server. Riprova più tardi!";
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(CatalogActivity.this);
                        builder.setMessage(message)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();

                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();

                    }
                });

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
        // Adding child data




    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // todo: goto back activity from here

                Intent intent = new Intent(CatalogActivity.this, Main2Activity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
