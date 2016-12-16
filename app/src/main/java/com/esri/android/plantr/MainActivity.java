package com.esri.android.plantr;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.*;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.loadable.LoadStatusChangedEvent;
import com.esri.arcgisruntime.loadable.LoadStatusChangedListener;
import com.esri.arcgisruntime.location.LocationDataSource;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.*;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements AddPlantDialog.AddPlantDialogListener {

  private static final String TAG = "IndoorAtlasExample";

  private static final String PLANT_SPECIES = "PlantR_WFL - PLANT_SPECIES";
  private static final String PLANT_READINGS = "PlantR_WFL - READINGS";
  private ServiceFeatureTable mPlantTable = null;
  private ServiceFeatureTable mReadingsTable = null;
  private ServiceFeatureTable mPlantFeatureTable = null;
  private AddPlantDialog mAddPlantDialog = null;
  private FeatureLayer mPlantFeatureLayer;
  private Point mPlantLocation = null;
  private ProgressDialog mProgressDialog = null;

  private ArcGISMap mMap;
  private MapView mMapView;
  private GraphicsOverlay mGraphicOverlay;
  private Viewpoint mViewpoint;
  private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
  private Map<String, PlantSpecies> mPlantSpeciesTable = new HashMap<>();

  private void setUpMapView(final View root){
    if (mProgressDialog == null){
      mProgressDialog = new ProgressDialog(this);
    }
    mProgressDialog.dismiss();
    mProgressDialog.setTitle("Loading indoor locations");
    mProgressDialog.setMessage("");
    mProgressDialog.show();
    mMapView = (MapView) root.findViewById(R.id.map);

    IALocationDataSource dataSource = new IALocationDataSource(this);

    mMapView.getLocationDisplay().setLocationDataSource(dataSource);

    mMapView.getLocationDisplay().addLocationChangedListener(new LocationDisplay.LocationChangedListener() {
      @Override public void onLocationChanged(LocationDisplay.LocationChangedEvent locationChangedEvent) {
        LocationDataSource.Location location = locationChangedEvent.getLocation();
        if(location.getPosition() != null){
          mMapView.setViewpointCenterAsync(location.getPosition(), 1000);
        }

      }
    });

    mMapView.getLocationDisplay().startAsync();
//    mMapView.setViewpointCenterAsync(new Point(-122.67747838776182, 45.521685475513955, SpatialReferences.getWgs84()), 1000);

    Portal portal = new Portal("http://www.arcgis.com");
    PortalItem item = new PortalItem(portal, "8dbd1c7cbadb41608b3fbc6952876cc2");

    mMap = new ArcGISMap(item);
    mMap.addDoneLoadingListener(new Runnable() {
      @Override public void run() {
        Log.d(TAG, "Web map loaded");
        generateTables();
        queryNonspatialPlantData(new ServiceApi.PlantDataCallback() {
          @Override public void onPlantDataLoaded(Collection<PlantSpecies> dataLoaded) {
            for (PlantSpecies p : dataLoaded) {
              Log.i(TAG, p.toString());
            }
          }
        });
      }
    });

  //  mMap.loadAsync();
    mMap.addLoadStatusChangedListener(new LoadStatusChangedListener() {
      @Override public void loadStatusChanged(LoadStatusChangedEvent loadStatusChangedEvent) {
        Log.d(TAG, loadStatusChangedEvent.getNewLoadStatus().name());
        LayerList layerList = mMap.getOperationalLayers();
        Log.i(TAG, "Operational list size " + layerList.size());
        Iterator<Layer> iter = layerList.iterator();
        while (iter.hasNext()){
          mPlantFeatureLayer = (FeatureLayer) iter.next();

          mPlantFeatureTable = (ServiceFeatureTable) mPlantFeatureLayer.getFeatureTable();

          Log.i(TAG, mPlantFeatureLayer.getName());
        }

      }
    });
    mMapView.setMap(mMap);

    if (mViewpoint != null){
      mMapView.setViewpoint(mViewpoint);
    }

    // Add graphics overlay for map markers
    mGraphicOverlay  = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(mGraphicOverlay);

    mMapView.addDrawStatusChangedListener(new DrawStatusChangedListener() {
      @Override public void drawStatusChanged(final DrawStatusChangedEvent drawStatusChangedEvent) {
        if (drawStatusChangedEvent.getDrawStatus() == DrawStatus.COMPLETED){
          if (mProgressDialog != null){
            mProgressDialog.dismiss();
          }
          mMapView.removeDrawStatusChangedListener(this);

        }
      }
    });


    // Setup OnTouchListener to detect and act on long-press
   mMapView.setOnTouchListener(new MapTouchListener(getApplicationContext(), mMapView));

  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ensurePermissions();

    // prevent the screen going to sleep while app is on foreground
    findViewById(android.R.id.content).setKeepScreenOn(true);

    // instantiate IALocationManager and IAResourceManager
  //  mIALocationManager = IALocationManager.create(this);
  //  mResourceManager = IAResourceManager.create(this);

    View view = findViewById(R.id.list_fragment_container);
    // Set up map
    setUpMapView(view);

  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (mMapView != null) {
      mMapView.resume();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (mMapView != null) {
      mMapView.pause();
    }
  }



  /**
   * Checks that we have access to required information, if not ask for users permission.
   */
  private void ensurePermissions() {

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {

      // we don't have access to coarse locations, hence we have not access to wifi either
      // check if this requires explanation to user
      if (ActivityCompat.shouldShowRequestPermissionRationale(this,
          Manifest.permission.ACCESS_COARSE_LOCATION)) {

        new AlertDialog.Builder(this)
            .setTitle(R.string.location_permission_request_title)
            .setMessage(R.string.location_permission_request_rationale)
            .setPositiveButton(R.string.permission_button_accept, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "request permissions");
                ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_ACCESS_COARSE_LOCATION);
              }
            })
            .setNegativeButton(R.string.permission_button_deny, null)
            .show();

      } else {

        // ask user for permission
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
            REQUEST_CODE_ACCESS_COARSE_LOCATION);

      }

    }
    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }


  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

    switch (requestCode) {
      case REQUEST_CODE_ACCESS_COARSE_LOCATION:

        if (grantResults.length == 0
            || grantResults[0] == PackageManager.PERMISSION_DENIED) {
          Toast.makeText(this, R.string.location_permission_denied_message,
              Toast.LENGTH_LONG).show();
        }
        break;
    }

  }

  @Override public void onCancel() {
    mAddPlantDialog.dismiss();
  }

  @Override public void onSave(String plantName, String locationName) {
    mAddPlantDialog.dismiss();
    saveNewFeature(plantName, locationName);
  }

  private void saveNewFeature(String plantName, String locationName) {
    if (mPlantLocation != null){
      Log.i(TAG, "saivng location name " + locationName + " to location position " + mPlantLocation.getX() + ", " + mPlantLocation.getY());
      // create the attributes for the feature
      java.util.Map<String, Object> attributes = new HashMap<String, Object>();
      attributes.put("LOCATION_DESCRIPTION", locationName);
      attributes.put("PLANT_NAME" , plantName);

      //create a new feature from the attributes and the point
      Feature feature = mPlantFeatureTable.createFeature(attributes, mPlantLocation);
      final MainActivity a = this;
      //add the new feature
      final ListenableFuture<Void> result = mPlantFeatureTable.addFeatureAsync(feature);

      result.addDoneListener(new Runnable() {
        @Override public void run() {
          try {
            // track adding feature to feature table
            result.get();
            //was it successful?
            if (result.isDone()) {
              Toast.makeText(a.getApplicationContext(),"Plant added to inventory", Toast.LENGTH_SHORT).show();
            }
          } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "Exception encountered adding plant to inventory, "+ e.getMessage());
            Toast.makeText(getParent(),"There was a problem adding plant", Toast.LENGTH_SHORT).show();
          }
        }
      });

      //apply edits to the server
      mPlantFeatureTable.applyEditsAsync();

    }
  }

  private class MapTouchListener extends DefaultMapViewOnTouchListener {
    /**
     * Instantiates a new DrawingMapViewOnTouchListener with the specified
     * context and MapView.
     *
     * @param context the application context from which to get the display
     *                metrics
     * @param mapView the MapView on which to control touch events
     */
    public MapTouchListener(final Context context, final MapView mapView) {
      super(context, mapView);
    }
    @Override
    public final boolean onSingleTapConfirmed(final MotionEvent motionEvent) {
      final android.graphics.Point screenPoint = new android.graphics.Point(
          (int) motionEvent.getX(),
          (int) motionEvent.getY());

      // Assign the current clicked location
      final Point point = mMapView.screenToLocation(screenPoint);
      Log.i(TAG, "Screen to location " + point.getX() + ", " + point.getY());
      mPlantLocation = (Point) GeometryEngine.project(point, SpatialReference.create(3857));
      Log.i(TAG, "WGS 84 location" + mPlantLocation.getX() + ", " + mPlantLocation.getY());
      selectFeature(mPlantLocation);
//      final ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphic = mMapView
//          .identifyGraphicsOverlayAsync(mGraphicOverlay, screenPoint, 10, false);
//
//      identifyGraphic.addDoneListener(new Runnable() {
//        @Override
//        public void run() {
//          try {
//            // get the list of graphics returned by identify
//            final IdentifyGraphicsOverlayResult graphic = identifyGraphic.get();
//
//            // get size of list in results
//            final int identifyResultSize = graphic.getGraphics().size();
//            if (identifyResultSize > 0){
//              final Graphic foundGraphic = graphic.getGraphics().get(0);
//
//            }else{
//
//
//              // Show a dialog for adding plant info
//              showAddPlantDialog();
//            }
//          } catch (InterruptedException | ExecutionException ie) {
//            Log.e(TAG, ie.getMessage());
//          }
//        }
//
//      });
      return true;
    }
  }
  private void generateTables(){

    Map<String,Object> unsupported = mMap.getUnsupportedJson();

    Set<String> unsupportedJsonKeys = unsupported.keySet();
    for (String key : unsupportedJsonKeys){
      Log.i(TAG, "Key - " + key + unsupported.get(key).toString());
      if (key.equalsIgnoreCase("tables")){
        Object tables = unsupported.get(key);
        if (tables instanceof Collection){
          Collection<Object> tableMap = (Collection<Object>) tables;
          Iterator<Object> iter = tableMap.iterator();
          while (iter.hasNext()) {
            Object o = iter.next();
            Map<String, Object> tableInfo = (Map) o;
            String title = tableInfo.get("title").toString();
            String url = tableInfo.get("url").toString();
            if (title.equalsIgnoreCase(PLANT_READINGS)){
              mReadingsTable = new ServiceFeatureTable(url);
            }
            if (title.equalsIgnoreCase(PLANT_SPECIES)){
              mPlantTable = new ServiceFeatureTable(url);
            }
            Log.i(TAG, "url = " + url);

          }
        }
      }
    }
  }
  private void queryNonspatialPlantData(final ServiceApi.PlantDataCallback callback) {
    mPlantTable.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.MANUAL_CACHE);
    mPlantTable.loadAsync();
    mPlantTable.addDoneLoadingListener(new Runnable() {
      @Override public void run() {
        QueryParameters queryParameters = new QueryParameters();
        // Get all the rows in the table
        queryParameters.setWhereClause("1 = 1");
        List<String> outFields = new ArrayList<String>();
        // Get all the fields in the table
        outFields.add("*");
        final ListenableFuture<FeatureQueryResult> futureResult = mPlantTable
            .populateFromServiceAsync(queryParameters, true, outFields);
        futureResult.addDoneListener(new Runnable() {
          @Override public void run() {
            try {
              FeatureQueryResult fqr = futureResult.get();
              if (fqr != null){
                final Iterator<Feature> iterator = fqr.iterator();
                while (iterator.hasNext()){
                  Feature feature = iterator.next();
                  Map<String,Object> map = feature.getAttributes();
                  PlantSpecies plantSpecies = parsePlantSpeciesData(map);
                  mPlantSpeciesTable.put(plantSpecies.getPlantName(), plantSpecies);
                }
              }
              callback.onPlantDataLoaded(mPlantSpeciesTable.values());
            } catch (Exception e) {
              e.printStackTrace();
              // TO DO: Handle errors
            }
          }
        });
      }
    });
  }
  private PlantSpecies parsePlantSpeciesData(Map<String,Object> map) {
    PlantSpecies plantSpecies = new PlantSpecies();
    plantSpecies.setPlantName(extractValueFromMap("PLANT_NAME", map));
    plantSpecies.setPreferredMoistureLevel(Integer.parseInt(extractValueFromMap("MOISTURE_LVL",map)));
    plantSpecies.setSpecialNeeds(extractValueFromMap("SPECIAL_NEEDS",map));
    plantSpecies.setPlantDescription(extractValueFromMap("DESCRIPTION", map));
    plantSpecies.setWarnings(extractValueFromMap("WARNINGS", map));
    return plantSpecies;
  }
  /**
   * Get the string value from the map given the column name
   * @param columnName - a non-null String representing the name of the column in the map
   * @param map - a map of objects indexed by string
   * @return  - the string value, may be empty but not null.
   */
  private String extractValueFromMap(@NonNull String columnName, @NonNull Map<String,Object> map){
    String value = "";
    if (map.containsKey(columnName) && map.get(columnName) != null){
      value = map.get(columnName).toString();
    }
    return value;
  }

  private void showAddPlantDialog(){
    List<String> plantTypes = new ArrayList<>();
    for (PlantSpecies s : mPlantSpeciesTable.values()){
      plantTypes.add(s.getPlantName());
    }
    mAddPlantDialog= new AddPlantDialog();
    FragmentManager fm = getSupportFragmentManager();
    mAddPlantDialog.show(fm,"add_plant_dialog");
    mAddPlantDialog.setPlantSpecies(plantTypes);

  }
  private void selectFeature(Point point) {

    //create a buffer from the point
    Polygon searchGeometry = GeometryEngine.buffer(point, 5);

    //create a query
    final QueryParameters queryParams = new QueryParameters();
    queryParams.setGeometry(searchGeometry);
    queryParams.setSpatialRelationship(QueryParameters.SpatialRelationship.WITHIN);

    //select based on the query
    final ListenableFuture<FeatureQueryResult> result = mPlantFeatureLayer.selectFeaturesAsync(queryParams, FeatureLayer.SelectionMode.NEW);
    result.addDoneListener(new Runnable() {
      @Override public void run() {
        try {
          FeatureQueryResult queryResult =result.get();
          Iterator<Feature> features = queryResult.iterator();
          if (features.hasNext()){

          }else{
            showAddPlantDialog();
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (ExecutionException e) {
          e.printStackTrace();
        }
      }
    });
  }
}

