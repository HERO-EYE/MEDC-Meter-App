package com.example.medcmeter;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.pd.chocobar.ChocoBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TargetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TargetFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "meterid";

    // TODO: Rename and change types of parameters
    private String mParam1;

    View view_target;
    TextView tv_percent;
    TextView tv_expected;
    EditText et_target;
    Button btn_update;
    RequestQueue queue;
    LinearLayout lay_target;
    String meter_id;
    int cost = 0;
    String currentDate;
    //String server = "http://dev-estrlab.atwebpages.com";
    //String server = "http://dev-estrlab.000webhostapp.com";
    String server = "http://89.147.133.137";
    View root;
    double month_days = 30.0;
    String local_target;
    double local_expected;
    int local_percent;
    String local_memory = "medc";

    public TargetFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TargetFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TargetFragment newInstance(String param1, String param2) {
        TargetFragment fragment = new TargetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_target, container, false);
        meter_id = ((MainActivity2) getActivity()).getIntent().getStringExtra(ARG_PARAM1);
        MAIN();
        return root;
    }


    public void MAIN() {
        ((MainActivity2) getActivity()).setActionBarTitle("");
        init_views();

        queue = Volley.newRequestQueue(root.getContext());

        currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        getMonthData();
    }

    public void init_views() {
        view_target = root.findViewById(R.id.view_target);
        lay_target = root.findViewById(R.id.layout_target_data);
        tv_percent = root.findViewById(R.id.textView_percent);
        tv_expected = root.findViewById(R.id.textView_expected);
        et_target   = root.findViewById(R.id.editTextNumber_target_bill);
        btn_update = root.findViewById(R.id.button_target_update);

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mytarget = et_target.getText().toString();
                showAllert(mytarget);
            }
        });
    }

    public static String nFormat(double d) {
        NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
        nf.setMaximumFractionDigits(3);
        String st= nf.format(d);
        return st;
    }

    public void TargetData() {

        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.CEILING);

        et_target.setText(String.format(Locale.ENGLISH , local_target));
        tv_expected.setText(nFormat(local_expected));

        tv_percent.setText("Good");

        tv_expected.setTextColor(getResources().getColor(R.color.green_dark));
        tv_percent.setTextColor(getResources().getColor(R.color.green_dark));
        lay_target.setBackgroundColor(getResources().getColor(R.color.medc_green_2));
        view_target.setBackgroundColor(getResources().getColor(R.color.green_dark));

        if (local_percent > 150) {
            tv_expected.setTextColor(getResources().getColor(R.color.red_dark));
            tv_percent.setTextColor(getResources().getColor(R.color.red_dark));
            tv_percent.setText("Over");
            lay_target.setBackgroundColor(getResources().getColor(R.color.red));
            view_target.setBackgroundColor(getResources().getColor(R.color.red_dark));
        } else if (local_percent > 100) {
            tv_expected.setTextColor(getResources().getColor(R.color.yellow_dark));
            tv_percent.setTextColor(getResources().getColor(R.color.yellow_dark));
            tv_percent.setText("Warning");
            lay_target.setBackgroundColor(getResources().getColor(R.color.yellow));
            view_target.setBackgroundColor(getResources().getColor(R.color.yellow_dark));
        }
    }

    public void getMonthData() {

        String url= server + "/medc/getMonthReading.php?meterid=" + meter_id;

        StringRequest ExampleStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()) {
                    try {
                        Log.v("data", response);
                        JSONObject jo = new JSONObject(response);
                        String target = jo.getString("bill_target");
                        String month = jo.getString("month");
                        String meter_id = jo.getString("meter_id");

                        String last_KWH = jo.getString("end_kwh");
                        String start_KWH = jo.getString("start_kwh");
                        String cost      = jo.getString("cost");
                        double expect = jo.getDouble("bill_expected");
                        int percent   = jo.getInt("percent");

                        // local
                        local_target = target;
                        local_percent = percent;
                        local_expected = expect;


                        Date currentDateTime = Calendar.getInstance().getTime();
                        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

                        month_days = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);

                        DecimalFormat df = new DecimalFormat("#.###");
                        df.setRoundingMode(RoundingMode.CEILING);
                        System.out.println(df.format(Double.parseDouble(cost)));

                        et_target.setText(String.format(Locale.ENGLISH , target));

                        double daily_target = Double.parseDouble(target)/month_days;
                        double daily_real   = Double.parseDouble(cost)/currentDateTime.getDate();
                        //double expected = daily_real * month_days;

                        tv_expected.setText(nFormat(expect));
                        //percent = (int) ((daily_real/daily_target)*100.0);
                        Log.v("value" , String.valueOf(daily_target) + ":" + String.valueOf(daily_real) + ":" + String.valueOf(month_days) + ":" + String.valueOf(currentDateTime.getDate()));
                        Log.v("percent" , String.valueOf(percent));
                        //tv_percent.setText( String.format(Locale.ENGLISH , String.valueOf(percent)));
                        //tv_percent.setText(nFormat(percent));
                        tv_percent.setText("Good");

                        tv_expected.setTextColor(getResources().getColor(R.color.green_dark));
                        tv_percent.setTextColor(getResources().getColor(R.color.green_dark));
                        lay_target.setBackgroundColor(getResources().getColor(R.color.medc_green_2));
                        view_target.setBackgroundColor(getResources().getColor(R.color.green_dark));

                        /*
                        if (percent > 100) {
                            tv_percent.setTextColor(getResources().getColor(R.color.red_dark));
                            lay_target.setBackgroundColor(getResources().getColor(R.color.red));
                            view_target.setBackgroundColor(getResources().getColor(R.color.red_dark));
                        } else {
                            tv_percent.setTextColor(0xff365144);
                            lay_target.setBackgroundColor(getResources().getColor(R.color.medc_green_2));
                            view_target.setBackgroundColor(0xff005330);
                        }
                        */

                        /*
                        if (expected > Double.parseDouble(target)) {
                            tv_expected.setTextColor(getResources().getColor(R.color.red_dark));
                            lay_target.setBackgroundColor(getResources().getColor(R.color.red));
                            view_target.setBackgroundColor(getResources().getColor(R.color.red_dark));
                        } else {
                            tv_expected.setTextColor(0xff365144);
                            lay_target.setBackgroundColor(getResources().getColor(R.color.medc_green_2));
                            view_target.setBackgroundColor(0xff005330);
                        }
                        */

                        if (percent > 150) {
                            tv_expected.setTextColor(getResources().getColor(R.color.red_dark));
                            tv_percent.setTextColor(getResources().getColor(R.color.red_dark));
                            tv_percent.setText("Over");
                            lay_target.setBackgroundColor(getResources().getColor(R.color.red));
                            view_target.setBackgroundColor(getResources().getColor(R.color.red_dark));
                        } else if (percent > 100) {
                            tv_expected.setTextColor(getResources().getColor(R.color.yellow_dark));
                            tv_percent.setTextColor(getResources().getColor(R.color.yellow_dark));
                            tv_percent.setText("Warning");
                            lay_target.setBackgroundColor(getResources().getColor(R.color.yellow));
                            view_target.setBackgroundColor(getResources().getColor(R.color.yellow_dark));
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

    public void updateTarget(String TARGET) {

        // http://89.147.133.137/medc/setMonthTarget.php?meterid=031900101243&month=2020-10&target=25
        String url= server + "/medc/setMonthTarget.php?meterid=" + meter_id + "&target=" + TARGET;

        StringRequest ExampleStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()) {
                    Log.v("data", response);
                    if (response.equals("1")) {
                        Toast.makeText(root.getContext(), "Updated Successfully", Toast.LENGTH_SHORT).show();
                        getMonthData();
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
            //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }
    }

    public final boolean containsDigit(String s) {
        boolean containsDigit = false;

        if (s != null && !s.isEmpty()) {
            for (char c : s.toCharArray()) {
                if (containsDigit = Character.isDigit(c)) {
                    break;
                }
            }
        }
        return containsDigit;
    }

    public void showAllert(String mytarget) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(root.getContext());
        alertDialogBuilder.setMessage("Are you sure to update bill target to " + mytarget + " ?");
        alertDialogBuilder.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (!mytarget.isEmpty() && containsDigit(mytarget) ) {
                            updateTarget(mytarget);
                        }
                    }
                });

        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        try {
            if (!isInternetAvailable()) {
                @SuppressLint("WrongConstant") SharedPreferences sh = getContext().getSharedPreferences(local_memory, 0x8000);

                local_target = sh.getString("local_target_" + meter_id, "");
                local_percent = sh.getInt("local_percent_" + meter_id, 0);
                local_expected = Double.parseDouble(sh.getString("local_expect_" + meter_id, ""));

                if (local_target.length() > 0) {
                    TargetData();
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
        myEdit.putString("local_target_"+meter_id , local_target);
        myEdit.putInt("local_percent_"+meter_id , local_percent);
        myEdit.putString("local_expect_"+meter_id , String.valueOf(local_expected));
        myEdit.commit();
    }

}