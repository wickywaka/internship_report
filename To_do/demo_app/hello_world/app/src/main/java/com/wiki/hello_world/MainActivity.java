package com.wiki.hello_world;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Network;
import android.os.Bundle;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.telephony.TelephonyManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.scottyab.rootbeer.RootBeer;
import com.stealthcopter.networktools.Ping;
import com.stealthcopter.networktools.ping.PingResult;

import android.bluetooth.BluetoothAdapter;
import android.widget.TextView;

import org.json.JSONObject;

import dalvik.system.DexClassLoader;


//todo: Anti-Hooking techniques http://d3adend.org/blog/?p=589


public class MainActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "MySimpleAppLogging";
    String wantPermission = Manifest.permission.READ_PHONE_STATE;
    private static final int ACCESS_NETWORK_STATE_CODE = 0;
    private static final int READ_PHONE_STATE_CODE = 1;
    Activity activity = MainActivity.this;
    File file;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StackTraceElement[] stackTrace;


        setContentView(R.layout.activity_main);
        //doLog("Testing void returning method hook!!");
        //Log.i(DEBUG_TAG, "Emulator detection demo app.");
        TextView logText = (TextView) findViewById(R.id.log_id);
        logText.setMovementMethod(new ScrollingMovementMethod());
        logText.setText("");
        logText.setText(getBuildInfo());
        try {
            FileOutputStream file = openFileOutput("hooks.json", Context.MODE_PRIVATE);
            file.write("Hello world!".getBytes());
            file.flush();
            file.close();
            logText.append("File written successfully");

        }
        catch (Exception e){
                logText.append("Exception "+ e.toString());
        }



        //TelephonyManager localTelephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);

        if (!checkPermission(Manifest.permission.ACCESS_NETWORK_STATE)){
            requestPermission(Manifest.permission.ACCESS_NETWORK_STATE,0);
        } else {
            //Log.d(DEBUG_TAG, networkDetect());
            logText.append(networkDetect());
        }

        if (!checkPermission(Manifest.permission.READ_PHONE_STATE)){
            requestPermission(Manifest.permission.READ_PHONE_STATE,1);
        } else {
            //Log.d(DEBUG_TAG, getPhone());
            logText.append(getPhone());
        }

        //ping("localhost");

        RootBeer rootBeer = new RootBeer(getApplicationContext());
        if (rootBeer.isRooted()) {
            logText.append( "Rootbeer POSITIVE\n");
        } else {
            logText.append( "Rootbeer NEGATIVE\n");
        }

        BluetoothAdapter BA = BluetoothAdapter.getDefaultAdapter();
        if (BA == null)
            logText.append( "Bluetooth device not present\n");
        else
            logText.append( "Bluetooth device Present\n"); // Add more test like using the device to make sure that its not emulator.

        SimpleDateFormat formatter = new SimpleDateFormat("dd:HH:mm:ss", Locale.GERMANY);
        //Date date = new Date(SystemClock.uptimeMillis());
        //Log.i(DEBUG_TAG, "System uptime is: " + formatter.format(date) + "\n in Minutes it is" + Long.toString((SystemClock.uptimeMillis()/(1000*60))));
        logText.append( "System uptime is " + Long.toString((SystemClock.uptimeMillis()/(1000*60))) + "  Minutes.\n");

        // Link https://github.com/stealthcopter/AndroidNetworkTools
        try {
            InetAddress ia = InetAddress.getLoopbackAddress(); // todo: How to display the ip address from inteaddress and how to ping something else than localhost
            logText.append( "Pinging www.example.com\n");
            PingResult pingResult = Ping.onAddress(ia).setTimeOutMillis(1000).doPing();
            logText.append( pingResult.toString()+"\n\n");
        }
        catch(Exception e){
            logText.append( e.toString() + e.getMessage()+"\n\n\n");
        }

        logText.append(get_stack_tace());



        String filename = "myfile";
        String string = "Hello world!";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }



    }


    private String get_stack_tace(){
        Throwable t = new Throwable();
        StackTraceElement[] trace = t.getStackTrace();

        String tt="\n [+]Stack Trace for : " + trace[1].getClassName()+"->"+trace[1].getMethodName()+"\n";
        for (StackTraceElement tr:trace){
            tt = tt.concat("==>" + tr.getClassName()+"->"+tr.getMethodName()+" : "+Integer.toString(tr.getLineNumber())+"\n");
        }
        return "";
    }

    private String getPhone() {
        TelephonyManager phoneMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        String phoneData = "getSimOperatorName : \t " + phoneMgr.getSimOperatorName() + "\n" +
                            "\tgetNetworkOperatorName : \t " + phoneMgr.getNetworkOperator() + "\n";

        if (ActivityCompat.checkSelfPermission(activity, wantPermission) != PackageManager.PERMISSION_GRANTED) return "";

        phoneData = phoneData.concat("Phone Number : " + phoneMgr.getLine1Number() + "\n");
        phoneData = phoneData.concat("Country : " + phoneMgr.getSimCountryIso() + "\n");
        phoneData = phoneData.concat("getDeviceID : " + phoneMgr.getDeviceId() +"\n");
        phoneData = phoneData.concat(get_stack_tace());
        return phoneData;
    }

    private void requestPermission(String permission, int PERMISSION_REQUEST_CODE){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)){
            Log.d(DEBUG_TAG,"Please allow this permission(s)... :)");
        }
        Log.i(DEBUG_TAG, "Trying to get" + Integer.toString(PERMISSION_REQUEST_CODE));
        ActivityCompat.requestPermissions(activity, new String[]{permission},PERMISSION_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        Log.d(DEBUG_TAG, "String permissions : "+ permissions);
        switch (requestCode){
            case READ_PHONE_STATE_CODE:
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                    Log.d(DEBUG_TAG, getPhone());
                else
                    Log.d(DEBUG_TAG, "Permission Denied. We can't get phone number.");
                break;

            case ACCESS_NETWORK_STATE_CODE:
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                    Log.d(DEBUG_TAG, "Inside onRequestPermissionsResult() prints : \n" + networkDetect());
                else
                    Log.d(DEBUG_TAG, "Permission Denied. We can't get networks information.");
                break;

        }
    }


    private boolean checkPermission(String permission){
        if (Build.VERSION.SDK_INT >= 23) {
            int result = ContextCompat.checkSelfPermission(activity, permission);
            if (result == PackageManager.PERMISSION_GRANTED){
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private String getBuildInfo(){

        return "\n\nBuild Information : \nBuild.FINGERPRINT : \t" + Build.FINGERPRINT + "\n" +
        "Build.HOST : \t" + Build.HOST + "\n" +
        "Build.HARDWARE : \t " + Build.HARDWARE + "\n" +
        "Build.PRODUCT :\t " + Build.PRODUCT + "\n" +
        "Build.MODEL : \t " + Build.MODEL+ "\n"+get_stack_tace();

    }

    private String networkDetect(){
        // Code for network detection
        boolean WIFI = false;
        boolean MOBILE = false;
        boolean ethernet = false;
        String networkData = "";

        ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        //CM.getAllNetworkInfo() deprecated in API 23, Use getAllNetworks() and getNetworkInfo(android.net.Network) instead.

        Network[] network = CM.getAllNetworks();
        NetworkInfo netInfo;
        for (Network net : network) {
            netInfo = CM.getNetworkInfo(net);
            //logText.append( "netInfo :" + netInfo.toString());
            if (netInfo.getTypeName().equalsIgnoreCase("WIFI"))
                if (netInfo.isConnected())
                    WIFI = true;

            if (netInfo.getTypeName().equalsIgnoreCase("MOBILE"))
                if (netInfo.isConnected())
                    MOBILE = true;
        }

        if(WIFI)
            networkData =  "WIFI :" + networkData.concat(GetDeviceipWiFiData()) + "\n";

        if(MOBILE)
            networkData = "MOBILE : " + networkData.concat(GetDeviceipMobileData()) + "\n";

        return networkData+get_stack_tace();
    }


    public String GetDeviceipMobileData(){
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements();) {
                NetworkInterface networkinterface = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = networkinterface.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress()+get_stack_tace();
                    }
                }
            }
        } catch (Exception ex) {
            Log.i("Current IP", ex.toString());
        }
        return null;
    }

    public String GetDeviceipWiFiData() {

        WifiManager wm = (WifiManager)  getApplicationContext().getSystemService(WIFI_SERVICE);

        @SuppressWarnings("deprecation")

        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        return ip+get_stack_tace();

    }

    public void doLog(String str){

        Log.i(DEBUG_TAG, str);
    }

}
