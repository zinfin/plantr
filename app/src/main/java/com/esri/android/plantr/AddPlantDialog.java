package com.esri.android.plantr;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

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

public class AddPlantDialog extends DialogFragment {
  private ArrayAdapter<String> mAdapter = null;
  private AddPlantDialogListener mCallback = null;
  private String mSelectedPlant = null;
  private TextView mLocationName = null;

  public AddPlantDialog(){}


  public interface AddPlantDialogListener{
    void onCancel();
    void onSave(String plantName, String locationName);
  }
  private List<String> list = new ArrayList<>();
  @Override
  public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
      final Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.add_plant, container,false);
    Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

      @Override public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        mSelectedPlant = mAdapter.getItem(i);
      }

      @Override public void onNothingSelected(AdapterView<?> adapterView) {

      }

    });
    mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
    spinner.setAdapter(mAdapter);
    final TextView locationText = (TextView) view.findViewById(R.id.txtPlantLocation) ;
    DialogFragment fragment = this;
    Button cancelBtn = (Button) view.findViewById(R.id.btnCancel);
    cancelBtn.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        mCallback.onCancel();
      }
    });

    Button saveBtn = (Button) view.findViewById(R.id.btnSave);
    saveBtn.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        String locationName = locationText.getText() != null ? locationText.getText().toString():"";
        mCallback.onSave(mSelectedPlant, locationName);
      }
    });
    return view;
  }
  public void setPlantSpecies(List<String> species){
    list = species;
  }
  @Override
  public void onAttach(Context activity) {
    super.onAttach(activity);

    // This makes sure that the container activity has implemented
    // the callback interface. If not, it throws an exception
    try {
      mCallback = (AddPlantDialogListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString()
          + " must implement AddPlantDialogListener");
    }
  }
}
