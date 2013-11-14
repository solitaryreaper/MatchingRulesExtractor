package walmartlabs.productmatching.autorulegenerator.driver;

import java.io.File;

import walmartlabs.productmatching.autorulegenerator.model.Dataset;
import walmartlabs.productmatching.autorulegenerator.model.DecisionTreeNode;
import walmartlabs.productmatching.autorulegenerator.utils.DatasetUtils;
import walmartlabs.productmatching.autorulegenerator.utils.DecisionTreeUtils;

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
		String trainingDataFileName = args[1];
		File trainDataFile = new File(trainingDataFileName);
		Dataset trainDataset = DatasetUtils.parseDataset(trainDataFile);
		
		// Step2: Build the decision tree from the training dataset
		DecisionTreeNode ruleDTree = 
				DecisionTreeUtils.learnRuleDecisionTree(trainDataset.getExamplePairs(), trainDataset.getFeatures(), 0);
		
		// Step3: Print the decision tree.
		DecisionTreeUtils.printRuleDecisionTree(ruleDTree);
		
		// Step4 : Extract the rules from the decision tree.
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
