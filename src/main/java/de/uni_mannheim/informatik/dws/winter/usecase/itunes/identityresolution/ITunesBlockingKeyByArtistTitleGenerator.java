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

package de.uni_mannheim.informatik.dws.winter.usecase.itunes.identityresolution;

import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.BlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.RecordBlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.usecase.itunes.model.Song;
import de.uni_mannheim.informatik.dws.winter.usecase.itunes.model.iTunesSong;

/**
 * {@link BlockingKeyGenerator} for iTunes, which generates a blocking
 * key based on the first four digits of the Artist's attributes and first 8 digits of the Name attributes from the iTunes dataset and the first 8 digits of the RDFSchema attribute from the Song dataset.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 * 
 */
public class ITunesBlockingKeyByArtistTitleGenerator extends
		RecordBlockingKeyGenerator<Record, Attribute> {

	private static final long serialVersionUID = 1L;


	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.blocking.generators.BlockingKeyGenerator#generateBlockingKeys(de.uni_mannheim.informatik.wdi.model.Matchable, de.uni_mannheim.informatik.wdi.model.Result, de.uni_mannheim.informatik.wdi.processing.DatasetIterator)
	 */
	@Override
	public void generateBlockingKeys(Record record,
			Processable<Correspondence<Attribute, Matchable>> correspondences,
			DataIterator<Pair<String, Record>> resultCollector) {
		String artist 	= "";
		String name 	= "";
		
		if(record.hasValue(iTunesSong.ARTIST)){
			artist 	= 	record.getValue(iTunesSong.ARTIST);
			name 	=  	record.getValue(iTunesSong.NAME);
		}
		else{
			artist = record.getValue(Song.ARTIST);
			name = record.getValue(Song.RDFSCHEMA);
		}
		
		if(artist == null)
			artist = "0000";
		else {
			artist = artist.concat("0000");
			artist = artist.substring(0,4).toUpperCase();
		}
		
		if(name == null)
			name = "00000000";
		else {
			name = name.concat("00000000");
			name = name.substring(0,8).toUpperCase();
		}
		
		String bkv = artist.concat(name);
		resultCollector.next(new Pair<>((bkv), record));
	}

}
