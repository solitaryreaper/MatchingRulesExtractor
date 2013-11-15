package walmartlabs.productmatching.autorulegenerator.utils;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.util.CollectionUtils;

import walmartlabs.productmatching.autorulegenerator.model.DecisionTreeClassLabel;
import walmartlabs.productmatching.autorulegenerator.model.DecisionTreeLinkType;
import walmartlabs.productmatching.autorulegenerator.model.DecisionTreeNode;
import walmartlabs.productmatching.autorulegenerator.model.DecisionTreeNodeType;
import walmartlabs.productmatching.autorulegenerator.model.ExamplePair;
import walmartlabs.productmatching.autorulegenerator.model.Feature;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.walmart.productgenome.pairComparison.model.rule.ItemMatchRule;

/**
 * Utility class for decision tree processing.
 * 
 * @author excelsior
 *
 */
public class DecisionTreeUtils {

	private static String LESS_THAN_VALUE = "<";
	private static String GREATER_THAN_VALUE = ">=";
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
			System.out.println("Creating leaf node with DEFAULT label because no example pairs left.");
			return createLeafNode(examplePairs, features, defaultClassLabel);
		}
		
		Map<DecisionTreeClassLabel, Integer> classLabelsMap = getClassLabelsMap(examplePairs);
		DecisionTreeClassLabel majorityClassLabel = getMajorityClassLabel(classLabelsMap);
		
		// Returns a leaf node with majority class label if features is empty
		if(CollectionUtils.isEmpty(features)) {
			System.out.println("Creating leaf node with MAJORITY label because no features left.");
			return createLeafNode(examplePairs, features, majorityClassLabel);
		}
		
		// Return if there is a pure classification
		if(classLabelsMap.size() == 1) {
			DecisionTreeClassLabel pureClassLabel = Lists.newArrayList(classLabelsMap.keySet()).get(0);
			System.out.println("Creating leaf node because PURE classification " + pureClassLabel.toString());
			return createLeafNode(examplePairs, features, pureClassLabel);
		}
		
		// Check if number of instances left is less than the leaf threshold
		if(examplePairs.size() < leafThreshold) {
			if(majorityClassLabel != null) {
				System.out.println("Creating leaf node because LESS THAN THRESHOLD with MAJORITY label");
				return createLeafNode(examplePairs, features, majorityClassLabel);
			}
			else {
				System.out.println("Creating leaf node because LESS THAN THRESHOLD with DEFAULT label");				
				return createLeafNode(examplePairs, features, defaultClassLabel);
			}
		}
		
		// Choose the best feature
		Feature bestFeature = getBestFeature(examplePairs, features);
		if(bestFeature == null) {
			System.out.println("Creating leaf node because no BEST feature could be found.");
			return createLeafNode(examplePairs, features, majorityClassLabel);
		}
		
		System.out.println("Best feature chosen " + bestFeature.getName());
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
		System.out.println("Best split value of " + bestFeatureSplitValue + " for feature " + bestFeature.getName());
		
		// Iterate through the possible feature values and generate apt tree branches
		for(DecisionTreeLinkType dTreeLinkType : DecisionTreeLinkType.getDecisonTreeLinkValues()) {
			String operatorToApply = dTreeLinkType.getOperatorToApply();
			List<ExamplePair> exPairs = 
				getExamplePairsWithFeatureValue(examplePairs, bestFeature, bestFeatureSplitValue, operatorToApply);
			DecisionTreeNode valueNode = learnRuleDecisionTree(exPairs, features, leafThreshold);
			valueNode.setParentFeatureName(bestFeature);
			valueNode.setParentFeatureLinkValue(bestFeatureSplitValue);
			valueNode.setParentLinkType(dTreeLinkType);
			
			featureValueNodes.add(valueNode);
		}
		
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
		
		// Couldn't find a single feature with positive information gain.
		if(Double.compare(maxInfoGain, 0) <= 0) {
			bestFeature = null;
		}
		
		System.out.println("#Best feature " + bestFeature.getName() + " with info gain " + maxInfoGain);
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
		
		System.out.println("#Best info gain for feature " + feature.getName() + " with split value " + bestSplitValue + " for gain " + 
		(totalInfo - featureInfo));
		return totalInfo - featureInfo;
	}
	
	/**
	 * Calculate the information gain for a numeric feature with a specific split value. 
	 */
	private static double getInfoForNumericValueSplit(List<ExamplePair> examplePairs, Feature feature, double splitValue)
	{
		List<ExamplePair> lessThanValueExPairs = 
			getExamplePairsWithFeatureValue(examplePairs, feature, splitValue, LESS_THAN_VALUE);
		List<ExamplePair> greaterThanValueExPairs = 
			getExamplePairsWithFeatureValue(examplePairs, feature, splitValue, GREATER_THAN_VALUE);
		List<ExamplePair> missingValueExPairs = 
			getExamplePairsWithFeatureValue(examplePairs, feature, splitValue, MISSING_VALUE);
		
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
		double bestSplitThreshold = 0.0;
		double bestInfoGain = -999.0;
		
		List<Double> candidateSplitValues = getCandidateSplitValues(exPairs, feature);
		if(CollectionUtils.isEmpty(candidateSplitValues)) {
			return bestSplitThreshold;
		}
		
		for(Double splitValue : candidateSplitValues) {
			double infoGain = 
					getInfo(exPairs) - getInfoForNumericValueSplit(exPairs, feature, splitValue);
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
		System.out.println("Feature values for feature " + feature.getName() + " are " + featureValues.toString());
		if(featureValues.size() <= 1) {
			System.out.println("0 or 1 feature values only. No need for split.");
			return candidateSplitValues;
		}

		double prevFeatureValue = featureValues.get(0);
		for(Double value : featureValues) {
			double nextFeatureValue = value;
			List<ExamplePair> prevFeatureValueExPairs = valueExPairsMap.get(prevFeatureValue);
			List<ExamplePair> nextFeatureValueExPairs = valueExPairsMap.get(nextFeatureValue);
			
			boolean isEligibleForSplit = isEligibleForNumericSplit(prevFeatureValueExPairs, nextFeatureValueExPairs);
			if(isEligibleForSplit) {
				candidateSplitValues.add((prevFeatureValue + nextFeatureValue)/2.0);
			}
		}
		
		System.out.println("Candidate split values for feature " + feature + " are " + candidateSplitValues.toString());
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
			firstClassInfo = -(firstClassExamples/(double)(totalExamples))*
					(Math.log(firstClassExamples/(double)(totalExamples))/Math.log(2.0));
		}
		if(secondClassExamples > 0) {
			secondClassInfo = -(secondClassExamples/(double)(totalExamples))*
					(Math.log(secondClassExamples/(double)(totalExamples))/Math.log(2.0));
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
			if(operator.equals(LESS_THAN_VALUE) && Double.compare(featureScore, bestFeatureValue) < 0) {
				filteredExPairs.add(exPair);
			}
			// Match case
			else if(operator.equals(GREATER_THAN_VALUE) && Double.compare(featureScore, bestFeatureValue) >= 0) {
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
	public static void printRuleDecisionTree(DecisionTreeNode dtree, String prefix)
	{
		if(dtree == null || dtree.getChildNodes() == null) {
			return;
		}
		
		List<DecisionTreeNode> dTreeChildNodes = dtree.getChildNodes();
		for(DecisionTreeNode childNode : dTreeChildNodes) {
			StringBuilder nodeState = new StringBuilder();
			nodeState.append(prefix).append(" ").append(childNode.getParentFeatureName());
			nodeState.append(" ").append(childNode.getParentFeatureLinkValue());
			nodeState.append(" ").append(getClassLabelsCount(childNode.getExamples()));
			if(childNode.getNodeType() == DecisionTreeNodeType.CLASS_NODE) {
				nodeState.append(" : ").append(childNode.getLabel());
			}
			
			// print the state of the current node in decision tree
			System.out.println(nodeState.toString());
			
			// recursively print the tree, separated by pipes and tabs for a visual effect
			printRuleDecisionTree(childNode, prefix + "|\t");
		}
		
	}
	
	public static String getClassLabelsCount(List<ExamplePair> exPairs)
	{
		Map<DecisionTreeClassLabel, Integer> classLabelsMap = getClassLabelsMap(exPairs);
		
		StringBuilder labelsCountStr = new StringBuilder();
		labelsCountStr.append(" [ ");
		if(classLabelsMap.containsKey(DecisionTreeClassLabel.MATCH)) {
			labelsCountStr.append(classLabelsMap.get(DecisionTreeClassLabel.MATCH));
		}
		else {
			labelsCountStr.append(" 0 ");
		}
		labelsCountStr.append(" (+) , ");

		if(classLabelsMap.containsKey(DecisionTreeClassLabel.MISMATCH)) {
			labelsCountStr.append(classLabelsMap.get(DecisionTreeClassLabel.MISMATCH));
		}
		else {
			labelsCountStr.append(" 0 ");
		}

		labelsCountStr.append(" (-) ] ");
		return labelsCountStr.toString();
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
