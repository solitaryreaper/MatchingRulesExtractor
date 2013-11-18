package walmartlabs.productmatching.autorulegenerator.model;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Represents the dataset to be evaluated.
 * 
 * @author excelsior
 *
 */
public class Dataset {
	private String name;
	private List<Feature> features = Lists.newArrayList();
	private List<ExamplePair> examplePairs = Lists.newArrayList();
	
	public Dataset(String name, List<Feature> features, List<ExamplePair> examplePairs)
	{
		this.name = name;
		this.features = Lists.newArrayList(features);
		this.examplePairs = Lists.newArrayList(examplePairs);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Dataset [name=").append(name).append(", features=")
				.append(features).append(", examplePairs=")
				.append(examplePairs).append("]");
		return builder.toString();
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<Feature> getFeatures() {
		return features;
	}
	
	public void setFeatures(List<Feature> features) {
		this.features = features;
	}
	
	public List<ExamplePair> getExamplePairs() {
		return examplePairs;
	}
	
	public void setExamplePairs(List<ExamplePair> examplePairs) {
		this.examplePairs = examplePairs;
	}
	
}
