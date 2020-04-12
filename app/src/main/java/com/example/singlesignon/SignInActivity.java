package com.example.singlesignon;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;

public class SignInActivity extends AppCompatActivity {

    //String address="ldap://server1.mydomain.com" ;
    String address = "10.0.2.2";
    int port = 10389;
    //String bindDN="CN=name,CN=users,DC=mydomain,DC=com";
    String bindDN = "uid=admin,ou=system";
    String passwordDN = "secret";
    boolean login_flag = true;

    LDAPConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        this.setTitle("Signed In.");
        checkRequiredPermission();
    }

    private void checkRequiredPermission() {

        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
            return;
        }

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        assert telephonyManager != null;
        String imei = telephonyManager.getImei();

        Toast.makeText(getBaseContext(), imei, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            checkRequiredPermission();
        } else {
            finish();
        }
    }

    @SuppressLint("HardwareIds")
    public void onClickSignInButton(View view) {
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            try {

                final EditText editTextUsername = findViewById(R.id.editTextUsername);
                final EditText editTextPassword = findViewById(R.id.editTextPassword);
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();

                connection = new LDAPConnection(address, port, "cn=" + username + ",ou=users,dc=example,dc=com", password);

//                SearchResult searchResult = connection.search("ou=scientists,dc=example,dc=com", SearchScope.SUB, Filter.createEqualityFilter("cn", "Tesla"));

//                Hashtable env = new Hashtable(); // hashtable tu kaya array
//                env.put(Context .INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
//                env.put(Context.PROVIDER_URL, "ldap://10.0.2.2:10389/uid=admin,ou=system");
//
//                env.put(Context.SECURITY_AUTHENTICATION, "simple");
//                env.put(Context.SECURITY_PRINCIPAL, editTextUsername);
//                env.put(Context.SECURITY_CREDENTIALS, editTextPassword);
//
//                try {
//                    Context ctx = new InitialContext(env); // ini buat connect butuh satu parameter yang isinya env 17-22
//                    NamingEnumeration enm = ctx.list("");
//
//                    while (enm.hasMore()) {
//                        System.out.println(enm.next());
//                    }
//
//                    enm.close();
//                    ctx.close();
//                } catch (NamingException e) {
//                    System.out.println(e.getMessage());
//                }

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);

//                Toast.makeText(getBaseContext(), "connected", Toast.LENGTH_LONG).show();
//                System.out.println(searchResult.getResultString());
//                if (username.equals(searchResult.getResultString()))
//                {
//                    Toast.makeText(getBaseContext(), "yay", Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(getBaseContext(),"Invalid username or passwordDN" , Toast.LENGTH_LONG).show();
//                }
//            System.out.println(searchResult.getEntryCount() + " entries returned.");
//            for (SearchResultEntry e : searchResult.getSearchEntries())
//            {
//                System.out.println(e.toLDIFString());
//                System.out.println();
//            }

            } catch (LDAPException e) {
                login_flag = false;
                e.printStackTrace();
                Toast.makeText(getBaseContext(), "Invalid username or password", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
//            finally {
//                if (login_flag) {
//                    connection.close();
//                    Toast.makeText(getBaseContext(), "Connection Closed successfully", Toast.LENGTH_LONG).show();
//                }
//            }
        }
    }
}
