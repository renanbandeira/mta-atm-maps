package br.com.renanbandeira.mta_atm_maps;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

  private GoogleMap mMap;

  private LocationRequest mLocationRequest;

  private LocationListener mLocationListener;

  private LatLng mPosition;

  private Address mAddress;

  private GoogleApiClient mGoogleApiClient;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_maps);
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
    mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API)
        .build();

    mLocationRequest = new LocationRequest();
    mLocationRequest.setInterval(10000);
    mLocationRequest.setSmallestDisplacement(50);
    mLocationRequest.setFastestInterval(5000);
    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    mLocationListener = new LocationListener() {
      @Override public void onLocationChanged(Location location) {
        goToLocation(location);
      }
    };
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
  @Override public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;

    // Add a marker in Fortaleza and move the camera
    LatLng fortaleza = new LatLng(-3.7319, -38.5267);
    mMap.addMarker(new MarkerOptions().position(fortaleza).title("Marker in Sydney"));
    mMap.animateCamera(CameraUpdateFactory.newLatLng(fortaleza));
    mMap.getUiSettings().setMyLocationButtonEnabled(true);
    mMap.getUiSettings().setZoomControlsEnabled(true);
    mMap.getUiSettings().setTiltGesturesEnabled(true);
    mMap.setOnCameraIdleListener(this);

    updateMyLocation();
  }

  private boolean hasPermission() {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      // TODO: Consider calling
      ActivityCompat.requestPermissions(this,
          new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 10);
      //    ActivityCompat#requestPermissions
      // here to request the missing permissions, and then overriding
      //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
      //                                          int[] grantResults)
      // to handle the case where the user grants the permission. See the documentation
      // for ActivityCompat#requestPermissions for more details.
      return false;
    }
    return true;
  }

  @Override public void onRequestPermissionsResult(int requestCode,
      @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == 10 && grantResults.length > 0 &&
        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      //Eu tenho a permissão do usuario
      if (mMap == null) return;
      updateMyLocation();
    }
  }

  private void goToLocation(Location location) {
    if (location == null) return;
    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
        new LatLng(location.getLatitude(),
            location.getLongitude()), 16
    ));
  }

  private void updateMyLocation() {
    if (!hasPermission()) return;
    mMap.setMyLocationEnabled(true);
    if (mGoogleApiClient.isConnected()) {
      Location location = LocationServices.FusedLocationApi
          .getLastLocation(mGoogleApiClient);
      goToLocation(location);
      LocationServices.FusedLocationApi
          .requestLocationUpdates(mGoogleApiClient,
              mLocationRequest, mLocationListener);
    }
  }

  @Override public void onCameraIdle() {
    if(mMap == null) {
      return;
    }
    mPosition = mMap.getCameraPosition().target;
    updateUI();
  }

  private Address getAddressFromLocation() {
    try {
      Geocoder geocoder = new Geocoder(this);
      List<Address> addresses = geocoder
          .getFromLocation(mPosition.latitude,
                        mPosition.longitude, 1);
      return addresses.isEmpty() ? null : addresses.get(0);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private void updateUI() {
    if (mMap == null ) return;
    mAddress = getAddressFromLocation();
    String snippet = "";
    if (mAddress != null) {
      EditText search = (EditText) findViewById(R.id.search);
      snippet = mAddress.getThoroughfare()
          + ", " + mAddress.getSubThoroughfare() + " - "
          + mAddress.getSubLocality()
          + " - "
          + mAddress.getLocality()
          + ", "
          + mAddress.getAdminArea();
      search.setText(snippet);
    }
    mMap.clear();
    mMap.addMarker(new MarkerOptions()
        .position(mPosition)
        .title("Marker")
        .snippet(snippet));
  }
  public void submit(View v) {
    EditText search = (EditText) findViewById(R.id.search);
    String query = search.getText().toString().trim();
    Address address = getAddress(query);
    if (address != null && mMap != null) {
      LatLng position = new LatLng(address.getLatitude(),
                              address.getLongitude());
      mMap.animateCamera(CameraUpdateFactory.newLatLng(position));
    }
  }
  public Address getAddress(String query) {
    try {
      Geocoder geocoder = new Geocoder(this);
      List<Address> addresses = geocoder.getFromLocationName(query, 1);
      return addresses.isEmpty() ? null : addresses.get(0);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override public void onConnected(@Nullable Bundle bundle) {
    updateMyLocation();
  }

  @Override protected void onStart() {
    super.onStart();
    mGoogleApiClient.connect();
  }

  @Override protected void onStop() {
    super.onStop();
    LocationServices.FusedLocationApi
        .removeLocationUpdates( mGoogleApiClient, mLocationListener);
    mGoogleApiClient.disconnect();
  }

  @Override public void onConnectionSuspended(int i) {

  }

  @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
  }
}
