package de.applicatum.shoprouter.ui.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import de.applicatum.shoprouter.Application;
import de.applicatum.shoprouter.R;
import de.applicatum.shoprouter.model.Shops.Shop;
import de.applicatum.shoprouter.model.Shops.ShopsController;
import de.applicatum.shoprouter.ui.MainActivity;
import de.applicatum.shoprouter.utils.AppLog;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

import static android.content.Context.LOCATION_SERVICE;


public class GlobalMapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = "GlobalMapFragment";

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private MainActivity activity;
    LayoutInflater inflater;
    private View rootView;

    private ArrayList<Shop> shops;

    public GlobalMapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.inflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_map, container, false);

        shops = ShopsController.getInstance().getShops();
        activity = (MainActivity) getActivity();

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //getMapAsync((MainActivity) getActivity());
        SupportMapFragment smf = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));

//        SupportMapFragment smf = new SupportMapFragment();
//
//        FragmentManager childFragMan = getChildFragmentManager();
//
//        FragmentTransaction childFragTrans = childFragMan.beginTransaction();
//        childFragTrans.add(R.id.fragment_content, smf, "SupportMapFragment");
//        childFragTrans.addToBackStack("SupportMapFragment");
//        childFragTrans.commit();

        activity.setTitle("Global Map");
        smf.getMapAsync(this);
    }

    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        try {
            Fragment fragment = (getChildFragmentManager()
                    .findFragmentById(R.id.map));
            FragmentTransaction ft = activity.getSupportFragmentManager()
                    .beginTransaction();
            if (fragment != null) {
                ft.remove(fragment);
                ft.commit();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        for (Shop shop : shops) {
            LatLng latLng = new LatLng(shop.getLatitude(), shop.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(shop.getName());
            markerOptions.snippet(shop.getAddress());
            mMap.addMarker(markerOptions);
        }
        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                alert.setTitle("New Shop");

                LayoutInflater inflater = activity.getLayoutInflater();
                final View view = inflater.inflate(R.layout.view_dialog_new_shop, null);
                final EditText input = (EditText) view.findViewById(R.id.text);
                final EditText address = (EditText) view.findViewById(R.id.address);
                alert.setView(view);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String title = input.getText().toString();
                        String addressString = address.getText().toString();
                        if (title.length() == 0)
                            return;
                        if (addressString.length() == 0)
                            return;
                        createShop(title, addressString, latLng);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

                alert.show();
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                LatLng latLng = marker.getPosition();
                Shop shop = ShopsController.getInstance().getShopAtPosition(latLng.latitude, latLng.longitude);
                if(shop != null){
                    if (shop.getWidth()>0 && shop.getHeight()>0) {
                        ShopNavigationFragment fragment = new ShopNavigationFragment();
                        fragment.setShop(shop);
                        activity.startFragment(fragment, true, 0);
                    } else {
                        startShopEditFragment(shop);
                    }
                }
            }
        });
    }

    private void createShop(String title, String address, LatLng latLng) {

        double lat = latLng.latitude;
        double lng = latLng.longitude;
        Shop shop = new Shop(title, lat, lng, address);
        shop.save((Application) activity.getApplication());
        ShopsController.getInstance().addShop(shop);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(title);
        markerOptions.snippet(address);
        //mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.addMarker(markerOptions);

        startShopEditFragment(shop);
    }

    private void startShopEditFragment(Shop shop){
        ShopEditFragment fragment = new ShopEditFragment();
        fragment.setShop(shop);
        activity.startFragment(fragment, true, 0);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location != null) {
                AppLog.d(TAG, "onConnected", "location is not null");
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,
                        13));
            } else {
                AppLog.d(TAG, "onConnected", "location is null");
            }
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        AppLog.d(TAG, "onConnectionSuspended", "location is null");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        AppLog.d(TAG, "onConnectionFailed", "location is null");
    }

    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        MyInfoWindowAdapter(){
            myContentsView = inflater.inflate(R.layout.view_marker_info_window, null);
        }

        @Override
        public View getInfoContents(Marker marker) {

            Shop shop = ShopsController.getInstance().getShopAtPosition(marker.getPosition().latitude, marker.getPosition().longitude);
            if(shop != null){
                TextView tvTitle = ((TextView)myContentsView.findViewById(R.id.textName));
                tvTitle.setText(shop.getName());
                TextView tvSnippet = ((TextView)myContentsView.findViewById(R.id.textAddress));
                tvSnippet.setText(shop.getAddress());
                MaterialRatingBar ratingBar = (MaterialRatingBar) myContentsView.findViewById(R.id.ratingBar);
                ratingBar.setProgress(shop.getRating());
            }


            return myContentsView;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // TODO Auto-generated method stub
            return null;
        }

    }
}
