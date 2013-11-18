package walmartlabs.productmatching.autorulegenerator.model;

import com.google.common.base.Objects;

/**
 * Represents a feature being evaluated during decision tree learning.
 * 
 * @author excelsior
 *
 */
public class Feature {
	// List of datatypes for feature values
	public enum DataType
	{
		STRING("string"),
		DATE("date"),
		NUMERIC("numeric"),
		NOMINAL("nominal");
		
		private String dataType;

		private DataType(String dataType)
		{
			this.dataType = dataType;
		}
		
		public String toString()
		{
			return dataType;
		}
		
		public static DataType getDataType(String dataType)
		{
			DataType dType = DataType.STRING;
			for(DataType type : DataType.values())
			{
				if(dataType.equals(type.toString())) {
					dType = type;
					break;
				}
			}
			
			return dType;
		}
	}
	
	private String name;
	private DataType dataType;

	public Feature(String name, DataType dataType)
	{
		this.name = name;
		this.dataType = dataType;
	}
	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Feature [name=").append(name).append(", dataType=")
				.append(dataType).append("]");
		return builder.toString();
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(this.name, this.dataType);
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null) return false;
	    if (getClass() != obj.getClass()) return false;
	    final Feature other = (Feature) obj;
	    return 	Objects.equal(this.name, other.name) &&
	    		Objects.equal(this.dataType, other.dataType);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = DataType.getDataType(dataType);
	}
}
