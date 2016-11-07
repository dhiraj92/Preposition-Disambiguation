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


	
public class WordNetLinksFER extends AbstractWordNetFER {

	private static final long serialVersionUID = 1L;
	
	public static final PointerType[] ptTypes = new PointerType[]{PointerType.ANTONYM, 
			PointerType.HYPERNYM, 
			PointerType.INSTANCE_HYPERNYM, 
			PointerType.HYPONYM, 
			PointerType.INSTANCE_HYPONYM, 
			PointerType.MEMBER_HOLONYM, 
			PointerType.SUBSTANCE_HOLONYM,
			PointerType.PART_HOLONYM,
			PointerType.MEMBER_MERONYM,
			PointerType.SUBSTANCE_MERONYM,
			PointerType.PART_MERONYM, 
			PointerType.ATTRIBUTE, 
			PointerType.DERIVED_FORM, 
			PointerType.DOMAIN_OF_SYNSET_TOPIC, 
			PointerType.MEMBER_OF_THIS_DOMAIN_TOPIC, 
			PointerType.DOMAIN_OF_SYNSET_REGION, 
			PointerType.MEMBER_OF_THIS_DOMAIN_REGION,
			PointerType.DOMAIN_OF_SYNSET_USAGE,
			PointerType.MEMBER_OF_THIS_DOMAIN_USAGE, 
			PointerType.ENTAILMENT, 
			PointerType.CAUSE, 
			PointerType.ALSO_SEE, 
			PointerType.VERB_GROUP, 
			PointerType.SIMILAR_TO, 
			PointerType.PARTICIPLE_OF_VERB, 
			PointerType.PERTAINYM};
	
	@Override
	public Set<String> generateFeatures(String term, String type, Set<String> productions) {
		POS pos = getPosForType(type);
		if(pos != null) {
			IndexEntry entry = WordNet.getInstance().lookupIndexEntry(pos, term);
			if(entry != null) {
				Sense[] senses = entry.getSenses();
				for(int i = 0; i < senses.length && i < mMaxSenseNum; i++) {
					Sense sense = senses[i];
					for(PointerType pType : ptTypes) {
						Pointer[] pointers = sense.getPointers(pType);
						if(pointers.length > 0) {
							productions.add(pType.name());
						}
					}
				}
			}
		}
		return productions;
	}
	
}
