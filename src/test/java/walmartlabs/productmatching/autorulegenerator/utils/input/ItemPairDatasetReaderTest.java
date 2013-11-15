package walmartlabs.productmatching.autorulegenerator.utils.input;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;

import org.junit.Test;

import walmartlabs.productmatching.autorulegenerator.model.Dataset;
import walmartlabs.productmatching.autorulegenerator.model.DecisionTreeClassLabel;
import walmartlabs.productmatching.autorulegenerator.model.ExamplePair;

public class ItemPairDatasetReaderTest {

	@Test
	public void testParseDataset()
	{
		File matchFile = new File("/home/excelsior/workspace/MatchingRulesExtractor/matched_pairs.txt");
		File mismatchFile = new File("/home/excelsior/workspace/MatchingRulesExtractor/mismatched_pairs.txt");
		
		Dataset dataset = ItemPairDatasetReader.parseDataset(matchFile, mismatchFile, "Restaurant Dataset.");
		assertNotNull(dataset);
		assertNotNull(dataset.getExamplePairs());
		assertFalse(dataset.getExamplePairs().isEmpty());
		System.out.println("Total itempairs : " + dataset.getExamplePairs().size());
		
		List<ExamplePair> exPairs = dataset.getExamplePairs();
		int matchedCount = 0;
		int mismatchedCount = 0;
		for(ExamplePair exPair : exPairs) {
			if(exPair.getClassLabel().equals(DecisionTreeClassLabel.MATCH)) {
				++matchedCount;
			}
			else{
				++mismatchedCount;
			}
		}
		
		System.out.println("Matched Pairs : " + matchedCount);
		System.out.println("Mismatched Pairs : " + mismatchedCount);
	}
}
