package de.uni_mannheim.informatik.dws.winter.model.defaultmodel.preprocessing;

import java.util.Map;

import org.slf4j.Logger;

import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.ValueDetectionType;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.ValueNormalizer;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.TypeDetector;

public class DataSetNormalizer<RecordType extends Record> {
	
	private static final Logger logger = WinterLogManager.getLogger();

	
	public void normalizeDataset(DataSet<RecordType, Attribute> dataSet, TypeDetector typeDetector){
		for(Attribute att: dataSet.getSchema().get()){
			ValueDetectionType columnType = this.detectColumnType(dataSet, att, typeDetector);
			this.normalizeColumn(columnType, dataSet, att);
		}
		logger.info("Type detection and normalization are done!");
	}
	
	public ValueDetectionType detectColumnType(DataSet<RecordType, Attribute> dataSet, Attribute att, TypeDetector typeDetector){
		
			String [] values = new String[dataSet.size()];
			int index = 0;
			for(RecordType record: dataSet.get()){
				values[index] = record.getValue(att);
				index++;
			}
			if(typeDetector != null){
				return (ValueDetectionType) typeDetector.detectTypeForColumn(values, att.getIdentifier());
			}
			else{
				logger.error("No type detector defined!");
				return null;
			}
		
	}
	
	public void normalizeColumn(ValueDetectionType columntype, DataSet<RecordType, Attribute> dataSet, Attribute att){
		ValueNormalizer valueNormalizer = new ValueNormalizer();
		for(RecordType record: dataSet.get()){
			
			Object value = valueNormalizer.normalize(record.getValue(att), columntype.getType(), columntype.getUnitCategory());
			
			if(value != null){
				record.setValue(att, value.toString()); 
			}
			
		}
		
	}

	public void normalizeDataset(DataSet<RecordType, Attribute> dataSet, Map<Attribute, ValueDetectionType> columnTypeMapping) {
		for(Attribute att: dataSet.getSchema().get()){
			ValueDetectionType columnType = columnTypeMapping.get(att);
			this.normalizeColumn(columnType, dataSet, att);
		}
		logger.info("Normalization done!");
	}

}
