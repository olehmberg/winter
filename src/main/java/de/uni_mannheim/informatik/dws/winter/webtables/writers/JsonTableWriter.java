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
package de.uni_mannheim.informatik.dws.winter.webtables.writers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;

import de.uni_mannheim.informatik.dws.winter.utils.query.Q;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableColumn;
import de.uni_mannheim.informatik.dws.winter.webtables.TableContext;
import de.uni_mannheim.informatik.dws.winter.webtables.TableRow;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.JsonTableMapping;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.JsonTableSchema;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.JsonTableSchema.Dependency;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.JsonTableSchema.HeaderPosition;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.JsonTableSchema.TableOrientation;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.JsonTableSchema.TableType;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.JsonTableWithMappingSchema;

/**
 * Writes a Web Table in the JSON format.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class JsonTableWriter implements TableWriter {

	private boolean writeMapping;
	
	/**
	 * @param writeMapping the writeMapping to set
	 */
	public void setWriteMapping(boolean writeMapping) {
		this.writeMapping = writeMapping;
	}
	
	public File getFileName(File f) {
		if(!f.getName().endsWith(".json")) {
			return new File(f.getAbsolutePath() + ".json");
		} else {
			return f;
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.t2k.webtables.writers.TableWriter#write(de.uni_mannheim.informatik.dws.t2k.webtables.Table, java.io.File)
	 */
	@Override
	public File write(Table t, File f) throws IOException {
		
		f = getFileName(f);
		
		JsonTableSchema data = new JsonTableSchema();
		
		data.setTableId(t.getTableId());
		data.setHasHeader(true);
		data.setHeaderPosition(HeaderPosition.FIRST_ROW);
		data.setHasKeyColumn(t.getSubjectColumnIndex()!=-1);
		data.setHeaderRowIndex(0);
		data.setKeyColumnIndex(t.getSubjectColumnIndex());
		data.setTableOrientation(TableOrientation.HORIZONTAL);
		data.setTableType(TableType.RELATION);
		
		if(t.getContext()!=null) {
			TableContext ctx = t.getContext();
			data.setTableNum(ctx.getTableNum());
			data.setUrl(ctx.getUrl());
			data.setPageTitle(ctx.getPageTitle());
			data.setTitle(ctx.getTableTitle());
			data.setTextBeforeTable(ctx.getTextBeforeTable());
			data.setTextAfterTable(ctx.getTextAfterTable());
			data.setLastModified(ctx.getLastModified());
			data.setTableContextTimeStampBeforeTable(ctx.getTimestampBeforeTable());
			data.setTableContextTimeStampAfterTable(ctx.getTimestampAfterTable());
		}
		
		String[][] relation = new String[t.getColumns().size()][]; 
		String[] values = null;
		List<TableRow> rows = new ArrayList<>(t.getRows());
		
		// rows
		for(TableColumn c : t.getColumns()) {
			values = new String[t.getRows().size()+1]; // all rows + one header
			values[0] = c.getHeader();
			
			for(int row = 0; row < rows.size(); row++) {
				TableRow r = rows.get(row);
				Object v = r.get(c.getColumnIndex());
				if(v!=null) {
					values[row+1] = v.toString();
				} else {
					values[row+1] = null;
				}
			}
			
			relation[c.getColumnIndex()] = values;
		}
		data.setRelation(relation);
		
		// functional dependencies
		if(t.getSchema().getFunctionalDependencies()!=null && t.getSchema().getFunctionalDependencies().size()>0) {
			ArrayList<Dependency> functionalDependencies = new ArrayList<>(t.getSchema().getFunctionalDependencies().size());
			
			for(Collection<TableColumn> det : t.getSchema().getFunctionalDependencies().keySet()) {
				Dependency fd = new Dependency();
				
				int[] indicesDet = new int[det.size()];
				int idx = 0;
				for(TableColumn c : det) {
					indicesDet[idx++] = c.getColumnIndex();
				}
				
				Collection<TableColumn> dep = t.getSchema().getFunctionalDependencies().get(det);
				int[] indicesDep = new int[dep.size()];
				idx = 0;
				for(TableColumn c : dep) {
					indicesDep[idx++] = c.getColumnIndex();
				}
				
				fd.setDeterminant(indicesDet);
				fd.setDependant(indicesDep);
				fd.setProbability(1.0);
				
				functionalDependencies.add(fd);
			}
			
			data.setFunctionalDependencies(functionalDependencies.toArray(new Dependency[functionalDependencies.size()]));
		}
		
		// key candidates
		if(t.getSchema().getCandidateKeys()!=null && t.getSchema().getCandidateKeys().size()>0) {
			Integer[][] candidateKeys = new Integer[t.getSchema().getCandidateKeys().size()][];
			
			int idx=0;
			for(Collection<TableColumn> key : t.getSchema().getCandidateKeys()) {
				candidateKeys[idx++] = Q.project(key, new TableColumn.ColumnIndexProjection()).toArray(new Integer[key.size()]);
			}
			
			data.setCandidateKeys(candidateKeys);
		}
		
		writeProvenance(t, data);
		
		Gson gson = new Gson();
        BufferedWriter w = new BufferedWriter(new FileWriter(f));
		
		if(writeMapping) {
		
			JsonTableWithMappingSchema d = new JsonTableWithMappingSchema();
			d.setTable(data);
			d.setMapping(JsonTableMapping.fromTableMapping(t.getMapping()));
			w.write(gson.toJson(d));
			
		} else {
			w.write(gson.toJson(data));
		}
		
        w.close();
		
		return f;
	}
	
	private void writeProvenance(Table t, JsonTableSchema data) {
		int provCount = 0;
		String[][] prov = new String[t.getSize()][];
		for(TableRow r : t.getRows()) {
			prov[r.getRowNumber()] = r.getProvenance().toArray(new String[r.getProvenance().size()]);
			provCount+=r.getProvenance().size();
		}
		if(provCount>0) {
			data.setRowProvenance(prov);
		}
		
		provCount=0;
		prov = new String[t.getColumns().size()][];
		for(TableColumn c : t.getColumns()) {
			prov[c.getColumnIndex()] = c.getProvenance().toArray(new String[c.getProvenance().size()]);
			provCount+=c.getProvenance().size();
		}
		if(provCount>0) {
			data.setColumnProvenance(prov);
		}
	}

}
