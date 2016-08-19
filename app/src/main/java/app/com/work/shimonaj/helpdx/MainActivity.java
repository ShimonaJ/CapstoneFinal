package app.com.work.shimonaj.helpdx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.LoaderManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdSize;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import app.com.work.shimonaj.helpdx.data.TicketAdapter;
import app.com.work.shimonaj.helpdx.data.TicketLoader;
import app.com.work.shimonaj.helpdx.data.UpdaterService;
import app.com.work.shimonaj.helpdx.remote.Config;
import app.com.work.shimonaj.helpdx.remote.RemoteEndpointUtil;
import app.com.work.shimonaj.helpdx.sync.TicketSyncAdapter;
import app.com.work.shimonaj.helpdx.util.Utility;

public class MainActivity extends AppCompatActivity
        implements
        LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = "MainActivity";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    public static  String SELECTED_LIST_POS_KEY="SelectedListPos";
    public  int mPosition;
    Tracker mTracker;

    private BroadcastReceiver messageReciever;
    AdView mAdView;
    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        Log.i(TAG, "Setting screen name: Main Activity" );
        mTracker.setScreenName("Main Activity" );
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        TicketSyncAdapter.initializeSyncAdapter(this);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


                Context context = getApplicationContext();
                Intent intent = new Intent(context, CreateTicketActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

//                    host.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(
//                            host, holder.thumbnailView, holder.thumbnailView.getTransitionName()).toBundle());
            }
        });

        MobileAds.initialize(getApplicationContext(),getString( R.string.banner_ad_unit_id));


        messageReciever = new MessageReciever();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReciever, filter);
        if (findViewById(R.id.ticket_detail_container) != null) {

            Config.mTwoPane = true;
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
               refresh();
            }
        });
        refresh();
        getLoaderManager().initLoader(0, null, this);

        if(savedInstanceState!=null && savedInstanceState.containsKey(SELECTED_LIST_POS_KEY)){
            mPosition=savedInstanceState.getInt(SELECTED_LIST_POS_KEY);
        }
        else{
            String position =Utility.getValFromSharedPref(this,MainActivity.SELECTED_LIST_POS_KEY);
            if(position!=""){
                mPosition=  Integer.parseInt(position);
            }else
            if(getIntent().getExtras()!=null){
                Bundle params =getIntent().getExtras();
                mPosition=  params.getInt(MainActivity.SELECTED_LIST_POS_KEY);
            }
        }
    }
    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onPause() {

        super.onPause();
    }
    @Override
    protected void onDestroy() {

        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReciever);
        super.onDestroy();
    }

    private void refresh() {

        mIsRefreshing=true;
        updateRefreshingUI();

        Intent intent = new Intent(this, UpdaterService.class);

        intent.setAction(UpdaterService.BROADCAST_ACTION_STATE_CHANGE);
        startService(intent);

      //  startService(new Intent(this, UpdaterService.class));
    }
    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }


    private boolean mIsRefreshing = false;

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return TicketLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        TicketAdapter adapter = new TicketAdapter(cursor,this);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(lm);
        if(mPosition!= RecyclerView.NO_POSITION){

            mRecyclerView.scrollToPosition(mPosition);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mPosition!=RecyclerView.NO_POSITION) {
            outState.putInt(SELECTED_LIST_POS_KEY, mPosition);
        }
        //  outState.putParcelableArrayList(MOVIE_KEY, (ArrayList<? extends Parcelable>) movies);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_logout) {
           // RemoteEndpointUtil.onLogout();
            Intent intent = new Intent(this, UpdaterService.class);

            intent.setAction(UpdaterService.LOGOUT);
            startService(intent);

            Intent loginscreen=new Intent(this,LoginPageActivity.class);
            loginscreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginscreen);
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
private void loadAd(){
    AdView mAdView = (AdView) findViewById(R.id.adView);
    AdRequest adRequest = new AdRequest.Builder().build();
    mAdView.loadAd(adRequest);

}
    private class MessageReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra(MESSAGE_KEY) != null) {
                Snackbar.make(getCurrentFocus(),  intent.getStringExtra(MESSAGE_KEY), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                loadAd();

            }
        }
    }
}
