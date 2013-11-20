package walmartlabs.productmatching.autorulegenerator.model;

import com.google.common.base.Objects;
import com.walmart.productgenome.pairComparison.utils.comparers.ComparersFactory;
import com.walmart.productgenome.pairComparison.utils.comparers.IComparer;

/**
 * Represents a feature being evaluated during decision tree learning. In case of matching, a
 * feature is a function of the attribute name and the similarity function being applied to it.
 * Thus, the decision tree nodes are not simple attribute values as features. Rather they are
 * the similarity score of a metric on the attribute name.
 * 
 * @author excelsior
 *
 */
public class Feature {
	// Name of the data attribute corresponding to this feature
	private String attrName;
	
	// Similarity metric to apply on the attribute name.
	private IComparer simMeasure;
	
	public Feature(String name, IComparer simMeasure)
	{
		this.attrName = name;
		this.simMeasure = simMeasure;
	}
	
	@Override
	public String toString()
	{
		return getFeatureName();
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hashCode(this.attrName, this.simMeasure);
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null) return false;
	    if (getClass() != obj.getClass()) return false;
	    final Feature other = (Feature) obj;
	    return 	Objects.equal(this.attrName, other.attrName) &&
	    		Objects.equal(this.simMeasure, other.simMeasure);
	}

	public String getAttrName() {
		return attrName;
	}

	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}

	public IComparer getSimMeasure() {
		return simMeasure;
	}

	public void setSimMeasure(IComparer simMeasure) {
		this.simMeasure = simMeasure;
	}

	/**
	 * Feature name includes both the attribute name and the similarity function applied to
	 * uniquely identify the feature.
	 */
	public String getFeatureName()
	{
		return attrName + "_" + ComparersFactory.getComparerAbbrvName(simMeasure.getClass().getSimpleName());
	}
}
