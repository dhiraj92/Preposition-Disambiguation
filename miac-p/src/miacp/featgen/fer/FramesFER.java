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
import miacp.jwni.LexPointer;
import miacp.jwni.POS;
import miacp.jwni.PointerType;
import miacp.jwni.Sense;
import miacp.jwni.WordNet;



/**
 * Feature Extraction Rule that returns the list of sentence frames for a given
 * word in WordNet
 *
 */
public class FramesFER extends AbstractWordNetFER {
	
	private static final long serialVersionUID = 1L;

	@Override
	public Set<String> generateFeatures(String term, String type, Set<String> productions) {
		POS pos = getPosForType(type);
		if(pos != null) {
		IndexEntry ie = WordNet.getInstance().lookupIndexEntry(pos, term);
		if(ie != null) {
			Sense[] senses = ie.getSenses();
			for(int s = 0; s < senses.length && s < mMaxSenseNum; s++) {
				if(pos == POS.NOUN) {
				LexPointer[][] lps = senses[s].getLexPointers(PointerType.DERIVED_FORM);
				int index = senses[s].getKeyIndex(term);
				if(index > -1) {
					for(int i = 0; i < lps[index].length; i++) {
						Sense targetSense = lps[index][i].getTargetSense();
						for(Sense.Key key : targetSense.getKeys()) {
							int[] frames = key.getFrames();
							if(frames != null) {
								for(int j = 0; j < frames.length; j++) {
									productions.add(Integer.toString(frames[j]));
								}
							}
						}
					}
				}
				}
				else {
					for(Sense.Key key : senses[s].getKeys()) {
						int[] frames = key.getFrames();
						if(frames != null) {
							final int numFrames = frames.length;
							for(int j = 0; j < numFrames; j++) {
								productions.add(Integer.toString(frames[j]));
							}
						}
					}
				}
			}
		}		
		}
		return productions;
	}
	
}
