package br.com.renanbandeira.mta_atm_maps;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
    GoogleMap.OnCameraIdleListener {

  private GoogleMap mMap;

  private LatLng mPosition;

  private Address mAddress;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_maps);
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
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
      snippet = mAddress.getFeatureName()
          + ", " + mAddress.getSubThoroughfare() + " - "
          + mAddress.getSubLocality()
          + " - "
          + mAddress.getSubAdminArea()
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
}
