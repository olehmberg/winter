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
 * A {@link AbstractRecord} representing an DBPedia Song.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de
 * 
 */
public class Song extends Record implements Serializable {

	/*
	 * example entry <song> <id>song_3</id> <name>NULL</name> <rdf_schema>Â½
	 * Full</rdf_schema> <runtime>4.16666666666666</runtime> <album>Riot Act
	 * (album)</album> <genre>{Blues rock|Alternative rock}</genre>
	 * <artist>Pearl Jam</artist> <producer>{Adam Kasper|Pearl Jam}</producer>
	 * <record>Epic Records</record> <writer>{Eddie Vedder|Jeff Ament}</writer>
	 * <composer>NULL</composer> <tracknumber>13</tracknumber> </song>
	 */

	private static final long serialVersionUID = 1L;

	public Song(String identifier, String provenance) {
		super(identifier, provenance);
	}

	private String album;
	private String genre;
	private String producer;
	private String record;
	private String writer;
	private String composer;
	private String name;
	private String artist;
	private String runtime;
	private String rdfschema;
	private String tracknumber;
	private String language;
	private String type;

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getProducer() {
		return producer;
	}

	public void setProducer(String producer) {
		this.producer = producer;
	}

	public String getRecord() {
		return record;
	}

	public void setRecord(String record) {
		this.record = record;
	}

	public String getWriter() {
		return writer;
	}

	public void setWriter(String writer) {
		this.writer = writer;
	}

	public String getComposer() {
		return composer;
	}

	public void setComposer(String composer) {
		this.composer = composer;
	}

	public String getRuntime() {
		return runtime;
	}

	public void setRuntime(String runtime) {
		this.runtime = runtime;
	}

	public String getRdfschema() {
		return rdfschema;
	}

	public void setRdfschema(String rdfschema) {
		this.rdfschema = rdfschema;
	}

	public String getTracknumber() {
		return tracknumber;
	}

	public void setTracknumber(String tracknumber) {
		this.tracknumber = tracknumber;
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

	public String getruntime() {
		return runtime;
	}

	public void setruntime(String runtime) {
		this.runtime = runtime;
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

	public static final Attribute ALBUM 		= new Attribute("Album");
	public static final Attribute GENRE 		= new Attribute("Genre");
	public static final Attribute PRODUCER 		= new Attribute("Producer");
	public static final Attribute RECORD 		= new Attribute("Record");
	public static final Attribute WRITER 		= new Attribute("Writer");
	public static final Attribute COMPOSER 		= new Attribute("Composer");
	public static final Attribute RUNTIME 		= new Attribute("Runtime");
	public static final Attribute NAME 			= new Attribute("Name");
	public static final Attribute ARTIST 		= new Attribute("Artist");
	public static final Attribute RDFSCHEMA 	= new Attribute("RDFSchema");
	public static final Attribute TRACKNUMBER 	= new Attribute("Tracknumber");
	public static final Attribute LANGUAGE 		= new Attribute("Language");
	public static final Attribute TYPE 			= new Attribute("Type");

	@Override
	public boolean hasValue(Attribute attribute) {
		if (attribute == ALBUM)
			return getAlbum() != null && !getAlbum().isEmpty();
		else if (attribute == GENRE)
			return getGenre() != null && !getGenre().isEmpty();
		else if (attribute == PRODUCER)
			return getProducer() != null && !getProducer().isEmpty();
		else if (attribute == RECORD)
			return getRecord() != null && !getRecord().isEmpty();
		else if (attribute == WRITER)
			return getWriter() != null && !getWriter().isEmpty();
		else if (attribute == COMPOSER)
			return getComposer() != null && !getComposer().isEmpty();
		else if (attribute == ARTIST)
			return getArtist() != null && getArtist().isEmpty();
		else if (attribute == RUNTIME)
			return getruntime() != null && getruntime().isEmpty();
		else if (attribute == NAME)
			return getName() != null && getName().isEmpty();
		else if (attribute == TRACKNUMBER)
			return getTracknumber() != null && getTracknumber().isEmpty();
		else if (attribute == RDFSCHEMA)
			return getRdfschema() != null && getRdfschema().isEmpty();
		else if (attribute == LANGUAGE)
			return getLanguage() != null && getLanguage().isEmpty();
		else if (attribute == TYPE)
			return getType() != null && getType().isEmpty();
		else
			return false;
	}

	@Override
	public String toString() {
		return String.format("[DBPedia Song: %s / %s / %s  / %s / %s / %s / %s / %s / %s / %s / %s, / %s, / %s]", getName(),
				getArtist(), getAlbum(), getGenre(), getProducer(), getRecord(), getWriter(), getComposer(),
				getruntime(), getTracknumber(), getRdfschema(), getLanguage(), getType());
	}

	@Override
	public int hashCode() {
		return getIdentifier().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Song) {
			return this.getIdentifier().equals(((Song) obj).getIdentifier());
		} else
			return false;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
