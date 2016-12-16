package com.esri.android.plantr;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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

public class PlantInfoDialog extends DialogFragment {

  public interface PlantInfoDialogListener {
    void onPlantInfoCancel();
    void onPlantInfoMeasure(String plantName, String locationName);
  }
  private PlantInfoDialogListener mCallback;
  private TextView mPlantNameText;
  private TextView mPlantLocation;
  private TextView mPlantDescription;
  private String mName="";
  private String mLocation ="";
  private String mDescription = "";
  @Override
  public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
      final Bundle savedInstanceState) {

    final View view = inflater.inflate(R.layout.plant_info, container,false);

    Button btnCancel = (Button) view.findViewById(R.id.btnClosePlantInfo);
    btnCancel.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        mCallback.onPlantInfoCancel();
      }
    });

    mPlantDescription = (TextView) view.findViewById(R.id.txtPlantInfo);
    mPlantNameText = (TextView) view.findViewById(R.id.txtPlantName);
    mPlantLocation = (TextView) view.findViewById(R.id.txtLocationName);
    mPlantDescription.setText(mDescription);
    mPlantNameText.setText(mName);
    mPlantLocation.setText(mLocation);
    return view;

  }
  @Override
  public void onAttach(Context activity) {
    super.onAttach(activity);

    // This makes sure that the container activity has implemented
    // the callback interface. If not, it throws an exception
    try {
      mCallback = (PlantInfoDialog.PlantInfoDialogListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString()
          + " must implement PlantInfoDialogListener");
    }
  }
  public void showData(String plantName, String description, String locationName){
    mName = plantName;
    mDescription = description;
    mLocation = locationName;
  }
}
