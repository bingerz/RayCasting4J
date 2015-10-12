package com.binger.raycasting4j;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.Geometry;
import com.cocoahero.android.geojson.Polygon;
import com.cocoahero.android.geojson.Position;
import com.cocoahero.android.geojson.Ring;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.MapViewListener;

import org.json.JSONException;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;

    private Marker mMarker;

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

        initMapView();
        setMapViewListener();
    }

    private void initMapView() {
        mMapView = (MapView) findViewById(R.id.mapview);
        mMapView.setMinZoomLevel(mMapView.getTileProvider().getMinimumZoomLevel());
        mMapView.setMaxZoomLevel(mMapView.getTileProvider().getMaximumZoomLevel());
        mMapView.setCenter(mMapView.getTileProvider().getCenterCoordinate());
        LatLng latLng = new LatLng(39.9385449, 116.1165825);
        mMapView.setCenter(latLng);
        mMapView.setZoom(4);
    }

    private void setMapViewListener() {
        mMapView.setMapViewListener(new MapViewListener() {
            @Override
            public void onShowMarker(MapView pMapView, Marker pMarker) {
            }

            @Override
            public void onHideMarker(MapView pMapView, Marker pMarker) {
            }

            @Override
            public void onTapMarker(MapView pMapView, Marker pMarker) {
            }

            @Override
            public void onLongPressMarker(MapView pMapView, Marker pMarker) {
            }

            @Override
            public void onTapMap(MapView pMapView, ILatLng pPosition) {
                mMapView.clear();
                LatLng latLng = new LatLng(pPosition.getLatitude(), pPosition.getLongitude());
                String result = printfCoordinates(latLng.getLatitude(), latLng.getLongitude());
                mMarker = new Marker(result, "", latLng);
                mMarker.setHotspot(Marker.HotspotPlace.CENTER);
                mMapView.addMarker(mMarker);
                ((TextView) findViewById(R.id.tv_hint)).setText(result);
            }

            @Override
            public void onLongPressMap(MapView pMapView, ILatLng pPosition) {
            }
        });
    }

    public String printfCoordinates(double lat, double lon) {
        RayCasting rayCasting = new RayCasting();
        StringBuffer sb = new StringBuffer();

        ArrayList<Vector> coordinates = readChinaCoordinates();
        boolean isOut = rayCasting.outOfChina(lat, lon, coordinates);
        sb.append("<" + lat + " " + lon + ">:" + !isOut);
        sb.append("\n");
        return sb.toString();
    }

    private ArrayList<Vector> readChinaCoordinates() {
        StringBuilder text = new StringBuilder();
        BufferedReader reader = null;
        ArrayList<Vector> vectors = new ArrayList<>();

        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open("china.geojson"),
                    "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }

            FeatureCollection featureCollection = (FeatureCollection) GeoJSON
                    .parse(text.toString());

            Feature feature = featureCollection.getFeatures().get(0);
            Geometry geometry = feature.getGeometry();
            List<Ring> rings = ((Polygon) geometry).getRings();
            List<Position> positions = rings.get(0).getPositions();

            for (int i = 0; i < positions.size(); i++) {
                Position position = positions.get(i);
                Vector vec = new Vector(position.getLatitude(), position.getLongitude());
                vectors.add(vec);
            }
        } catch (IOException e) {
            //log the exception
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
            return vectors;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
