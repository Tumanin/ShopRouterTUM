package de.applicatum.shoprouter.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Iterator;

import de.applicatum.shoprouter.Application;
import de.applicatum.shoprouter.R;
import de.applicatum.shoprouter.model.Products.ShoppingListItem;
import de.applicatum.shoprouter.ui.fragments.GlobalMapFragment;
import de.applicatum.shoprouter.ui.fragments.MyShoppingListsFragment;
import de.applicatum.shoprouter.ui.fragments.ProductListFragment;
import de.applicatum.shoprouter.utils.AppLog;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    public static final String TAG = "MainActivity";
    private GoogleMap mMap;
    private Toolbar toolbar;
    DrawerLayout drawer;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ((Application)getApplication()).loadData();



        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        loadProfile();
        MyShoppingListsFragment fragment = new MyShoppingListsFragment();
        startFragment(fragment, false, 0);
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

    private void loadProfile(){

        SharedPreferences pref = this.getSharedPreferences("ShopRouterPref", Context.MODE_PRIVATE);
        String name = pref.getString("username", "");
        String userId = pref.getString("userId", "");

        AppLog.d(TAG, "loadProfile", "name: "+name+"; userId: "+userId);
        View headerLayout = navigationView.getHeaderView(0);
        TextView username = (TextView) headerLayout.findViewById(R.id.username);

        ProfilePictureView profilePictureView = (ProfilePictureView) headerLayout.findViewById(R.id.imageView);

        if (!name.equals("")) {
            username.setText(name);

        } else {
            username.setText("Gast");
        }
        profilePictureView.setProfileId(userId);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void setTitle(String title){
        toolbar.setTitle(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_shopping_lists) {
            // Handle the camera action
            MyShoppingListsFragment fragment = new MyShoppingListsFragment();
            startFragment(fragment, false, 0);
        } else if (id == R.id.nav_product_list) {
            ProductListFragment fragment = new ProductListFragment();
            startFragment(fragment, true, 0);
        } else if (id == R.id.nav_shopping) {
            GlobalMapFragment fragment = new GlobalMapFragment();
            startFragment(fragment, true, 0);
        } else if (id == R.id.logout) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("Sicher? Abmelden?");
            alert.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    logout();
                }
            });
            alert.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            alert.show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public void startFragment(Fragment fragment, boolean addToBackStack, int appearance) {



        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(appearance == 0){
            //fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        if(appearance == 1){
            //fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
        if(appearance == -1){
            //fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
        }

        fragmentTransaction.replace(R.id.content_main, fragment);

        if(addToBackStack) {
            fragmentTransaction.addToBackStack("frame");
        }
        else {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        fragmentTransaction.commit();
    }

    private void logout() {
        Application application = (Application) getApplication();
        application.logout();
    }

    private Query getQuery() {
        Application application = (Application) getApplication();
        Query query = application.getUserProfilesView().createQuery();
        return query;
    }
}
