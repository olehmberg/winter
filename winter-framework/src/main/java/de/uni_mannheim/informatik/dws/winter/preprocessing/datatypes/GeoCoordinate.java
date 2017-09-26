/*
 * Copyright (c) 2017 Data and Web Science Group, University of Mannheim, Germany (http://dws.informatik.uni-mannheim.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes;

/**
 *
 * @author domi
 */
public class GeoCoordinate {
    
    private Double latitude;
    private Double longitude;
    private Double oneValue;
    
    public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getOneValue() {
		return oneValue;
	}

	public void setOneValue(Double oneValue) {
		this.oneValue = oneValue;
	}

	public GeoCoordinate(){}
    
    public static GeoCoordinate parseCoordinate(String cooridante) {
        Double longi=Double.MAX_VALUE, lat=Double.MAX_VALUE;
        try {
        if(!cooridante.contains(",") && !cooridante.contains("\\s")) {
            GeoCoordinate g = new GeoCoordinate();
            g.oneValue = Double.parseDouble(cooridante);
        }     
        if(cooridante.contains(",")) {
            lat = Double.parseDouble(cooridante.split(" ")[0]);
            longi = Double.parseDouble(cooridante.split(" ")[1]);
        }
        else {
            String[] values = cooridante.split("\\s");
            
            if(values.length>1) {
                lat = Double.parseDouble(cooridante.split("\\s")[0]);
                longi = Double.parseDouble(cooridante.split("\\s")[1]);
            }
        }
        } catch(Exception e ) {
            //e.printStackTrace();
        }
        GeoCoordinate g = new GeoCoordinate();
        g.latitude = lat;
        g.longitude = longi;
        return g;
    }
}
