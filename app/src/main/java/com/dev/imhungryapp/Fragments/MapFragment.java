package com.dev.imhungryapp.Fragments;

import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.provider.Telephony;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dev.imhungryapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;



public class MapFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.my_map);
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng latLng) {
//                        Location targetLocation = new Location("");//provider name is unnecessary
//                        targetLocation.setLatitude(45.767165789060186d);//your coords of course
//                        targetLocation.setLongitude(21.228271498200456d);
                        MarkerOptions markerOptions = new MarkerOptions();
                        latLng = new LatLng(45.76696846074291, 21.22826730463397);
                        markerOptions.position(latLng);
                        markerOptions.title(latLng.latitude + " KG " + latLng.latitude);
                        googleMap.clear();
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 70));
                        googleMap.addMarker(markerOptions);
                        //45.767165789060186, 21.228271498200456
                    }
                });
            }
        });

        return view;
    }
}