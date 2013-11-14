package walmartlabs.productmatching.autorulegenerator.utils;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

public class Temp {

	public static void main(String[] args) {
		Map<Double, String> test = Maps.newTreeMap();
		test.put(-1.7, "a");
		test.put(1.2, "b");
		test.put(0.2, "c");
		
		Set<Double> keys = test.keySet();
		System.out.println(keys.toString());

	}

}
