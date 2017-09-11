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

import de.uni_mannheim.informatik.dws.winter.matching.rules.Comparator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparator;
import de.uni_mannheim.informatik.dws.winter.similarity.numeric.DeviationSimilarity;

/**
 * {@link Comparator} for Songs based on the runtime
 * value.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 * 
 */
public class ITunesRuntimeComparatorDeviationSimilarity extends RecordComparator {

	private static final long serialVersionUID = 1L;
	DeviationSimilarity sim = new DeviationSimilarity();

	public ITunesRuntimeComparatorDeviationSimilarity(Attribute attributeRecord1, Attribute attributeRecord2) {
		super(attributeRecord1, attributeRecord2);
	}

	@Override
	public double compare(
			Record record1,
			Record record2,
			Correspondence<Attribute, Matchable> schemaCorrespondences) {
		
		double sim_temp = 0.00;
		double similarity = 0.00;
		
		String s1 = record1.getValue(this.getAttributeRecord1());
		String s2 = convertTimeToSongFormat(record2.getValue(this.getAttributeRecord2()));
		
		if(s1.equals("NULL")){
			return similarity;
		}
		else if(s1.contains("{")){
			String runtime_temp 		= s1.replace("{", "");
			runtime_temp 				= runtime_temp.replace("}", "");
			s1							= runtime_temp.replaceAll(" ", "");
			String[] s1_array			= runtime_temp.split("\\|");
			for(String s1_entry : s1_array){
				sim_temp = sim.calculate(Double.parseDouble(s1_entry), Double.parseDouble(s2));
				if(sim_temp >= similarity)
					similarity = sim_temp;
			}
		}
		else{
			similarity = sim.calculate(Double.parseDouble(s1), Double.parseDouble(s2));
		}
		return similarity;
	}
	
	private String convertTimeToSongFormat(String time) {
		
		String time_split[] 	= time.split(":");
		Double runtime			= 0.00;
		String time_converted 	= "";
		if(time_split.length == 2){
			runtime = Double.parseDouble(time_split[0]) + (Double.parseDouble(time_split[1])/60);
			
		}
		else if(time_split.length == 3){
			runtime = 60*Double.parseDouble(time_split[0]) + Double.parseDouble(time_split[1]) + (Double.parseDouble(time_split[2])/60);
		}
		time_converted = runtime.toString();
		
		return time_converted;
	}

}
