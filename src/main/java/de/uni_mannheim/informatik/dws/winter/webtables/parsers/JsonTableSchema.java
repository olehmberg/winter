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
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;

/**
 * Represents the JSON schema of the Web Table data. This class is used to parse from the JSON format or write to it.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class JsonTableSchema {

	    public enum HeaderPosition {
	        FIRST_ROW
	    }

	    public enum TableType {
	        RELATION
	    }

	    public enum TableOrientation {
	        HORIZONTAL, VERTICAL
	    }
	    
	    public static class Dependency {
	    	private int[] determinant;
	    	private int[] dependant;
	    	private double probability;
			public int[] getDeterminant() {
				return determinant;
			}
			public void setDeterminant(int[] determinant) {
				this.determinant = determinant;
			}
			public int[] getDependant() {
				return dependant;
			}
			public void setDependant(int[] dependant) {
				this.dependant = dependant;
			}
			public double getProbability() {
				return probability;
			}
			public void setProbability(double probability) {
				this.probability = probability;
			}
			
			public Dependency() {}
			public Dependency(int[] determinant, int[] dependant, double probability) {
				this.determinant = determinant;
				this.dependant = dependant;
				this.probability = probability;
			}
	    }

	    private String[][] relation;
	    private String pageTitle;
	    private String title;
	    private String url;
	    private boolean hasHeader;
	    private HeaderPosition headerPosition;
	    private TableType tableType;
	    private int tableNum;
	    private String s3Link;
	    private int recordEndOffset;
	    private int recordOffset;
	    private TableOrientation tableOrientation;
	    private String TableContextTimeStampBeforeTable;
	    private String TableContextTimeStampAfterTable;
	    private String lastModified;
	    private String textBeforeTable;
	    private String textAfterTable;
	    private boolean hasKeyColumn;
	    private int keyColumnIndex;
	    private int headerRowIndex;
		private Dependency[] functionalDependencies;
	    private Integer[][] candidateKeys;
	    private String[][] rowProvenance;
	    private String[][] columnProvenance;
	    private int tableId;

	    public String[][] getRelation() {
	        return relation;
	    }

	    public void setRelation(String[][] relation) {
	        this.relation = relation;
	    }

	    public String getPageTitle() {
	        return pageTitle;
	    }

	    public void setPageTitle(String pageTitle) {
	        this.pageTitle = pageTitle;
	    }

	    public String getTitle() {
	        return title;
	    }

	    public void setTitle(String title) {
	        this.title = title;
	    }

	    public String getUrl() {
	        return url;
	    }

	    public void setUrl(String url) {
	        this.url = url;
	    }

	    public boolean isHasHeader() {
	        return hasHeader;
	    }

	    public void setHasHeader(boolean hasHeader) {
	        this.hasHeader = hasHeader;
	    }

	    public HeaderPosition getHeaderPosition() {
	        return headerPosition;
	    }

	    public void setHeaderPosition(HeaderPosition headerPosition) {
	        this.headerPosition = headerPosition;
	    }

	    public TableType getTableType() {
	        return tableType;
	    }

	    public void setTableType(TableType tableType) {
	        this.tableType = tableType;
	    }

	    public int getTableNum() {
	        return tableNum;
	    }

	    public void setTableNum(int tableNum) {
	        this.tableNum = tableNum;
	    }

	    public String getS3Link() {
	        return s3Link;
	    }

	    public void setS3Link(String s3Link) {
	        this.s3Link = s3Link;
	    }

	    public int getRecordEndOffset() {
	        return recordEndOffset;
	    }

	    public void setRecordEndOffset(int recordEndOffset) {
	        this.recordEndOffset = recordEndOffset;
	    }

	    public int getRecordOffset() {
	        return recordOffset;
	    }

	    public void setRecordOffset(int recordOffset) {
	        this.recordOffset = recordOffset;
	    }

	    public TableOrientation getTableOrientation() {
	        return tableOrientation;
	    }

	    public void setTableOrientation(TableOrientation tableOrientation) {
	        this.tableOrientation = tableOrientation;
	    }

	    public String getTableContextTimeStampBeforeTable() {
	        return TableContextTimeStampBeforeTable;
	    }

	    public void setTableContextTimeStampBeforeTable(
	            String tableContextTimeStampBeforeTable) {
	        TableContextTimeStampBeforeTable = tableContextTimeStampBeforeTable;
	    }

	    public String getTextBeforeTable() {
	        return textBeforeTable;
	    }

	    public void setTextBeforeTable(String textBeforeTable) {
	        this.textBeforeTable = textBeforeTable;
	    }

	    public String getTextAfterTable() {
	        return textAfterTable;
	    }

	    public void setTextAfterTable(String textAfterTable) {
	        this.textAfterTable = textAfterTable;
	    }

	    public boolean isHasKeyColumn() {
	        return hasKeyColumn;
	    }

	    public void setHasKeyColumn(boolean hasKeyColumn) {
	        this.hasKeyColumn = hasKeyColumn;
	    }

	    public int getKeyColumnIndex() {
	        return keyColumnIndex;
	    }

	    public void setKeyColumnIndex(int keyColumnIndex) {
	        this.keyColumnIndex = keyColumnIndex;
	    }

	    public int getHeaderRowIndex() {
	        return headerRowIndex;
	    }

	    public void setHeaderRowIndex(int headerRowIndex) {
	        this.headerRowIndex = headerRowIndex;
	    }

	    public String[][] getRowProvenance() {
			return rowProvenance;
		}

		public void setRowProvenance(String[][] rowProvenance) {
			this.rowProvenance = rowProvenance;
		}

		public String[][] getColumnProvenance() {
			return columnProvenance;
		}

		public void setColumnProvenance(String[][] columnProvenance) {
			this.columnProvenance = columnProvenance;
		}

		public int getNumberOfHeaderRows() {
	        if (tableType != TableType.RELATION
	                || tableOrientation != TableOrientation.HORIZONTAL
	                || !hasHeader || headerPosition != HeaderPosition.FIRST_ROW) {
	            return 0;
	        } else {
	        	return headerRowIndex+1;
	        }
	    }

	    public String[] getColumnHeaders() {
	        if (tableType != TableType.RELATION
	                || tableOrientation != TableOrientation.HORIZONTAL
	                || !hasHeader) {
	            return null;
	        }

	        String[] headers = null;

	        if(headerPosition!=null) {
	            switch (headerPosition) {
	            case FIRST_ROW:
	                headers = new String[relation.length];
	    
	                for (int col = 0; col < relation.length; col++) {
	                	headers[col] = relation[col][headerRowIndex];
	                }
	                break;
	            default:
	    
	            }
	        }

	        return headers;
	    }
	    
	    /**
		 * @return the functionalDependencies
		 */
		public Dependency[] getFunctionalDependencies() {
			return functionalDependencies;
		}
		
		/**
		 * @param functionalDependencies the functionalDependencies to set
		 */
		public void setFunctionalDependencies(
				Dependency[] functionalDependencies) {
			this.functionalDependencies = functionalDependencies;
		}
		
		/**
		 * @return the candidateKeys
		 */
		public Integer[][] getCandidateKeys() {
			return candidateKeys;
		}
		
		/**
		 * @param candidateKeys the candidateKeys to set
		 */
		public void setCandidateKeys(Integer[][] candidateKeys) {
			this.candidateKeys = candidateKeys;
		}
	    
	    public static JsonTableSchema fromJson(File file) throws IOException {
	        Gson gson = new Gson();
	        
	        FileReader reader = new FileReader(file);
	        // get the data from the JSON source
	        JsonTableSchema data = gson.fromJson(reader, JsonTableSchema.class);
	        
	        reader.close();
	        
	        return data;
	    }
	    
	    public void transposeRelation() {
	        int colNum = 0;
	        
	        for(int i = 0; i < relation.length; i++) {
	            colNum = Math.max(colNum, relation[i].length);
	        }
	        
	        String[][] newRelation = new String[colNum][relation.length];
	        
	        for(int i = 0; i < relation.length; i++) {
	            for(int j = 0; j < colNum; j++) {
	                
	                if(j < relation[i].length) {
	                    newRelation[j][i] = relation[i][j];
	                }
	                
	            }
	        }
	        
	        relation = newRelation;
	    }

		/**
		 * @return the tableId
		 */
		public int getTableId() {
			return tableId;
		}

		/**
		 * @param tableId the tableId to set
		 */
		public void setTableId(int tableId) {
			this.tableId = tableId;
		}

	    public String getTableContextTimeStampAfterTable() {
			return TableContextTimeStampAfterTable;
		}

		public void setTableContextTimeStampAfterTable(String tableContextTimeStampAfterTable) {
			TableContextTimeStampAfterTable = tableContextTimeStampAfterTable;
		}

		public String getLastModified() {
			return lastModified;
		}

		public void setLastModified(String lastModified) {
			this.lastModified = lastModified;
		}	    
}
