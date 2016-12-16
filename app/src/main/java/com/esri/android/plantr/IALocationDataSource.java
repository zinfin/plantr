package com.esri.android.plantr;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.location.LocationDataSource;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.indooratlas.android.sdk.*;
import com.indooratlas.android.sdk.resources.IALocationListenerSupport;

/* Copyright 2016 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For additional information, contact:
 * Environmental Systems Research Institute, Inc.
 * Attn: Contracts Dept
 * 380 New York Street
 * Redlands, California, USA 92373
 *
 * email: contracts@esri.com
 *
 */

public class IALocationDataSource extends LocationDataSource {
  private IALocationManager mIALocationManager;

  public IALocationDataSource(Context context){
    super();
    mIALocationManager = IALocationManager.create(context);
  }

  @Override protected void onStart() {
    mIALocationManager.requestLocationUpdates(IALocationRequest.create(), mListener);
    onStartCompleted(null);
  }

  @Override protected void onStop() {
    // unregister location & region changes
    mIALocationManager.removeLocationUpdates(mListener);
  }
  /**
   * Listener that handles location change events.
   */
  private IALocationListener mListener = new IALocationListenerSupport() {

    /**
     * Location changed, move marker and camera position.
     */
    @Override
    public void onLocationChanged(IALocation location) {

      Log.d("IALocationDataSource", "new location received with coordinates: " + location.getLatitude()
          + "," + location.getLongitude() + ",  " + location.getFloorLevel());
      updateLocation(new Location(new Point(location.getLongitude(), location.getLatitude(), SpatialReferences.getWgs84())));
    }
  };

}
