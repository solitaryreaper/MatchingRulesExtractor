package walmartlabs.productmatching.autorulegenerator.model;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Annotates the type of link between child and parent node
 * @author excelsior
 *
 */
public enum DecisionTreeLinkType
{
	MATCH_LINK("<"),
	//MISSING_LINK("?"),
	MISMATCH_LINK(">=");
	
	private String operatorValue;
	
	private DecisionTreeLinkType(String operatorValue)
	{
		this.operatorValue = operatorValue;
	}
	
	public static List<DecisionTreeLinkType> getDecisonTreeLinkValues()
	{
		return Lists.newArrayList(DecisionTreeLinkType.values());
	}
	
	public String getOperatorToApply()
	{
		return operatorValue;
	}

}