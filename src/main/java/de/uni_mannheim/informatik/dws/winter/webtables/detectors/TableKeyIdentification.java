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
package de.uni_mannheim.informatik.dws.winter.webtables.detectors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableColumn;
import de.uni_mannheim.informatik.dws.winter.webtables.TableRow;

public class TableKeyIdentification {

	private double keyUniquenessThreshold;
	public double getKeyUniquenessThreshold() {
		return keyUniquenessThreshold;
	}
	public void setKeyUniquenessThreshold(double keyUniquenessThreshold) {
		this.keyUniquenessThreshold = keyUniquenessThreshold;
	}
	
	private boolean verbose = false;
	/**
	 * @param verbose the verbose to set
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	/**
	 * @return the verbose
	 */
	public boolean isVerbose() {
		return verbose;
	}

    private static final Pattern prefLabelPattern = Pattern.compile("([^#]*#)?([a-z]{1,9})?prefLabel$");
    private static final Pattern namePattern =Pattern.compile("([^#]*#)?name$");
    private static final Pattern labelPattern = Pattern.compile("([^#]*#)?([a-z]{1,9})?label$");
    private static final Pattern titlePattern = Pattern.compile("([^#]*#)?([a-z]{1,9})?title$");
    private static final Pattern labelPattern2 =Pattern.compile("([^#]*#)?.*Label$");
    private static final Pattern namePattern2 = Pattern.compile("([^#]*#)?.*Name$");
    private static final Pattern titlePattern2 = Pattern.compile("([^#]*#)?.*Title$");
    private static final Pattern alternateNamePattern = Pattern.compile("([^#]*#)?([a-z]{1,9})?alternateName$");
    
    public void identifyKeys(Table table) {
        TableColumn key = null;
        int keyColumnIndex = -1;
        List<Double> columnUniqueness = new ArrayList<>(table.getColumns().size());
        List<Double> columnValueLength = new ArrayList<>(table.getColumns().size());

        for (int i = 0; i < table.getSchema().getSize(); i++) {
        	
        	//int valueCount = 0;
        	int nullCount = 0;
        	int numRows = 0;
        	List<Integer> valueLength = new ArrayList<>(table.getSize());
        	HashSet<Object> uniqueValues = new HashSet<>();
        	
        	for(TableRow r : table.getRows()) {
        		Object value = r.get(i);
        		if(value!=null) {
	        		uniqueValues.add(value);
	        		//valueCount++;
	        		valueLength.add(value.toString().length());
        		} else {
        			nullCount++;
        		}
        		numRows++;
        	}
        	
        	double uniqueness = (double)uniqueValues.size() / (double)numRows;
        	double nullness = (double)nullCount / (double)numRows;
        	
            columnUniqueness.add(uniqueness - nullness);
            columnValueLength.add(Q.average(valueLength));
            
            if(isVerbose()) {
            	TableColumn c = table.getSchema().get(i);
            	System.err.println(String.format("[TableKeyIdentification] [%d]%s (%s) Uniqueness=%.4f; Nullness=%.4f; Combined=%.4f; Length=%.4f", i, c.getHeader(), c.getDataType(), uniqueness, nullness, columnUniqueness.get(columnUniqueness.size()-1), columnValueLength.get(columnValueLength.size()-1)));
            }
        }
        
        for (int i=table.getColumns().size()-1; i>=0; i--) {
            TableColumn column = table.getSchema().get(i);

            if (column.getDataType() != DataType.string) {
                continue;
            }
            if (prefLabelPattern.matcher(column.getHeader()).matches()) {
                key = column;
                break;
            }
            if (namePattern.matcher(column.getHeader()).matches()) {
                key = column;
                break;
            }
            if (labelPattern.matcher(column.getHeader()).matches()) {
                key = column;
            }

            if (titlePattern.matcher(column.getHeader()).matches()) {
                key = column;
            }
            if (labelPattern2.matcher(column.getHeader()).matches()) {
                key = column;
            }

            if (namePattern2.matcher(column.getHeader()).matches()) {
                key = column;
            }

            if (titlePattern2.matcher(column.getHeader()).matches()) {
                key = column;
            }
            if (alternateNamePattern.matcher(column.getHeader()).matches()) {
                key = column;
            }

        }
        
        
        if (key != null) {
            keyColumnIndex = table.getSchema().indexOf(key);
            
            if (columnUniqueness.get(keyColumnIndex) >= getKeyUniquenessThreshold()
                    && columnValueLength.get(keyColumnIndex) > 3.5
                    && columnValueLength.get(keyColumnIndex) <= 200) {
            	
            	table.setSubjectColumnIndex(keyColumnIndex);

            	if(isVerbose()) {
            		System.err.println(String.format("[TableKeyIdentification] RegEx Header Match: '%s'", table.getSchema().get(keyColumnIndex).getHeader()));
            	}
            	
                return;
            }
            //the found key does not fit the requirements, see if another column does
            key = null;
            
            if(isVerbose()) {
            	System.err.println(String.format("[TableKeyIdentification] RegEx Header Match: '%s' - insufficient", table.getSchema().get(keyColumnIndex).getHeader()));
            }
        }

        if (columnUniqueness.isEmpty()) {
        	if(isVerbose()) {
        		System.err.println("[TableKeyIdentification] no columns");
        	}
            return;
        }
        double maxCount = -1;
        int maxColumn = -1;

        for (int i = 0; i < columnUniqueness.size(); i++) {
            if (columnUniqueness.get(i) > maxCount && table.getSchema().get(i).getDataType() == DataType.string
                    && columnValueLength.get(i) > 3.5
                    && columnValueLength.get(i) <= 200) {
                maxCount = (Double) columnUniqueness.get(i);
                maxColumn = i;
            }
        }

        if (key == null) {
            if (maxColumn == -1) {
            	if(isVerbose()) {
            		System.err.println("[TableKeyIdentification] no columns that match criteria (data type, min length, max length)");
            	}
                return;
            }
            key = table.getSchema().get(maxColumn);
        }
        keyColumnIndex = table.getSchema().indexOf(key);

        if (columnUniqueness.get(keyColumnIndex) < getKeyUniquenessThreshold()) {
        	
        	if(isVerbose()) {
        		System.err.println(String.format("[TableKeyIdentification] Most unique column: '%s' - insufficient (%.4f)", table.getSchema().get(keyColumnIndex).getHeader(), columnUniqueness.get(keyColumnIndex)));
        	}
        	
            return;
        }

        if(isVerbose()) {
        	System.err.println(String.format("[TableKeyIdentification] Most unique column: '%s'", table.getSchema().get(keyColumnIndex).getHeader()));
        }
        table.setSubjectColumnIndex(keyColumnIndex);
    }
	
}
