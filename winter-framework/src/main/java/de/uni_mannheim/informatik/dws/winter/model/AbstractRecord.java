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

import java.io.Serializable;

/**
 * The super class for models, should be extended by all model classes.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 */
public abstract class AbstractRecord<SchemaElementType> implements Matchable, Fusible<SchemaElementType>, Serializable {

	private static final long serialVersionUID = 1L;
	protected String id;
	protected String provenance;

	public AbstractRecord() {
		
	}
	
	public AbstractRecord(String identifier, String provenance) {
		id = identifier;
		this.provenance = provenance;
	}

	@Override
	public String getIdentifier() {
		return id;
	}

	@Override
	public String getProvenance() {
		return provenance;
	}

	@Override
	public abstract boolean hasValue(SchemaElementType attribute);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AbstractRecord))
			return false;
		AbstractRecord other = (AbstractRecord) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
}
