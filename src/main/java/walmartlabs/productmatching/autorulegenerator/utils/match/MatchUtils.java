package walmartlabs.productmatching.autorulegenerator.utils.match;

import java.text.DecimalFormat;
import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.walmart.productgenome.pairComparison.audit.TokenAuditEntity;
import com.walmart.productgenome.pairComparison.model.Constants;
import com.walmart.productgenome.pairComparison.utils.comparers.ComparersFactory;
import com.walmart.productgenome.pairComparison.utils.comparers.IComparer;
import com.walmart.productgenome.pairComparison.utils.rule.calculator.StringTokenContainmentScoreCalculator;
import com.walmart.productgenome.pairComparison.utils.tokenizers.StandardAnalyzerTokenizer;

import walmartlabs.productmatching.autorulegenerator.model.Example;
import walmartlabs.productmatching.autorulegenerator.model.Feature;

/**
 * Utility class to do the actual matching.
 * 
 * @author excelsior
 *
 */
public class MatchUtils {

	private static DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("##.00");
	private static StandardAnalyzerTokenizer TOKENIZER = new StandardAnalyzerTokenizer();
	
	/**
	 * Calculates the similarity score between two values for a feature. Feature object
	 * has information that guides the matching process.
	 * 
	 * @param val1
	 * @param val2
	 * @param feature
	 */
	public static double getTwoWaySimilarityScore(String val1, String val2, Feature feature)
	{
		double score =  Math.max(getSimilarityScore(val1, val2, feature), getSimilarityScore(val2, val1, feature));
		return Double.valueOf(DECIMAL_FORMATTER.format(score));
	}
	
	private static double getSimilarityScore(String val1, String val2, Feature feature)
	{
		if(Strings.isNullOrEmpty(val1) || Strings.isNullOrEmpty(val2)) {
			return 0.0;
		}
		
		List<String> sourceTokens = TOKENIZER.tokenize(val1);
		List<String> targetTokens = TOKENIZER.tokenize(val2);		
		List<List<String>> targetTokenLists = Lists.newArrayList();
		targetTokenLists.add(targetTokens);
		List<IComparer> comparers = ComparersFactory.getComparers(Constants.FUZZY_STRING_COMPARER);
		List<TokenAuditEntity> tokenAuditValues = Lists.newArrayList();
		
		StringTokenContainmentScoreCalculator calculator = new StringTokenContainmentScoreCalculator();
		return calculator.containmentScore(sourceTokens, targetTokenLists, comparers, tokenAuditValues);		
	}
	
	/**
	 * Gets the similarity score for two items based on a subset of the attributes. The subset of
	 * attributes have been chosen such that they can be used to uniquely identify an example. 
	 */
	public static double getExamplePairSimilarityScore(Example ex1, Example ex2, List<String> idKeys)
	{
		double totalScore = 0.0;
		
		for(String idKey : idKeys) {
			String val1 = ex1.getValueForAttribute(idKey);
			String val2 = ex2.getValueForAttribute(idKey);
			totalScore += getTwoWaySimilarityScore(val1, val2, null);
		}
		
		return (totalScore/(double)(idKeys.size()));
	}
}
