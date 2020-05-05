package com.example.singlesignon;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchScope;

public class SignInActivity extends AppCompatActivity {

    String address = "10.0.2.2";
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
                startActivity(intent);
            }
        });

        if (getIntent().getExtras() != null) {
            imei = getIntent().getStringExtra("imei");
        }

    }

    @SuppressLint("HardwareIds")
    public void onClickSignInButton(View view) {
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
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
                startActivity(intent);

            } catch (LDAPException e) {
                if (e.getResultCode().intValue() == 91) { // used in line 76 above, process connecting and binding
                    Toast.makeText(getBaseContext(), "Cannot connect to the server", Toast.LENGTH_LONG).show();
                } else if (e.getResultCode().intValue() == 0) { // used in line 68 above, user with this device not found
                    Toast.makeText(getBaseContext(), "Can't sign in from this device", Toast.LENGTH_LONG).show();
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
