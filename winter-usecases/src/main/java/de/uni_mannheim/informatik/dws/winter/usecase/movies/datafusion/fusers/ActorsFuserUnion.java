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
package de.uni_mannheim.informatik.dws.winter.usecase.movies.datafusion.fusers;

import java.util.List;

import de.uni_mannheim.informatik.dws.winter.datafusion.AttributeValueFuser;
import de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.list.Union;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroup;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Actor;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;

/**
 * {@link AttributeValueFuser} for the actors of {@link Movie}s. 
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 */
public class ActorsFuserUnion extends AttributeValueFuser<List<Actor>, Movie, Attribute> {
	
	public ActorsFuserUnion() {
		super(new Union<Actor, Movie, Attribute>());
	}
	
	@Override
	public boolean hasValue(Movie record, Correspondence<Attribute, Matchable> correspondence) {
		return record.hasValue(Movie.ACTORS);
	}
	
	@Override
	protected List<Actor> getValue(Movie record, Correspondence<Attribute, Matchable> correspondence) {
		return record.getActors();
	}

	@Override
	public void fuse(RecordGroup<Movie, Attribute> group, Movie fusedRecord, Processable<Correspondence<Attribute, Matchable>> schemaCorrespondences, Attribute schemaElement) {
		FusedValue<List<Actor>, Movie, Attribute> fused = getFusedValue(group, schemaCorrespondences, schemaElement);
		fusedRecord.setActors(fused.getValue());
		fusedRecord.setAttributeProvenance(Movie.ACTORS, fused.getOriginalIds());
	}

}
