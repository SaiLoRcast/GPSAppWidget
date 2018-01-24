package com.polygalov.gpsappwidget;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

/**
 * Created by Константин on 24.01.2018.
 */

public class MapInFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnCameraChangeListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MapInFragment.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    public static final int ALERT_ADDRESS_RESULT_RECIVER = 0;
    public static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;

    private Context mContext;

    // Клиент Google для взаимодействия с Google API
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LatLng userPosition;

    //FragmentMap UI
    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;

    private Snackbar mSnackBar;
    private Snackbar mSnackBarPermisions;

    public MapInFragment() {
        //Должен быть паблик пустой конструктор
    }

    public static MapInFragment newInstance() {
        MapInFragment fragment = new MapInFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(mContext);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.map_fragment, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMapView = mView.findViewById(R.id.map);

        if (checkPlayServices()) {

            buildGoogleApiClient();

            if (mMapView != null) {
                mMapView.onCreate(null);
                mMapView.onResume();
                mMapView.getMapAsync(this);
            }
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleMap == null) {

            if (checkPlayServices()) {

                if (mGoogleApiClient == null) {

                    buildGoogleApiClient();
                }

                if (mMapView != null) {
                    mMapView.onCreate(null);
                    mMapView.onResume();
                    mMapView.getMapAsync(this);
                }
            }
        } else {
            if (!mayRequestLocation()) {
                return;
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this
                ).addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();
        int result = googleApi.isGooglePlayServicesAvailable(mContext);
        if (result != ConnectionResult.SUCCESS) {
            if (googleApi.isUserResolvableError(result)) {
                googleApi.getErrorDialog(getActivity(), result,
                        PLAY_SERVICES_RESOLUTION_REQUEST);
            }
            return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(final GoogleMap googleMap) {

        MapsInitializer.initialize(mContext);

        mGoogleMap = googleMap;

        setUpGoogleMap();

        mGoogleApiClient.connect();

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        Log.i(TAG, "Change Camera to Latitude:" + cameraPosition.target.latitude + " Longitude: " + cameraPosition.target.longitude);

        userPosition = new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
    }

    private void getUserLocation() {
        if (!mayRequestLocation()) {

            userPosition = new LatLng(34.0089919, -118.4996126);
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, Constants.USER_ZOOM));

            return;
        }

        if (ContextCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        if (mLastLocation != null) {

            userPosition = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, Constants.USER_ZOOM));

        } else {
            showAlert(ALERT_ADDRESS_RESULT_RECIVER);
        }
    }

    private boolean mayRequestLocation() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

            if (!mGoogleMap.isMyLocationEnabled()) {
                mGoogleMap.setMyLocationEnabled(true);
            }

            return true;
        }

        if (ContextCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (!mGoogleMap.isMyLocationEnabled()) {
                mGoogleMap.setMyLocationEnabled(true);
            }
            return true;
        }

        if (shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION)) {

            if (mSnackBarPermisions == null) {
                mSnackBarPermisions = Snackbar.make(mView, R.string.permission_access_coarse_location, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.action_snack_permission_access_coarse_location, new View.OnClickListener() {
                            @Override
                            @TargetApi(Build.VERSION_CODES.M)
                            public void onClick(View v) {
                                requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
                            }
                        });
                mSnackBarPermisions.show();
            } else {
                if (!mSnackBarPermisions.isShown()) {
                    mSnackBarPermisions.show();
                }
            }

        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (requestCode == REQUEST_PERMISSION_ACCESS_COARSE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            }
        } else {
            if (mSnackBarPermisions == null) {
                mSnackBarPermisions = Snackbar.make(mView, R.string.permission_access_coarse_location, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.action_snack_permission_access_coarse_location, new View.OnClickListener() {
                            @Override
                            @TargetApi(Build.VERSION_CODES.M)
                            public void onClick(View v) {
                                requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
                            }
                        });

            }
        }


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getUserLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    public void setUpGoogleMap() {

        if (mGoogleMap != null) {

            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
            mGoogleMap.getUiSettings().setCompassEnabled(false);
            mGoogleMap.setOnCameraChangeListener(this);
        }
    }

    public void showAlert(final int action) {

        String title, message, positive_btn_title, negative_btn_title;

        if (action == ALERT_ADDRESS_RESULT_RECIVER) {
            title = mContext.getResources().getString(R.string.location_alert_title);
            message = mContext.getResources().getString(R.string.location_alert_message);
            positive_btn_title = mContext.getResources().getString(R.string.location_alert_pos_btn);
            negative_btn_title = mContext.getResources().getString(R.string.location_alert_neg_btn);
        } else {
            title = mContext.getResources().getString(R.string.permission_alert_title);
            message = mContext.getResources().getString(R.string.permission_alert_message);
            positive_btn_title = mContext.getResources().getString(R.string.open_location_settings);
            negative_btn_title = mContext.getResources().getString(R.string.location_alert_neg_btn);
        }

        final AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setPositiveButton(positive_btn_title, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub

                if (action == 0) {
                    getUserLocation();
                } else {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    mContext.startActivity(myIntent);
                }

            }
        });

        dialog.setNegativeButton(negative_btn_title, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub
            }
        });
        dialog.show();
    }

}
