package app.com.work.shimonaj.helpdx;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONException;
import org.json.JSONObject;

import app.com.work.shimonaj.helpdx.data.UpdaterService;
import app.com.work.shimonaj.helpdx.remote.Config;
import app.com.work.shimonaj.helpdx.util.Utility;

public class CreateTicketActivity extends AppCompatActivity  {
    private static final String TAG = CreateTicketActivity.class.getName();
    Tracker mTracker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utility.putKeyValInSharedPref(this,Config.TicketId,"0");
        Utility.putKeyValInSharedPref(this,MainActivity.SELECTED_LIST_POS_KEY,"0");
        setContentView(R.layout.activity_create_ticket);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Button btn =(Button)this.findViewById(R.id.create_ticket_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCreateTicketClick(view);
            }

        });
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        Log.i(TAG, "Setting screen name: Create Ticket" );
        mTracker.setScreenName("Create Ticket" );
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

    }

    public void onCreateTicketClick(View view) {
        Toast.makeText(this, getResources().getString(R.string.notification_ticket_post), Toast.LENGTH_LONG).show();
        Log.v(TAG,"Button click");
        JSONObject userObj = Utility.getUserInfo(getApplicationContext());
        String EmpId="";
        String CompanyId="";
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Ticket Created")
                .setAction("Share")
                .build());

        try{
            CompanyId = userObj.getString("CompanyId");
            EmpId = userObj.getString("EmployeeId");
        }catch (JSONException e){
            e.printStackTrace();
        }

        JSONObject json = new JSONObject();
        JSONObject ticket = new JSONObject();
        try {
            ticket.putOpt("Title",((TextView)this.findViewById(R.id.addTicketTitle)).getText());
            ticket.putOpt("Description",((TextView)this.findViewById(R.id.addTicketDesc)).getText());
            ticket.putOpt("TicketSource","Android App");
            ticket.putOpt("CompanyId",CompanyId);
            ticket.put("RequestorId", EmpId);

            json.put("EmployeeId", EmpId);
            json.putOpt("ticket",ticket);
            json.put("hostname","");
            json.put("tokenKey","");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, UpdaterService.class);
        intent.putExtra("ticketData",json.toString());
        intent.setAction(UpdaterService.TICKET_POST);
        startService(intent);

        Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainActivityIntent);
    }


}
