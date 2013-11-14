package walmartlabs.productmatching.autorulegenerator.utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.util.CollectionUtils;

import walmartlabs.productmatching.autorulegenerator.model.DecisionTreeClassLabel;
import walmartlabs.productmatching.autorulegenerator.model.DecisionTreeNode;
import walmartlabs.productmatching.autorulegenerator.model.DecisionTreeNodeType;
import walmartlabs.productmatching.autorulegenerator.model.ExamplePair;
import walmartlabs.productmatching.autorulegenerator.model.Feature;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.walmart.productgenome.pairComparison.model.rule.ItemMatchRule;
import com.walmart.productgenome.pairComparison.model.rule.MatchEntityPair;

/**
 * Utility class for decision tree processing.
 * 
 * @author excelsior
 *
 */
public class DecisionTreeUtils {

	private static String LESS_THAN_VALUE = "<=";
	private static String GREATER_THAN_VALUE = ">";
	private static String MISSING_VALUE = "?";
	
	/**
	 * Learn the decision tree so that it efficiently fits the training dataset.
	 * 
	 * This decision tree represents various rules for matching/mismatching an itempair via its
	 * branches.
	 * 
	 * @param examples
	 */
	public static DecisionTreeNode learnRuleDecisionTree(List<ExamplePair> examplePairs, 
			List<Feature> features, int leafThreshold)
	{
		DecisionTreeClassLabel defaultClassLabel = DecisionTreeClassLabel.MISMATCH;
		// Returns a leaf node with default class label if examples is empty 
		if(CollectionUtils.isEmpty(examplePairs)) {
			return createLeafNode(examplePairs, features, defaultClassLabel);
		}
		
		Map<DecisionTreeClassLabel, Integer> classLabelsMap = getClassLabelsMap(examplePairs);
		DecisionTreeClassLabel majorityClassLabel = getMajorityClassLabel(classLabelsMap);
		
		// Returns a leaf node with majority class label if features is empty
		if(CollectionUtils.isEmpty(features)) {
			return createLeafNode(examplePairs, features, majorityClassLabel);
		}
		
		// Return if there is a pure classification
		if(classLabelsMap.size() == 1) {
			DecisionTreeClassLabel pureClassLabel = Lists.newArrayList(classLabelsMap.keySet()).get(0);
			return createLeafNode(examplePairs, features, pureClassLabel);
		}
		
		// Check if number of instances left is less than the leaf threshold
		if(examplePairs.size() < leafThreshold) {
			if(majorityClassLabel != null) {
				return createLeafNode(examplePairs, features, majorityClassLabel);
			}
			else {
				return createLeafNode(examplePairs, features, defaultClassLabel);
			}
		}
		
		// Choose the best feature
		Feature bestFeature = getBestFeature(examplePairs, features);
		if(bestFeature == null) {
			return createLeafNode(examplePairs, features, majorityClassLabel);
		}
		
		DecisionTreeNode root = createFeatureNode(examplePairs, features, bestFeature);
		
		// All the various branches emanating downwards from this decision node
		//							BEST FEATURE
		//								|
		//								|
		//							Best Split Value = X
		//			------------------------------------------
		//			|					|					|
		//		MISMATCH (<= X)		MISSING					MATCH ( > X)
		List<DecisionTreeNode> featureValueNodes = Lists.newArrayList();
		
		double bestFeatureSplitValue = getBestSplitThreshold(examplePairs, bestFeature);
		
		// Child node to represent the MISMATCH branch for current best feature
		List<ExamplePair> lessThanValueExPairs = 
				getExamplePairsWithFeatureValue(examplePairs, bestFeature, bestFeatureSplitValue, LESS_THAN_VALUE);
		DecisionTreeNode lessThanValueNode = learnRuleDecisionTree(lessThanValueExPairs, features, leafThreshold);
		lessThanValueNode.setParentFeatureName(bestFeature);
		lessThanValueNode.setParentFeatureLinkValue(bestFeatureSplitValue);
		featureValueNodes.add(lessThanValueNode);
		
		// Child node to represent the MATCH branch for current best feature
		List<ExamplePair> greaterThanValueExPairs = 
				getExamplePairsWithFeatureValue(examplePairs, bestFeature, bestFeatureSplitValue, GREATER_THAN_VALUE);
		DecisionTreeNode greaterThanValueNode = learnRuleDecisionTree(greaterThanValueExPairs, features, leafThreshold);
		greaterThanValueNode.setParentFeatureName(bestFeature);
		greaterThanValueNode.setParentFeatureLinkValue(bestFeatureSplitValue);
		featureValueNodes.add(greaterThanValueNode);
		
		// Child node to represent MISSING branch for current best feature
		List<ExamplePair> missingValueExPairs = 
				getExamplePairsWithFeatureValue(examplePairs, bestFeature, bestFeatureSplitValue, MISSING_VALUE);
		DecisionTreeNode missingValueNode = learnRuleDecisionTree(missingValueExPairs, features, leafThreshold);
		missingValueNode.setParentFeatureName(bestFeature);
		missingValueNode.setParentFeatureLinkValue(bestFeatureSplitValue);
		featureValueNodes.add(missingValueNode);
		
		root.setChildNodes(featureValueNodes);
		return root;
	}
	
	/**
	 * Determines the best feature using information gain metric for the current set of examples. 
	 */
	private static Feature getBestFeature(List<ExamplePair> examplePairs, List<Feature> features)
	{
		Feature bestFeature = null;
		double maxInfoGain = -999;
		
		for(Feature feature : features) {
			double infoGain = getInfoGainNumericFeature(examplePairs, feature);
			if(Double.compare(infoGain, maxInfoGain) > 0) {
				maxInfoGain = infoGain;
				bestFeature = feature;
			}
		}
		
		if(Double.compare(maxInfoGain, 0) <= 0) {
			bestFeature = null;
		}
		
		return bestFeature;
	}
	
	/**
	 * Determines the information gain for a numeric feature 
	 */
	private static double getInfoGainNumericFeature(List<ExamplePair> examplePairs, Feature feature)
	{
		double totalInfo = getInfo(examplePairs);
		
		double bestSplitValue = getBestSplitThreshold(examplePairs, feature);
		// Failed to find a good split threshold value
		if (bestSplitValue < 0) {
			return 0.0;
		}
		
		double featureInfo = getInfoForNumericValueSplit(examplePairs, feature, bestSplitValue);
		
		return totalInfo - featureInfo;
	}
	
	/**
	 * Calculate the information gain for a numeric feature with a specific split value. 
	 */
	private static double getInfoForNumericValueSplit(List<ExamplePair> examplePairs, Feature feature, double splitValue)
	{
		List<ExamplePair> lessThanValueExPairs = getExamplePairsWithFeatureValue(examplePairs, feature, splitValue, LESS_THAN_VALUE);
		List<ExamplePair> greaterThanValueExPairs = getExamplePairsWithFeatureValue(examplePairs, feature, splitValue, GREATER_THAN_VALUE);
		List<ExamplePair> missingValueExPairs = getExamplePairsWithFeatureValue(examplePairs, feature, splitValue, MISSING_VALUE);
		
		int totalExPairsCnt = examplePairs.size();
		int lessThanValueExPairsCnt = lessThanValueExPairs.size();
		int greaterThanValueExPairsCnt = greaterThanValueExPairs.size();
		int missingValueExPairsCnt = missingValueExPairs.size();
		
		double featureInfo = 0.0;
		featureInfo += (lessThanValueExPairsCnt/(double)totalExPairsCnt)*getInfo(lessThanValueExPairs);
		featureInfo += (greaterThanValueExPairsCnt/(double)totalExPairsCnt)*getInfo(greaterThanValueExPairs);
		featureInfo += (missingValueExPairsCnt/(double)totalExPairsCnt)*getInfo(missingValueExPairs);
		
		return featureInfo;
		
	}
	
	/**
	 * Determines the best numeric split threshold for maximizing the information gain for the
	 * current example pairs. 
	 */
	private static double getBestSplitThreshold(List<ExamplePair> exPairs, Feature feature)
	{
		double bestSplitThreshold = -999.0;
		double bestInfoGain = -999.0;
		
		List<Double> candidateSplitValues = getCandidateSplitValues(exPairs, feature);
		if(CollectionUtils.isEmpty(candidateSplitValues)) {
			return bestSplitThreshold;
		}
		
		for(Double splitValue : candidateSplitValues) {
			double infoGain = getInfo(exPairs) - getInfoForNumericValueSplit(exPairs, feature, splitValue);
			if(Double.compare(infoGain, bestInfoGain) > 0) {
				bestInfoGain = infoGain;
				bestSplitThreshold = splitValue;
			}
		}
		
		return bestSplitThreshold;
	}
	
	/**
	 * Determine all the possible split points in the range of values of this numeric feature.
	 * 
	 * A good split point is one which efficiently segregates example pairs of different classes.
	 */
	private static List<Double> getCandidateSplitValues(List<ExamplePair> exPairs, Feature feature)
	{
		List<Double> candidateSplitValues = Lists.newArrayList();
		
		// Get a distribution of the various values for this feature in the set of example pairs
		Map<Double, List<ExamplePair>> valueExPairsMap = Maps.newTreeMap();
		for(ExamplePair exPair : exPairs) {
			double featureValue = exPair.getAttributeMatchScore(feature);
			List<ExamplePair> featureValueExPairs = null;
			if(valueExPairsMap.containsKey(featureValue)) {
				featureValueExPairs = valueExPairsMap.get(featureValue);
			}
			else {
				featureValueExPairs = Lists.newArrayList();
			}
			
			featureValueExPairs.add(exPair);
			valueExPairsMap.put(featureValue, featureValueExPairs);
		}
		
		// Get the sorted list of feature values
		List<Double> featureValues = Lists.newArrayList(valueExPairsMap.keySet());
		if(featureValues.size() <= 1) {
			return candidateSplitValues;
		}

		double prevFeatureValue = candidateSplitValues.get(0);
		for(Double value : featureValues) {
			double nextFeatureValue = value;
			List<ExamplePair> prevFeatureValueExPairs = valueExPairsMap.get(prevFeatureValue);
			List<ExamplePair> nextFeatureValueExPairs = valueExPairsMap.get(nextFeatureValue);
			
			boolean isEligibleForSplit = isEligibleForNumericSplit(prevFeatureValueExPairs, nextFeatureValueExPairs);
			if(isEligibleForSplit) {
				candidateSplitValues.add((prevFeatureValue + nextFeatureValue)/2.0);
			}
		}
		
		return candidateSplitValues;
	}
	
	/**
	 * A numeric split is allowed if the example fairs for two consecutive values for a feature
	 * have example pairs of different class labels. 
	 */
	private static boolean isEligibleForNumericSplit(List<ExamplePair> prevValExPairs, List<ExamplePair> nextValExPairs)
	{
		boolean isEligibleForSplit = false;
		Map<DecisionTreeClassLabel, Integer> prevValExPairsLabelMap = getClassLabelsMap(prevValExPairs);
		Map<DecisionTreeClassLabel, Integer> nextValExPairsLabelMap = getClassLabelsMap(nextValExPairs);
		
		List<DecisionTreeClassLabel> prevValExPairsLabels = Lists.newArrayList(prevValExPairsLabelMap.keySet());
		List<DecisionTreeClassLabel> nextValExPairsLabels = Lists.newArrayList(nextValExPairsLabelMap.keySet());
		
		if((prevValExPairsLabels.size() == 2 && nextValExPairsLabels.size() == 2) ||
		   (Math.abs(prevValExPairsLabels.size() - nextValExPairsLabels.size()) == 1) ||
		   (!prevValExPairsLabels.equals(nextValExPairsLabels))
		  )  
		{
			isEligibleForSplit = true;
		}
		
		return isEligibleForSplit;
	}
	
	/**
	 * Determines the total information contained in the examples.
	 */
	private static double getInfo(List<ExamplePair> examplePairs)
	{
		if(CollectionUtils.isEmpty(examplePairs)) {
			return 0.0;
		}
		
		int totalExamples = examplePairs.size();
		Map<DecisionTreeClassLabel, Integer> classLabelsMap = getClassLabelsMap(examplePairs);
		
		List<DecisionTreeClassLabel> classLabels = Lists.newArrayList(classLabelsMap.keySet());
		int firstClassExamples = 0;
		int secondClassExamples = 0;
		
		firstClassExamples = classLabelsMap.get(classLabels.get(0));
		if(classLabels.size() > 1) {
			secondClassExamples = classLabelsMap.get(classLabels.get(1));
		}
		
		double firstClassInfo = 0.0;
		double secondClassInfo = 0.0;
		if(firstClassExamples > 0) {
			firstClassInfo = -(firstClassExamples/(float)(totalExamples))*(Math.log(firstClassExamples/(float)(totalExamples))/Math.log(2.0));
		}
		if(secondClassExamples > 0) {
			secondClassInfo = -(secondClassExamples/(float)(totalExamples))*(Math.log(secondClassExamples/(float)(totalExamples))/Math.log(2.0));
		}
		
		return firstClassInfo + secondClassInfo;
	}
	
	/**
	 * Filters the example pairs based on the value of the best feature.
	 */
	private static List<ExamplePair> getExamplePairsWithFeatureValue(List<ExamplePair> examplePairs, 
			Feature bestFeature, double bestFeatureValue, String operator)
	{
		List<ExamplePair> filteredExPairs = Lists.newArrayList();
		
		for(ExamplePair exPair : examplePairs) {
			double featureScore = exPair.getAttributeMatchScore(bestFeature);
			// Mismatch case
			if(operator.equals(LESS_THAN_VALUE) && Double.compare(featureScore, bestFeatureValue) <= 0) {
				filteredExPairs.add(exPair);
			}
			// Match case
			else if(operator.equals(GREATER_THAN_VALUE) && Double.compare(featureScore, bestFeatureValue) > 0) {
				filteredExPairs.add(exPair);
			}
			// Missing value case
			else if(operator.equals(MISSING_VALUE) && featureScore < 0) {
				filteredExPairs.add(exPair);
			}
		}
		
		return filteredExPairs;
	}
	
	/**
	 * Creates a classification node in the decision tree
	 */
	private static DecisionTreeNode createLeafNode(List<ExamplePair> examplePairs, List<Feature> features, 
			DecisionTreeClassLabel classLabel)
	{
		DecisionTreeNode node = new DecisionTreeNode(examplePairs, features);
		node.setLabel(classLabel);
		node.setNodeType(DecisionTreeNodeType.CLASS_NODE);
		
		return node;
	}
	
	/**
	 * Creates an intermediate feature node in the classification tree.
	 */
	private static DecisionTreeNode createFeatureNode(List<ExamplePair> examplePairs, List<Feature> features, 
			Feature bestFeature)
	{
		DecisionTreeNode node = new DecisionTreeNode(examplePairs, features);
		node.setBestFeature(bestFeature);
		node.setNodeType(DecisionTreeNodeType.FEATURE_NODE);
		
		return node;
	}
	
	/**
	 * Gets a count of the various class labels in the set of example pairs. 
	 */
	private static Map<DecisionTreeClassLabel, Integer> getClassLabelsMap(List<ExamplePair> examplePairs)
	{
		Map<DecisionTreeClassLabel, Integer> classLabelsMap = Maps.newHashMap();
		if(CollectionUtils.isEmpty(examplePairs)) {
			return classLabelsMap;
		}
		
		for(ExamplePair exPair : examplePairs) {
			DecisionTreeClassLabel currLabel = exPair.getClassLabel();
			int count = 0;
			if(classLabelsMap.containsKey(currLabel)) {
				count = classLabelsMap.get(currLabel);
			}
			count++;
			classLabelsMap.put(currLabel, count);
		}
		
		return classLabelsMap;
	}
	
	/**
	 * Returns the most frequently occurring class label in a dataset.
	 */
	private static DecisionTreeClassLabel getMajorityClassLabel(Map<DecisionTreeClassLabel, Integer> classLabelsMap)
	{
		if(MapUtils.isEmpty(classLabelsMap)) {
			return null;
		}
		
		DecisionTreeClassLabel majorityClassLabel = null;
		List<DecisionTreeClassLabel> classLabels = Lists.newArrayList(classLabelsMap.keySet());
		int firstClassLabelCnt = classLabelsMap.get(classLabels.get(0));
		int secondClassLabelCnt = 0;
		if(classLabels.size() > 1) {
			secondClassLabelCnt = classLabelsMap.get(classLabels.get(1));			
		}

		if(firstClassLabelCnt == secondClassLabelCnt) {
			majorityClassLabel = null;
		}
		else if(firstClassLabelCnt > secondClassLabelCnt) {
			majorityClassLabel = classLabels.get(0);
		}
		else {
			majorityClassLabel = classLabels.get(1);
		}
	
		return majorityClassLabel;
	}
	
	/**
	 * Prints the decision tree for visual analysis.
	 * @param dtree
	 */
	public static void printRuleDecisionTree(DecisionTreeNode dtree)
	{
		
	}
	
	/**
	 * Extracts all the matching rules from the decision tree.
	 * 
	 * Specifically, traverses all the paths in the decision tree that end up in a MATCH leaf node.
	 * @param dtree
	 * @return
	 */
	public static List<ItemMatchRule> extractMatchingRules(DecisionTreeNode dtree)
	{
		return null;
	}
}
