package com.example.reusisam;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;

public class MapaFragment extends Fragment {

    private static final String USER_AGENT = "BarberApp-DAM235/1.0 (proyecto estudiantil UES; Android)";
    private static final double LATITUD = 13.6929;
    private static final double LONGITUD = -89.2182;

    private MapView mapView;

    public MapaFragment() {
        // Constructor vacío requerido
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        // OSM bloquea los User-Agent genéricos (ej. el nombre del paquete); usar uno propio.
        Configuration.getInstance().setUserAgentValue(USER_AGENT);
        Configuration.getInstance().setOsmdroidBasePath(new File(requireContext().getCacheDir(), "osmdroid-v2"));
        Configuration.getInstance().setOsmdroidTileCache(new File(requireContext().getCacheDir(), "osmdroid-v2/tiles"));

        View view = inflater.inflate(R.layout.fragment_mapa, container, false);

        mapView = view.findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        GeoPoint punto = new GeoPoint(LATITUD, LONGITUD);
        mapView.getController().setZoom(14.0);
        mapView.getController().setCenter(punto);

        Marker marcador = new Marker(mapView);
        marcador.setPosition(punto);
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marcador.setTitle("San Salvador, El Salvador");
        mapView.getOverlays().add(marcador);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) {
            mapView.onDetach();
            mapView = null;
        }
    }
}
