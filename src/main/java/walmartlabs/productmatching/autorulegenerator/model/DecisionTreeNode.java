package walmartlabs.productmatching.autorulegenerator.model;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * A model class that represents an intermediate node in the decision tree.
 * 
 * This decision tree contains the relevant features at various levels to depict the various
 * paths that can be used for matching or mismatching an itempair. Each path in the tree is 
 * equivalent to a rule, a conjunction of all the features nodes in the path. Depending on the
 * final classification node on the path, it can either be a MATCH or MISMATCH rule.
 * 
 * @author excelsior
 *
 */
public class DecisionTreeNode {

	// What is the best feature selected at the current decision tree node ?
	private Feature bestFeature = null;
	
	// What are the examples pairs that reached this node ?
	private List<ExamplePair> examplePairs = Lists.newArrayList();
	// What are the features that reached this node ?
	private List<Feature> features = Lists.newArrayList();
	
	// Is this the final leaf/classification node or intermediate feature node ?
	private DecisionTreeNodeType nodeType = DecisionTreeNodeType.FEATURE_NODE;
	// Links to all the child nodes for the current node
	private List<DecisionTreeNode> childNodes = Lists.newArrayList();
	
	// Name of the feature corresponding to the parent node
	private Feature parentFeatureName = null;
	// Value of the link connecting the child and parent node
	private double parentFeatureLinkValue = 0.0;
	// Type of link connecting child and parent node
	private DecisionTreeLinkType parentLinkType = null;
	
	// Default label for a leaf node
	private DecisionTreeClassLabel label = DecisionTreeClassLabel.MISMATCH;
	
	public DecisionTreeNode(List<ExamplePair> examplePairs, List<Feature> features)
	{
		this.examplePairs = examplePairs;
		this.features = features;
	}
	
	@Override
	public String toString()
	{
		return ""; // TODO
	}

	public Feature getBestFeature() {
		return bestFeature;
	}

	public void setBestFeature(Feature bestFeature) {
		this.bestFeature = bestFeature;
	}

	public List<ExamplePair> getExamples() {
		return examplePairs;
	}

	public void setExamples(List<ExamplePair> examplePairs) {
		this.examplePairs = examplePairs;
	}

	public List<Feature> getFeatures() {
		return features;
	}

	public void setFeatures(List<Feature> features) {
		this.features = features;
	}

	public DecisionTreeNodeType getNodeType() {
		return nodeType;
	}

	public void setNodeType(DecisionTreeNodeType nodeType) {
		this.nodeType = nodeType;
	}

	public List<DecisionTreeNode> getChildNodes() {
		return childNodes;
	}

	public void setChildNodes(List<DecisionTreeNode> childNodes) {
		this.childNodes = childNodes;
	}

	public Feature getParentFeatureName() {
		return parentFeatureName;
	}

	public void setParentFeatureName(Feature parentFeatureName) {
		this.parentFeatureName = parentFeatureName;
	}

	public double getParentFeatureLinkValue() {
		return parentFeatureLinkValue;
	}

	public void setParentFeatureLinkValue(double parentFeatureLinkValue) {
		this.parentFeatureLinkValue = parentFeatureLinkValue;
	}

	public DecisionTreeLinkType getParentLinkType() {
		return parentLinkType;
	}

	public void setParentLinkType(DecisionTreeLinkType parentLinkType) {
		this.parentLinkType = parentLinkType;
	}

	public DecisionTreeClassLabel getLabel() {
		return label;
	}

	public void setLabel(DecisionTreeClassLabel label) {
		this.label = label;
	}
}
