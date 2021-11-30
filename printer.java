package com.flexural.developers.prixapp.activity;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.flexural.developers.prixapp.MainActivity;
import com.flexural.developers.prixapp.R;
import com.flexural.developers.prixapp.adapters.AirtimeAdapter;
import com.flexural.developers.prixapp.model.Airtime;
import com.flexural.developers.prixapp.model.AirtimeId;
import com.flexural.developers.prixapp.utils.BitMapUtil;
import com.flexural.developers.prixapp.utils.BluetoothUtil;
import com.flexural.developers.prixapp.utils.ButtonDelayUtils;
import com.flexural.developers.prixapp.utils.ESCUtil;
import com.flexural.developers.prixapp.utils.HandlerUtils;
import com.flexural.developers.prixapp.utils.ThreadPoolManager;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.flexural.developers.prixapp.activity.LoginScreen.BASE_URL;

public class PrinterActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private String prod_id, pin_no, serial_no, selectedAirtime, acc_no;
    private int voucherAmount = 1;

    private RecyclerView mAirtimeRecycler;
    private List<Airtime> airtimeList;
    private List<AirtimeId> airtimeIdList;
    private AirtimeAdapter airtimeAdapter;
    private TextView mAirtimeAmount, mVoucherText, mText;
    private SeekBar mVoucherAmount;

    private String URL =  BASE_URL + "airtimeList.php";
    private String URL_QNTY =  BASE_URL + "airtimeQuantity.php";
    private String NEW_SALES_URL = "https://prix.co.za/admin/products/sellproduct";
    // comment : its sellproduct - its function. Not sellproduct.php

    private String URL_WALLET = BASE_URL + "merchantWallet.php";

    private String currentDate, currentTime, day, currentAcc;
    private Calendar calendar;
    private Airtime airtime;
    private AirtimeId airtimeId;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mBluetoothPrinterDevice = null;
    private BluetoothSocket socket = null;

    private Button btn_printer_test;
    private Button btn_load_bluetoothPrinter,btn_printer_init;
    /*定义打印机状态*/
    private final int PRINTER_NORMAL = 0;
    private final int PRINTER_ERROR_UNKNOWN = 5;
    /*打印机当前状态*/
    private int printerStatus = PRINTER_ERROR_UNKNOWN;

    /*定义状态广播*/
    private final String  PRINTER_NORMAL_ACTION = "com.iposprinter.iposprinterservice.NORMAL_ACTION";
    private final String  PRINTER_PAPERLESS_ACTION = "com.iposprinter.iposprinterservice.PAPERLESS_ACTION";
    private final String  PRINTER_PAPEREXISTS_ACTION = "com.iposprinter.iposprinterservice.PAPEREXISTS_ACTION";
    private final String  PRINTER_THP_HIGHTEMP_ACTION = "com.iposprinter.iposprinterservice.THP_HIGHTEMP_ACTION";
    private final String  PRINTER_THP_NORMALTEMP_ACTION = "com.iposprinter.iposprinterservice.THP_NORMALTEMP_ACTION";
    private final String  PRINTER_MOTOR_HIGHTEMP_ACTION = "com.iposprinter.iposprinterservice.MOTOR_HIGHTEMP_ACTION";
    private final String  PRINTER_BUSY_ACTION = "com.iposprinter.iposprinterservice.BUSY_ACTION";
    private final String  PRINTER_CURRENT_TASK_PRINT_COMPLETE_ACTION = "com.iposprinter.iposprinterservice.CURRENT_TASK_PRINT_COMPLETE_ACTION";

    /*定义消息*/
    private final int MSG_TEST                               = 1;
    private final int MSG_IS_NORMAL                          = 2;
    private final int MSG_IS_BUSY                            = 3;
    private final int MSG_PAPER_LESS                         = 4;
    private final int MSG_PAPER_EXISTS                       = 5;
    private final int MSG_THP_HIGH_TEMP                      = 6;
    private final int MSG_THP_TEMP_NORMAL                    = 7;
    private final int MSG_MOTOR_HIGH_TEMP                    = 8;
    private final int MSG_MOTOR_HIGH_TEMP_INIT_PRINTER       = 9;
    private final int MSG_CURRENT_TASK_PRINT_COMPLETE     = 10;

    /*循环打印类型*/
    private final int  MULTI_THREAD_LOOP_PRINT  = 1;
    private final int  DEFAULT_LOOP_PRINT       = 0;

    //循环打印标志位
    private       int  loopPrintFlag            = DEFAULT_LOOP_PRINT;

    private boolean isBluetoothOpen = false;
    private Random random = new Random();
    private HandlerUtils.PrinterHandler mPrinterHandler;

    /**
     * 消息处理
     */
    private HandlerUtils.IHandlerIntent mHandlerIntent = new HandlerUtils.IHandlerIntent() {
        @Override
        public void handlerIntent(Message msg)
        {
            switch (msg.what)
            {
                case MSG_TEST:
                    break;
                case MSG_IS_NORMAL:
                    if (getPrinterStatus() == PRINTER_NORMAL) {
                        print_loop(loopPrintFlag);
                    }
                    break;
                case MSG_IS_BUSY:
                    Toast.makeText(PrinterActivity.this, R.string.printer_is_working, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_PAPER_LESS:
                    loopPrintFlag = DEFAULT_LOOP_PRINT;
                    Toast.makeText(PrinterActivity.this, R.string.out_of_paper, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_PAPER_EXISTS:
                    Toast.makeText(PrinterActivity.this, R.string.exists_paper, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_THP_HIGH_TEMP:
                    Toast.makeText(PrinterActivity.this, R.string.printer_high_temp_alarm, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_MOTOR_HIGH_TEMP:
                    loopPrintFlag = DEFAULT_LOOP_PRINT;
                    Toast.makeText(PrinterActivity.this, R.string.motor_high_temp_alarm, Toast.LENGTH_SHORT).show();
                    mPrinterHandler.sendEmptyMessageDelayed(MSG_MOTOR_HIGH_TEMP_INIT_PRINTER, 180000);  //马达高温报警，等待3分钟后复位打印机
                    break;
                case MSG_MOTOR_HIGH_TEMP_INIT_PRINTER:
                    loopPrintFlag = DEFAULT_LOOP_PRINT;
                    printerInit();
                    break;
                case MSG_CURRENT_TASK_PRINT_COMPLETE:
                    Toast.makeText(PrinterActivity.this, R.string.printer_current_task_print_complete, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    private BroadcastReceiver IPosPrinterStatusListener = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if(action == null)
            {
                Log.d(TAG,"IPosPrinterStatusListener onReceive action = null");
                return;
            }
            // Log.d(TAG,"IPosPrinterStatusListener action = "+action);
            if(action.equals(PRINTER_NORMAL_ACTION))
            {
                mPrinterHandler.sendEmptyMessageDelayed(MSG_IS_NORMAL,0);
            }
            else if (action.equals(PRINTER_PAPERLESS_ACTION))
            {
                mPrinterHandler.sendEmptyMessageDelayed(MSG_PAPER_LESS,0);
            }
            else if (action.equals(PRINTER_BUSY_ACTION))
            {
                mPrinterHandler.sendEmptyMessageDelayed(MSG_IS_BUSY,0);
            }
            else if (action.equals(PRINTER_PAPEREXISTS_ACTION))
            {
                mPrinterHandler.sendEmptyMessageDelayed(MSG_PAPER_EXISTS,0);
            }
            else if (action.equals(PRINTER_THP_HIGHTEMP_ACTION))
            {
                mPrinterHandler.sendEmptyMessageDelayed(MSG_THP_HIGH_TEMP,0);
            }
            else if (action.equals(PRINTER_THP_NORMALTEMP_ACTION))
            {
                mPrinterHandler.sendEmptyMessageDelayed(MSG_THP_TEMP_NORMAL,0);
            }
            else if (action.equals(PRINTER_MOTOR_HIGHTEMP_ACTION))  //此时当前任务会继续打印，完成当前任务后，请等待2分钟以上时间，继续下一个打印任务
            {
                mPrinterHandler.sendEmptyMessageDelayed(MSG_MOTOR_HIGH_TEMP,0);
            }
            else if(action.equals(PRINTER_CURRENT_TASK_PRINT_COMPLETE_ACTION))
            {
                mPrinterHandler.sendEmptyMessageDelayed(MSG_CURRENT_TASK_PRINT_COMPLETE,0);
            }
            else if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
            {
                int state= intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d("aaa", "STATE_OFF 蓝牙关闭");
                        isBluetoothOpen = false;
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d("aaa", "STATE_TURNING_OFF 蓝牙正在关闭");
                        isBluetoothOpen = false;
                        if(mBluetoothAdapter != null)
                            mBluetoothAdapter = null;
                        if(mBluetoothPrinterDevice != null)
                            mBluetoothPrinterDevice = null;
                        try {
                            if (socket != null && (socket.isConnected())) {
                                socket.close();
                                socket = null;
                            }
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d("aaa", "STATE_ON 蓝牙开启");
                        loopPrintFlag = DEFAULT_LOOP_PRINT;
                        isBluetoothOpen = true;
                        LoadBluetoothPrinter();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        isBluetoothOpen = true;
                        Log.d("aaa", "STATE_TURNING_ON 蓝牙正在开启");
                        break;
                }
            }
            else
            {
                mPrinterHandler.sendEmptyMessageDelayed(MSG_TEST,0);
            }
        }
    };

    private void setButtonEnable(boolean flag){
        btn_load_bluetoothPrinter.setEnabled(flag);
        btn_printer_init.setEnabled(flag);
        btn_printer_test.setEnabled(flag);
    }

    private void innitView() {
        btn_load_bluetoothPrinter = findViewById(R.id.btn_load_bluetoothPrinter);
        btn_printer_init = findViewById(R.id.btn_printer_init);
        btn_printer_test = findViewById(R.id.btn_printer_test);

        btn_load_bluetoothPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loopPrintFlag = DEFAULT_LOOP_PRINT;
                LoadBluetoothPrinter();
            }
        });
        btn_printer_init.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loopPrintFlag = DEFAULT_LOOP_PRINT;
                if(getPrinterStatus() == PRINTER_NORMAL)
                    printerInit();
            }
        });
        setButtonEnable(true);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer);

        mPrinterHandler = new HandlerUtils.PrinterHandler(mHandlerIntent);

        mAirtimeRecycler = findViewById(R.id.recycler_view);
        mAirtimeAmount = findViewById(R.id.airtime_amount);
        mVoucherAmount = findViewById(R.id.voucher_amount);
        mVoucherText = findViewById(R.id.voucher_text);
        mText = findViewById(R.id.text);

        mAirtimeRecycler.setLayoutManager(new LinearLayoutManager(this));

        airtimeList = new ArrayList<>();
        airtimeIdList = new ArrayList<>();

        calendar = Calendar.getInstance();


        getData();
        innitView();
        receiveIntent();




        btn_printer_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    // int subLength = airtimeIdList.subList(0, Integer.valueOf(mVoucherText.getText().toString())).size();

                Toast.makeText(PrinterActivity.this, "Please wait a while..", Toast.LENGTH_LONG).show();

                postNewSale();

            }
        });

    }

    private void receiveIntent() {
        Intent intent = getIntent();
        selectedAirtime = intent.getStringExtra("airtime");
        mAirtimeAmount.setText("R" + selectedAirtime);
        currentAcc = "MPB32";// intent.getStringExtra("currentAcc");

        mVoucherAmount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    mVoucherText.setText("1");
                  //  getAirtime(selectedAirtime, Integer.valueOf(mVoucherText.getText().toString()));

                } else {
                    mVoucherText.setText(String.valueOf(progress));
                  //  getAirtime(selectedAirtime, Integer.valueOf(mVoucherText.getText().toString()));

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

       // getAirtime(selectedAirtime, voucherAmount);

    }

//    private void getQuantity(String selectedAirtime) {
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_QNTY, new com.android.volley.Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                try {
//                    JSONArray array = new JSONArray(response);
//                    for (int i = 0; i < array.length(); i++) {
//                        JSONObject object = array.getJSONObject(i);
//
//                        prod_id = object.getString("prod_id");
//                        av_qty = object.getString("av_qty");
//
//                        if (prod_id.equals(selectedAirtime.substring(1))) {
//                            if (!av_qty.equals("0")) {
//
//                            } else {
//                                Snackbar.make(findViewById(android.R.id.content), "Out of Stock. Select Another One.", Snackbar.LENGTH_INDEFINITE)
//                                        .setAction("OK", new View.OnClickListener() {
//                                            @Override
//                                            public void onClick(View v) {
//                                                onBackPressed();
//                                            }
//                                        });
//                            }
//
//
//                        } else {
//                            Snackbar.make(findViewById(android.R.id.content), "Out of Stock. Select Another One.", Snackbar.LENGTH_INDEFINITE)
//                                    .setAction("OK", new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View v) {
//                                            onBackPressed();
//                                        }
//                                    });
//                        }
//
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }, new com.android.volley.Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(PrinterActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        Volley.newRequestQueue(PrinterActivity.this).add(stringRequest);
//    }

    private void getAirtime(String selectedAirtime, int voucherAmount) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);

                        prod_id = object.getString("prod_id");
                        pin_no = object.getString("pin_no");
                        serial_no = object.getString("serial_no");
                        String status = object.getString("status");
//                        String expired_date = object.getString("expired_date");
                        String id = object.getString("id");


                        if (prod_id.equals(selectedAirtime) && status.equals("Available")) {
                         //   airtime = new Airtime(prod_id, pin_no, serial_no, status, id);
                            airtimeId = new AirtimeId(id);

                            airtimeList.add(airtime);
                            airtimeIdList.add(airtimeId);

                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                airtimeAdapter = new AirtimeAdapter(PrinterActivity.this, airtimeList, voucherAmount);
                mAirtimeRecycler.setAdapter(airtimeAdapter);
                airtimeAdapter.notifyDataSetChanged();

                btn_printer_test.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        if(getPrinterStatus() == PRINTER_NORMAL) {
//                            int subLength = airtimeIdList.subList(0, Integer.valueOf(mVoucherText.getText().toString())).size();
//
//                            for (int i = 0; i < subLength; i++) {
////                                bluetoothPrinterTest(prod_id, pin_no, serial_no);
////                                Toast.makeText(PrinterActivity.this, "How many times: " + i, Toast.LENGTH_SHORT).show();
////                                mText.setText("Length: " + subLength + "\n" + airtimeList.subList(0, subLength) + "\nPin: " + airtimeList.get(i).id);
//
//
//                            }
//
////                            Intent intent = new Intent(PrinterActivity.this, PrinterActivity.class);
////                            intent.putExtra("title", "prix");
////                            startActivity(intent);
//
//                        }
                      //  postInfo();

                    }
                });

            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(PrinterActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        Volley.newRequestQueue(PrinterActivity.this).add(stringRequest);
    }





    private void postNewSale() {

        Log.d(TAG, "airtime: "+selectedAirtime);
        Log.d(TAG, "voucher: "+mVoucherText.getText().toString());
        Log.d(TAG, "currentAcc: "+currentAcc);


        StringRequest request = new StringRequest(Request.Method.POST, NEW_SALES_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try{
                Log.d("TAG", "onResponse: "+ response);

                JSONArray array = new JSONArray(response);

                if(array.length() > 0) {

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);

                        // only we need pin_no and serial_no

                        pin_no = object.getString("pin_no");
                        serial_no = object.getString("serial_no");

                        airtime = new Airtime(pin_no, serial_no);
                        airtimeList.add(airtime);

                        if (getPrinterStatus() == PRINTER_NORMAL) {

                            bluetoothPrinterTest("14", pin_no, serial_no);
                            //  mText.setText("Length: " + airtimeList.size() + "\n" + airtimeList.subList(0, airtimeList.size()) + "\nPin: " + airtimeList.get(i).pin_no);
                        }

                    }
                }else{
                    Toast.makeText(PrinterActivity.this, "No Data Found for R"+selectedAirtime, Toast.LENGTH_SHORT).show();
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            airtimeAdapter = new AirtimeAdapter(PrinterActivity.this, airtimeList, voucherAmount);
                mAirtimeRecycler.setAdapter(airtimeAdapter);
                airtimeAdapter.notifyDataSetChanged();


            }
        }, error -> {
            Snackbar.make(findViewById(android.R.id.content), error.toString().trim(), Snackbar.LENGTH_SHORT).show();
            Log.d("TAG", "onErrorResponse: "+error.getMessage());

        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> data = new HashMap<>();
                data.put("airtime", selectedAirtime);
                data.put("qty", mVoucherText.getText().toString());
                data.put("currentAcc", currentAcc);
                return data;

            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }

    @Override
    protected void onResume() {
        // Log.d(TAG, "activity onResume");
        super.onResume();
        //注册打印机状态接收器
        IntentFilter printerStatusFilter = new IntentFilter();
        printerStatusFilter.addAction(PRINTER_NORMAL_ACTION);
        printerStatusFilter.addAction(PRINTER_PAPERLESS_ACTION);
        printerStatusFilter.addAction(PRINTER_PAPEREXISTS_ACTION);
        printerStatusFilter.addAction(PRINTER_THP_HIGHTEMP_ACTION);
        printerStatusFilter.addAction(PRINTER_THP_NORMALTEMP_ACTION);
        printerStatusFilter.addAction(PRINTER_MOTOR_HIGHTEMP_ACTION);
        printerStatusFilter.addAction(PRINTER_BUSY_ACTION);
        printerStatusFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(IPosPrinterStatusListener,printerStatusFilter);

        loopPrintFlag = DEFAULT_LOOP_PRINT;
        LoadBluetoothPrinter();
        if(getPrinterStatus() == PRINTER_NORMAL) {
            printerInit();

        }
    }

    @Override
    protected void onPause() {
        // Log.d(TAG, "activity onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        // Log.e(TAG, "activity onStop");
        super.onStop();
        unregisterReceiver(IPosPrinterStatusListener);
        loopPrintFlag = DEFAULT_LOOP_PRINT;
    }

    @Override
    protected void onDestroy() {
        // Log.d(TAG, "activity onDestroy");
        super.onDestroy();
        mPrinterHandler.removeCallbacksAndMessages(null);
        if(mBluetoothAdapter != null)
            mBluetoothAdapter = null;
        if(mBluetoothPrinterDevice != null)
            mBluetoothPrinterDevice = null;
        try {
            if (socket != null && (socket.isConnected())) {
                socket.close();
                socket = null;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void print_loop(int flag) {
        switch (flag)
        {
            case MULTI_THREAD_LOOP_PRINT:
                multiThreadPrintTest();
                break;
            default:
                break;
        }
    }

    public void multiThreadPrintTest() {
        switch (random.nextInt(16))
        {
            case 0:
                bluetoothPrinterTest(prod_id, pin_no, serial_no);
                break;
            default:
                break;
        }
    }

    public void LoadBluetoothPrinter() {
        // 1: Get BluetoothAdapter
        mBluetoothAdapter = BluetoothUtil.getBluetoothAdapter();
        if(mBluetoothAdapter == null)
        {
            Toast.makeText(getBaseContext(), R.string.get_BluetoothAdapter_fail, Toast.LENGTH_LONG).show();
            isBluetoothOpen = false;
            return;
        }
        else
        {
            isBluetoothOpen =true;
        }
        //2: Get bluetoothPrinter Devices
        mBluetoothPrinterDevice = BluetoothUtil.getIposPrinterDevice(mBluetoothAdapter);
        if(mBluetoothPrinterDevice == null)
        {
            Toast.makeText(getBaseContext(), R.string.get_BluetoothPrinterDevice_fail, Toast.LENGTH_LONG).show();
            return;
        }
        //3: Get connect Socket
        try {
            socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }
//        Toast.makeText(getBaseContext(), R.string.get_BluetoothPrinterDevice_success, Toast.LENGTH_LONG).show();
    }

    public int getPrinterStatus() {
        byte[] statusData = new byte[3];
        if(!isBluetoothOpen)
        {
            printerStatus = PRINTER_ERROR_UNKNOWN;
            return printerStatus;
        }
        if((socket == null) || (!socket.isConnected()))
        {
            try {
                socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return printerStatus;
            }
        }
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            byte[] data = ESCUtil.getPrinterStatus();
            out.write(data,0,data.length);
            int readsize = in.read(statusData);
            Log.d(TAG,"~~~ readsize:"+readsize+" statusData:"+statusData[0]+" "+statusData[1]+" "+statusData[2]);
            if((readsize > 0) &&(statusData[0] == ESCUtil.ACK && statusData[1] == 0x11)) {
                printerStatus = statusData[2];
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return printerStatus;
    }

    private void printerInit() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    if((socket == null) || (!socket.isConnected()))
                    {
                        socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                    }
                    //Log.d(TAG,"=====printerInit======");
                    OutputStream out = socket.getOutputStream();
                    byte[] data = ESCUtil.init_printer();
                    out.write(data,0,data.length);
                    out.close();
                    socket.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    private void bluetoothPrinterTest(String ProdId, String PinNumber, String SerialNumber) {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try{
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.prix_logo);
                    DateFormat df = new SimpleDateFormat("dd/MM/yy");
                    currentDate = df.format(Calendar.getInstance().getTime());

                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    currentTime = dateFormat.format(Calendar.getInstance().getTime());
                    int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

                    if (currentDay == 1) {
                        day = "Sun";

                    } else if (currentDay == 2) {
                        day = "Mon";

                    } else if (currentDay == 3) {
                        day = "Tue";

                    } else if (currentDay == 4) {
                        day = "Wed";

                    } else if (currentDay == 5) {
                        day = "Thur";

                    } else if (currentDay == 6) {
                        day = "Fri";

                    } else if (currentDay == 7) {
                        day = "Sat";

                    }

                    byte[] printer_init = ESCUtil.init_printer();
                    byte[] fontSize0 = ESCUtil.fontSizeSet((byte) 0x00);
                    byte[] fontSize1 = ESCUtil.fontSizeSet((byte) 0x01);
                    byte[] fontSize2 = ESCUtil.fontSizeSet((byte) 0x10);
                    byte[] fontSize3 = ESCUtil.fontSizeSet((byte) 0x11);
                    byte[] lineH0 = ESCUtil.setLineHeight((byte)26);
                    byte[] lineH1 = ESCUtil.setLineHeight((byte)50);

                    byte[] align0 = ESCUtil.alignMode((byte)0);
                    byte[] align1 = ESCUtil.alignMode((byte)1);
                    byte[] align2 = ESCUtil.alignMode((byte)2);
                    String price = "R" + selectedAirtime;
                    String heading = "To recharge. Dial";
                    String pin = "*130*7467*" + PinNumber + "#";
                    String scan = "OR Scan the barcode below:";
                    //Make PIN bold
                    byte[] title1 = price.getBytes("GBK");
                    byte[] title2 = heading.getBytes("GBK");
                    byte[] title3 = pin.getBytes("GBK");
                    byte[] title4 = scan.getBytes("GBK");

                    String lowerData = "Date: " + currentDate + " " + day + " " + currentTime;
                    byte[] orderSerinum = lowerData.getBytes("GBK");
                    String serialNumber = "S/N: " + SerialNumber;
                    byte[] testInfo = serialNumber.getBytes("GBK");
                    byte[] QrCodeData = PinNumber.getBytes("GBK");

                    byte[] nextLine = ESCUtil.nextLines(2);
                    byte[] performPrint = ESCUtil.performPrintAndFeedPaper((byte)200);

                    byte[][] cmdBytes = {printer_init,lineH0,fontSize3,align1, BitMapUtil.getBitmapPrintData(bitmap, 325, 1), nextLine, fontSize3, title1, nextLine, fontSize1,title2,nextLine,
                            fontSize3, title3, nextLine, fontSize1,title4,nextLine, lineH1,fontSize1,align1,ESCUtil.setQRsize(8), ESCUtil.setQRCorrectionLevel(48), ESCUtil.cacheQRData(QrCodeData), nextLine, lineH0,fontSize0,orderSerinum,
                            align1,fontSize1,lineH1,testInfo,nextLine,performPrint};
                    try {
                        if((socket == null) || (!socket.isConnected()))
                        {
                            socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                        }
                        byte[] data = ESCUtil.byteMerger(cmdBytes);
                        OutputStream out = socket.getOutputStream();
                        out.write(data,0,data.length);
                        out.close();
                        socket.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }

            }
        });
    }


    private void getData(){
        StringRequest request = new StringRequest(Request.Method.GET, URL_WALLET, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        acc_no = object.getString("acc_no");

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(PrinterActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        Volley.newRequestQueue(PrinterActivity.this).add(request);
    }

}
