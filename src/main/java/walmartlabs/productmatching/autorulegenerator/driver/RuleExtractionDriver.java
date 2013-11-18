package walmartlabs.productmatching.autorulegenerator.driver;

import java.io.File;
import java.util.Map;

import walmartlabs.productmatching.autorulegenerator.model.Dataset;
import walmartlabs.productmatching.autorulegenerator.model.DecisionTreeClassLabel;
import walmartlabs.productmatching.autorulegenerator.model.DecisionTreeNode;
import walmartlabs.productmatching.autorulegenerator.utils.DecisionTreeUtils;
import walmartlabs.productmatching.autorulegenerator.utils.input.ItemPairDatasetReader;

/**
 * Driver class that encapsulates the entire workflow for auto-generation of rules from a dataset.
 * 
 * Following steps are executed in this workflow :
 * 1) Load the itempair dataset in memory.
 * 2) Execute the decision tree learning algorithm on the dataset.
 * 3) Print the decision tree.
 * 4) Extract the rules from the decision tree.
 * 5) Test the accuracy of the rules on the test data set.
 * @author excelsior
 *
 */
public class RuleExtractionDriver {

	public static void main(String[] args) {
		// Step1 : Load the training dataset
		System.out.println("Loading the training data set .." );
		String matchPairsFileName = args[0].trim();
		String mismatchPairsFileName = args[1].trim();
		File matchPairsFile = new File(matchPairsFileName);
		File mismatchPairsFile = new File(mismatchPairsFileName);
		Dataset trainDataset = ItemPairDatasetReader.parseDataset(matchPairsFile, mismatchPairsFile, "Restaurant Dataset");
		
		// Step2: Build the decision tree from the training dataset
		System.out.println("Generating the decision tree ..");
		
		// If only 1% of itempairs are left at a node then create a leaf node with majority label
		// to avoid overfitting.
		int leafThreshold = (int)(1*trainDataset.getExamplePairs().size()/100);
		System.out.println("Threshold is : " + leafThreshold);
		DecisionTreeNode ruleDTree = 
			DecisionTreeUtils.learnRuleDecisionTree(trainDataset.getExamplePairs(), trainDataset.getFeatures(), leafThreshold);
		
		// Step3: Print the decision tree.
		System.out.println("Printing the decision tree ..");
		DecisionTreeUtils.printRuleDecisionTree(ruleDTree, "");
		
		// Step4 : Extract the rules from the decision tree.
		System.out.println("Generating matching rules ..");
		Map<DecisionTreeClassLabel, Integer> datasetStats = trainDataset.getDatatsetStats();
		int totalMatchedPairs = datasetStats.get(DecisionTreeClassLabel.MATCH);
		int totalMismatchedPairs = datasetStats.get(DecisionTreeClassLabel.MISMATCH); 
		DecisionTreeUtils.generateMatchingRules(ruleDTree, "", totalMatchedPairs, totalMismatchedPairs);
		
		// TODO
		/*
		List<ItemMatchRule> matchingRules = DecisionTreeUtils.extractMatchingRules(ruleDTree);
		*/
		
		// Step5: Test the accuracy of rules on the test dataset.
		// TODO
		/*
		String testDataFileName = args[2];
		File testDataFile = new File(testDataFileName);
		DecisionTreeUtils.testMatchingRules(testDataFile)
		*/
	}
}
