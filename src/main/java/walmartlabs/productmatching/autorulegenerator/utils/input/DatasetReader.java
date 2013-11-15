package walmartlabs.productmatching.autorulegenerator.utils.input;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import walmartlabs.productmatching.autorulegenerator.model.Dataset;
import walmartlabs.productmatching.autorulegenerator.model.DecisionTreeClassLabel;
import walmartlabs.productmatching.autorulegenerator.model.Example;
import walmartlabs.productmatching.autorulegenerator.model.ExamplePair;

import com.google.common.collect.Lists;

import walmartlabs.productmatching.autorulegenerator.utils.match.MatchUtils;

/**
 * An interface for reading various kind of input data files.
 * @author excelsior
 *
 */
public abstract class DatasetReader {
	/**
	 * Parses the file to extract the dataset and relevant metadata. It includes :
	 * 
	 * - name of the dataset.
	 * - features present in the dataset.
	 * - feature values for each example in the dataset.
	 * 
	 * @param dataFile
	 * @return Dataset
	 */
	public abstract Dataset parseDataset(File dataFile, List<String> idKeys);
	
	/**
	 * Generates a unique identifier for this example 
	 */
	public String getIdForExample(List<String> idKeys, Example ex)
	{
		StringBuilder idForEx = new StringBuilder();
		for(String idKey : idKeys) {
			String idKeyVal = ex.getValueForAttribute(idKey);
			if(idKeyVal != null) {
				idForEx.append(idKeyVal).append("-");
			}
		}
		return idForEx.toString();
	}
	
	/**
	 * Generates all the example pairs from the examples
	 * @throws IOException 
	 */
	public List<ExamplePair> getExamplePairs(List<Example> examples, List<String> idKeys) throws IOException
	{
		File matchedFile = new File("matched_pairs.txt");
		File mismatchedFile = new File("mismatched_pairs.txt");
		
		System.out.println("Matched file location : " + matchedFile.getAbsolutePath());
		System.out.println("Mismatched file location : " + mismatchedFile.getAbsolutePath());		
		
		BufferedWriter matchedWriter = new BufferedWriter(new FileWriter(matchedFile));
		BufferedWriter mismatchedWriter = new BufferedWriter(new FileWriter(mismatchedFile)); 
		
		double match_lower_limit_score = 0.25;
		double match_upper_limit_score = 1.00;
		
		double maxScore = 0.0;
		double minScore = 0.0;
		double totalScore = 0.0;
		
		Collections.sort(examples);
		List<ExamplePair> exPairs = Lists.newArrayList();
		int matchedExamplePairs = 0;
		int totalPairs = 0;
		for(int i=0; i < examples.size()-1; i++) {
			Example ex1 = examples.get(i);
			for(int j=i+1; j < examples.size(); j++) {
				++totalPairs;
				boolean isPairWorthMatching = true;
				boolean isPairSureMatch = false;
				Example ex2 = examples.get(j);
				double simScore = MatchUtils.getExamplePairSimilarityScore(ex1, ex2, idKeys);
				totalScore += simScore;
				if(simScore > maxScore) {
					maxScore = simScore;
				}
				else if(simScore < minScore) {
					minScore = simScore;
				}
				
				if(Double.compare(simScore, match_upper_limit_score) >= 0) {
					++matchedExamplePairs;
					isPairSureMatch = true;
					isPairWorthMatching = true;
				}
				else if(Double.compare(match_lower_limit_score, simScore) > 0) {
					isPairWorthMatching = false;
				}
				
				// TODO : The class label has to be determined
				if(isPairWorthMatching) {
					DecisionTreeClassLabel label = DecisionTreeClassLabel.MISMATCH;
					BufferedWriter writer = mismatchedWriter;
					if(isPairSureMatch) {
						writer = matchedWriter;
						label = DecisionTreeClassLabel.MATCH;
					}
					ExamplePair exPair = new ExamplePair(ex1, ex2, label);
					exPairs.add(exPair);
					writer.write(exPair.toString());
					writer.newLine();
				}

			}
		}
		
		matchedWriter.close();
		mismatchedWriter.close();
		double avgScore = (totalScore/(double)totalPairs);
		System.out.println("Collected " + exPairs.size() + " example pairs.");
		System.out.println("Matched Pairs for sure : " + matchedExamplePairs);
		System.out.println("Min score : " + minScore);
		System.out.println("Max score : " + maxScore);
		System.out.println("Average score : " + avgScore);
		return exPairs;
	}
}
