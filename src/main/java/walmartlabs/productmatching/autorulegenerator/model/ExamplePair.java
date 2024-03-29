package walmartlabs.productmatching.autorulegenerator.model;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

/**
 * Represents a pair of examples in the dataset and their match status.
 * 
 * @author excelsior
 *
 */
public class ExamplePair {
	private Example sourceItem;
	private Example targetItem;
	
	// The classification label for this itempair.
	private DecisionTreeClassLabel classLabel = DecisionTreeClassLabel.MISMATCH;

	private Map<Feature, Double> attributeMatchScoreMap = null;
	
	public ExamplePair(Example sourceItem, Example targetItem, DecisionTreeClassLabel classLabel)
	{
		this.sourceItem = sourceItem;
		this.targetItem = targetItem;
		this.classLabel = classLabel;
		
		// calculate the score for each attribute and persist in a map
		attributeMatchScoreMap = Maps.newHashMap();
		Map<Feature, Double> tempAttributeMatchScoreMap = calculateAttributeMatchScoreMap(sourceItem, targetItem);
		attributeMatchScoreMap.putAll(tempAttributeMatchScoreMap);
	}
	
	public Example getSourceItem() {
		return sourceItem;
	}

	public void setSourceItem(Example sourceItem) {
		this.sourceItem = sourceItem;
	}

	public Example getTargetItem() {
		return targetItem;
	}

	public void setTargetItem(Example targetItem) {
		this.targetItem = targetItem;
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

	/**
	 * Calculate the similiarity score for each attribute in the example pair by comparing the
	 * value in source and target item for the current attribute.
	 * 
	 * @param sourceItem
	 * @param targetItem
	 * @return
	 */
	private static Map<Feature, Double> calculateAttributeMatchScoreMap(Example sourceItem, Example targetItem) 
	{
		Map<Feature, String> sourceItemAttrValMap = sourceItem.getFeatureValueMap();
		Map<Feature, String> targetItemAttrValMap = targetItem.getFeatureValueMap();
		
		Map<Feature, Double> attrSimScoreMap = Maps.newHashMap();
		Set<Feature> features = sourceItemAttrValMap.keySet();
		for(Feature f : features) {
			String sourceAttrVal = sourceItemAttrValMap.get(f);
			String targetAttrVal = targetItemAttrValMap.get(f);
			double simScore = f.getSimMeasure().compare(sourceAttrVal, targetAttrVal);
			attrSimScoreMap.put(f, simScore);
		}
		
		return attrSimScoreMap;
	}
	
	/**
	 * Gets the match score for an attribute in the current itempair.
	 * 
	 *  Specifically, it measures how similar the attribute values are to each other for two
	 *  different items in the itempair.
	 */
	public double getAttributeMatchScore(Feature feature)
	{
		double matchScore = 0.0;
		if(attributeMatchScoreMap.containsKey(feature)) {
			matchScore = attributeMatchScoreMap.get(feature);
		}
		
		return matchScore;
	}
	
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ID").append("|#").append(sourceItem.getId()).append("|#").append(targetItem.getId()).append("|#").append("\n");
		Map<Feature, String> sourceItemAttrMap = sourceItem.getFeatureValueMap();
		Map<Feature, String> targetItemAttrMap = targetItem.getFeatureValueMap();
		
		Set<Feature> features = sourceItemAttrMap.keySet();
		//features.addAll(targetItemAttrMap.keySet());
		for(Feature feature : features) {
			Double score = getAttributeMatchScore(feature);
			String attrName = feature.getAttrName();
			String sourceVal = sourceItem.getValueForAttribute(attrName);
			String targetVal = targetItem.getValueForAttribute(attrName);
			builder.append(score).append(" ==> ");
			builder.append(attrName).append("|#").append(sourceVal).append("|#").append(targetVal).append("|#").append("\n");
		}
		builder.append("\n");
		
		return builder.toString();
	}
}
