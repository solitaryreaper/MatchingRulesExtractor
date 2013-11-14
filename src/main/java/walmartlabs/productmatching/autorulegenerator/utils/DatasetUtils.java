package walmartlabs.productmatching.autorulegenerator.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;

import walmartlabs.productmatching.autorulegenerator.model.Dataset;
import walmartlabs.productmatching.autorulegenerator.model.ExamplePair;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.walmart.productgenome.pairComparison.model.Constants;
import com.walmart.productgenome.pairComparison.model.rule.MatchEntity;
import com.walmart.productgenome.pairComparison.model.rule.MatchEntityPair;

/**
 * Reads a file containing item pairs for matching and generates item pair objects.
 * 
 * Note : The data file has to follow a strict format to be parsed properly.
 * TODO : 1) Come up with a better value delimiter. Comma is error-prone.
 * 		  2) Come up with a itempair delimiter. It is easier to find the end of the itempair that way.
 * 			 Relying on a blank line to signal the end of the itempair is error-prone.
 * @author sprasa4
 *
 */
public class DatasetUtils {
	
	public static Dataset parseDataset(File dataFile)
	{
		
	}
	
	private static final String SOURCE_ITEM_ATTR = "source_item_attr";
	private static final String TARGET_ITEM_ATTR = "target_item_attr";
			
	/**
	 * Returns item pair objects from a file containing item pair in raw form.
	 * @param itemPairDataFile	A file object representing the file containg itempairs.
	 * @return	List of itempair objects.
	 */
	@SuppressWarnings("resource")
	public static List<ExamplePair> getDataFileExamplePairs(File dataFile)
	{
		String firstItemID = null;
		String firstItemSource = null;
		String firstItemTitle = null;
		Map<String, Set<String>> sourceItemAttributes = new HashMap<String, Set<String>>();
		
		String secondItemID = null;
		String secondItemSource = null;
		String secondItemTitle = null;
		Map<String, Set<String>> targetItemAttributes = new HashMap<String, Set<String>>();
		
		MatchEntity firstItem = null;
		MatchEntity secondItem = null;
		
		List<ExamplePair> itemPairs = Lists.newArrayList();
		BufferedReader br = null; 
		try {
 			String currLine; 	
 			br = new BufferedReader(new FileReader(dataFile));

			// TODO : Ask Aswath for a better itempair delimiter. Relying on an empty line seems
			// too error-prone. Shall we have a more explicit itempair end row delimiter ?
 			// This hack is to ensure that presence of unnecessary empty lines don't fail the
 			// parsing logic.
 			boolean isOneItemPairFullyRead = true;
 			while ((currLine = br.readLine()) != null) {
				// Itempair information is finished. Create itempair and reinitialise all the
				// item state variables for the next itempair.
				if(currLine.isEmpty() && !isOneItemPairFullyRead) {
					// This state variable marks the end of reading of a single itempair.
					isOneItemPairFullyRead = true;
					
					firstItem = new MatchEntity(firstItemID, firstItemSource, firstItemTitle);
					Map<String, Set<String>>  sourceAttrsMap = new HashMap<String, Set<String>>(sourceItemAttributes);
					firstItem.setAttributeNameValueSetMap(sourceAttrsMap);
					
					secondItem = new MatchEntity(secondItemID, secondItemSource, secondItemTitle);
					Map<String, Set<String>> targetAttrsMap = new HashMap<String, Set<String>>(targetItemAttributes);
					secondItem.setAttributeNameValueSetMap(targetAttrsMap);
					
					MatchEntityPair itemPair = new MatchEntityPair(firstItem, secondItem);
					itemPairs.add(itemPair);
					
					firstItem = null; 
					firstItemID = null; 
					firstItemSource = null; 
					firstItemTitle = null;
					sourceItemAttributes = Maps.newHashMap();
					secondItem = null; 
					secondItemID = null; 
					secondItemSource = null; 
					secondItemTitle = null;
					targetItemAttributes = Maps.newHashMap();
				}
				else {
					// Set the IDs of items
					if(currLine.startsWith(Constants.ID)) {
						// This state variable marks the beginning of reading of an itempair
						isOneItemPairFullyRead = false;
						
						Map<String, String> idMap = parseItemIDs(currLine);
						firstItemID = idMap.get(SOURCE_ITEM_ATTR);
						secondItemID = idMap.get(TARGET_ITEM_ATTR);
					}
					// Set the sources of the items
					else if(currLine.startsWith(Constants.SOURCE)) {
						Map<String, String> sourceMap = parseSources(currLine);
						firstItemSource = sourceMap.get(SOURCE_ITEM_ATTR);
						secondItemSource = sourceMap.get(TARGET_ITEM_ATTR);
						
					}
					// Set the titles/names of the items
					else if(currLine.startsWith(Constants.TITLE)) {
						Map<String, String> titleMap = parseItemNames(currLine);
						firstItemTitle = titleMap.get(SOURCE_ITEM_ATTR);
						secondItemTitle = titleMap.get(TARGET_ITEM_ATTR);
					}
					// Set the source specific attribute for the first item
					else if(firstItemSource != null && currLine.startsWith(firstItemSource)) {
						Map<String, Set<String>> attrMap = parseSourceSpecificAttributes(firstItemSource, currLine);
						if(MapUtils.isNotEmpty(attrMap)) {
							for(Map.Entry<String, Set<String>> entry : attrMap.entrySet()) {
								sourceItemAttributes.put(entry.getKey(), entry.getValue());
							}							
						}
					}
					// Set the target specific attribute for the second item
					else if(secondItemSource != null && currLine.startsWith(secondItemSource)) {
						Map<String, Set<String>> attrMap = parseSourceSpecificAttributes(secondItemSource, currLine);
						if(MapUtils.isNotEmpty(attrMap)) {
							for(Map.Entry<String, Set<String>> entry : attrMap.entrySet()) {
								targetItemAttributes.put(entry.getKey(), entry.getValue());
							}							
						}
					}
					// Set the common attributes for both the items
					else {
						Map<String, Map<String, Set<String>>> attrMap = parseCommonAttributesOfItems(currLine);
						
						if(attrMap.containsKey(SOURCE_ITEM_ATTR)) {
							Map<String, Set<String>> sourceAttrMap = attrMap.get(SOURCE_ITEM_ATTR);
							for(Map.Entry<String, Set<String>> entry : sourceAttrMap.entrySet()) {
								sourceItemAttributes.put(entry.getKey(), entry.getValue());
							}
						}

						if(attrMap.containsKey(TARGET_ITEM_ATTR)) {
							Map<String, Set<String>> targetAttrMap = attrMap.get(TARGET_ITEM_ATTR);
							for(Map.Entry<String, Set<String>> entry : targetAttrMap.entrySet()) {
								targetItemAttributes.put(entry.getKey(), entry.getValue());
							}
						}
					}
				}
				
			} // end of while loop
 			
 			// TODO : Hack. What if someone put to forgot to put an empty line as itempair delimiter
 			// for the very last itempair in the file. Handle that corner case here. We should have
 			// an explicit itempair delimiter.
 			if(!isOneItemPairFullyRead) {
				// This state variable marks the end of reading of a single itempair.
				isOneItemPairFullyRead = true;
				
				firstItem = new MatchEntity(firstItemID, firstItemSource, firstItemTitle);
				Map<String, Set<String>>  sourceAttrsMap = new HashMap<String, Set<String>>(sourceItemAttributes);
				firstItem.setAttributeNameValueSetMap(sourceAttrsMap);
				
				secondItem = new MatchEntity(secondItemID, secondItemSource, secondItemTitle);
				Map<String, Set<String>> targetAttrsMap = new HashMap<String, Set<String>>(targetItemAttributes);
				secondItem.setAttributeNameValueSetMap(targetAttrsMap);
				
				MatchEntityPair itemPair = new MatchEntityPair(firstItem, secondItem);
				itemPairs.add(itemPair); 				
 			}
 		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		
		return itemPairs;
	}
	
	/**
	 * Parses the item ids of the item pair.
	 * @param itemIdsLine
	 * @return
	 */
	public static Map<String, String> parseItemIDs(String itemIdsLine)
	{
		String[] idTokens = itemIdsLine.split(Constants.COLUMN_DELIMITER);
		return getKeyValuePair(idTokens);
	}

	/**
	 * Parses the source of each item.
	 * @param sourceLine
	 * @return
	 */
	public static Map<String, String> parseSources(String sourcesLine)
	{
		String[] idTokens = sourcesLine.split(Constants.COLUMN_DELIMITER);
		return getKeyValuePair(idTokens);
	}

	/**
	 * Parses the title/name of the items.
	 */
	public static Map<String, String> parseItemNames(String itemNameLine)
	{
		String[] titleTokens = itemNameLine.split(Constants.COLUMN_DELIMITER);
		return getKeyValuePair(titleTokens);
	}

	private static Map<String, String> getKeyValuePair(String[] tokens)
	{
		Map<String, String> keyValueMap = new HashMap<String, String>();
		keyValueMap.put(SOURCE_ITEM_ATTR, tokens[1].trim());
		keyValueMap.put(TARGET_ITEM_ATTR, tokens[2].trim());
		
		return keyValueMap;
	}

	/**
	 * Parses the attributes and their values for all the attributes which occur for both source
	 * and target item.
	 */
	public static Map<String, Map<String, Set<String>>> parseCommonAttributesOfItems(String commonAttrLine)
	{
		Map<String, Map<String, Set<String>>> commonAttrMap = new HashMap<String, Map<String, Set<String>>>();
		
		String[] attrTokens = commonAttrLine.split(Constants.COLUMN_DELIMITER);
		String attrName = attrTokens[0].trim();
		Set<String> sourceAttrValues = parseValues(attrTokens[1].trim(), attrName);
		Set<String> targetAttrValues = parseValues(attrTokens[2].trim(), attrName);
		
		if(checkIfNotNullList(sourceAttrValues)) {
			Map<String, Set<String>> sourceAttrMap = new HashMap<String, Set<String>>();
			sourceAttrMap.put(attrName, sourceAttrValues);
			commonAttrMap.put(SOURCE_ITEM_ATTR, sourceAttrMap);
		}
		if(checkIfNotNullList(targetAttrValues)) {
			Map<String, Set<String>> targetAttrMap = new HashMap<String, Set<String>>();
			targetAttrMap.put(attrName, targetAttrValues);
			commonAttrMap.put(TARGET_ITEM_ATTR, targetAttrMap);
		}
		
		return commonAttrMap;
	}
	
	/**
	 * Parses attributes that are specific to a single source in the mismatch file and don't appear
	 * for the other source.
	 * @return
	 */
	public static Map<String, Set<String>> parseSourceSpecificAttributes(String sourceName, String sourceSpecificAttrLine)
	{
		Map<String, Set<String>> attrValueMap = new HashMap<String, Set<String>>();
		String attrKey = null;
		Set<String> attrValues = null;
		
		// Skip the source name at the beginning
		sourceSpecificAttrLine = sourceSpecificAttrLine.substring(sourceName.length());
		String[] lineTokens = sourceSpecificAttrLine.split(Constants.COLUMN_DELIMITER);
		if(lineTokens.length != 3) {
			System.err.println("Incorrectly formatted source specific attribute line " + sourceSpecificAttrLine);
		}
		
		attrKey = lineTokens[0].trim();
		if(!lineTokens[1].contains(Constants.NULL_STRING)) {
			attrValues = parseValues(lineTokens[1], attrKey);
		}
		else if(!lineTokens[2].contains(Constants.NULL_STRING)) {
			attrValues = parseValues(lineTokens[2], attrKey);
		}
		
		// There might be cases where both the source
		if(attrValues != null && !attrValues.isEmpty()) {
			attrValueMap.put(attrKey, attrValues);			
		}		

		return attrValueMap;
	}
	
	/**
	 * Parse a string which maybe a single value or a comma separated and bracket enclosed set of
	 * values.
	 */
	public static Set<String> parseValues(String values, String attrName)
	{
		Set<String> valueSet = Sets.newHashSet();
		
		values = values.replaceAll("\\[", "");
		values = values.replaceAll("]", "");
		values = values.trim();
		
		// Slight hack to handle pd_title* attributes as it can have COMMA(",") in the value itself. 
		// So, don't split this attribute based on the delimiter.
		if(attrName.equals(Constants.PD_TITLE) || 
		   attrName.equals(Constants.PD_TITLE_WITHOUT_NUMBER_UNITS_AND_VARIATIONS) ||
		   attrName.equals(Constants.SIGNING_DESCRIPTION)) 
		{
			return Sets.newHashSet(values);
		}
		
		String[] valueTokens = values.split(Constants.VALUE_DELIMITER);
		for(String token : valueTokens) {
			valueSet.add(token.trim());
		}
		
		return valueSet;
	}
	
	/**
	 * Checks if the list is not null or doesn't contain NULL as string as the only element.
	 */
	private static boolean checkIfNotNullList(Set<String> values)
	{
		boolean isNotNullList = true;
		if(values == null || values.isEmpty()) {
			isNotNullList = false;
		}
		else {
			if(values.size() == 1 && values.contains("null")) {
				isNotNullList = false;
			}
		}
		
		return isNotNullList;
	}
	
}
