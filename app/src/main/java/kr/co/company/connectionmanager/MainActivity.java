package kr.co.company.connectionmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.PatternMatcher;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

class ConnectionMgr extends Activity {

    Context context;
    ConnectivityManager connectivityManager;

    static final int PERMISSIONS_REQUEST = 0x0000001;

    public ConnectionMgr(Context context){
        this.context = context;
    }


    public void enableWifi() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder();
                builder.setSsidPattern(new PatternMatcher("CarKey",PatternMatcher.PATTERN_PREFIX));
                builder.setWpa2Passphrase("1234qqqq");

                WifiNetworkSpecifier wifiNetworkSpecifier = builder.build();

                final NetworkRequest.Builder networkRequestBuilder = new NetworkRequest.Builder();
                networkRequestBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
                networkRequestBuilder.setNetworkSpecifier(wifiNetworkSpecifier);

                NetworkRequest networkRequest = networkRequestBuilder.build();

                connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
                connectivityManager.requestNetwork(networkRequest, networkCallback);


            } else {
                WifiConfiguration wifiConfiguration = new WifiConfiguration();
                wifiConfiguration.SSID = String.format("\"%s\"", "Carkey_WiFi11"); // ??????????????? ?????? SSID
                wifiConfiguration.preSharedKey = String.format("\"%s\"", "1234qqqq"); // ????????????
                int wifiId = wifiManager.addNetwork(wifiConfiguration);
                wifiManager.enableNetwork(wifiId, true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    void disableWifi() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            if (wifiManager.isWifiEnabled()) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                } else {
                    if (wifiManager.getConnectionInfo().getNetworkId() == -1) {

                    } else {
                        int networkId = wifiManager.getConnectionInfo().getNetworkId();
                        wifiManager.removeNetwork(networkId);
                        wifiManager.saveConfiguration();
                        wifiManager.disconnect();

                        boolean a = isConnected(context);
                        if(a){
                            Log.d("??????", "lte");
                        } else{
                            Log.d("??????", "lte??????");
                        }
                    }
                }

            } else
                Toast.makeText(context, "Wifi ??????", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "?????? ?????? ?????? : " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public ConnectivityManager.NetworkCallback networkCallback =
            new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            Log.e("onAvailable", "WI-FI ??????");
        }
        public void onLost(@NonNull Network network) {
            super.onAvailable(network);
            Log.e("onLost", "WI-FI ?????? ??????");

        }
    };
    public void OnCheckPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "??? ????????? ???????????? ????????? ???????????? ?????????", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSIONS_REQUEST);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSIONS_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "??? ????????? ?????? ????????? ?????? ???????????????", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "??? ????????? ?????? ????????? ?????? ???????????????", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public boolean checkSystemPermission() {

        boolean permission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   //23?????? ??????
            permission = Settings.System.canWrite(this);
            if (permission) {
            } else {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 2127);
                permission = false;
            }
        } else {

        }

        return permission;
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

}

class NetworkStatus {
    public static final int TYPE_WIFI = 1;
    public static final int TYPE_MOBILE = 2;
    public static final int TYPE_NOT_CONNECTED = 3;

    public static int getConnectivityStatus(Context context){ //?????? context??? ???????????? ????????????????????? context????????? ?????????.
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if(networkInfo != null){
            int type = networkInfo.getType();
            if(type == ConnectivityManager.TYPE_MOBILE){//???????????? LTE??? ????????????(???????????? ?????????.)
                return TYPE_MOBILE;
            }else if(type == ConnectivityManager.TYPE_WIFI){//???????????? ????????????
                return TYPE_WIFI;
            }
        }
        return TYPE_NOT_CONNECTED;  //????????? ???????????? ??????
    }
}
public class MainActivity extends AppCompatActivity {

    Button on;
    Button off;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());

        if (status == NetworkStatus.TYPE_WIFI){
            startActivity(new Intent(Settings.Panel.ACTION_WIFI));
            Toast.makeText(this, "???????????? ????????? ??????????????????.", Toast.LENGTH_LONG).show();

        }

        on = (Button) findViewById(R.id.btn_connect);
        off = (Button) findViewById(R.id.btn_disconnect);

        View.OnClickListener onClickListener
                = new View.OnClickListener(){
            @Override
            public void onClick(View v){
                ConnectionMgr cmg = new ConnectionMgr(getApplicationContext());
                switch (v.getId()){
                    case R.id.btn_connect:

                        if (status != NetworkStatus.TYPE_WIFI){
                            startActivity(new Intent(Settings.Panel.ACTION_WIFI));

                        }
                        cmg.enableWifi();
                        break;
                    case R.id.btn_disconnect:
                        startActivity(new Intent(Settings.Panel.ACTION_WIFI));
                        break;
                }
            }
        };

        on.setOnClickListener(onClickListener);
        off.setOnClickListener(onClickListener);

    }
}