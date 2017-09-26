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
package de.uni_mannheim.informatik.dws.winter.webtables.lod;

import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableColumn;

public class LodTableColumn extends TableColumn {

	private static final long serialVersionUID = 1L;
	private String xmlType;
	private String range;
	
	private boolean isReferenceLabel = false;
	
	public void setReferenceLabel(boolean isReferenceLabel) {
		this.isReferenceLabel = isReferenceLabel;
	}
	
	public boolean isReferenceLabel() {
		return isReferenceLabel;
	}

	public LodTableColumn(int columnIndex, Table table) {
		super(columnIndex, table);
	}

	@Override
	public String getIdentifier() {
		return getUri();
	}
	
	@Override
	public String getUniqueName() {
		return getTable().getPath() + "/" + super.getUniqueName() + "/" + isReferenceLabel;
	}
	
	/**
	 * @return the xmlType
	 */
	public String getXmlType() {
		return xmlType;
	}
	/**
	 * @param xmlType the xmlType to set
	 */
	public void setXmlType(String xmlType) {
		this.xmlType = xmlType;
	}
	/**
	 * @return the range
	 */
	public String getRange() {
		return range;
	}
	/**
	 * @param range the range to set
	 */
	public void setRange(String range) {
		this.range = range;
	}
}
