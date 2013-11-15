package walmartlabs.productmatching.autorulegenerator.utils.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import walmartlabs.productmatching.autorulegenerator.model.Dataset;
import walmartlabs.productmatching.autorulegenerator.model.Example;
import walmartlabs.productmatching.autorulegenerator.model.ExamplePair;
import walmartlabs.productmatching.autorulegenerator.model.Feature;
import walmartlabs.productmatching.autorulegenerator.model.Feature.DataType;

/**
 * Reads dataset from a file in the standard arff format 
 * (http://weka.wikispaces.com/ARFF+%28stable+version%29).
 * @author excelsior
 *
 */
public class ArffDatasetReader extends DatasetReader {

	private static String DATASET_NAME_IDENTIFIER = "@relation";
	private static String ATTRIBUTE_IDENTIFIER = "@attribute";
	private static String DATA_IDENTIFIER = "@data";
	
	private static String WHITESPACE_DELIMITER = " ";
	private static String VALUE_DELIMITER = "\",";
	
	public Dataset parseDataset(File dataFile, List<String> idKeys) {
		String datasetName = null;
		List<Feature> features = Lists.newArrayList();
		List<ExamplePair> exPairs = Lists.newArrayList();
		List<Example> examples = Lists.newArrayList();
		
		BufferedReader br = null; 
		try {
 			String currLine; 	
 			br = new BufferedReader(new FileReader(dataFile));
 			boolean isDataReadStarted = false;
 			while ((currLine = br.readLine()) != null) {
 				currLine = currLine.trim();
 				if(currLine.trim().isEmpty()) {
 					continue;
 				}
 				
 				// Collect metadata before data read starts
 				if(!isDataReadStarted) {
 	 				if(currLine.startsWith(DATASET_NAME_IDENTIFIER)) {
 	 					String[] temp = currLine.split(WHITESPACE_DELIMITER);
 	 					datasetName = temp[1].trim().replace("\"", "");
 	 				}
 	 				else if(currLine.startsWith(ATTRIBUTE_IDENTIFIER)) {
 	 					String[] temp = currLine.split(WHITESPACE_DELIMITER);
 	 					String attrName = temp[1].trim();
 	 					String attrType = temp[2].trim();
 	 					
  	 					DataType attrDataType = DataType.getDataType(attrType);
	 					features.add(new Feature(attrName, attrDataType));
 	 				}
 	 				else if(currLine.startsWith(DATA_IDENTIFIER)) {
 	 					isDataReadStarted = true;
 	 				} 					
 				}
 				// Start collecting the actual data
 				else {
 					String temp[] = currLine.split(VALUE_DELIMITER);
 					assert(temp.length == features.size());

 					Map<Feature, String> featureValMap = Maps.newHashMap();
 					int index = 0;
 					for(String featureValue : temp) {
 						featureValue = featureValue.trim().replace("\"", "").replace("'", "");
 						Feature feature = features.get(index);
 						
 						featureValMap.put(feature, featureValue);
 						++index;
 					}
 					
 					Example ex = new Example(featureValMap);
 					ex.setId(getIdForExample(idKeys, ex));
 					examples.add(ex);
 				}
 			}
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
		
		System.out.println("Collected " + examples.size() + " examples. ");
		try {
			exPairs = getExamplePairs(examples, idKeys);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new Dataset(datasetName, features, exPairs);
		
	}
	
}
