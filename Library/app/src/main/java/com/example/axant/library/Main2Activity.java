package com.example.axant.library;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.id.content;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.login_preferences), Context.MODE_PRIVATE);

        int token = sharedPref.getInt(getString(R.string.login_token), -1);
        if (token == -1) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
       setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),CatalogActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        Intent intent = getIntent();
        TextView textWelcome = (TextView) findViewById(R.id.textWelcome);

        String username = intent.getStringExtra("user");
        if (username != null) {

            textWelcome.setText("Bentornato " + username + "!");
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("user", username);
            editor.commit();
        } else {
            textWelcome.setText("Ciao " + sharedPref.getString("user", "") + "!");
        }

        prepareListData();
    }


    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        String url = getString(R.string.base_url) + getString(R.string.api_my_loans) + "?id=1";


        JsonArrayRequest jsObjRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        // get the listview
                        expListView = (ExpandableListView) findViewById(R.id.bookList);

                        // preparing list data

                        Log.d("Volley", "Response: " + response);
                        TextView no_books = (TextView) findViewById(R.id.no_books);
                        TextView consulta = (TextView) findViewById(R.id.consulta);
                        if (response.length() == 0) {

                            no_books.setVisibility(View.VISIBLE);
                            consulta.setVisibility(View.VISIBLE);
                        } else {
                            no_books.setVisibility(View.GONE);
                            consulta.setVisibility(View.GONE);
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject book = response.getJSONObject(i);
                                    listDataHeader.add(book.getString("id") + ";" + book.getString("title"));
                                    // Adding child data
                                    List<String> book_details = new ArrayList<String>();
                                    book_details.add("Autore: " + book.getString("author"));
                                    book_details.add("Anno di pubblicazione: " + book.getString("year"));
                                    book_details.add("Casa editrice: " + book.getString("publishing"));
                                    listDataChild.put(listDataHeader.get(i), book_details); // Header, Child data
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                            listAdapter = new ExpandableListAdapter(getApplicationContext(), Main2Activity.this, listDataHeader, listDataChild);

                            // setting list adapter
                            expListView.setAdapter(listAdapter);

                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley", "Error: " + error.toString());
                        String message = "Error";
                        if (error instanceof NoConnectionError) {
                            message = "Impossibile connettersi al server. Riprova più tardi!";
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this);
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
    public void onRestart() {
        super.onResume();
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.login_preferences), Context.MODE_PRIVATE);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);

        int token = sharedPref.getInt(getString(R.string.login_token), -1);
        if (token != -1) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        } else {
            prepareListData();
        }

        Log.d("Restart", "Restart");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

    }
    


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        item.setChecked(true);

        if (id == R.id.catalog) {
            Intent intent = new Intent(getApplicationContext(),CatalogActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.loans) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;

        } else if (id == R.id.logout) {

            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                    getString(R.string.login_preferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(getString(R.string.login_token), -1);
            editor.commit();
            Log.d("Token", String.valueOf(sharedPref.getInt(getString(R.string.login_token), -1)));
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            finish();
            startActivity(intent);


        }
        return true;
    }


}
