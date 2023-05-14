package com.example.medcmeter;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.pd.chocobar.ChocoBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MeterDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MeterDataFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "meterid";

    // TODO: Rename and change types of parameters
    private String mParam1;
    TextView tv_status;
    TextView tv_month_kwh_unit;
    TextView tv_month_kwh;
    TextView tv_kwh;
    TextView tv_power;
    TextView tv_power_unit;
    TextView tv_cost;
    TextView tv_date ;
    TextView tv_time;
    TextView tv_id;
    ImageView im_status;
    RequestQueue queue;
    LinearLayout lay_month_kwh, lay_cost, lay_kwh, lay_power;
    String meter_id;
    int cost = 0;
    String currentDate, currentTime;
    boolean once = true;
    boolean connected = false;
    //String server = "http://dev-estrlab.atwebpages.com";
    //String server = "http://dev-estrlab.000webhostapp.com";
    String server = "http://89.147.133.137";
    View root;
    String local_memory = "medc";

    String local_meterid, local_date, local_time, local_cost, local_meter_reading, local_month_reading, local_consumption, local_voltage;
    int local_count;
    String local_meters[] = new String[100];
    

    public MeterDataFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment MeterDataFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MeterDataFragment newInstance(String param1, String param2) {
        MeterDataFragment fragment = new MeterDataFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.meter_data2, container, false);
        meter_id = ((MainActivity2) getActivity()).getIntent().getStringExtra(ARG_PARAM1);
        MAIN();
        return root;
        
    }
    
    /////////////////////////////////////////

    public void MAIN() {
        ((MainActivity2) getActivity()).setActionBarTitle("");
        init_views();

        queue = Volley.newRequestQueue(root.getContext());

        currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        NoDataConfig();

        once = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if(!isInternetAvailable()) {
                        if (once) {

                            try {
                                ChocoBar.builder().setView(root.findViewById(R.id.meter_data2_layout))
                                        .setText("No internet access.. check internet connection!")
                                        .setDuration(ChocoBar.LENGTH_LONG)
                                        .red()
                                        .show();
                            } catch (Exception E) {

                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    NO_NET();
                                }
                            });
                            once = false;
                        }

                        Log.v("net" , "No internet access.. check internet connection!");
                    } else {
                        once = true;
                        getMeterData2();
                    }
                }
            }
        }).start();

    }
    
    public void init_views() {
        tv_month_kwh_unit = root.findViewById(R.id.textView_month_kwh_unit);
        tv_month_kwh = root.findViewById(R.id.textView_month_kwh);
        tv_kwh   = root.findViewById(R.id.textView_kwh);
        tv_cost = root.findViewById(R.id.textView_cost);
        tv_date = root.findViewById(R.id.textView_date);
        tv_time = root.findViewById(R.id.textView_time);
        lay_cost = root.findViewById(R.id.layout_cost);
        lay_month_kwh = root.findViewById(R.id.layout_month_kwh);
        lay_kwh = root.findViewById(R.id.layout_kwh);
        lay_power = root.findViewById(R.id.layout_power);
        im_status = root.findViewById(R.id.imageView_status);
        tv_id = root.findViewById(R.id.textView_id);
        tv_power   = root.findViewById(R.id.textView_power);
        tv_power_unit   = root.findViewById(R.id.textView_power_unit);
    }

    public void NO_NET() {
        try {
            ((MainActivity2) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary_NONET)));
        } catch (Exception E) {

        }

        lay_kwh.setBackgroundColor(Color.GRAY);
        lay_month_kwh.setBackgroundColor(Color.GRAY);
        lay_cost.setBackgroundColor(Color.GRAY);
        lay_power.setBackgroundColor(Color.GRAY);
    }

    public void NoDataConfig() {

        tv_kwh.setText("0");
        tv_month_kwh.setText("0");
        tv_cost.setText("0");
        tv_power.setText("0");

        currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        tv_date.setText(currentDate);
        tv_time.setText(TIME(currentTime));

        lay_cost.setBackgroundColor(Color.GRAY);
        lay_month_kwh.setBackgroundColor(Color.GRAY);
        im_status.setImageResource(R.drawable.off);
        lay_kwh.setBackgroundColor(Color.GRAY);
        lay_power.setBackgroundColor(Color.GRAY);
    }

    public void DataConfig() {

        tv_kwh.setText("0");
        tv_month_kwh.setText("0");
        tv_cost.setText("0");
        tv_power.setText("0");

        currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        tv_date.setText(currentDate);
        tv_time.setText(TIME(currentTime));

        lay_cost.setBackgroundColor(Color.GRAY);
        lay_month_kwh.setBackgroundColor(Color.GRAY);
        im_status.setImageResource(R.drawable.off);
        lay_kwh.setBackgroundColor(Color.GRAY);
        lay_power.setBackgroundColor(Color.GRAY);
    }

    public String TIME(String T) {
        String newTime = "";
        String AMPM = "";
        int H = Integer.parseInt(T.split(":")[0]);
        if (H>=24) H -= 24;

        if (H>12) {
            H -= 12;
            if (H==12 || H==0) AMPM= " AM";
            else AMPM = " PM";
        } else {
            if (H==12) AMPM= " PM";
            else if (H==0) {
                H = 12;
                AMPM = " AM";
            }
            else AMPM = " AM";
        }
        newTime = H + ":" + T.split(":")[1] + ":" + T.split(":")[2] + AMPM;

        return newTime;
    }

    public static String nFormat(double d) {
        NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
        nf.setMaximumFractionDigits(3);
        String st= nf.format(d);
        return st;
    }

    public void MeterData() {

        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.CEILING);

        tv_id.setText(String.format(Locale.ENGLISH , local_meterid));
        tv_kwh.setText(String.format(Locale.ENGLISH , local_meter_reading));
        tv_month_kwh.setText(local_month_reading);
        tv_cost.setText(nFormat(Double.parseDouble(local_cost)));

        tv_power.setText(String.format(Locale.ENGLISH , local_consumption));
        tv_power.setText(local_consumption);
        tv_date.setText(String.format(Locale.ENGLISH , local_date ));
        tv_time.setText(String.format(Locale.ENGLISH, local_time));

        lay_cost.setBackgroundColor(0xFF009F91);
        lay_month_kwh.setBackgroundColor(0xFF029857);

        if (Double.parseDouble(local_consumption)>1000) {
            tv_power_unit.setText("kW");
            //tv_power.setText("" + String.format(Locale.ENGLISH , String.valueOf(Double.parseDouble(POWER)/1000.0)) );
            tv_power.setText(nFormat(Double.parseDouble(local_consumption)/1000.0));
        }
        else tv_power_unit.setText("W");

        if (Double.parseDouble(local_consumption)==0) lay_power.setBackgroundColor(Color.GRAY);
        else lay_power.setBackgroundColor(0xFF029857);
        if (Integer.parseInt(local_voltage)==0) {

            try {
                ((MainActivity2) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark_RED)));
            } catch(Exception E) {

            }
            im_status.setImageResource(R.drawable.off);
            lay_kwh.setBackgroundColor(Color.GRAY);
            lay_month_kwh.setBackgroundColor(Color.GRAY);
            lay_cost.setBackgroundColor(Color.GRAY);
            lay_power.setBackgroundColor(Color.GRAY);

            if (connected) {
                try {
                    ChocoBar.builder().setView(root.findViewById(R.id.meter_data2_layout))
                            .setText("Meter if OFF.. check Meter!")
                            .setDuration(ChocoBar.LENGTH_LONG)
                            .orange()
                            .show();
                } catch (Exception E) {

                }
                connected = false;
            }
        }
        else {

            try {
                ((MainActivity2) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
            } catch(Exception E) {

            }
            im_status.setImageResource(R.drawable.on);
            lay_kwh.setBackgroundColor(0xFFFF5C50);
            connected = true;
        }
    }

    public void getMeterData2() {

        String url= server + "/medc/getMeterData.php?meterid=" + meter_id;
        //String url= "http://89.147.133.137/medc/getLastMeter.php";

        StringRequest ExampleStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()) {
                    try {
                        Log.v("data", response);
                        JSONObject jo = new JSONObject(response);
                        String data = jo.getString("data");
                        String meter_datetime = jo.getString("meter_datetime");
                        String real_datetime = jo.getString("datetime");
                        String meter_id = jo.getString("meter_id");
                        Log.v("data", "data : " + data);
                        Log.v("data", "meter datetime : " + meter_datetime);
                        Log.v("data", "real datetime : " + real_datetime);

                        String last_KWH = jo.getString("kwh");
                        String start_KWH = jo.getString("start_kwh");
                        String cost      = jo.getString("cost");

                        String POWER = data.split("@")[1];
                        String VOLTAGE = data.split("@")[2];

                        double month_kwh = Double.parseDouble(last_KWH) - Double.parseDouble(start_KWH);

                        DecimalFormat df = new DecimalFormat("#.###");
                        df.setRoundingMode(RoundingMode.CEILING);
                        System.out.println(df.format(Double.parseDouble(cost)));

                        // local
                        local_meterid = meter_id;
                        local_date    = real_datetime.split(" ")[0];
                        local_time    = TIME(real_datetime.split(" ")[1]);
                        local_consumption = POWER;
                        local_cost = cost;
                        local_meter_reading = last_KWH;
                        local_month_reading = nFormat(month_kwh);
                        local_voltage = VOLTAGE;


                        //tv_id.setText(meter_id);
                        tv_id.setText(String.format(Locale.ENGLISH , meter_id));
                        //tv_kwh.setText(last_KWH);
                        tv_kwh.setText(String.format(Locale.ENGLISH , last_KWH));
                        //tv_month_kwh.setText(df.format(month_kwh));
                        tv_month_kwh.setText(nFormat(month_kwh));
                        //tv_month_kwh.setText(String.format(Locale.ENGLISH , df.format(month_kwh)));
                        //txtScale.setText(String.format(Locale.ENGLISH, "x%d", scaleValue));
                        //tv_cost.setTextLocale(Locale.ENGLISH);
                        //tv_cost.setText(""+ String.format(Locale.ENGLISH , df.format(Double.parseDouble(cost))));
                        tv_cost.setText(nFormat(Double.parseDouble(cost)));
                        //tv_cost.setText(df.format(Double.parseDouble(cost)));

                        tv_power.setText(String.format(Locale.ENGLISH , POWER));
                        tv_power.setText(POWER);
                        //v_date.setText(String.format(Locale.ENGLISH , meter_datetime.split(" ")[0]) );
                        //tv_time.setText(String.format(Locale.ENGLISH, TIME(meter_datetime.split(" ")[1])));
                        tv_date.setText(String.format(Locale.ENGLISH , real_datetime.split(" ")[0]) );
                        tv_time.setText(String.format(Locale.ENGLISH, TIME(real_datetime.split(" ")[1])));

                        lay_cost.setBackgroundColor(0xFF009F91);
                        lay_month_kwh.setBackgroundColor(0xFF029857);

                        if (Double.parseDouble(POWER)>1000) {
                            tv_power_unit.setText("kW");
                            //tv_power.setText("" + String.format(Locale.ENGLISH , String.valueOf(Double.parseDouble(POWER)/1000.0)) );
                            tv_power.setText(nFormat(Double.parseDouble(POWER)/1000.0));
                        }
                        else tv_power_unit.setText("W");

                        if (Double.parseDouble(POWER)==0) lay_power.setBackgroundColor(Color.GRAY);
                        else lay_power.setBackgroundColor(0xFF029857);
                        if (Integer.parseInt(VOLTAGE)==0) {

                            try {
                                ((MainActivity2) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark_RED)));
                            } catch(Exception E) {
                                
                            }
                            im_status.setImageResource(R.drawable.off);
                            lay_kwh.setBackgroundColor(Color.GRAY);
                            lay_month_kwh.setBackgroundColor(Color.GRAY);
                            lay_cost.setBackgroundColor(Color.GRAY);
                            lay_power.setBackgroundColor(Color.GRAY);

                            if (connected) {
                                try {
                                    ChocoBar.builder().setView(root.findViewById(R.id.meter_data2_layout))
                                            .setText("Meter if OFF.. check Meter!")
                                            .setDuration(ChocoBar.LENGTH_LONG)
                                            .orange()
                                            .show();
                                } catch (Exception E) {

                                }
                                connected = false;
                            }
                        }
                        else {

                            try {
                                ((MainActivity2) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
                            } catch(Exception E) {

                            }
                            im_status.setImageResource(R.drawable.on);
                            lay_kwh.setBackgroundColor(0xFFFF5C50);
                            connected = true;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.v("error" , error.getMessage());
            }
        });
        queue.add(ExampleStringRequest);

    }

    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }
    }


    /////////////////////

    // Fetch the stored data in onResume()
    // Because this is what will be called
    // when the app opens again
    @Override
    public void onResume()
    {
        super.onResume();

        try {
            if (!isInternetAvailable()) {
                @SuppressLint("WrongConstant") SharedPreferences sh = getContext().getSharedPreferences(local_memory, 0x8000);

                local_meterid = sh.getString("local_meter_" + meter_id, "");
                local_date = sh.getString("local_date_" + meter_id, "");
                local_time = sh.getString("local_time_" + meter_id, "");
                local_cost = sh.getString("local_cost_" + meter_id, "");
                local_meter_reading = sh.getString("local_meter_reading_"+meter_id, "");
                local_month_reading = sh.getString("local_month_reading_"+meter_id, "");
                local_consumption = sh.getString("local_consumption_"+meter_id, "");

                if (local_meter_reading.length() > 1) {
                    MeterData();
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
    public void onPause()
    {
        super.onPause();

        // Creating a shared pref object
        // with a file name "MySharedPref" in private mode
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(local_memory, MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        //String meterid = "";
        //myEdit.putString("meterid", meterid);
        myEdit.putString("local_meter_" + meter_id , meter_id);
        myEdit.putString("local_meter_reading_" +meter_id , local_meter_reading);
        myEdit.putString("local_date_"+meter_id , local_date);
        myEdit.putString("local_time_"+meter_id , local_time);
        myEdit.putString("local_cost_"+meter_id , local_cost);
        myEdit.putString("local_month_reading_"+meter_id , local_month_reading);
        myEdit.putString("local_consumption_"+meter_id , local_consumption);
        myEdit.commit();
    }

}