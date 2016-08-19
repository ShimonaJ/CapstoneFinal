package app.com.work.shimonaj.helpdx;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.common.Util;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabClickListener;

import app.com.work.shimonaj.helpdx.data.TicketLoader;
import app.com.work.shimonaj.helpdx.data.UpdaterService;
import app.com.work.shimonaj.helpdx.remote.Config;
import app.com.work.shimonaj.helpdx.util.Utility;

/**
 * An activity representing a single Ticket detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MainActivity}.
 */
public class TicketDetailActivity extends AppCompatActivity implements     LoaderManager.LoaderCallbacks<Cursor> {
   // private boolean mIsRefreshing = false;
    private static final int TICKET_DETAIL_LOADER=1;
    Tracker mTracker;
    private TicketCommentsFragment commentFragment;
    private ReplyActivity replyFragment;
    private  Bundle arguments ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ticket_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTracker =((AnalyticsApplication) getApplication()).getDefaultTracker();

        mTracker.setScreenName("Ticket Detail Activity" );
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        if (savedInstanceState == null) {
            String ticketId = Utility.getValFromSharedPref(this,Config.TicketId);

            Intent intent = new Intent(this, UpdaterService.class);
            intent.putExtra("ticketId", Long.parseLong(ticketId));
            intent.setAction(UpdaterService.TICKET_DETAIL);
            startService(intent);
            Toast.makeText(this, "Fetching Latest Ticket Details .", Toast.LENGTH_LONG).show();
        }


        getLoaderManager().initLoader(TICKET_DETAIL_LOADER, null, this);
        commentFragment = new TicketCommentsFragment();
        // commentFragment.setArguments(arguments);

       // TicketDetailFragment fragment = new TicketDetailFragment();

        // fragment.setArguments(arguments);
        getFragmentManager().beginTransaction()
         //       .add( fragment,"ticketDetailFrag")
                .add( R.id.ticket_comment_container,commentFragment)
                .commit();
    }
//public void onReplyDone(String ticketId){
//    Intent intent = new Intent(this, TicketDetailActivity.class);
//    intent.putExtra(TicketDetailFragment.TicketId, ticketId);
//
//    //   intent.putExtra(TicketDetailFragment._Id,  mCursor.getString(TicketLoader.Query._ID));
//
//    startActivity(intent);
//this.finish();
//}
@Override
public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
    String ticketId = Utility.getValFromSharedPref(this,Config.TicketId);
    if(ticketId!="") {
        long l = Long.parseLong(ticketId);
        return TicketLoader.newInstanceForItemId(this, l);
    }
    return null;
}

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if(!cursor.moveToFirst()){return ;}

        TextView titleView =(TextView)this.findViewById(R.id.detail_title);
       titleView.setText(  "#"+cursor.getString(TicketLoader.Query.TICKETID)+" "+ cursor.getString(TicketLoader.Query.TITLE));
       // getSupportActionBar().setTitle( "#"+cursor.getString(TicketLoader.Query.TICKETID)+" "+ cursor.getString(TicketLoader.Query.TITLE));
      TextView subTitleView =(TextView)this.findViewById(R.id.detail_subtitle);
        String subtitle = cursor.getString(TicketLoader.Query.REQUESTEDBY)+" reported on "+ cursor.getString(TicketLoader.Query.CREATEDON)+" via "+ cursor.getString(TicketLoader.Query.SOURCE);
       // getSupportActionBar().setSubtitle(subtitle);
        subTitleView.setText(  subtitle);
       // subTitleView.setVisibility(View.VISIBLE);
//
        TextView descView =(TextView)this.findViewById(R.id.detail_desc);
        descView.setTypeface(Utility.mediumRobotoFont);
        descView.setText(  cursor.getString(TicketLoader.Query.DESCRIPTION));
        descView.setVisibility(View.VISIBLE);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id== android.R.id.home){
            goToMainActivity();
            return true;
        }
        if (id == R.id.action_reply) {
            Intent replyActivityIntent = new Intent(getApplicationContext(), ReplyActivity.class);
           // String ticketId = Utility.getValFromSharedPref(this,TicketDetailFragment.TicketId);
           // replyActivityIntent.putExtra(TicketDetailFragment.TicketId,arguments.getString(TicketDetailFragment.TicketId) );

            startActivity(replyActivityIntent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ticket_detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    private void goToMainActivity(){
            Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);

               // mainActivityIntent.putExtras(arguments);
                startActivity(mainActivityIntent);
    }

}
