package com.example.medcmeter;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.axismarkers.Line;
import com.anychart.core.axismarkers.Text;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.highsoft.highcharts.common.HIColor;
import com.highsoft.highcharts.common.hichartsclasses.HICSSObject;
import com.highsoft.highcharts.common.hichartsclasses.HIChart;
import com.highsoft.highcharts.common.hichartsclasses.HIColumn;
import com.highsoft.highcharts.common.hichartsclasses.HIDrilldown;
import com.highsoft.highcharts.common.hichartsclasses.HILabel;
import com.highsoft.highcharts.common.hichartsclasses.HILabels;
import com.highsoft.highcharts.common.hichartsclasses.HILegend;
import com.highsoft.highcharts.common.hichartsclasses.HINavigation;
import com.highsoft.highcharts.common.hichartsclasses.HIOptions;
import com.highsoft.highcharts.common.hichartsclasses.HIPlotBands;
import com.highsoft.highcharts.common.hichartsclasses.HISpline;
import com.highsoft.highcharts.common.hichartsclasses.HISubtitle;
import com.highsoft.highcharts.common.hichartsclasses.HITitle;
import com.highsoft.highcharts.common.hichartsclasses.HITooltip;
import com.highsoft.highcharts.common.hichartsclasses.HIXAxis;
import com.highsoft.highcharts.common.hichartsclasses.HIYAxis;
import com.highsoft.highcharts.core.HIChartView;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "meterid";

    // TODO: Rename and change types of parameters
    private String mParam1;
    RequestQueue queue;
    String meter_id;
    //String server = "http://dev-estrlab.atwebpages.com";
    //String server = "http://dev-estrlab.000webhostapp.com";
    String server = "http://89.147.133.137";
    View root;
    String local_memory = "medc";
    String local_month[];
    double local_cost[];
    int local_chart_count;

    public DashboardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment DashboardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DashboardFragment newInstance(String param1) {
        DashboardFragment fragment = new DashboardFragment();
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

        root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        ((MainActivity2) getActivity()).setActionBarTitle("");
        queue = Volley.newRequestQueue(root.getContext());
        meter_id  = ((MainActivity2) getActivity()).getIntent().getStringExtra(ARG_PARAM1);

        getCostData();
        return root;
    }


    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }
    }


    public void getCostData() {

        String url= server + "/medc/getCostMonthly.php?meterid=" + meter_id;
        Log.v("url", url);
        StringRequest ExampleStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()) {
                    try {
                        Log.v("data", response);
                        JSONArray ja = new JSONArray(response);
                        Chart_new2(ja);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(ExampleStringRequest);

    }

    void Chart_new2(JSONArray ja) throws JSONException {
        AnyChartView anyChartView = root.findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(root.findViewById(R.id.progress_bar));

        Cartesian cartesian = AnyChart.column();

        List<DataEntry> data = new ArrayList<>();
        local_chart_count = ja.length();
        local_month = new String[local_chart_count];
        local_cost = new double[local_chart_count];
        for (int i=0 ; i<ja.length(); i++) {
            String month = ja.getJSONObject(i).getString("month").split("-")[1] + "-" + ja.getJSONObject(i).getString("month").split("-")[0];
            double cost = ja.getJSONObject(i).getDouble("cost");
            data.add(new ValueDataEntry(month, cost));
            local_month[i] = month;
            local_cost[i]  = cost;
        }

        Column column = cartesian.column(data);

        column.tooltip()
                .titleFormat("{%X}")
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(5d)
                .format("{%Value} OMR");

        cartesian.animation(true);
        cartesian.title("Bill Monthly");

        cartesian.yScale().minimum(0d);

        cartesian.yAxis(0).labels().format("{%Value}");

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);

        cartesian.xAxis(0).title("Month");
        cartesian.yAxis(0).title("Bill (OMR)");

        ///////////
        /*
        Line lineMarkerMin = cartesian.lineMarker(0);
        lineMarkerMin.value(20);


        Text textMarkerMin = cartesian.textMarker(0);
        textMarkerMin.value(20);
        textMarkerMin.text("Target");
        textMarkerMin.anchor("left-bottom");
        textMarkerMin.align("left");
        */
        ////////////////

        anyChartView.setChart(cartesian);
    }

    void Chart_new_(String month[], double cost[]) throws JSONException {
        AnyChartView anyChartView = root.findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(root.findViewById(R.id.progress_bar));

        Cartesian cartesian = AnyChart.column();

        List<DataEntry> data = new ArrayList<>();
        for (int i=0 ; i<local_chart_count; i++) {
            data.add(new ValueDataEntry(month[i], cost[i]));
        }

        Column column = cartesian.column(data);

        column.tooltip()
                .titleFormat("{%X}")
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(5d)
                .format("{%Value} OMR");

        cartesian.animation(true);
        cartesian.title("Bill Monthly");

        cartesian.yScale().minimum(0d);

        cartesian.yAxis(0).labels().format("{%Value}");

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);

        cartesian.xAxis(0).title("Month");
        cartesian.yAxis(0).title("Bill (OMR)");

        ///////////
        /*
        Line lineMarkerMin = cartesian.lineMarker(0);
        lineMarkerMin.value(20);


        Text textMarkerMin = cartesian.textMarker(0);
        textMarkerMin.value(20);
        textMarkerMin.text("Target");
        textMarkerMin.anchor("left-bottom");
        textMarkerMin.align("left");
        */
        ////////////////

        anyChartView.setChart(cartesian);
    }

    void Chart_new() {
        AnyChartView anyChartView = root.findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(root.findViewById(R.id.progress_bar));

        Cartesian cartesian = AnyChart.column();

        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry("Rouge", 80540));
        data.add(new ValueDataEntry("Foundation", 94190));
        data.add(new ValueDataEntry("Mascara", 102610));
        data.add(new ValueDataEntry("Lip gloss", 110430));
        data.add(new ValueDataEntry("Lipstick", 128000));
        data.add(new ValueDataEntry("Nail polish", 143760));
        data.add(new ValueDataEntry("Eyebrow pencil", 170670));
        data.add(new ValueDataEntry("Eyeliner", 213210));
        data.add(new ValueDataEntry("Eyeshadows", 249980));

        Column column = cartesian.column(data);

        column.tooltip()
                .titleFormat("{%X}")
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(5d)
                .format("${%Value}{groupsSeparator: }");

        cartesian.animation(true);
        cartesian.title("Top 10 Cosmetic Products by Revenue");

        cartesian.yScale().minimum(0d);

        cartesian.yAxis(0).labels().format("${%Value}{groupsSeparator: }");

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);

        cartesian.xAxis(0).title("Product");
        cartesian.yAxis(0).title("Revenue");

        anyChartView.setChart(cartesian);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        try {
            if (!isInternetAvailable()) {
                @SuppressLint("WrongConstant") SharedPreferences sh = getContext().getSharedPreferences(local_memory, 0x8000);

                local_chart_count = sh.getInt("local_chart_count" , 0);
                for (int i=0 ;i<local_chart_count; i++) {
                    local_month[i] = sh.getString("local_chart_month_" + meter_id + local_month[i], "");
                    local_cost[i] = Double.parseDouble(sh.getString("local_chart_cost_" + meter_id + local_cost[i], ""));
                }
                if (local_chart_count > 0) {
                    Chart_new_(local_month, local_cost);
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
        myEdit.putInt("local_chart_count_" +meter_id , local_chart_count);
        for (int i=0 ;i<local_chart_count; i++) {
            myEdit.putString("local_chart_month_" + meter_id + local_month[i] , local_month[i]);
            myEdit.putString("local_chart_cost_" + meter_id + local_cost[i] , String.valueOf(local_cost[i]));
        }
        myEdit.commit();
    }

}