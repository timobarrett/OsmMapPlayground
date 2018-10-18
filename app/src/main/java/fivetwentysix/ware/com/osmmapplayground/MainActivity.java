package fivetwentysix.ware.com.osmmapplayground;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity  {
    private final String LOG_TAG = "osmmapplaygound - " + MainActivity.class.getSimpleName();
    private MapView mMapView;
    private Context mContext;
    private RoadManager roadManager;
    private  ArrayList<GeoPoint> runPoints;
    private List<Location> points;
    private MyLocationNewOverlay myLocationNewOverlay;
    private boolean mGpsPermissionGranted = false;
    static final int PERMISSION_REQUEST_GPS = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG,"IN onCreate");
        mContext = getApplicationContext();
        //starting lat and lon "42.8309512" lon="-71.5687185"
        File foo = Environment.getExternalStorageDirectory();
        //the line followingis key to getting location for maptiles.  Fails otherwise
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(mContext));
        setContentView(R.layout.activity_main);
        mMapView = (MapView)findViewById(R.id.mapRoute);

    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d(LOG_TAG,"IN onResume");
//        mMapView.onResume();
        mMapView.setTileSource(TileSourceFactory.MAPNIK);
        methodCreatingOverlay();
        myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(mContext),mMapView);
        mMapView.getOverlays().add(this.myLocationNewOverlay);
        loadPointsToMap();
    }

    @Override
    protected void onPause(){
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantedResults){
        if (requestCode == PERMISSION_REQUEST_GPS){
            if (grantedResults.length == 1 && grantedResults[0] == PackageManager.PERMISSION_GRANTED){
                mGpsPermissionGranted = true;
                Log.d(LOG_TAG,"Permission granted");
            }else{
                Log.d(LOG_TAG,"Permission not granted");
            }
        }
    }
    private void methodCreatingOverlay(){
        GeoPoint startGP = new GeoPoint(42.8309512,-71.5687185);
        IMapController mapController = mMapView.getController();
        roadManager = new OSRMRoadManager(this);
        GeoPoint midGP = new GeoPoint(42.8497926,-71.5823523);
        runPoints = new ArrayList<GeoPoint>();
        runPoints.add(startGP);
        runPoints.add(midGP);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Road road = roadManager.getRoad(runPoints);
                Polyline roadOverlay = RoadManager.buildRoadOverlay(road, Color.RED,12);
                mMapView.getOverlays().add(roadOverlay);
            }
        }).start();

        mapController.setCenter(midGP);
        mapController.setZoom(15.1);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);
//        mMapView.setMultiTouchControls(false);
    }

    private void loadPointsToMap(){
        Log.d(LOG_TAG,"IN loadPointsToMap");
        //reveals 13 items in points array
        getSplitsData();
        Log.d(LOG_TAG,"After loading GPX Data - "+points.size());
        ArrayList<OverlayItem>splitpoints = new ArrayList<OverlayItem>();
        Drawable myMarkerOut = mContext.getResources().getDrawable(R.drawable.ic_baseline_place_24px,null);
        Drawable myMarkerBack = mContext.getResources().getDrawable(R.drawable.ic_baseline_place_24px,null);;
        myMarkerOut.setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
        myMarkerBack.setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
        int splitHalf = points.size()/4;

        int index = 1;
        for(Location locate:points){
            if (index % 2 == 0) {
                OverlayItem overlayItem = new OverlayItem("mile ", Integer.toString(index), new GeoPoint(locate.getLatitude(), locate.getLongitude()));
                if (index <= splitHalf) {
                    overlayItem.setMarker(myMarkerOut);
                }else { overlayItem.setMarker(myMarkerBack);}
                splitpoints.add(overlayItem);
//                splitpoints.add(new OverlayItem("mile ", Integer.toString(index), new GeoPoint(locate.getLatitude(), locate.getLongitude())));
            }
            index++;
        }
      /* caused by a null marker due to bad png format
java.lang.RuntimeException: Unable to resume activity {fivetwentysix.ware.com.osmmapplayground/fivetwentysix.ware.com.osmmapplayground.MainActivity}:
java.lang.IllegalArgumentException: You must pass a default marker to ItemizedOverlay.*/
        //Either of these work - commented or uncommented
//        ItemizedOverlayWithFocus<OverlayItem> splitsOverlay = new ItemizedOverlayWithFocus<OverlayItem>(splitpoints,myMarker,myMarker,Color.BLUE,
        ItemizedOverlayWithFocus<OverlayItem> splitsOverlay = new ItemizedOverlayWithFocus<OverlayItem>(mContext,splitpoints,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        Log.d(LOG_TAG,"Single Tap");
                        return true;
                    }
                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        Log.d(LOG_TAG,"Long press");
                        return false;
                    }
                });
        mMapView.getOverlays().add(splitsOverlay);
    }

    /**
     * This will just focus on the run.gpx file
     */
    private void getSplitsData(){
        Log.d(LOG_TAG,"IN addSplitsToMap");
        FileInputStream fileInputStream = null;
        points = null;
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            try {
                fileInputStream = mContext.openFileInput("run.gpx");
            } catch (FileNotFoundException e) {
                System.out.println("ERROR FILE NOT FOUND");
            }
            Document dom = builder.parse(fileInputStream);
            Element root = dom.getDocumentElement();
            NodeList items = root.getElementsByTagName("trkpt");
            points = new ArrayList<Location>();
            for(int j = 0; j < items.getLength(); j++)
            {
                Node item = items.item(j);
                NamedNodeMap attrs = item.getAttributes();
                NodeList props = item.getChildNodes();

                Location pt = new Location("test");

                pt.setLatitude(Double.parseDouble(attrs.getNamedItem("lat").getTextContent()));
                pt.setLongitude(Double.parseDouble(attrs.getNamedItem("lon").getTextContent()));

                points.add(pt);
            }
            fileInputStream.close();
        }

        catch(FileNotFoundException  e)
        {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        catch(ParserConfigurationException ex)
        {

        }

        catch (SAXException ex) {
        }
    }

    private void addingMarkersToMap(ArrayList<OverlayItem>splitpoints){
        ItemizedOverlayWithFocus<OverlayItem> splitsOverlay = new ItemizedOverlayWithFocus<OverlayItem>(mContext,splitpoints,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        Log.d(LOG_TAG,"Single Tap");
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        Log.d(LOG_TAG,"Long press");
                        return false;
                    }
                });
        mMapView.getOverlays().add(splitsOverlay);
    }

//    public class itemizedOverlay extends ItemizedOverlay{
//        @Override
//        private boolean itemGestureListener(final int index, final OverlayItem item){
//
//        }
//    }

}
