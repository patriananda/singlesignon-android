package com.example.singlesignon;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
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

        // memastikan kalau dari intent sebelumnya tidak null
        if (getIntent().getExtras() != null) {
            username = getIntent().getStringExtra("username");
            textView = findViewById(R.id.textViewSubWelcome);
            textView.setText("You are now signed in as " + username.substring(0, 1).toUpperCase() + username.substring(1) + ", you need to sign out before signing in as different user.");
        }
    }

    public void onClickSignOutButton(View view) throws LDAPException {
        Log.d("onClickSignOutButton", "salah bro");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        // still error binding to LDAP
        LDAPConnection connection = new LDAPConnection(address, port, rootDN, rootPassword);
        SearchResult searchResult = connection.search("ou=users,dc=example,dc=com", SearchScope.SUB, "(&(l=connected)(sn=" + username + "))");
        if (searchResult.getEntryCount() == 1) {
            connection.delete("l=connected,cn=" + username + ",ou=users,dc=example,dc=com");

            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_favorite:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }



}
