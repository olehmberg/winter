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
package de.uni_mannheim.informatik.dws.winter.model;

/**
 * An interface defining the basic requirements for everything that can be
 * matched with this framework.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 */
public interface Matchable {

	/**
	 * 
	 * @return Returns the (unique) identifier for this record.
	 */
	String getIdentifier();

	/**
	 * 
	 * @return Returns the provenance information for this record.
	 */
	String getProvenance();
	
	/**
	 * 
	 * @return
	 * 		Returns a data source identifier for the matchable. Only required if matchables from different sources are stored in the same {@link DataSet}.
	 * 		Returns 0 by default.
	 */
	public default int getDataSourceIdentifier() {
		return 0;
	}
}
