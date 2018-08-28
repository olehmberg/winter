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
package de.uni_mannheim.informatik.dws.winter.usecase.cities.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.uni_mannheim.informatik.dws.winter.model.AbstractRecord;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;

/**
 * A {@link AbstractRecord} representing a City.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de
 * 
 */
public class City extends AbstractRecord<Attribute> implements Serializable {

	/*
	 * example entry <city> <index>0</index> <city>MacAllen</city> <population>	 
	 * 110 in 1000</population> <country>USA</country> <Metro population in 1000>
	 * 1,058 in 1000</Metro population in 1000> <World rank>n/k</World rank>
	 * <sameAs>http://dbpedia.org/resource/McAllen,_Texas</sameAs> </city>
	 */
	
	private static final long serialVersionUID = 1L;

	public City(String identifier, String provenance) {
		super(identifier, provenance);
	}
		
	private String name;
	private double population;
	private String country;
	private String countryCode;
	private double latitude;
	private double longitude;
	private String officialName;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPopulation() {
		return population;
	}

	public void setPopulation(double population) {
		this.population = population;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getOfficialName() {
		return officialName;
	}

	public void setOfficialName(String officialName) {
		this.officialName = officialName;
	}

	private Map<Attribute, Collection<String>> provenance = new HashMap<>();
	private Collection<String> recordProvenance;

	public void setRecordProvenance(Collection<String> provenance) {
		recordProvenance = provenance;
	}

	public Collection<String> getRecordProvenance() {
		return recordProvenance;
	}

	public void setAttributeProvenance(Attribute attribute, Collection<String> provenance) {
		this.provenance.put(attribute, provenance);
	}

	public Collection<String> getAttributeProvenance(String attribute) {
		return provenance.get(attribute);
	}

	public String getMergedAttributeProvenance(Attribute attribute) {
		Collection<String> prov = provenance.get(attribute);

		if (prov != null) {
			return StringUtils.join(prov, "+");
		} else {
			return "";
		}
	}
	
	public static final Attribute ID = new Attribute("Index");
	public static final Attribute NAME = new Attribute("name");
	public static final Attribute POPULATION = new Attribute("population");
	public static final Attribute COUNTRY = new Attribute("country");
	public static final Attribute COUNTRYCODE = new Attribute("countryCode");
	public static final Attribute LATITUDE = new Attribute("latitude");
	public static final Attribute LONGITUDE = new Attribute("longitude");
	public static final Attribute OFFICIALNAME = new Attribute("officialName");
	
	@Override
	public boolean hasValue(Attribute attribute) {
		if(attribute==NAME)
			return getName() != null && !getName().isEmpty();
		else if(attribute==POPULATION)
			return getPopulation() != 0.00;
		else if(attribute==COUNTRY)
			return getCountry() != null && !getCountry().isEmpty();
		else if(attribute==COUNTRYCODE)
			return getCountryCode() != null && !getCountryCode().isEmpty();
		else if(attribute==LATITUDE)
			return getLatitude() != 0.00;
		else if(attribute==LONGITUDE)
			return getLongitude() != 0.00;
		else if(attribute==OFFICIALNAME)
			return getOfficialName() != null && !getOfficialName().isEmpty();
		else
			return false;
	}
	

	@Override
	public String toString() {
		return String.format("[City: %s / %s / %s  / %s / %s / %s / %s / %s ]", getIdentifier(), getName(), getPopulation(),
				getCountry(), getCountryCode(), getLatitude(), getLongitude(), getOfficialName());
	}

	@Override
	public int hashCode() {
		return getIdentifier().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof City) {
			return this.getIdentifier().equals(((City) obj).getIdentifier());
		} else
			return false;
	}
}
