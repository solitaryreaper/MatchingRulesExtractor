package walmartlabs.productmatching.autorulegenerator.driver;

import java.text.DecimalFormat;
import java.util.Map;

import walmartlabs.productmatching.autorulegenerator.model.Feature;
import walmartlabs.productmatching.autorulegenerator.model.Feature.DataType;

import com.google.common.collect.Maps;

public class Labs {

	public static void main(String[] args) {
		String temp = "a and b and";
		String x = temp.substring(0, temp.lastIndexOf("and")).trim();
		System.out.println(x);
	}

}
