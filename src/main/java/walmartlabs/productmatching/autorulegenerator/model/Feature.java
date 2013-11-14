package walmartlabs.productmatching.autorulegenerator.model;

import com.google.common.base.Objects;

/**
 * Represents a feature being evaluated during decision tree learning.
 * 
 * @author excelsior
 *
 */
public class Feature {
	private String name;
	
	public int hashCode() {
		return Objects.hashCode(name);
	}

	public boolean equals(Object obj) {
	    if (obj == null) return false;
	    if (getClass() != obj.getClass()) return false;
	    final Feature other = (Feature) obj;
	    return 	Objects.equal(this.name, other.name);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
