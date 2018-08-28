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
package de.uni_mannheim.informatik.dws.winter.webtables.parsers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class TableFactory {
	
	private static final Logger logger = WinterLogManager.getLogger();
	private Map<String, TableParser> parsers = new HashMap<>();
	
	public void addParser(String extension, TableParser p) {
		parsers.put(extension, p);
	}
	
	public TableFactory() {
		addParser(".json", new JsonTableParser());
		addParser(".csv", new CsvTableParser());
		addParser(".csv.gz", new CsvTableParser());
	}
	
	public Table createTableFromFile(File f) {
		Table t = null;
		TableParser p = null;
		
		for(String extension : parsers.keySet()) {
			if(f.getName().endsWith(extension)) {
				p = parsers.get(extension);
			}
		}
		
		if(p!=null) {
			p.setConvertValues(false);
			t = p.parseTable(f);
		} else {
			logger.error(String.format("Unsupported table format: %s", f.getName()));
		}
		
		return t;
	}
	
}
