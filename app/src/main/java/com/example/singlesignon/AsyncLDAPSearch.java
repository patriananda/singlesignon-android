package com.example.singlesignon;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

public class AsyncLDAPSearch extends AsyncTask<String, Void, String> {
    private Context context;

    private static String DEFAULT_HOST = "10.0.2.2";
    private static int DEFAULT_PORT = 10389;
    private static SearchScope DEFAULT_SCOPE = SearchScope.SUB;

    AsyncLDAPSearch(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... args) {
        String imei = args[0];

        String username = "";

        try {
            Log.i("doInBG", "Trying...");
            // binding to DN
            LDAPConnection conn = new LDAPConnection(DEFAULT_HOST, DEFAULT_PORT);
            // search imei registered on DN
            SearchResult searchResult = conn.search("ou=users,dc=example,dc=com", DEFAULT_SCOPE, "(uid=" + imei + ")");

            for (SearchResultEntry entry : searchResult.getSearchEntries()) {
                // log entries with imei registered
                Log.i("DN", entry.getDN());
                // split entries with coma
                String[] attributeList = entry.getDN().split(",");
                // take only the username, on second coma with the start of array 3 (cn=tesla)
                //                                                                   0123
                String userDN = attributeList[2].substring(3);
                // search l=connected with sn=username
                SearchResult searchResult2 = conn.search("ou=users,dc=example,dc=com", DEFAULT_SCOPE, "(&(l=connected)(sn=" + userDN + "))");
                Log.i("Connected count", String.valueOf(searchResult2.getEntryCount()));

                // the first founded entry is the taken username, and then done
                if (searchResult2.getEntryCount() == 1) {
                    username = userDN;
                    break;
                }
            }
        } catch (LDAPException e) {
            Log.i("doInBG", "ga bisa konek LDAP bro");
            e.printStackTrace();
        }

        // return the username, then move from this method to next method (onPostExecute)
        return username;
    }

    @Override
    protected void onPostExecute(String result) {
        Intent intent = new Intent(context, SignInActivity.class);

        // if username had passed not empty, go to index
        if (!"".equals(result)) {
            intent = new Intent(context, MainActivity.class);
            intent.putExtra("username", result);
        }

        // aplikasi yang jalanin activity bukan activity lain dalam aplikasi
        context.startActivity(intent);
    }
}
