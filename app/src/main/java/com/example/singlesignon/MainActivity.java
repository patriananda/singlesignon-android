package com.example.singlesignon;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchScope;

public class MainActivity extends AppCompatActivity {
    String address = "10.0.2.2";
    int port = 10389;
    String rootDN = "uid=admin,ou=system";
    String rootPassword = "secret";
    String username = "";
    TextView textView;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setTitle("Signed In.");

        if (getIntent().getExtras() != null) {
            username = getIntent().getStringExtra("username");
            textView = findViewById(R.id.textViewSubWelcome);
            textView.setText("You are now signed in as " + username.substring(0, 1).toUpperCase() + username.substring(1) + ", you need to sign out before signing in as different user.");
        }

    }

    public void onClickSignOutButton(View view) throws LDAPException {
        LDAPConnection connection = new LDAPConnection(address, port, rootDN, rootPassword);
        Log.d("onClickSignOutButton", "salah bro");
        SearchResult searchResult = connection.search("ou=users,dc=example,dc=com", SearchScope.SUB, "(&(l=connected)(sn=" + username + "))");
        if (searchResult.getEntryCount() == 1) {
            connection.delete("l=connected,cn=" + username + ",ou=users,dc=example,dc=com");
        }

        Intent intent = new Intent(getApplicationContext(), AuthActivity.class);
        startActivity(intent);
    }

}
