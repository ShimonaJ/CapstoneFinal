package app.com.work.shimonaj.helpdx;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class ReplyActivity extends AppCompatActivity  {
    private static final String TAG = ReplyActivity.class.getName();
    Tracker mTracker;

    public ReplyActivity() {
        // Required empty public constructor

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reply_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }



        @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:goToTicketDetailView(); return true;
            case R.id.action_reply:
                    doReply(0);return true;

            case R.id.action_reply_close:
                    doReply(1);return true;
            case R.id.action_cancel:
                goToTicketDetailView();

                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    //private String ticketId="";
private void doReply(int actionType){
    Toast.makeText(this,     getResources().getString(R.string.notification_reply_post)
            , Toast.LENGTH_LONG).show();

    JSONObject userObj = Utility.getUserInfo(this);
    String EmpId="";

    String ticketId = Utility.getValFromSharedPref(this, Config.TicketId);
    try{

       // ticketId = getArguments().getString(TicketDetailFragment.TicketId);
        EmpId = userObj.getString("EmployeeId");
    }catch (JSONException e){
        e.printStackTrace();
    }
    JSONObject json = new JSONObject();

    try {

        json.put("ticketId", ticketId);
        json.put("IsPrivate", 0);
        json.put("actionType", actionType);
        json.put("EmployeeId", EmpId);
        json.put("comment",((TextView)findViewById(R.id.replyText)).getText());
        json.put("tokenKey","");
    } catch (JSONException e) {
        e.printStackTrace();
    }
   // registerReceiver(mRefreshingReceiver,
   //         new IntentFilter(UpdaterService.TICKET_COMMENT_POST));
    Intent intent = new Intent(this, UpdaterService.class);
    intent.putExtra("commentData",json.toString());
    intent.setAction(UpdaterService.TICKET_COMMENT_POST);
    startService(intent);
    goToTicketDetailView();

}

    private void goToTicketDetailView(){
        Intent detailActivityIntent = new Intent(getApplicationContext(), TicketDetailActivity.class);

        startActivity(detailActivityIntent);
        this.finish();
    }



    @Override
    public void onBackPressed() {
        goToTicketDetailView();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_reply);
        mTracker =((AnalyticsApplication) getApplication()).getDefaultTracker();

        mTracker.setScreenName("Reply Activity" );
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


}
