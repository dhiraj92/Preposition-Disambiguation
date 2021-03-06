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
import miacp.jwni.Sense.Key;
import miacp.jwni.WordNet;



/**
 * Feature extraction rule for producing WordNet hypernyms
 *
 */
public class HypernymsFER extends AbstractWordNetFER {
	
	private static final long serialVersionUID = 1L;

	@Override
	public Set<String> generateFeatures(String input, String type, Set<String> productions) {
		POS pos = getPosForType(type);
		if(pos != null) {
			IndexEntry entry = WordNet.getInstance().lookupIndexEntry(pos, input, false);
			if(entry != null) {
				if(pos == POS.ADJECTIVE) {
					/*Sense[] senses = entry.getSenses();
					for(int i = 0; i < senses.length && i < mMaxSenseNum; i++) {
						Sense sense = senses[i];
						Pointer[] derPointers = sense.getPointers(PointerType.DERIVED_FORM,PointerType.PERTAINYM);
						for(Pointer p : derPointers) {
							Sense targetSense = p.getTargetSense();
							if(targetSense.getPOS() == POS.NOUN) {
								addHypernyms(productions, targetSense, 0);
							}
						}	
					}*/
				}
				else if(pos == POS.ADVERB) {
					/*Sense[] senses = entry.getSenses();
					for(int i = 0; i < senses.length && i < mMaxSenseNum; i++) {
						Sense sense = senses[i];
						Pointer[] derPointers = sense.getPointers(PointerType.DERIVED_FORM,PointerType.PERTAINYM);
						for(Pointer p : derPointers) {
							Sense targetSense = p.getTargetSense();
							if(targetSense.getPOS() == POS.ADJECTIVE) {
								Pointer[] derPointers2 = targetSense.getPointers(PointerType.DERIVED_FORM,PointerType.PERTAINYM);
								for(Pointer p2 : derPointers2) {
									Sense targetSense2 = p2.getTargetSense();
									if(targetSense2.getPOS() == POS.NOUN) {
										addHypernyms(productions, targetSense2, 0);
									}
								}	
							}
						}	
					}*/
				}
				else {
					addHypernymsForEntry(entry, productions);
				}
				
			}
		}
		return productions;
	}
	
	private final void addHypernymsForEntry(IndexEntry entry, Set<String> productions) {
		Sense[] senses = entry.getSenses();
		for(int i = 0; i < senses.length && i < mMaxSenseNum; i++) {
			Sense sense = senses[i];
			Pointer[] hyperPointers = sense.getSemPointers(PointerType.HYPERNYM, PointerType.INSTANCE_HYPERNYM);
			for(Pointer p : hyperPointers) {
				addHypernyms(productions, p.getTargetSense(), 0);
			}	
		}
	}
	
	private void addHypernyms(Set<String> productions, Sense sense, int level) {
		if(level < 15) {
			Pointer[] hyperPointers = sense.getSemPointers(PointerType.HYPERNYM, PointerType.INSTANCE_HYPERNYM);
			for(Key key : sense.getKeys()) {
				productions.add(key.getLemma());
			}
			for(Pointer p : hyperPointers) {
				addHypernyms(productions, p.getTargetSense(), level+1);
			}
		}
	}
	
}
