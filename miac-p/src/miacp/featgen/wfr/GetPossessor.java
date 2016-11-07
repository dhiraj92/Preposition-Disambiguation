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
import java.util.Map;
import java.util.Set;

import miacp.jwni.IndexEntry;
import miacp.jwni.POS;
import miacp.jwni.WordNet;
import miacp.parse.types.Arc;
import miacp.parse.types.Parse;
import miacp.parse.types.Token;
import miacp.parse.util.ParseConstants;


/**
 * Looks up the possessor of the given word
 *
 */
public class GetPossessor extends AbstractWordFindingRule {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public void init(Map<String, String> params) {
		
	}

	@Override
	public Set<Token> getProductions(List<Token> tokenList, Parse parse,  int tokIndex) {
		Set<Token> results = new HashSet<Token>();
		Token proposedPossessee = tokenList.get(tokIndex);
		
		List<Arc> children = parse.getDependentArcLists()[proposedPossessee.getIndex()];
		if(children != null) {
			for(Arc childArc : children) {
				if(childArc.getDependency().equals(ParseConstants.POSSESSOR_DEP)) {
					Token modifier = childArc.getChild();
					String pl = modifier.getText();
					if(modifier.getText().toLowerCase().matches("(corp|co|plc|inc|ag|ltd|llc)\\.?")) {
						pl = "corporation";
					}
					else if(modifier.getPos().startsWith("NNP")) {
						IndexEntry ie = WordNet.getInstance().lookupIndexEntry(POS.NOUN, pl);
						if(ie == null) {
							StringBuilder nounMods = new StringBuilder();
							List<Arc> arcs = parse.getDependentArcLists()[modifier.getIndex()];
							if(arcs != null) {
							for(Arc a : arcs) {
								if(a.getDependency().equals(ParseConstants.NOUN_COMPOUND_DEP)) {
									nounMods.append(a.getChild().getText()+" ");
								}
							}
							if(nounMods.length() > 0) {
								String newLeftString = nounMods.toString() + pl;
								ie = WordNet.getInstance().lookupIndexEntry(POS.NOUN, newLeftString);
								if(ie != null) {
									pl = ie.getLemma();
								}
							}
							}
						}
					}
					
					String pos = modifier.getPos();
					if(pl.toLowerCase().equals("her") || 
							pl.toLowerCase().equals("his") || 
							pl.toLowerCase().equals("my") || 
							pl.toLowerCase().equals("your")) {
						pl = "person";
						pos = "NN";
					}
					
					results.add(new Token(pl, pos, modifier.getIndex()));
					break;
				}
			}
		}
		return results;
	}
	
}
