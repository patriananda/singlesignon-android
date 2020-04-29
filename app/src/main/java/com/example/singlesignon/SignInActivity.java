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

public class SignInActivity extends AppCompatActivity {

    String address = "10.0.2.2";
    int port = 10389;
    boolean login_flag = true;

    LDAPConnection connection;
    String username;

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
                login_flag = false;
                e.printStackTrace();
                Toast.makeText(getBaseContext(), "Invalid username or password", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
