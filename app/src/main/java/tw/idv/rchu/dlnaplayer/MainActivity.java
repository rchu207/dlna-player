package tw.idv.rchu.dlnaplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.io.IOException;

import fi.iki.elonen.SimpleWebServer;
import tw.idv.rchu.dlnaplayer.upnp.Upnp;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        FileFragment.OnListFragmentInteractionListener {
    private static final String TAG = "[DLNA]Main";
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private SimpleWebServer mServer;
    private String mHost;
    private int mPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Fragment fragment = FileFragment.newInstance(1);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.media_fragment, fragment, FileFragment.TAG);
        transaction.commit();

        startWebService();
        Upnp.getInstance().init();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {

        } else if (id == R.id.nav_gallery) {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "sample.1080P.mp4");
            Fragment fragment = MediaControllerFragment.newInstance(mHost, mPort, file);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.media_fragment, fragment);
            transaction.commit();
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Request run-time permissions.
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            onListFragmentInteraction(FileContent.ITEM);
        } else {
            // Don't show explanation, request the permission directly.
            Log.d(TAG, "Request WRITE_EXTERNAL_STORAGE permission.");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onDestroy() {
        stopWebService();
        Upnp.getInstance().destroy();

        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onListFragmentInteraction(FileContent.ITEM);
        }
    }

    public void startWebService() {
        stopWebService();

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(
                AppCompatActivity.WIFI_SERVICE);
        mHost = Utils.getNetworkIp(wifiManager.getConnectionInfo());
        mPort = 8030;

        // Start DMS http server.
        mServer = new SimpleWebServer(mHost, mPort, new File("/"), true);
        int tryCount = 0;
        while (!mServer.isAlive()) {
            try {
                mServer.start();
                break;
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                mServer = null;
                if (tryCount > 10) {
                    break;
                }

                mPort++;
                mServer = new SimpleWebServer(mHost, mPort, new File("/"), true);
                tryCount++;
            }
        }
    }

    public void stopWebService() {
        // Stop DMS http server.
        if (mServer != null) {
            mServer.stop();
            mServer = null;
        }
    }

    @Override
    public void onListFragmentInteraction(FileContent.FileItem item) {
        if (item.content.getAuthority().equals(FileContent.AUTHORITY_FILE)) {
            // Open file in the MediaController fragment.
            File file = new File(item.content.getPath());
            Fragment fragment = MediaControllerFragment.newInstance(mHost, mPort, file);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.media_fragment, fragment, MediaControllerFragment.TAG);
            transaction.commit();
        } else {
            FileFragment fileFragment = (FileFragment) getSupportFragmentManager().findFragmentByTag(FileFragment.TAG);
            fileFragment.setRoot(item);
        }
    }
}
