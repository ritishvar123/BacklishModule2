package com.example.hp.backlishmodule2;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Button btn_from, btn_to;
    ImageButton btn_img_change, btn_img_confirm;
    TextView tv_from, tv_to;
    Spinner spinner;
    ProgressDialog progressDialog;
    DatePickerDialog.OnDateSetListener dateSetListener1, dateSetListener2;
    Calendar calendar = null;
    int year, month, day, y, m, d, yfrom, mfrom, dfrom, yto, mto, dto;
    String minDate;
    long millis;
    String select_type[] = {"Select Type", "CSV", "Pdf", "Txt"};
    String record;
    Boolean isSuccess1 = true;
    DownloadManager downloadManager;
    int STORAGE_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestStorageapermission();
        getSupportActionBar().setTitle("Export your PDF");
        btn_from = (Button)findViewById(R.id.button);
        btn_to = (Button)findViewById(R.id.button2);
        btn_img_change = (ImageButton)findViewById(R.id.imageButton);
        btn_img_confirm = (ImageButton)findViewById(R.id.imageButton2);
        tv_from = (TextView)findViewById(R.id.textView);
        tv_to = (TextView)findViewById(R.id.textView2);
        spinner = (Spinner)findViewById(R.id.spinner);
        progressDialog = new ProgressDialog(this);
        if (!isOnline())    Toast.makeText(MainActivity.this, "Check your internet connection !!", Toast.LENGTH_LONG).show();
        else                getMinDate_in_fromDatePicker();

        long date1 = System.currentTimeMillis();
        long date2 = System.currentTimeMillis();
        long date3 = System.currentTimeMillis();
        y = Integer.parseInt(new SimpleDateFormat("yyyy").format(date1));
        m = Integer.parseInt(new SimpleDateFormat("MM").format(date2));
        d = Integer.parseInt(new SimpleDateFormat("dd").format(date3));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            calendar = Calendar.getInstance(Locale.ENGLISH);
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        }
        btn_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth, dateSetListener1, year+y, month+m-1, day+d);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis()/*-86400421*/);
                datePickerDialog.getDatePicker().setMinDate(millis); // method is made at the end.
                // convert miliseconds to date use this:--  https://codechi.com/dev-tools/date-to-millisecond-calculators/
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });
        dateSetListener1 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month++;
                tv_from.setText(day+"-"+month+"-"+year);
                yfrom = year;       mfrom = month;  dfrom = day;
            }
        };
        btn_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String frm = tv_from.getText().toString();
                if (frm.equals("Select date")){
                    Toast.makeText(MainActivity.this, "Please select from date", Toast.LENGTH_LONG).show();
                } else {
                    String temp = yfrom+"-"+mfrom+"-"+dfrom;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar c = Calendar.getInstance();
                    try {c.setTime(sdf.parse(temp));} catch (ParseException e) {e.printStackTrace();}
                    DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                            android.R.style.Theme_Holo_Light_Dialog_MinWidth, dateSetListener2, year + y, month + m - 1, day + d);
                    datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                    datePickerDialog.getDatePicker().setMinDate(c.getTime().getTime()/*Long.parseLong("1547231460000")*/);
                    // convert miliseconds to date use this:--  https://codechi.com/dev-tools/date-to-millisecond-calculators/
                    datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    datePickerDialog.show();
                }
            }
        });
        dateSetListener2 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month++;
                tv_to.setText(day+"-"+month+"-"+year);
                yto = year;        mto = month;           dto = day;
            }
        };
        btn_img_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String from = tv_from.getText().toString();
                String to = tv_to.getText().toString();
                tv_from.setText(to);
                tv_to.setText(from);
                int temp;
                temp = yto;    yto = yfrom;     yfrom = temp;
                temp = mto;    mto = mfrom;     mfrom = temp;
                temp = dto;    dto = dfrom;     dfrom = temp;

            }
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, select_type);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                record = select_type[i];
                String from = tv_from.getText().toString();
                String to = tv_to.getText().toString();
                if (!isOnline()){
                    Toast.makeText(MainActivity.this, "Check your internet connection !!", Toast.LENGTH_LONG).show();
                } else if (from.equals("Select date") || to.equals("Select date")) {
                    Toast.makeText(MainActivity.this, "Select date first", Toast.LENGTH_LONG).show();
                } else {
                    switch (i) {
                        case 1:
                            record = select_type[i];
                            break;
                        case 2:
                            record = select_type[i];
                            break;
                        case 3:
                            record = select_type[i];
                            break;
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                record = "";
            }
        });
    }

    public void download_files_byDate(final String from, final String to) {
        /*progressDialog.setMessage("Downloading files...");
        progressDialog.show();*/
        Toast.makeText(MainActivity.this, "Download will start in 5 seconds...", Toast.LENGTH_LONG).show();
        String url = "https://boxinall.in/BIAOfficeAttendence/export.php";
        StringRequest stringRequest = new StringRequest(1, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Slow internet", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("from", from);
                map.put("to", to);
                map.put("record", record);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (record.equals(select_type[1])) {
                    downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    Uri uri = Uri.parse("https://boxinall.in/BIAOfficeAttendence/attendence_details.csv");
                    DownloadManager.Request request = new DownloadManager.Request(uri);
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir("/bia", "attendence_details.csv");
                    Long reference = downloadManager.enqueue(request);
                    Toast.makeText(MainActivity.this, "Download Completed", Toast.LENGTH_LONG).show();
                }
            }
        }, 5000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                btn_img_confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!isOnline()) {
                            Toast.makeText(MainActivity.this, "Check your internet connection !!", Toast.LENGTH_LONG).show();
                        } else {
                            String from = tv_from.getText().toString();
                            String to = tv_to.getText().toString();
                            Boolean check = true;
                            if (from.equals("Select date") || to.equals("Select date")) {
                                Toast.makeText(MainActivity.this, "Please select dates", Toast.LENGTH_LONG).show();
                            } else if (record.equals("") || record.equals("Select Type")) {
                                Toast.makeText(MainActivity.this, "Please select export type", Toast.LENGTH_LONG).show();
                            } else {
                                if (yto >= yfrom) {
                                    if (mto >= mfrom) {
                                        if (dto >= dfrom) {
                                            String f = yfrom + "-" + mfrom + "-" + dfrom;
                                            String t = yto + "-" + mto + "-" + dto;
                                            download_files_byDate(f, t);
                                        } else check = false;
                                    } else check = false;
                                } else check = false;
                            }
                            if (!check) {
                                Toast.makeText(MainActivity.this, "Date should be Interchanged",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            } else {
                Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void requestStorageapermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}
                    , STORAGE_PERMISSION_CODE);
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}
                    , STORAGE_PERMISSION_CODE);
        }
    }

    public void getMinDate_in_fromDatePicker(){
        String url = "https://boxinall.in/BIAOfficeAttendence/getMinDate_in_fromDatePicker.php";
        StringRequest stringRequest = new StringRequest(1, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    minDate = jsonArray.getJSONObject(0).getString("minDate");
                    isSuccess1 = true;
                } catch (JSONException e) {e.printStackTrace();}
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                isSuccess1 = false;
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isSuccess1) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = null;
                    try {
                        date = sdf.parse(minDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    millis = date.getTime();
                    //Toast.makeText(MainActivity5_customAttendenceRecord.this, minDate+"\n"+date+"\n"+millis, Toast.LENGTH_LONG).show();
                } else Toast.makeText(MainActivity.this, "Check your internet connection !!", Toast.LENGTH_LONG).show();
            }
        }, 3700);
    }

    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable())
            return false;
        return true;
    }

}
