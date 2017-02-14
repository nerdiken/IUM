package com.example.axant.library;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by axant on 13/02/2017.
 */

public class CatalogAdapter extends ArrayAdapter<Book> implements View.OnClickListener{
    private ArrayList<Book> dataSet;
    Context mContext;
    Activity a;

    // View lookup cache
    private static class ViewHolder {
        TextView txtTitle;
        TextView txtAuthor;
        TextView txtYear;
        TextView txtPublishing;
        TextView txtStatus;
        Button btnTake;
    }

    public CatalogAdapter(ArrayList<Book> data, Context context, Activity a) {
        super(context, R.layout.catalog_list_layout, data);
        this.dataSet = data;
        this.mContext=context;
        this.a=a;

    }

    @Override
    public void onClick(View v) {

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Book dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.catalog_list_layout, parent, false);
            viewHolder.txtTitle = (TextView) convertView.findViewById(R.id.catalog_title);
            viewHolder.txtAuthor = (TextView) convertView.findViewById(R.id.catalog_author);
            viewHolder.txtYear = (TextView) convertView.findViewById(R.id.catalog_year);
            viewHolder.txtPublishing = (TextView) convertView.findViewById(R.id.catalog_publishing);
            viewHolder.txtStatus = (TextView) convertView.findViewById(R.id.catalog_status);
             viewHolder.btnTake =  (Button) convertView.findViewById(R.id.catalog_button);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.txtTitle.setText(dataModel.getTitle());
        viewHolder.txtAuthor.setText("di " + dataModel.getAuthor());
        viewHolder.txtYear.setText("Pubblicato nel " +String.valueOf(dataModel.getYear()));
        viewHolder.txtPublishing.setText("Casa Editrice: " +dataModel.getPublishing());


        if(dataModel.getStatus().equals("on_loan")){
            viewHolder.txtStatus.setTextColor(Color.parseColor("#B70000"));
            viewHolder.txtStatus.setText("In prestito!");
            viewHolder.btnTake.setVisibility(View.INVISIBLE);
        }
        else{
            viewHolder.txtStatus.setTextColor(Color.parseColor("#00B200"));
            viewHolder.txtStatus.setText("Disponibile!");

        }

        viewHolder.btnTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(a);
                builder.setMessage("Sei sicuro di voler prendere in prestito questo libro?")
                        .setCancelable(false)
                        .setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String url = mContext.getString(R.string.base_url) + "/get_loan";

                                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                // Display the first 500 characters of the response string.
                                                Log.d("Volley","Response is: "+ response);
                                                a.finish();
                                                a.startActivity(a.getIntent());

                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.d("Volley", error.getMessage());
                                    }
                                }){
                                    @Override
                                    protected Map<String,String> getParams(){
                                        Map<String,String> params = new HashMap<>();
                                        params.put("book_id", String.valueOf(dataModel.getId()));
                                        SharedPreferences sharedPref = mContext.getSharedPreferences(
                                                mContext.getString(R.string.login_preferences), Context.MODE_PRIVATE);
                                        int token = sharedPref.getInt(mContext.getString(R.string.login_token), -1);
                                        params.put("user_id", String.valueOf(token));
                                        return params;
                                    }

                                    @Override
                                    public Map<String, String> getHeaders() throws AuthFailureError {
                                        Map<String,String> params = new HashMap<>();
                                        params.put("Content-Type","application/x-www-form-urlencoded");
                                        return params;
                                    }
                                };
                                VolleySingleton.getInstance(mContext).addToRequestQueue(stringRequest);
                                dialog.dismiss();
                                Toast.makeText(a, "Hai preso in prestito il libro! Lo troverai nella sezione 'I miei prestiti'",
                                        Toast.LENGTH_LONG).show();
                                a.finish();
                                a.startActivity(a.getIntent());


                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }
}
