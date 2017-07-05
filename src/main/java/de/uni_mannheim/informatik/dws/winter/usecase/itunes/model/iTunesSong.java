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
package de.uni_mannheim.informatik.dws.winter.usecase.itunes.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.uni_mannheim.informatik.dws.winter.model.AbstractRecord;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;

/**
 * A {@link AbstractRecord} representing an iTunes-Song.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de
 * 
 */
public class iTunesSong extends Record implements Serializable {

	/*
	 * example entry <song> <id>itunes_1</id> <page>itunes - music - let me take
	 * - ep by mangelt</page> <uri0>gr</uri0> <uri1>album</uri1>
	 * <uri2>let-me-take-ep</uri2> <uri3>id359003765</uri3>
	 * <position>1</position> <name>let me take</name> <artist>mangelt</artist>
	 * <time>0.31875</time>
	 * </song>
	 */

	private static final long serialVersionUID = 1L;

	public iTunesSong(String identifier, String provenance) {
		super(identifier, provenance);
	}

	private String page;
	private String uri0;
	private String uri1;
	private String uri2;
	private String uri3;
	private String position;
	private String name;
	private String artist;
	private String time;
	

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getUri0() {
		return uri0;
	}

	public void setUri0(String uri0) {
		this.uri0 = uri0;
	}

	public String getUri1() {
		return uri1;
	}

	public void setUri1(String uri1) {
		this.uri1 = uri1;
	}

	public String getUri2() {
		return uri2;
	}

	public void setUri2(String uri2) {
		this.uri2 = uri2;
	}

	public String getUri3() {
		return uri3;
	}

	public void setUri3(String uri3) {
		this.uri3 = uri3;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
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

	public static final Attribute PAGE 		= new Attribute("PAGE");
	public static final Attribute URI0 		= new Attribute("URI0");
	public static final Attribute URI1 		= new Attribute("URI1");
	public static final Attribute URI2 		= new Attribute("URI2");
	public static final Attribute URI3 		= new Attribute("URI3");
	public static final Attribute POSITION 	= new Attribute("Position");
	public static final Attribute ARTIST 	= new Attribute("Artist");
	public static final Attribute TIME 		= new Attribute("Time");
	public static final Attribute NAME 		= new Attribute("Name");
	
	
	@Override
	public boolean hasValue(Attribute attribute) {
		if (attribute == PAGE)
			return getPage() != null && !getPage().isEmpty();
		else if (attribute == URI0)
			return getUri0() != null && !getUri0().isEmpty();
		else if (attribute == URI1)
			return getUri1() != null && !getUri1().isEmpty();
		else if (attribute == URI2)
			return getUri2() != null && !getUri2().isEmpty();
		else if (attribute == URI3)
			return getUri3() != null && !getUri3().isEmpty();
		else if (attribute == POSITION)
			return getPosition() != null && !getPosition().isEmpty();
		else if (attribute == ARTIST)
			return getArtist() != null && getArtist().isEmpty();
		else if (attribute == TIME)
			return getTime() != null && getTime().isEmpty();
		else if (attribute == NAME)
			return getName() != null && getName().isEmpty();
		else
			return false;
	}

	@Override
	public String toString() {
		return String.format("[iTunes Song: %s / %s / %s  / %s / %s / %s / %s / %s / %s]",getName(), getArtist(), getPage(), getUri0(), getUri1(), getUri2(), getUri3(), 
				getPosition(),  getTime());
	}

	@Override
	public int hashCode() {
		return getIdentifier().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof iTunesSong) {
			return this.getIdentifier().equals(((iTunesSong) obj).getIdentifier());
		} else
			return false;
	}
	
	/**
	 * Returns the runTime in minutes
	 */
	
	public static double runTimeInMinutes(Record r){
		
		double runTimeInMinutes = 0;
		String runtime = r.getValue(TIME);
		
		if(runtime.isEmpty() || runtime.equals("--"))
			return runTimeInMinutes;
		
		String[] runtimeArray = runtime.split(":");
		
		if(runtime.length()>6){
			runTimeInMinutes += Integer.parseInt(runtimeArray[0]) * 60;
			runTimeInMinutes += Integer.parseInt(runtimeArray[1]);
			runTimeInMinutes += 100/ Integer.parseInt(runtimeArray[2]);
		}
		else if(runtime.length()>4){
			runTimeInMinutes += Integer.parseInt(runtimeArray[1]);
			runTimeInMinutes += 100/ Integer.parseInt(runtimeArray[2]);
		}
		
		return runTimeInMinutes;
		
	}

}
