package walmartlabs.productmatching.autorulegenerator.utils.match;

import org.junit.Test;

import com.google.common.collect.Lists;

public class MatchUtilsTest {

	private MatchUtils matchUtils = new MatchUtils();
	
	@Test
	public void testName()
	{
		String str1 = "arnie morton's of chicago";
		String str2 = "arnie morton's of chicago";
		runTest(str1, str2);
		
		str1 = "art's delicatessen";
		str2 = "art's deli";
		runTest(str1, str2);
	}

	@Test
	public void testAddress()
	{
		String str1 = "435 s. la cienega blv.";
		String str2 = "435 s. la cienega blvd.";
		runTest(str1, str2);
		
		str1 = "12224 ventura blvd.";
		str2 = "12224 ventura blvd.";
		runTest(str1, str2);
	}	
	
	@Test
	public void testCity()
	{
		String str1 = "los angeles";
		String str2 = "los angeles";
		runTest(str1, str2);
		
		str1 = "studio city";
		str2 = "studio city";
		runTest(str1, str2);
	}
	
	@Test
	public void testPhone()
	{
		String str1 = "310/246-1501";
		String str2 = "310-246-1501";
		runTest(str1, str2);
		
		str1 = "818/762-1221";
		str2 = "818-762-1221";	
		runTest(str1, str2);
	}	
	
	@Test
	public void testType()
	{
		String str1 = "american";
		String str2 = "steakhouses";
		runTest(str1, str2);
		
		str1 = "american";
		str2 = "delis";	
		runTest(str1, str2);
	}	
	
	@Test
	public void testClass()
	{
		String str1 = "0";
		String str2 = "0";
		runTest(str1, str2);
		
		str1 = "1";
		str2 = "1";		
		runTest(str1, str2);
	}	
	
	/*
	public void testExampleSimilarity()
	{
		Example ex1 = ;
		Example ex2 = ;
		List<String> idKeys = Lists.newArrayList("name", "addr");
	}
	*/
	
	private void runTest(String str1, String str2)
	{
		double score = matchUtils.getSimilarityScore(str1, str2, null);
		System.out.println("Score for str1: " + str1 + ", str2: " + str2 + " is " + score);		
	}
}
