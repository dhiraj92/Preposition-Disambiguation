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

package miacp.featgen.fer;

import java.util.Set;

import miacp.jwni.IndexEntry;
import miacp.jwni.POS;
import miacp.jwni.Pointer;
import miacp.jwni.PointerType;
import miacp.jwni.Sense;
import miacp.jwni.WordNet;



/**
 * Feature extraction rule for extracting some WordNet features based upon
 * attribute links and antonym links
 *
 */
public class AdjectiveAttributesAndSimilars extends AbstractWordNetFER {
	
	private static final long serialVersionUID = 1L;

	public Set<String> generateFeatures(String term, String type, Set<String> productions) {
		POS pos = getPosForType(type);
		if(pos != null) {
			IndexEntry entry = WordNet.getInstance().lookupIndexEntry(pos, term);
			if(entry != null) {
				Sense[] senses = entry.getSenses();
				for(int i = 0; i < senses.length && i < mMaxSenseNum; i++) {
					Sense sense = senses[i];
					Pointer[] pointers = sense.getPointers(/*PointerType.SIMILAR_TO,*/PointerType.ATTRIBUTE);
					for(Pointer ptr : pointers) {
						productions.add(ptr.getPointerType() + ":" + ptr.getTargetSense().getKeys()[0].toString());
					}
					Pointer[] antonyms = sense.getPointers(PointerType.ANTONYM);
					
					if(antonyms.length > 0) {
						// add in self
						productions.add(PointerType.ANTONYM + ":" + sense.getKeys()[0].toString());
						for(Pointer anto : antonyms) {
							productions.add(PointerType.ANTONYM+":"+anto.getTargetSense().getKeys()[0].toString());
						}
					}
				}
			}
		}
		return productions;
	}
	
}
