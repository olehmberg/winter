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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;

/**
 * Contains the mapping of a Web Table to a Knowledge Base
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class TableMapping {

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
//    private HashMap<Integer, Pair<String, Double>> mappedProperties = new HashMap<>();
//    private HashMap<Integer, Pair<String, Double>> mappedInstances = new HashMap<>();
    private Pair<String, Double>[] mappedProperties;
    private Pair<String, Double>[] mappedInstances;
    private DataType[] dataTypes;
    private int keyIndex;
//    private HashMap<Integer, DataType> dataTypes = new HashMap<>();

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

    public Pair<String, Double>[] getMappedProperties() {
        return mappedProperties;
    }

    public void setMappedProperties(
            Pair<String, Double>[] mappedProperties) {
        this.mappedProperties = mappedProperties;
    }

    public Pair<String, Double> getMappedProperty(int index) {
    	if(mappedProperties==null || mappedProperties.length<=index) {
    		return null;
    	} else {
    		return mappedProperties[index];
    	}
    }
    
    @SuppressWarnings("unchecked")
	public void setMappedProperty(int index, Pair<String, Double> mapping) {
    	if(mappedProperties==null) {
    		mappedProperties = new Pair[index+1];
    	}
    	else if(mappedProperties.length<=index) {
    		mappedProperties = Arrays.copyOf(mappedProperties, index+1);
    	}
    	mappedProperties[index] = mapping;
    }

    public Pair<String, Double>[] getMappedInstances() {
        return mappedInstances;
    }
    
    public Pair<String, Double> getMappedInstance(int index) {
    	if(mappedInstances==null || mappedInstances.length<=index) {
    		return null;
    	} else {
    		return mappedInstances[index];
    	}
    }

    public void setMappedInstances(
            Pair<String, Double>[] mappedInstances) {
        this.mappedInstances = mappedInstances;
    }
    
    @SuppressWarnings("unchecked")
	public void setMappedInstance(int index, Pair<String, Double> mapping) {
    	if(mappedInstances==null) {
    		mappedInstances = new Pair[index+1];
    	}
    	else if(mappedInstances.length<=index) {
    		mappedInstances = Arrays.copyOf(mappedInstances, index+1);
    	}
    	mappedInstances[index] = mapping;
    }

    public int getKeyIndex() {
        return keyIndex;
    }

    public void setKeyIndex(int keyIndex) {
        this.keyIndex = keyIndex;
    }

    public DataType[] getDataTypes() {
        return dataTypes;
    }
    
    public DataType getDataType(int index) {
    	if(dataTypes==null || dataTypes.length<=index) {
    		return null;
    	} else {
    		return dataTypes[index];
    	}
    }
    
    public void setDataTypes(DataType[] dataTypes) {
        this.dataTypes = dataTypes;
    }
    
    public void setDataType(int index, DataType dataType) {
    	if(dataTypes==null) {
    		dataTypes = new DataType[index+1];
    	}
    	else if(dataTypes.length<=index) {
    		dataTypes = Arrays.copyOf(dataTypes, index+1);
    	}
    	dataTypes[index] = dataType;
    }
    
    public String getURI() {
        return URI;
    }

    public void setURI(String uRI) {
        URI = uRI;
    }
    
    public static TableMapping read(String fileName) throws IOException {
        TableMapping m = new TableMapping();
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
                        Pair<String, Double> p = getMappedProperty(i);

                        if (p == null) {
                            p = new Pair<String, Double>(values[i], 0.0);
                        } else {
                            p = new Pair<String, Double>(values[i],
                                    p.getSecond());
                        }

                        setMappedProperty(i, p);
                    }
                }
            }
        } else if (parts[0].equals(PROPERTY_SCORES)) {
            if (parts[1].length() > 2) {
                String data = parts[1].substring(1, parts[1].length() - 1);
                String[] values = data.split("\\|");

                for (int i = 0; i < values.length; i++) {
                    if (values[i] != null && !values[i].equals("")) {
                        Pair<String, Double> p = getMappedProperty(i);

                        try {
                            Double score = Double.parseDouble(values[i]);

                            if (p == null) {
                                p = new Pair<String, Double>("", score);
                            } else {
                                p = new Pair<String, Double>(p.getFirst(),
                                        score);
                            }

                            setMappedProperty(i, p);
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
                        Pair<String, Double> p = getMappedInstance(i);

                        if (p == null) {
                            p = new Pair<String, Double>(values[i], 0.0);
                        } else {
                            p = new Pair<String, Double>(values[i],
                                    p.getSecond());
                        }

                        setMappedInstance(i, p);
                    }
                }
            }
        } else if (parts[0].equals(INSTANCE_SCORES)) {
            if (parts[1].length() > 2) {
                String data = parts[1].substring(1, parts[1].length() - 1);
                String[] values = data.split("\\|");

                for (int i = 0; i < values.length; i++) {
                    if (values[i] != null && !values[i].equals("")) {
                        Pair<String, Double> p = getMappedInstance(i);

                        try {
                            Double score = Double.parseDouble(values[i]);

                            if (p == null) {
                                p = new Pair<String, Double>("", score);
                            } else {
                                p = new Pair<String, Double>(p.getFirst(),
                                        score);
                            }

                            setMappedInstance(i, p);
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
                    	setDataType(i, DataType.valueOf(values[i]));
//                        getDataTypes().put(i, DataType.valueOf(values[i]));
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

        if (getMappedProperties() != null && getMappedProperties().length > 0) {
            StringBuilder sbUris = new StringBuilder();
            StringBuilder sbConf = new StringBuilder();

            sbUris.append("{");
            sbConf.append("{");
            for (int i = 0; i <= numberOfColumns; i++) {

                if (i != 0) {
                    sbUris.append("|");
                    sbConf.append("|");
                }

                Pair<String, Double> mapping = getMappedProperty(i);
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

        if (getMappedInstances() != null && getMappedInstances().length > 0) {
            StringBuilder sbUris = new StringBuilder();
            StringBuilder sbConf = new StringBuilder();
          
            sbUris.append("{");
            sbConf.append("{");
            for (int i = 0; i <= numberOfRows; i++) {

                if (i != 0) {
                    sbUris.append("|");
                    sbConf.append("|");
                }

                Pair<String, Double> mapping = getMappedInstance(i);
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
            types.add(getDataType(i).toString());
        }
        w.write(String.format("%s=%s\n", DATA_TYPES, ListHandler.formatList(types)));
    }
	
}
