package walmartlabs.productmatching.autorulegenerator.driver;

import java.text.DecimalFormat;
import java.util.Map;

import walmartlabs.productmatching.autorulegenerator.model.Feature;
import walmartlabs.productmatching.autorulegenerator.model.Feature.DataType;

import com.google.common.collect.Maps;

public class Labs {

	public static void main(String[] args) {
		DecimalFormat f = new DecimalFormat("##.00");
		double x = 2.0;
		double y = Double.valueOf(f.format(x));
		System.out.println(y);
	}

}
