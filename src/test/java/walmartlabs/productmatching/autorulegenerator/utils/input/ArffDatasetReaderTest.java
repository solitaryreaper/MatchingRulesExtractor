package walmartlabs.productmatching.autorulegenerator.utils.input;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

import walmartlabs.productmatching.autorulegenerator.model.Dataset;

import com.google.common.collect.Lists;

/**
 * @author excelsior
 *
 */
public class ArffDatasetReaderTest{

	private ArffDatasetReader reader = new ArffDatasetReader();
	
	@Ignore
	public void testParseCoraDataset()
	{
		String coraFilePath = System.getProperty("user.dir") + "/src/main/resources/data/cora/cora.arff";
		File coraFile = new File(coraFilePath);
		Dataset coraDataset = reader.parseDataset(coraFile, Lists.newArrayList("author", "title", "venue", "pages"));
		assertNotNull(coraDataset);
		assertNotNull(coraDataset.getExamplePairs());
		assertNotNull(coraDataset.getFeatures());
		assertTrue(coraDataset.getExamplePairs().size() > 0);
	}

	@Test
	public void testParseRestaurantDataset()
	{
		String filePath = System.getProperty("user.dir") + "/src/main/resources/data/restaurant/fz.arff";
		File file = new File(filePath);
		Dataset dataset = reader.parseDataset(file, Lists.newArrayList("phone", "class"));
		assertNotNull(dataset);
		assertNotNull(dataset.getExamplePairs());
		assertNotNull(dataset.getFeatures());
		assertTrue(dataset.getExamplePairs().size() > 0);
	}
}
