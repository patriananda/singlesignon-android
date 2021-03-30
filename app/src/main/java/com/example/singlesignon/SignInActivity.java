package com.example.singlesignon;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

public class SignInActivity extends AppCompatActivity {

    String address = "103.214.112.199";
    int port = 10389;

    LDAPConnection connection;
    String username;
    String imei;

    SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        this.setTitle("Welcome.");

        refreshLayout = findViewById(R.id.swipeRefresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // agar loading ga muter terus pas refresh
                refreshLayout.setRefreshing(false);

                Intent intent = new Intent(getApplicationContext(), AuthActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        if (getIntent().getExtras() != null) {
            imei = getIntent().getStringExtra("imei");
        }

    }

    @SuppressLint("HardwareIds")
    public void onClickSignInButton(View view) {
        int SDK_INT = Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            try {
                connection = new LDAPConnection(address, port);
                // search imei registered on DN
                SearchResult searchResult = connection.search("ou=users,dc=example,dc=com", SearchScope.SUB, "(uid=" + imei + ")");

                if (searchResult.getEntryCount() < 1) {
                    throw new LDAPException(searchResult); // melempar dari try ke catch
                }

                final EditText editTextUsername = findViewById(R.id.editTextUsername);
                final EditText editTextPassword = findViewById(R.id.editTextPassword);
                username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();

                // ambil username dari dn searchResult line 68
                String userDN = null;
                for (SearchResultEntry entry : searchResult.getSearchEntries()) {
                    String[] attributeList = entry.getDN().split(",");
                    userDN = attributeList[2].substring(3);
                }
                if (!username.equals(userDN)) {
                    throw new LDAPException(ResultCode.valueOf(0));
                }

                connection = new LDAPConnection(address, port, "cn=" + username + ",ou=users,dc=example,dc=com", password);

                // add an entry on LDAP Server
                Attribute[] attributes =
                    {
                        new Attribute("objectClass", "top", "person", "inetOrgPerson"),
                        new Attribute("cn", "connected"),
                        new Attribute("sn", username),
                        new Attribute("l", "connected"),
                    };
                connection.add("l=connected,cn=" + username + ",ou=users,dc=example,dc=com", attributes);

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                // mengoper username ke intent selanjutnya yaitu MainActivity
                intent.putExtra("username", username);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            } catch (LDAPException e) {
                if (e.getResultCode().intValue() == 91) { // used in line 89 above, process connecting and binding
                    Toast.makeText(getBaseContext(), "Cannot connect to the server", Toast.LENGTH_LONG).show();
                } else if (e.getResultCode().intValue() == 0) { // used in line 71 above, user with this device not found
                    Toast.makeText(getBaseContext(), "Can't sign in from this device", Toast.LENGTH_LONG).show();
                } else if (e.getResultCode().intValue() == 68) { // used in line 71 above, user with this device not found
                    Toast.makeText(getBaseContext(), "Already signed in, pull down to refresh", Toast.LENGTH_LONG).show();
                } else {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), "Invalid username or password", Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
