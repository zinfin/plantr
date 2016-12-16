package com.esri.android.plantr;
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

import java.util.Date;

public class PlantSpecies {
  private String plantName;
  private Integer preferredMoistureLevel;
  private String specialNeeds;
  private String plantDescription;
  private String warnings;
  private String otherInformation;
  private Date creationDate;
  private String creator;
  private Date editDate;
  private String editor;

  public PlantSpecies(){}

  @Override public String toString() {
    return "PlantSpecies{" +
        "plantName='" + plantName + '\'' +
        ", preferredMoistureLevel=" + preferredMoistureLevel +
        ", specialNeeds='" + specialNeeds + '\'' +
        ", plantDescription='" + plantDescription + '\'' +
        ", warnings='" + warnings + '\'' +
        ", otherInformation='" + otherInformation + '\'' +
        ", creationDate=" + creationDate +
        ", creator='" + creator + '\'' +
        ", editDate=" + editDate +
        ", editor='" + editor + '\'' +
        '}';
  }

  public PlantSpecies(String plantName, Integer preferredMoistureLevel, String specialNeeds,
      String plantDescription, String warnings, String otherInformation, Date creationDate, String creator,
      Date editDate, String editor) {
    this.plantName = plantName;
    this.preferredMoistureLevel = preferredMoistureLevel;
    this.specialNeeds = specialNeeds;
    this.plantDescription = plantDescription;
    this.warnings = warnings;
    this.otherInformation = otherInformation;
    this.creationDate = creationDate;
    this.creator = creator;
    this.editDate = editDate;
    this.editor = editor;
  }

  public String getPlantName() {
    return plantName;
  }

  public void setPlantName(String plantName) {
    this.plantName = plantName;
  }

  public Integer getPreferredMoistureLevel() {
    return preferredMoistureLevel;
  }

  public void setPreferredMoistureLevel(Integer preferredMoistureLevel) {
    this.preferredMoistureLevel = preferredMoistureLevel;
  }

  public String getSpecialNeeds() {
    return specialNeeds;
  }

  public void setSpecialNeeds(String specialNeeds) {
    this.specialNeeds = specialNeeds;
  }

  public String getPlantDescription() {
    return plantDescription;
  }

  public void setPlantDescription(String plantDescription) {
    this.plantDescription = plantDescription;
  }

  public String getWarnings() {
    return warnings;
  }

  public void setWarnings(String warnings) {
    this.warnings = warnings;
  }

  public String getOtherInformation() {
    return otherInformation;
  }

  public void setOtherInformation(String otherInformation) {
    this.otherInformation = otherInformation;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public Date getEditDate() {
    return editDate;
  }

  public void setEditDate(Date editDate) {
    this.editDate = editDate;
  }

  public String getEditor() {
    return editor;
  }

  public void setEditor(String editor) {
    this.editor = editor;
  }
}
