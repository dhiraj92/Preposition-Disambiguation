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

package miacp.featgen.wfr;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import miacp.parse.types.Parse;
import miacp.parse.types.Token;
import miacp.util.TreebankConstants;

/**
 * Word-finding rule for finding the first likely syntactic governor of a preposition
 * (to the left of the preposition)
 *
 */
public class PrepositionGovernorHeuristic extends AbstractWordFindingRule {
	
	private static final long serialVersionUID = 1L;
	private static final char[] ILLEGAL_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':' };

	@Override
	public Set<Token> getProductions(List<Token> tokenList, Parse parse, int headIndex) {
		Set<Token> results = new HashSet<Token>();

			for(int i = headIndex-1; i >= 0; i--) {
				Token tok = tokenList.get(i);
				String pos = tok.getPos();
				if(TreebankConstants.NOUN_LABELS.contains(pos) || 
				   TreebankConstants.VERB_LABELS.contains(pos) || 
				   TreebankConstants.PRON_LABELS.contains(pos) || 
				   TreebankConstants.ADJ_LABELS.contains(pos)) {
					results.add(tok);
					//System.out.println(tokenList.get(headIndex).getText()+" from earlier heurstic "+ tok.getText());
					break;
				}
			}
		
		
		return results;
	}
	
}
