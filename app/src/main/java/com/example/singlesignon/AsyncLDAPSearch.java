package com.example.singlesignon;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

public class AsyncLDAPSearch extends AsyncTask<String, Void, String> {
    private Activity activity;

    private static String DEFAULT_HOST = "10.0.2.2";
    private static int DEFAULT_PORT = 10389;
    private static SearchScope DEFAULT_SCOPE = SearchScope.SUB;

    AsyncLDAPSearch(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(String... args) {
        String imei = args[0];

        String username = "";

        try {
            Log.i("doInBG", "Trying...");
            LDAPConnection conn = new LDAPConnection(DEFAULT_HOST, DEFAULT_PORT);
            SearchResult searchResult = conn.search("ou=users,dc=example,dc=com", DEFAULT_SCOPE, "(uid=" + imei + ")");

            for (SearchResultEntry entry : searchResult.getSearchEntries()) {
                Log.i("DN", entry.getDN());
                String[] attributeList = entry.getDN().split(",");
                String userDN = attributeList[2].substring(3);
                SearchResult searchResult2 = conn.search("ou=users,dc=example,dc=com", DEFAULT_SCOPE, "(&(l=connected)(sn=" + userDN + "))");
                Log.i("Connected count", String.valueOf(searchResult2.getEntryCount()));

                if (searchResult2.getEntryCount() == 1) {
                    username = userDN;
                    break;
                }
            }
        } catch (LDAPException e) {
            Log.i("doInBG", "ga bisa konek LDAP bro");
            e.printStackTrace();
        }

        return username;
    }

    @Override
    protected void onPostExecute(String result) {
        Intent intent = new Intent(activity, SignInActivity.class);

        if (!"".equals(result)) {
            intent = new Intent(activity, MainActivity.class);
            intent.putExtra("username", result);
        }

        activity.startActivity(intent);
    }
}
