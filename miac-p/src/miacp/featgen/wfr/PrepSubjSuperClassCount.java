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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jigsaw.JIGSAW;
import miacp.featgen.InitException;
import miacp.jwni.IndexEntry;
import miacp.jwni.POS;
import miacp.jwni.Sense;
import miacp.jwni.Sense.Key;
import miacp.jwni.WordNet;
import miacp.parse.types.Arc;
import miacp.parse.types.Parse;
import miacp.parse.types.Token;
import miacp.parse.util.ParseConstants;
import miacp.util.TreebankConstants;

/**
 * Returns the complement of a preposition
 *
 */
public class PrepSubjSuperClassCount extends AbstractWordFindingRule {

	private static final long serialVersionUID = 1L;
	HashMap<String, Integer> priors;

	@Override
	public void init(Map<String, String> params) throws InitException {
		// TODO Auto-generated method stub
		super.init(params);

		try {
			FileInputStream f_in = new FileInputStream("data/hashmapEight_ALL.ser");
			ObjectInputStream obj_in = new ObjectInputStream(f_in);
			priors = (HashMap<String, Integer>) obj_in.readObject();
			obj_in.close();
			f_in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Set<Token> getProductions(List<Token> tokenList, Parse parse, int tokenIndex) {
		Set<Token> results = new HashSet<Token>();

		try {
			Token tok = tokenList.get(tokenIndex);
			Collection<Token> objects = new HashSet<Token>();
			Arc headArc = parse.getHeadArcs()[tok.getIndex()];
			if (tok != null && headArc != null) {
				if (ParseConstants.SUBORDINATE_CLAUSE_MARKER_DEP.equals(headArc.getDependency())) {
					Token subordinatedClauseHead = headArc.getHead();
					Arc ghead = parse.getHeadArcs()[subordinatedClauseHead.getIndex()];
					if (ghead != null && ParseConstants.ADVERBIAL_CLAUSE_DEP.equals(ghead.getDependency())) {
						Token headClauseHead = ghead.getHead();
						objects.add(headClauseHead);
					}
				} else {
					Token headToken = headArc.getHead();
					if (headToken != null) {
						objects.add(headToken);
					}
				}
			}

			// Get probabilities
			for (Token token : objects) {
				HashSet<String> superClasses = getSuperClasses(token);

				if (superClasses != null && superClasses.size() > 1) {
					for (String superClass : superClasses) {
						String probKey = superClass + ":" + tokenList.get(tokenIndex).getText();
						Integer count = priors.get(probKey);
						if (count != null) {
							// System.out.println(probKey + ":" + count);
							results.add(new Token(String.valueOf(count), token.getIndex()));
						}
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return results;
	}

	POS getPosForType(String type) {
		POS pos = null;
		if (TreebankConstants.NOUN_LABELS.contains(type)) {
			pos = POS.NOUN;
		} else if (TreebankConstants.VERB_LABELS.contains(type)) {
			pos = POS.VERB;
		} else if (TreebankConstants.ADJ_LABELS.contains(type)) {
			pos = POS.ADJECTIVE;
		} else if (TreebankConstants.ADV_LABELS.contains(type)) {
			pos = POS.ADVERB;
		}
		return pos;
	}

	HashSet<String> getSuperClasses(Token token){
		POS pos = getPosForType(token.getPos());
		HashSet<String> lexNames = new HashSet<String>();
		if(pos != null) {
			IndexEntry entry = WordNet.getInstance().lookupIndexEntry(pos, token.getText());
			if(entry != null) {
				Sense[] senses = entry.getSenses();
				for(int i = 0; i < senses.length && i < 1;i++) {
					Key[] keys = senses[0].getKeys();
					if (keys.length > 0){
						String lexFileName = keys[0].getLexFileName();
						lexNames.add(lexFileName.substring(lexFileName.indexOf(".") + 1));
					}					
				}
			}
		}
		return lexNames;
	}
}
