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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
public class PrepObjSuperClassCount extends AbstractWordFindingRule {

	private static final long serialVersionUID = 1L;
	HashMap<String, Integer> priors;
	JIGSAW js;
	String kParserDir = "data/kParser";
	
	@Override
	public void init(Map<String, String> params) throws InitException {
		// TODO Auto-generated method stub
		super.init(params);
		js = new JIGSAW();
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
			// Get Object of Preposition Token //
			Token head = tokenList.get(tokenIndex);
			Collection<Token> objects = new HashSet<Token>();
			if (head != null) {
				Arc garc = parse.getHeadArcs()[head.getIndex()];
				if (garc != null && ParseConstants.SUBORDINATE_CLAUSE_MARKER_DEP.equals(garc.getDependency())) {
					Token subordinatedClauseHead = garc.getHead();
					objects.add(subordinatedClauseHead);
				} else {
					List<Arc> children = parse.getDependentArcLists()[head.getIndex()];
					if (children != null) {
						for (Arc child : children) {
							if (child.getDependency().matches("pobj|pcomp")) {
								objects.add(child.getChild());
							}
						}
					}
				}
			}
			
			String sentence = parse.getSentence().toString();
			
			String fileName = sentence.substring(0,15) + sentence.hashCode();
			fileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
			String filePath = kParserDir + "/" + fileName;
			File file = new File(filePath);
			HashMap<String, ArrayList<String>> jsMap;
			if(file.exists() && ! file.isDirectory()){
				FileInputStream fin = new FileInputStream(file);
				ObjectInputStream oin = new ObjectInputStream(fin);
				jsMap = (HashMap<String, ArrayList<String>>) oin.readObject();
				fin.close();
				oin.close();
			} else{
				jsMap = js.getWordSenses(sentence, 0);
				FileOutputStream fout = new FileOutputStream(file);
				ObjectOutputStream oos = new ObjectOutputStream(fout);
				oos.writeObject(jsMap);
				oos.close();
				fout.close();
			}

			// Get probabilities
			for (Token token : objects) {
				String mapKey = token.getText().toLowerCase() + "_" + token.getIndex();
				
				if(jsMap.containsKey(mapKey)){
					ArrayList<String> arr = jsMap.get(mapKey);
					String superClass = arr.get(1);
					if(superClass != null){
						String countKey = superClass.substring(superClass.indexOf(".") + 1) + ":" + tokenList.get(tokenIndex).getText().toLowerCase();
						Integer count = priors.get(countKey);
						if (count != null){
							// System.out.println(countKey + "-" + count);
							results.add(new Token(String.valueOf(count), token.getIndex()));
						}
					}					
				}
				
				/*HashSet<String> superClasses = getSuperClasses(token);
				if (superClasses != null && superClasses.size() > 0) {
					// System.out.println(token.getText() + " : " +
					// superClasses);sentence
					for (String superClass : superClasses) {
						String probKey = superClass + ":" + tokenList.get(tokenIndex).getText();
						Integer count = priors.get(probKey);
						if (count != null) {
							System.out.println(probKey + ":" + count);
							String mapKey = token.getText().toLowerCase() + "_" + token.getIndex();
							if(jsMap.containsKey(mapKey)){
								System.out.println("JIGSAW : " + jsMap.get(mapKey));
							}
							results.add(new Token(String.valueOf(count), token.getIndex()));
						}
					}
				}*/
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

	HashSet<String> getSuperClasses(Token token) {
		POS pos = getPosForType(token.getPos());
		HashSet<String> lexNames = new HashSet<String>();
		if (pos != null) {
			IndexEntry entry = WordNet.getInstance().lookupIndexEntry(pos, token.getText());
			if (entry != null) {			
				
				Sense[] senses = entry.getSenses();
				
				HashSet<String> allSenses = new HashSet<String>();
				
				for (int i = 0; i < senses.length; i++) {					
					Key[] keys = senses[i].getKeys();
					if (keys.length > 0) {
						for(Key key : keys){
							allSenses.add(key.getLexFileName());
						}
					}
				}
				
				System.out.println(token.getText() + " : " + allSenses);
				
				for (int i = 0; i < senses.length && i < 1; i++) {					
					Key[] keys = senses[0].getKeys();
					if (keys.length > 0) {						
						String lexFileName = keys[0].getLexFileName();
						lexNames.add(lexFileName.substring(lexFileName.indexOf(".") + 1));
					}
				}
				
				
			}
		}
		return lexNames;
	}
}
