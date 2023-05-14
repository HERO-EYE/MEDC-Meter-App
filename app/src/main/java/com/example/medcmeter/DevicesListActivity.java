package com.example.medcmeter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DevicesListActivity extends AppCompatActivity {

    LinearLayout layout;
    RequestQueue queue;
    String server = "http://89.147.133.137";
    //String server = "http://dev-estrlab.atwebpages.com";
    //String server = "http://dev-estrlab.000webhostapp.com";
    String username = "medc";
    String password = "medc";
    EditText barcode_et;
    String barcode_text;
    String mac;
    String local_memory = "medc";
    //boolean longClicked = false;
    final int max_ = 100;
    String meters[] = new String[max_];
    Set<String> meters_set;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_list);

        getSupportActionBar().setTitle("");
        layout = findViewById(R.id.deviceslist_layout_list);
        queue = Volley.newRequestQueue(getApplicationContext());
        barcode_et = findViewById(R.id.editText_meterid);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        //mac = wInfo.getMacAddress();
        mac = getMacAddr();
        //Toast.makeText(getApplicationContext() , mac , Toast.LENGTH_SHORT).show();
        meters_set = new Set<String>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(@Nullable Object o) {
                return false;
            }

            @NonNull
            @Override
            public Iterator<String> iterator() {
                return null;
            }

            @NonNull
            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @NonNull
            @Override
            public <T> T[] toArray(@NonNull T[] a) {
                return null;
            }

            @Override
            public boolean add(String s) {
                return false;
            }

            @Override
            public boolean remove(@Nullable Object o) {
                return false;
            }

            @Override
            public boolean containsAll(@NonNull Collection<?> c) {
                return false;
            }

            @Override
            public boolean addAll(@NonNull Collection<? extends String> c) {
                return false;
            }

            @Override
            public boolean retainAll(@NonNull Collection<?> c) {
                return false;
            }

            @Override
            public boolean removeAll(@NonNull Collection<?> c) {
                return false;
            }

            @Override
            public void clear() {

            }
        };

        //getLocal();
        getMetersList(mac);

    }

    public void getLocal() {
        try {
            @SuppressLint("WrongConstant") SharedPreferences sh = getSharedPreferences(local_memory, 0x8000);

            count = sh.getInt("count", 0);
            if (count>0) {
                for (int i=0; i<count; i++) {
                    meters[i] = sh.getString("meterid"+i , "");
                    addDeviceRow(meters[i] , i);
                }
            }
        } catch (Exception E) {

        }
    }

    public void showAllert(String meterid, LinearLayout layout_meter, Button btn_delete) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Are you sure to delete this meter " + meterid + " ?");
         alertDialogBuilder.setPositiveButton("yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Toast.makeText(getApplicationContext(),"deleting " + meterid,Toast.LENGTH_LONG).show();
                                removeMeter(meterid, mac);
                            }
                        });

        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                layout_meter.setBackgroundColor(0xFF029857);
                btn_delete.setVisibility(View.INVISIBLE);
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void addDeviceRow(JSONArray ja , int i) throws JSONException {

        JSONObject jo = ja.getJSONObject(i);
        final boolean[] longClicked = {false};

        // #2
        final LinearLayout L = new LinearLayout(getApplicationContext());
        L.setOrientation(LinearLayout.VERTICAL);
        L.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        final View LayoutRaw = LayoutInflater.from(getApplicationContext()).inflate(R.layout.device_item, null);

        LayoutRaw.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // set value to views
        TextView text_place = LayoutRaw.findViewById(R.id.textView_deviceitem_place);
        TextView text_name = LayoutRaw.findViewById(R.id.textView_deviceitem_name);
        TextView text_meterid = LayoutRaw.findViewById(R.id.textView_deviceitem_meterid);
        LinearLayout layout_meter = LayoutRaw.findViewById(R.id.layout_item_meterinfo);
        Button btn_delete = LayoutRaw.findViewById(R.id.button_item_delete);

        String meterid = jo.getString("meter_id");
        String name    = "Meter " + String.valueOf(i+1);
        String place   = "";

        text_name.setText(name);
        text_place.setText(place);
        text_meterid.setText(meterid);

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAllert(meterid, layout_meter, btn_delete);

            }
        });

        LayoutRaw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent page = new Intent(getApplicationContext(), MainActivity2.class);
                page.putExtra("meterid" , meterid);
                startActivity(page);
            }
        });

        LayoutRaw.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                layout_meter.setBackgroundColor(0xFFFF5C50);
                btn_delete.setVisibility(View.VISIBLE);
                longClicked[0] = true;
                showAllert(meterid, layout_meter, btn_delete);
                return true;
            }
        });

        L.addView(LayoutRaw);
        layout.addView(L);
    }

    public void addDeviceRow(String meter , int i) throws JSONException {

        final boolean[] longClicked = {false};

        // #2
        final LinearLayout L = new LinearLayout(getApplicationContext());
        L.setOrientation(LinearLayout.VERTICAL);
        L.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        final View LayoutRaw = LayoutInflater.from(getApplicationContext()).inflate(R.layout.device_item, null);

        LayoutRaw.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // set value to views
        TextView text_place = LayoutRaw.findViewById(R.id.textView_deviceitem_place);
        TextView text_name = LayoutRaw.findViewById(R.id.textView_deviceitem_name);
        TextView text_meterid = LayoutRaw.findViewById(R.id.textView_deviceitem_meterid);
        LinearLayout layout_meter = LayoutRaw.findViewById(R.id.layout_item_meterinfo);
        Button btn_delete = LayoutRaw.findViewById(R.id.button_item_delete);

        String meterid = meter;
        String name    = "Meter " + String.valueOf(i+1);
        String place   = "";

        text_name.setText(name);
        text_place.setText(place);
        text_meterid.setText(meterid);

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAllert(meterid, layout_meter, btn_delete);

            }
        });

        LayoutRaw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent page = new Intent(getApplicationContext(), MainActivity2.class);
                page.putExtra("meterid" , meterid);
                startActivity(page);
            }
        });

        LayoutRaw.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                layout_meter.setBackgroundColor(0xFFFF5C50);
                btn_delete.setVisibility(View.VISIBLE);
                longClicked[0] = true;
                showAllert(meterid, layout_meter, btn_delete);
                return true;
            }
        });

        L.addView(LayoutRaw);
        layout.addView(L);
    }

    public void getMetersList(String mac) {

        // http://89.147.133.137/medc/getDevicesList.php?mac=12:33:11:ae&username=root&password=root
        String url = server + "/medc/getDevicesList.php?mac=" + mac + "&username=" + username + "&password=" + password;
        //String url = "http://89.147.133.137/medc/getDevicesList.php?mac=D4:40:F0:FA:F0:4D&username=medc&password=medc";

        StringRequest ExampleStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()) {
                    try {
                        Log.v("data", response);
                        JSONArray ja = new JSONArray(response);
                        count = ja.length();
                        layout.removeAllViews();
                        for (int i=0 ; i< ja.length() ; i++) {
                            addDeviceRow(ja , i);
                            meters[i] = ja.getJSONObject(i).getString("meter_id");
                         }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(getApplicationContext() , "No Meters! .. Add new Meter" , Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("error" , error.getMessage());
            }
        });
        queue.add(ExampleStringRequest);
    }

    public void RESET_LIST() {
        layout.removeAllViews();
        getMetersList(mac);
    }

    public void removeMeter(String meterid , String mac) {
        String url = server + "/medc/deleteMeter.php?meterid=" + meterid + "&mac=" + mac + "&username=" + username + "&password=" + password;
        Log.v("url" , url);
        StringRequest ExampleStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()) {
                    Toast.makeText(getApplicationContext() , "Meter removed" , Toast.LENGTH_SHORT).show();
                    RESET_LIST();
                } else {
                    Toast.makeText(getApplicationContext() , "failed!" , Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
                Log.v("error" , error.getMessage());
            }
        });
        queue.add(ExampleStringRequest);
    }


    public void addMeter(String meterid , String mac) {
        if (meterid.length()>10) {
            String url = server + "/medc/addDevice.php?meterid=" + meterid + "&mac=" + mac + "&username=" + username + "&password=" + password;
            Log.v("url", url);
            StringRequest ExampleStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                @Override
                public void onResponse(String response) {
                    if (!response.isEmpty()) {
                        if (response.equals("1")) {
                            Toast.makeText(getApplicationContext(), "Meter added successfully", Toast.LENGTH_SHORT).show();
                            Intent page = new Intent(getApplicationContext(), MainActivity2.class);
                            page.putExtra("meterid", meterid);
                            startActivity(page);
                            finish();
                        } else
                            Toast.makeText(getApplicationContext(), "Meter is not registered yet..\nPlease connect the device with the meter!", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(getApplicationContext(), "failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                @Override
                public void onErrorResponse(VolleyError error) {
                    //This code is executed if there is an error.
                    Log.v("error", error.getMessage());
                }
            });
            queue.add(ExampleStringRequest);
        } else {
            Toast.makeText(getApplicationContext(), "Invalid meter id .. Please try another or contact the company !", Toast.LENGTH_LONG).show();
        }
    }

    public void AddNewDevice_by_scan_barcode(View view) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(DevicesListActivity.this);
        intentIntegrator.setDesiredBarcodeFormats(intentIntegrator.ALL_CODE_TYPES);
        intentIntegrator.setBeepEnabled(false);
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.setCameraId(0);
        intentIntegrator.setPrompt("SCAN");
        intentIntegrator.setBarcodeImageEnabled(false);
        intentIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
        intentIntegrator.initiateScan();
    }

    public void AddNewDevice_by_text(View view) {
        barcode_text = barcode_et.getText().toString();
        if (!barcode_text.isEmpty() && !mac.isEmpty())
            addMeter(barcode_text , mac);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult Result = IntentIntegrator.parseActivityResult(requestCode , resultCode ,data);
        if(Result != null){
            if(Result.getContents() == null){
                Log.d("MainActivity" , "cancelled scan");
                Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show();
            }
            else {
                Log.d("MainActivity" , "Scanned");
                Toast.makeText(this,"Scanned -> " + Result.getContents(), Toast.LENGTH_SHORT).show();

                if (mac.isEmpty()) {
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wInfo = wifiManager.getConnectionInfo();
                    //mac = wInfo.getMacAddress();
                    mac = getMacAddr();
                }
                String meterid = Result.getContents();
                addMeter(meterid, mac);
            }
        }
        else {
            super.onActivityResult(requestCode , resultCode , data);
        }
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }


    // Fetch the stored data in onResume()
    // Because this is what will be called
    // when the app opens again
    @Override
    protected void onResume()
    {
        super.onResume();

        try {
            if (!isInternetAvailable()) {
                @SuppressLint("WrongConstant") SharedPreferences sh = getSharedPreferences(local_memory, 0x8000);

                //meters_set = sh.getStringSet("meters", null);
                count = sh.getInt("count", 0);
                if (count > 0) {
                    layout.removeAllViews();
                    for (int i = 0; i < count; i++) {
                        meters[i] = sh.getString("meterid" + i, "");
                        addDeviceRow(meters[i], i);
                    }
                }
            }
        } catch (Exception E) {

        }

    }

    // Store the data in the SharedPreference
    // in the onPause() method
    // When the user closes the application
    // onPause() will be called
    // and data will be stored
    @Override
    protected void onPause()
    {
        super.onPause();

        // Creating a shared pref object
        // with a file name "MySharedPref" in private mode
        SharedPreferences sharedPreferences = getSharedPreferences(local_memory, MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        //String meterid = "";
        //myEdit.putString("meterid", meterid);
        //myEdit.putStringSet("meters" , meters_set);
        myEdit.putInt("count", count);
        for (int i=0 ; i< count; i++) {
            myEdit.putString("meterid" + i, meters[i]);
        }
        myEdit.commit();
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }
    }
}