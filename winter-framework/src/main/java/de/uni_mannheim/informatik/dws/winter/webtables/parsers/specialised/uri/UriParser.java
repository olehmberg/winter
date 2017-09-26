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
package de.uni_mannheim.informatik.dws.winter.webtables.parsers.specialised.uri;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableContext;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class UriParser {
	

    public Table parseTable(File file) {
        FileReader fr;
        Table t = null;
        try {
            fr = new FileReader(file);
            
            t = parseTable(fr, file.getName());
            
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return t;
    }
    
    public Table parseTable(Reader reader, String fileName) throws IOException {
        Gson gson = new Gson();
        
        String json = IOUtils.toString(reader);
        
        // get the data from the JSON source
        JsonTableUri data = gson.fromJson(json, JsonTableUri.class);

        if(data.getUrl()==null) {
        	JsonTableWithMapping dataWithMapping = gson.fromJson(json, JsonTableWithMapping.class);
        	if(dataWithMapping.getTable()!=null) {
        		data = dataWithMapping.getTable();
        	}
        }
        
        if(data.getUrl()!=null) {
        	Table t = new Table();
        	t.setPath(fileName);
        	TableContext c = new TableContext();
        	c.setUrl(data.getUrl());
        	t.setContext(c);
        	return t;
        } else {
        	return null;
        }
        
    }
}
