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
package de.uni_mannheim.informatik.dws.winter.usecase.restaurants.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.uni_mannheim.informatik.dws.winter.model.AbstractRecord;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;

/**
 * A {@link AbstractRecord} representing a restaurant.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de
 * 
 */
public class Restaurant extends Record implements Serializable {

	/*
	 * example entry <restaurant> <id>fodors_1</id> <Name>Adriano&apos;s
	 * Ristorante</Name> <Address>2930 Beverly Glen Circle</Address> <City>Los
	 * Angeles</City> <Phone>310/475-9807</Phone> <Style>Italian</Style>
	 * </restaurant>
	 */

	private static final long serialVersionUID = 1L;

	public Restaurant(String identifier, String provenance) {
		super(identifier, provenance);
	}

	private String name;
	private String address;
	private String city;
	private String phone;
	private String style;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
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

	public static final Attribute NAME = new Attribute("Name");
	public static final Attribute ADDRESS = new Attribute("Address");
	public static final Attribute CITY = new Attribute("City");
	public static final Attribute PHONE = new Attribute("Phone");
	public static final Attribute STYLE = new Attribute("Style");

	@Override
	public boolean hasValue(Attribute attribute) {
		if (attribute == NAME)
			return getName() != null && !getName().isEmpty();
		else if (attribute == ADDRESS)
			return getAddress() != null && !getAddress().isEmpty();
		else if (attribute == CITY)
			return getCity() != null && !getCity().isEmpty();
		else if (attribute == PHONE)
			return getPhone() != null && getPhone().isEmpty();
		else if (attribute == STYLE)
			return getStyle() != null && getStyle().isEmpty();
		else
			return false;
	}

	@Override
	public String toString() {
		return String.format("[Restaurant: %s / %s / %s / %s / %s]", getName(), getAddress(), getCity(), getPhone(),
				getStyle());
	}

	@Override
	public int hashCode() {
		return getIdentifier().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Restaurant) {
			return this.getIdentifier().equals(((Restaurant) obj).getIdentifier());
		} else
			return false;
	}

}
