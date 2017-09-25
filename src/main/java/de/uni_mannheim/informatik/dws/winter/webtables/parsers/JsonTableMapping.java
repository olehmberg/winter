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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;
import de.uni_mannheim.informatik.dws.winter.webtables.ListHandler;
import de.uni_mannheim.informatik.dws.winter.webtables.TableMapping;

// the JSON format uses this class' structure, but somehow krypo can't serialise it, so we use a different structure to store the same data in the table objects ...

public class JsonTableMapping {

    public static final String SOURCE = "#source";
    public static final String CLASS = "#class";
    public static final String CLASS_CONFIDENCE = "#classConf";
    public static final String NUM_HEADER_ROWS = "#numHeaderRows";
    public static final String PROPERTIES = "#properties";
    public static final String PROPERTY_SCORES = "#propertyScores";
    public static final String INSTANCES = "#instances";
    public static final String INSTANCE_SCORES = "#instanceScores";
    public static final String KEY_COLUMN = "#keyColumn";
    public static final String DATA_TYPES = "#dataTypes";

    public static final String[] VALID_ANNOTATIONS = new String[] { SOURCE, CLASS, CLASS_CONFIDENCE, NUM_HEADER_ROWS, PROPERTIES, PROPERTY_SCORES, INSTANCES, INSTANCE_SCORES, KEY_COLUMN, DATA_TYPES };
    
    private String URI;
    private String tableName;
    private int numHeaderRows;
    private Pair<String, Double> mappedClass;
    private Map<Integer, Pair<String, Double>> mappedProperties = new HashMap<>();
    private Map<Integer, Pair<String, Double>> mappedInstances = new HashMap<>();
    private int keyIndex;
    private Map<Integer, DataType> dataTypes = new HashMap<>();

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getNumHeaderRows() {
        return numHeaderRows;
    }

    public void setNumHeaderRows(int numHeaderRows) {
        this.numHeaderRows = numHeaderRows;
    }

    public Pair<String, Double> getMappedClass() {
        return mappedClass;
    }

    public void setMappedClass(Pair<String, Double> mappedClass) {
        this.mappedClass = mappedClass;
    }

    public Map<Integer, Pair<String, Double>> getMappedProperties() {
        return mappedProperties;
    }

    public void setMappedProperties(
            Map<Integer, Pair<String, Double>> mappedProperties) {
        this.mappedProperties = mappedProperties;
    }


    public Map<Integer, Pair<String, Double>> getMappedInstances() {
        return mappedInstances;
    }
    
    
    public void setMappedInstances(
            Map<Integer, Pair<String, Double>> mappedInstances) {
        this.mappedInstances = mappedInstances;
    }

    public int getKeyIndex() {
        return keyIndex;
    }

    public void setKeyIndex(int keyIndex) {
        this.keyIndex = keyIndex;
    }

    public Map<Integer, DataType> getDataTypes() {
        return dataTypes;
    }
    
    public void setDataTypes(Map<Integer, DataType> dataTypes) {
        this.dataTypes = dataTypes;
    }
    
    
    public String getURI() {
        return URI;
    }

    public void setURI(String uRI) {
        URI = uRI;
    }
    
    public static JsonTableMapping read(String fileName) throws IOException {
        JsonTableMapping m = new JsonTableMapping();
        m.readMapping(fileName);
        return m;
    }

    public void readMapping(String fileName) throws IOException {
        readMapping(new FileInputStream(fileName), fileName);
    }

    public void readMapping(InputStream is, String fileName) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(is));

        setTableName(new File(fileName).getName());

        String line = null;

        while ((line = r.readLine()) != null) {

            if (line.startsWith("#")) {
                parseMetadata(line);
            } else {
                break;
            }
        }

        r.close();
    }

    public void parseMetadata(String line) {
        String[] parts = new String[] { "", "" };
        for(String s : VALID_ANNOTATIONS) {
            if(line.startsWith(s+"=")) {
                parts[0] = s;
                
                if(line.length()==s.length()+1) {
                    parts[1] = "";
                } else {
                    parts[1] = line.substring(s.length()+1);
                }
                
                break;
            }
        }

        if(parts[1].equals("")) {
            return;
        }
        
        if (parts[0].equals(SOURCE) && parts.length > 1) {
            if(parts.length>1) {
                setURI(parts[1]);
            }
            else {
                setURI("");
            }            
        } else if (parts[0].equals(CLASS)) {
            Pair<String, Double> p = getMappedClass();

            String cls = parts[1].replace(".gz", "");

            if (p == null) {
                p = new Pair<String, Double>(cls, 0.0);
            } else {
                p = new Pair<String, Double>(cls, p.getSecond());

            }

            setMappedClass(p);
        } else if (parts[0].equals(CLASS_CONFIDENCE)) {
            Pair<String, Double> p = getMappedClass();

            Double conf = Double.parseDouble(parts[1]);

            if (p == null) {
                p = new Pair<String, Double>("", conf);
            } else {
                p = new Pair<String, Double>(p.getFirst(), conf);

            }

            setMappedClass(p);
        } else if (parts[0].equals(NUM_HEADER_ROWS)) {
            Integer num = Integer.parseInt(parts[1]);
            setNumHeaderRows(num);
        } else if (parts[0].equals(PROPERTIES)) {
            if (parts[1].length() > 2) {
                String data = parts[1].substring(1, parts[1].length() - 1);
                String[] values = data.split("\\|");

                for (int i = 0; i < values.length; i++) {
                    if (values[i] != null && !values[i].equals("")) {
                        Pair<String, Double> p = getMappedProperties().get(i);

                        if (p == null) {
                            p = new Pair<String, Double>(values[i], 0.0);
                        } else {
                            p = new Pair<String, Double>(values[i],
                                    p.getSecond());
                        }

                        getMappedProperties().put(i, p);
                    }
                }
            }
        } else if (parts[0].equals(PROPERTY_SCORES)) {
            if (parts[1].length() > 2) {
                String data = parts[1].substring(1, parts[1].length() - 1);
                String[] values = data.split("\\|");

                for (int i = 0; i < values.length; i++) {
                    if (values[i] != null && !values[i].equals("")) {
                        Pair<String, Double> p = getMappedProperties().get(i);

                        try {
                            Double score = Double.parseDouble(values[i]);

                            if (p == null) {
                                p = new Pair<String, Double>("", score);
                            } else {
                                p = new Pair<String, Double>(p.getFirst(),
                                        score);
                            }

                            getMappedProperties().put(i, p);
                        } catch (Exception e) {
                        }
                    }
                }
            }
        } else if ((parts[0].equals(INSTANCES))) {
            if (parts[1].length() > 2) {
                String data = parts[1].substring(1, parts[1].length() - 1);
                String[] values = data.split("\\|");

                for (int i = 0; i < values.length; i++) {
                    if (values[i] != null && !values[i].equals("")) {
                        Pair<String, Double> p = getMappedInstances().get(i);

                        if (p == null) {
                            p = new Pair<String, Double>(values[i], 0.0);
                        } else {
                            p = new Pair<String, Double>(values[i],
                                    p.getSecond());
                        }

                        getMappedInstances().put(i, p);
                    }
                }
            }
        } else if (parts[0].equals(INSTANCE_SCORES)) {
            if (parts[1].length() > 2) {
                String data = parts[1].substring(1, parts[1].length() - 1);
                String[] values = data.split("\\|");

                for (int i = 0; i < values.length; i++) {
                    if (values[i] != null && !values[i].equals("")) {
                        Pair<String, Double> p = getMappedInstances().get(i);

                        try {
                            Double score = Double.parseDouble(values[i]);

                            if (p == null) {
                                p = new Pair<String, Double>("", score);
                            } else {
                                p = new Pair<String, Double>(p.getFirst(),
                                        score);
                            }

                            getMappedInstances().put(i, p);
                        } catch (Exception e) {
                        }
                    }
                }
            }
        } else if (parts[0].equals(KEY_COLUMN)) {
            int idx = Integer.parseInt(parts[1]);
            setKeyIndex(idx);
        }  else if (parts[0].equals(DATA_TYPES)) {
            if (parts[1].length() > 2) {
                String[] values = ListHandler.splitList(parts[1]);

                for (int i = 0; i < values.length; i++) {
                    if (values[i] != null && !values[i].equals("")) {
                        getDataTypes().put(i, DataType.valueOf(values[i]));
                    }
                }
            }
        }
    }

    public void write(String fileName, int numberOfRows,int numberOfColumns) throws IOException {
        BufferedWriter w = new BufferedWriter(
                new FileWriter(new File(fileName)));

        write(w, numberOfRows, numberOfColumns);
        
        w.close();
    }
    
    public void write(Writer w, int numberOfRows, int numberOfColumns) throws IOException {
        w.write(String.format("%s=%s\n", SOURCE, getURI()));
        w.write(String.format("%s=%s\n", CLASS, getMappedClass().getFirst()));
        w.write(String.format("%s=%s\n", CLASS_CONFIDENCE,
                Double.toString(getMappedClass().getSecond())));
        w.write(String.format("%s=%d\n", KEY_COLUMN, getKeyIndex()));
        w.write(String.format("%s=%d\n", NUM_HEADER_ROWS, getNumHeaderRows()));

        if (getMappedProperties() != null && getMappedProperties().size() > 0) {
            StringBuilder sbUris = new StringBuilder();
            StringBuilder sbConf = new StringBuilder();

            sbUris.append("{");
            sbConf.append("{");
            for (int i = 0; i <= numberOfColumns; i++) {

                if (i != 0) {
                    sbUris.append("|");
                    sbConf.append("|");
                }

                Pair<String, Double> mapping = getMappedProperties().get(i);
                if (mapping != null) {
                    sbUris.append(mapping.getFirst());
                    sbConf.append(mapping.getSecond().toString());
                }

            }
            sbUris.append("}");
            sbConf.append("}");

            w.write(String.format("%s=%s\n", PROPERTIES, sbUris.toString()));
            w.write(String.format("%s=%s\n", PROPERTY_SCORES, sbConf.toString()));
        }

        if (getMappedInstances() != null && getMappedInstances().size() > 0) {
            StringBuilder sbUris = new StringBuilder();
            StringBuilder sbConf = new StringBuilder();
          
            sbUris.append("{");
            sbConf.append("{");
            for (int i = 0; i <= numberOfRows; i++) {

                if (i != 0) {
                    sbUris.append("|");
                    sbConf.append("|");
                }

                Pair<String, Double> mapping = getMappedInstances().get(i);
                if (mapping != null) {
                    sbUris.append(mapping.getFirst());
                    sbConf.append(mapping.getSecond().toString());
                }

            }
            sbUris.append("}");
            sbConf.append("}");

            w.write(String.format("%s=%s\n", INSTANCES, sbUris.toString()));
            w.write(String.format("%s=%s\n", INSTANCE_SCORES, sbConf.toString()));
        }
        
        List<String> types = new LinkedList<>();
        for(int i = 0; i < numberOfColumns; i++) {
            types.add(getDataTypes().get(i).toString());
        }
        w.write(String.format("%s=%s\n", DATA_TYPES, ListHandler.formatList(types)));
    }

    public static JsonTableMapping fromTableMapping(TableMapping mapping) {
    	JsonTableMapping m = new JsonTableMapping();
    	
    	if(mapping.getDataTypes()!=null) {
    		Map<Integer, DataType> dataTypes = new HashMap<>();
    		for(int i = 0; i < mapping.getDataTypes().length; i++) {
    			dataTypes.put(i, mapping.getDataTypes()[i]);
    		}
    		m.setDataTypes(dataTypes);
    	}
    	
    	m.setKeyIndex(mapping.getKeyIndex());
    	m.setMappedClass(mapping.getMappedClass());
    	
    	if(mapping.getMappedInstances()!=null && mapping.getMappedInstances().length>0) {
    		Map<Integer, Pair<String, Double>> mappedInstances = new HashMap<>();
    		for(int i = 0; i < mapping.getMappedInstances().length; i++) {
    			mappedInstances.put(i, mapping.getMappedInstances()[i]);
    		}
    		m.setMappedInstances(mappedInstances);
    	}
    	
    	if(mapping.getMappedProperties()!=null && mapping.getMappedProperties().length>0) {
    		Map<Integer, Pair<String, Double>> mappedProperties = new HashMap<>();
    		for(int i = 0; i < mapping.getMappedProperties().length; i++) {
    			mappedProperties.put(i, mapping.getMappedProperties()[i]);
    		}
    		m.setMappedProperties(mappedProperties);
    	}
    	
    	return m;
    }
    
    @SuppressWarnings("unchecked")
	public TableMapping toTableMapping() {
    	TableMapping tm = new TableMapping();
    	
    	if(dataTypes!=null && dataTypes.size()>0) {
	    	DataType[] dt = new DataType[Q.max(dataTypes.keySet())+1];
	    	for(int key : dataTypes.keySet()) {
	    		dt[key] = dataTypes.get(key);
	    	}
	    	tm.setDataTypes(dt);
    	}
    	
    	tm.setKeyIndex(keyIndex);
    	tm.setMappedClass(getMappedClass());
    	
    	if(mappedInstances!=null && mappedInstances.size()>0) {
	    	Pair<String, Double>[] inst = new Pair[Q.max(mappedInstances.keySet())+1];
	    	for(int key : mappedInstances.keySet()) {
	    		inst[key] = mappedInstances.get(key);
	    	}
	    	tm.setMappedInstances(inst);
    	}
    	
    	if(mappedProperties!=null && mappedProperties.size()>0) {
	    	Pair<String, Double>[] prop = new Pair[Q.max(mappedProperties.keySet())+1];
	    	for(int key : mappedProperties.keySet()) {
	    		prop[key] = mappedProperties.get(key);
	    	}
	    	tm.setMappedProperties(prop);
    	}
    	
    	tm.setNumHeaderRows(numHeaderRows);
    	tm.setTableName(tableName);
    	tm.setURI(URI);
    	
    	return tm;
    }
    
}
