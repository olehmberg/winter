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
package de.uni_mannheim.informatik.dws.winter.webtables;

import java.io.Serializable;

/**
 * Contains information about the context of a Web Table.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class TableContext implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String url;
	private String pageTitle;
	private String tableTitle;
	private int tableNum;
	private String textBeforeTable;
	private String textAfterTable;
	private String timestampBeforeTable;
	private String timestampAfterTable;
	private String lastModified;
	
	/**
	 * @return Returns the URL from which this table was extracted
	 */
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * @return Returns the title (&gt;meta&gt;&lt;title&gt;) of the HTML page from which this table was extracted
	 */
	public String getPageTitle() {
		return pageTitle;
	}
	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}
	
	/**
	 * @return Returns the text of the closest heading (&gt;h...&gt;) to the table
	 */
	public String getTableTitle() {
		return tableTitle;
	}
	public void setTableTitle(String tableTitle) {
		this.tableTitle = tableTitle;
	}
	
	/**
	 * @return Returns the table's index on the HTML page (i.e., 0 means it's the first table, 1 means it's the second table, etc.)
	 */
	public int getTableNum() {
		return tableNum;
	}
	public void setTableNum(int tableNum) {
		this.tableNum = tableNum;
	}
	
	/**
	 * @return Returns the text before the table
	 */
	public String getTextBeforeTable() {
		return textBeforeTable;
	}
	public void setTextBeforeTable(String textBeforeTable) {
		this.textBeforeTable = textBeforeTable;
	}
	
	/**
	 * @return Returns the text after the table
	 */
	public String getTextAfterTable() {
		return textAfterTable;
	}
	public void setTextAfterTable(String textAfterTable) {
		this.textAfterTable = textAfterTable;
	}
	
	/**
	 * @return Returns the text before the table that contains a timestamp
	 */
	public String getTimestampBeforeTable() {
		return timestampBeforeTable;
	}
	public void setTimestampBeforeTable(String timestampBeforeTable) {
		this.timestampBeforeTable = timestampBeforeTable;
	}
	
	/**
	 * @return Returns the text after the table that contains a timestamp
	 */
	public String getTimestampAfterTable() {
		return timestampAfterTable;
	}
	public void setTimestampAfterTable(String timestampAfterTable) {
		this.timestampAfterTable = timestampAfterTable;
	}
	
	/**
	 * @return Returns the last modified data of the HTML page
	 */
	public String getLastModified() {
		return lastModified;
	}
	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}
	
	
}
