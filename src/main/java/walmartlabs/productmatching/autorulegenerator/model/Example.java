package walmartlabs.productmatching.autorulegenerator.model;

import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

/**
 * Represents a specific labelled example in the dataset.
 * 
 * @author excelsior
 *
 */
public class Example implements Comparable<Example> {
	private String id;
	private Map<Feature, String> featureValueMap = Maps.newHashMap();
	
	public Example(Map<Feature, String> featureValueMap) {
		super();
		this.featureValueMap = featureValueMap;
	}
	
	public Example(String id, Map<Feature, String> featureValueMap) {
		super();
		this.id = id;
		this.featureValueMap = featureValueMap;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Example [id=").append(id).append(", featureValueMap=")
				.append(featureValueMap).append("]");
		return builder.toString();
	}

	public int hashcode()
	{
		return Objects.hashCode(id, featureValueMap);
	}
	
	public boolean equals(Object obj)
	{
	    if (obj == null) return false;
	    if (getClass() != obj.getClass()) return false;
	    final Example that = (Example) obj;
	    return 	Objects.equal(this.id, that.id) && 
	    		Objects.equal(this.featureValueMap, that.featureValueMap);		
	}
	
	/**
	 * Returns the value for a specific attribute name, if it exists.
	 */
	public String getValueForAttribute(String attrName)
	{
		String attrValue = "NA";
		for(Map.Entry<Feature, String> entry : getFeatureValueMap().entrySet()) {
			if(entry.getKey().getAttrName().toLowerCase().equals(attrName.toLowerCase())) {
				attrValue = entry.getValue();
				break;
			}
		}
		
		return attrValue;
	}
	
	public Map<Feature, String> getFeatureValueMap() {
		return featureValueMap;
	}

	public void setFeatureValueMap(Map<Feature, String> featureValueMap) {
		this.featureValueMap = featureValueMap;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int compareTo(Example that) {
		return this.getId().compareTo(that.getId());
	}
	
}
