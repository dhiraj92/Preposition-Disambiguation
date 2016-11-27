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
import miacp.jwni.Sense;
import miacp.jwni.Sense.Key;
import miacp.jwni.WordNet;
import miacp.util.TreebankConstants;



/**
 * Feature extraction rule for returning the list of sentence frames associated
 * with a particular word (only applicable to verbs)
 *
 */
public class SentenceFramesFER extends AbstractWordNetFER {
	
	private static final long serialVersionUID = 1L;

	@Override
	public Set<String> generateFeatures(String input, String type, Set<String> productions) {
		POS pos = null;
		if(TreebankConstants.VERB_LABELS.contains(type)) {
			pos = POS.VERB;		
			IndexEntry entry = WordNet.getInstance().getMorpho().lookupIndexEntry(pos, input, true);
			if(entry != null) {
				Sense[] senses = entry.getSenses();
				for(int s = 0; s < senses.length && s < mMaxSenseNum; s++) {
					Sense sense = senses[s];
					for(Key key : sense.getKeys()) {
						int[] sentenceFrames = key.getFrames();
						int numFrames = sentenceFrames.length;
						for(int i = 0; i < numFrames; i++) {
							productions.add(Integer.toString(sentenceFrames[i]));
						}
					}
				}
			}
		}
		return productions;
	}
	
}
