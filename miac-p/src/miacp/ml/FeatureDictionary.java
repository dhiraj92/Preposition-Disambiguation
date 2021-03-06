/* Note: This message is to inform you that this code was modified by Stephen Tratz in early 2012 and that therefore this code will
 * be somewhat different from that made available at the Information Sciences Institute's website (unless similar changes are made there).
 * This message is here to comply with the terms of the Apache license ("You must cause any modified files to carry prominent notices stating that You changed the files").
 */

/*
 * Copyright 2011 University of Southern California 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 *      
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package miacp.ml;

import java.io.Serializable;
import java.util.Map;

import miacp.types.ChecksumMap;
import miacp.types.ChecksumMap.TwoPartKey;


/**
 * Basically this just wraps a map that contains feature->index pairs
 *
 */
public class FeatureDictionary implements Serializable {
	public final static long serialVersionUID = 1;
	
	private ChecksumMap mDict = new ChecksumMap();
	private int maxIndex;
	
	public FeatureDictionary() {
		
	}
	
	public Map<TwoPartKey, Integer> getKeySet() {
		return mDict.getKeyToIndexMap();
	}
	
	public void override(String key, int index) {
		mDict.put(key, index);
		if(index > maxIndex) {
			maxIndex = index;
		}
	}
	
	public int lookupIndex(String key, boolean add) {
		int result = -1;
		int index = mDict.get(key);
		if(index == ChecksumMap.DEFAULT_NOT_FOUND_VALUE) {
			if(add) {
				maxIndex++;
				mDict.put(key, maxIndex);
				result = maxIndex;
			}
		}
		else {
			result = index;//.intValue();
		}
		return result;
	}
	
	public int lookupIndex(TwoPartKey twoPartKey, boolean add) {
		int result = -1;
		int index = mDict.get(twoPartKey.hash, twoPartKey.checksum);
		if(index == ChecksumMap.DEFAULT_NOT_FOUND_VALUE) {
			if(add) {
				maxIndex++;
				mDict.put(twoPartKey.hash, twoPartKey.checksum, maxIndex);
				result = maxIndex;
			}
		}
		else {
			result = index;//.intValue();
		}
		return result;
	}
	
	public int size() {
		return mDict.size();
	}
	
}
