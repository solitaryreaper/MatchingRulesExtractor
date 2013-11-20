package walmartlabs.productmatching.autorulegenerator.utils.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import walmartlabs.productmatching.autorulegenerator.model.Dataset;
import walmartlabs.productmatching.autorulegenerator.model.DecisionTreeClassLabel;
import walmartlabs.productmatching.autorulegenerator.model.Example;
import walmartlabs.productmatching.autorulegenerator.model.ExamplePair;
import walmartlabs.productmatching.autorulegenerator.model.Feature;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.walmart.productgenome.pairComparison.utils.comparers.ComparersFactory;
import com.walmart.productgenome.pairComparison.utils.comparers.IComparer;

/**
 * Reads the dataset containing itempairs to be matched.
 * 
 * @author excelsior
 *
 */
public class ItemPairDatasetReader {

	private static String DATASET_NAME_IDENTIFIER = "@relation";
	private static String ATTRIBUTE_IDENTIFIER = "@attribute";
	private static String DATA_IDENTIFIER = "@data";
	
	private static String VALUE_DELIMITER = "\\|#";
	private static String WHITESPACE_DELIMITER = " ";
	
	public static Dataset parseDataset(File matchFile, File mismatchFile, String datasetName)
	{
		Dataset matchDataset = parseFile(matchFile, DecisionTreeClassLabel.MATCH);
		Dataset mismatchDataset = parseFile(mismatchFile, DecisionTreeClassLabel.MISMATCH);
		
		List<ExamplePair> allItemPairs = Lists.newArrayList();
		allItemPairs.addAll(matchDataset.getExamplePairs());
		allItemPairs.addAll(mismatchDataset.getExamplePairs());
		
		System.out.println("Dataset Name : " + datasetName);
		System.out.println("#Features : " + matchDataset.getFeatures().size());
		System.out.println("#Matched Pairs : " + matchDataset.getExamplePairs().size());
		System.out.println("#Mismatched Pairs : " + mismatchDataset.getExamplePairs().size());
		
		return new Dataset(datasetName, matchDataset.getFeatures(), allItemPairs);
	}
	
	private static Dataset parseFile(File file, DecisionTreeClassLabel label)
	{
		List<ExamplePair> exPairs = Lists.newArrayList();
		List<Feature> allFeatures = Lists.newArrayList();
		String datasetName = null;
		
		ExamplePair exPair = null;
		Example sourceEx = null;
		Example targetEx = null;
		Map<Feature, String> sourceFeatureValMap = null;
		Map<Feature, String> targetFeatureValMap = null;
		String sourceId = null;
		String targetId = null;
		
		BufferedReader br = null; 
		boolean isDataReadStarted = false;
		try {
 			String currLine; 	
 			br = new BufferedReader(new FileReader(file));
			while ((currLine = br.readLine()) != null) {
				currLine = currLine.trim();
				if(currLine.isEmpty()) {
					if(sourceFeatureValMap != null) {
						sourceEx = new Example(sourceId, sourceFeatureValMap);
						targetEx = new Example(targetId, targetFeatureValMap);
						exPair = new ExamplePair(sourceEx, targetEx, label);
						exPairs.add(exPair);
					}
					
					exPair = null;
					sourceEx = null;
					targetEx = null;
					sourceId = null;
					targetId = null;
					sourceFeatureValMap = null;
					targetFeatureValMap = null;
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
 	 					
 	 					allFeatures.addAll(getFeaturesForAttributeName(attrName));
 	 				}
 	 				else if(currLine.startsWith(DATA_IDENTIFIER)) {
 	 					isDataReadStarted = true;
 	 				} 					
 				}
 				else {
 					if(currLine.startsWith("ID")) {
 						String[] temp = currLine.split(VALUE_DELIMITER);
 						sourceId = temp[1].trim();
 						targetId = temp[2].trim();
 						
 						sourceFeatureValMap = Maps.newHashMap();
 						targetFeatureValMap = Maps.newHashMap();
 					}
 					else {
 						String[] temp = currLine.split(VALUE_DELIMITER);
 						String attrKey = temp[0].trim();
 						String sourceAttrValue = "NA";
 						String targetAttrValue = "NA";
 						if(temp.length > 1) {
 							sourceAttrValue = temp[1];
 						}
 						if(temp.length > 2) {
 							targetAttrValue = temp[2];
 						}
 						
 						List<Feature> attrFeatures = findFeaturesByAttrName(attrKey, allFeatures);
 						for(Feature f : attrFeatures) {
 	 						sourceFeatureValMap.put(f, sourceAttrValue);
 	 						targetFeatureValMap.put(f, targetAttrValue); 							
 						}
 					}
 				}
			}
 			
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
		
		return new Dataset(datasetName, allFeatures, exPairs);
	}
	
	/**
	 * Get all the relevant features for an attribute name. 
	 */
	private static List<Feature> getFeaturesForAttributeName(String attrName)
	{
		List<Feature> features = Lists.newArrayList();
		for(IComparer comparer : ComparersFactory.MATCH_COMPARERS) {
			features.add(new Feature(attrName, comparer));
		}
		
		return features;
	}
	
	private static List<Feature> findFeaturesByAttrName(String attrName, List<Feature> allFeatures)
	{
		List<Feature> features = Lists.newArrayList();
		for(Feature f : allFeatures) {
			if(f.getAttrName().equals(attrName)) {
				features.add(f);
			}
		}
		
		return features;
	}
}
