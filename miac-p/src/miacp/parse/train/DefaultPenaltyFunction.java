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

package miacp.parse.train;

import java.util.List;

import miacp.parse.ParseAction;
import miacp.parse.types.Arc;
import miacp.parse.types.Token;
import miacp.parse.types.TokenPointer;
import miacp.parse.util.ParseConstants;


/**
 * The default penalty function. All penalties are of value 1.
 * Basically, the penalty function returns 1 if the action is wrong and 0 if it is okay.
 *
 */
public class DefaultPenaltyFunction implements PenaltyFunction {
	
	public double calculatePenalty(TokenPointer ptr, 
            ParseAction action, 
            boolean goldParseIsIncomplete,
            Arc[] tokenToHead, 
            List[] gold, 
            List[] current, 
            Token[] tokenToSubcomponentHead, 
            int[] projectiveIndices) {
		double penalty = 0;
		Token tok = action.token;

		if(action.actionName.equals(ParseConstants.SWAP_RIGHT_ACTION_NAME)) {
			penalty = ptr.next != null && projectiveIndices[tok.getIndex()] > projectiveIndices[ptr.next.tok.getIndex()] ? 0 : 1;
		}
		else if(action.actionName.equals(ParseConstants.SWAP_LEFT_ACTION_NAME)) {
			penalty = ptr.prev != null && projectiveIndices[tok.getIndex()] < projectiveIndices[ptr.prev.tok.getIndex()] ? 0 : 1;
		}
		else {
			TokenPointer head = null, child = null;
			if(action.actionName.endsWith("l")) {
				head = ptr.next;
				child = ptr;
			}
			else if(action.actionName.endsWith("r")) {
				head = ptr;
				child = ptr.next;
			}
			else {
				throw new IllegalArgumentException("ERROR! Unexpected action (" + action.actionName + ") encountered!!! Doesn't end with 'l' or 'r'");
			}

			if(child == null || head == null) {
				penalty = 1;
			}
			else {
				List<Arc> goldArcs = gold[child.tok.getIndex()];
				List<Arc> currentArcs = current[child.tok.getIndex()];
				int numGoldArcs = (goldArcs == null) ? 0 : goldArcs.size();
				int numCurrentArcs = (currentArcs == null) ? 0 : currentArcs.size();

				String dependency = action.actionName.substring(0, action.actionName.length()-1);
				Arc headArc = tokenToHead[child.tok.getIndex()];
				// 	penalize incorrect head OR incorrect relationship type
				if(headArc != null && headArc.getHead() != head.tok) {
					penalty = 1;
				}
				else if(headArc != null && !headArc.getDependency().equals(dependency)) {
					penalty = 1; // correct attachment, but wrong dep
				}
				// penalize premature attachment
				if(numGoldArcs > numCurrentArcs) {
					penalty = 1;
				}
				else if(goldParseIsIncomplete) {
					// If the gold parse is incomplete, there may be the right number of arcs, but not have the *correct* arcs
					// Note: This is somewhat expensive. 
					boolean hasAllGoldArcs = true;
					if(goldArcs != null) {
						for(Arc goldArc : goldArcs) {
							boolean hasArc = false;
							for(Arc currentArc : currentArcs) {
								if(currentArc.getChild().getIndex() == goldArc.getChild().getIndex()) {
									hasArc = true;
									break;
								}
							}
							if(!hasArc) {
								hasAllGoldArcs = false;
								break;
							}
						}
					}
					if(!hasAllGoldArcs) {
						penalty = 1; 
					}
				}
				//penalty = numGoldArcs-numCurrentArcs;	
			}
		}
		return penalty;
	}
	
}