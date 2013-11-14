package walmartlabs.productmatching.autorulegenerator.model;

import java.util.Map;

/**
 * Represents a pair of examples in the dataset and their match status.
 * 
 * @author excelsior
 *
 */
public class ExamplePair {
	// All the attribute name and value pairs for the first item in itempair
	private Map<Feature, String> item1FeatureValMap = null;
	// All the attribute name and value pairs for the second item in itempair
	private Map<Feature, String> item2FeatureValMap = null;
	
	// The classification label for this itempair.
	private DecisionTreeClassLabel classLabel = DecisionTreeClassLabel.MISMATCH;

	private Map<Feature, Double> attributeMatchScoreMap = null;
	
	public ExamplePair(Map<Feature, String> item1FeatureValMap, Map<Feature, String> item2FeatureValMap, 
			DecisionTreeClassLabel classLabel)
	{
		this.item1FeatureValMap = item1FeatureValMap;
		this.item2FeatureValMap = item2FeatureValMap;
		this.classLabel = classLabel;
		
		// calculate the score for each attribute and persist in a map
		// TODO
	}
	
	public Map<Feature, String> getItem1FeatureValMap() {
		return item1FeatureValMap;
	}

	public void setItem1FeatureValMap(Map<Feature, String> item1FeatureValMap) {
		this.item1FeatureValMap = item1FeatureValMap;
	}

	public Map<Feature, String> getItem2FeatureValMap() {
		return item2FeatureValMap;
	}

	public void setItem2FeatureValMap(Map<Feature, String> item2FeatureValMap) {
		this.item2FeatureValMap = item2FeatureValMap;
	}

	public DecisionTreeClassLabel getClassLabel() {
		return classLabel;
	}

	public void setClassLabel(DecisionTreeClassLabel classLabel) {
		this.classLabel = classLabel;
	}

	public Map<Feature, Double> getAttributeMatchScoreMap() {
		return attributeMatchScoreMap;
	}

	public void setAttributeMatchScoreMap(Map<Feature, Double> attributeMatchScoreMap) {
		this.attributeMatchScoreMap = attributeMatchScoreMap;
	}
	
	/**
	 * Gets the match score for an attribute in the current itempair.
	 * 
	 *  Specifically, it measures how similar the attribute values are to each other for two
	 *  different items in the itempair.
	 */
	public double getAttributeMatchScore(Feature feature)
	{
		double matchScore = -1.0;
		if(attributeMatchScoreMap.containsKey(feature)) {
			matchScore = attributeMatchScoreMap.get(feature);
		}
		
		return matchScore;
	}
}
