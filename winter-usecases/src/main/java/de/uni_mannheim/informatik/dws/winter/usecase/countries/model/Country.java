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
package de.uni_mannheim.informatik.dws.winter.usecase.countries.model;

import java.io.Serializable;
import java.time.LocalDateTime;
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
public class Country extends AbstractRecord<Attribute> implements Serializable {

	/*
	 * example entry <country> <index>1</index> <name>Federal Republic of Germany</name> <population>	 
	 * 82.6 million</population> <area>357,386 km2;;23.05.1949</area> <Speed Limit> </Speed Limit> 
	 * <Latest Constitution>23.05.1949</Latest Constitution> </country>
	 */
	
	private static final long serialVersionUID = 1L;

	public Country(String identifier, String provenance) {
		super(identifier, provenance);
	}
		
	private String name;
	private double population;
	private double area;
	private double speedLimit;
	private LocalDateTime dateLatestConstitution;


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
	
	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}

	public double getSpeedLimit() {
		return speedLimit;
	}

	public void setSpeedLimit(double speedLimit) {
		this.speedLimit = speedLimit;
	}

	public LocalDateTime getDateLatestConstitution() {
		return dateLatestConstitution;
	}

	public void setDateLatestConstitution(LocalDateTime dateLatestConstitution) {
		this.dateLatestConstitution = dateLatestConstitution;
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
	
	public static final Attribute ID = new Attribute("index");
	public static final Attribute NAME = new Attribute("name");
	public static final Attribute POPULATION = new Attribute("population");
	public static final Attribute AREA = new Attribute("area");
	public static final Attribute SPEEDLIMIT = new Attribute("speedLimit");
	public static final Attribute LATESTCONSTITUTION = new Attribute("latestConstitution");

	
	@Override
	public boolean hasValue(Attribute attribute) {
		if(attribute==ID)
			return getIdentifier() != null && !getIdentifier().isEmpty();
		else if(attribute==NAME)
			return getName() != null && !getName().isEmpty();
		else if(attribute==POPULATION)
			return getPopulation() != 0.00;
		else if(attribute==AREA)
			return getArea() != 0.00;
		else if(attribute==SPEEDLIMIT)
			return getSpeedLimit() != 0.00;
		else if(attribute==LATESTCONSTITUTION)
			return getDateLatestConstitution() != null;
		else
			return false;
	}
	

	@Override
	public String toString() {
		return String.format("[Country: %s / %s / %s / %s / %s / %s ]", getIdentifier(), getName(), getPopulation(),
				getArea(), getSpeedLimit(), getDateLatestConstitution());
	}

	@Override
	public int hashCode() {
		return getIdentifier().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Country) {
			return this.getIdentifier().equals(((Country) obj).getIdentifier());
		} else
			return false;
	}

}
