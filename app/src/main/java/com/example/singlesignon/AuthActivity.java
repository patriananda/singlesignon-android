package com.example.singlesignon;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class AuthActivity extends AppCompatActivity {
    String imei;
    AsyncTask asyncTaskLDAP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        checkRequiredPermission();
    }

    private void checkRequiredPermission() {
        // check if permission not granted
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // request phone permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
            return;
        }

        // get imei
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        assert telephonyManager != null;
        imei = telephonyManager.getImei();

        Toast.makeText(getBaseContext(), imei, Toast.LENGTH_LONG).show();
        // membuat dan langsung execute AsyncLDAPSearch dengan membawa parameter imei
        asyncTaskLDAP = new AsyncLDAPSearch(getApplicationContext()).execute(imei);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            checkRequiredPermission();
        } else {
            System.exit(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        asyncTaskLDAP.cancel(true);
    }
}
