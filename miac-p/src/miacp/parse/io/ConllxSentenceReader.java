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

package miacp.parse.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import miacp.parse.types.Arc;
import miacp.parse.types.Parse;
import miacp.parse.types.Sentence;
import miacp.parse.types.Token;


/**
 * Designed to read in CoNLL-X format
 *
 */
public class ConllxSentenceReader implements SentenceReader {
	
	public final static String PARAM_CANONICALIZE = "canonicalize";
	
	// canonicalizing map.. this ought to be made optional to avoid memory leak situations
	private Map<String, String> mCanon = new HashMap<String, String>();
	
	private boolean mCanonicalize = true;
	
	private class ConllxLine {
		public final static int WORD_NUM_INDEX = 0,
								WORD_FORM_INDEX = 1,
								LEMMA_INDEX = 2,
								CPOS_INDEX = 3,
								POS_INDEX = 4,
								FEATS_INDEX = 5,
								HEAD_NUM_INDEX = 6,
								DEPENDENCY_INDEX = 7;
		String form, lemma, cpos, pos, dependency;
		int wordNum, headNumIndex;
		public ConllxLine(String line) {
			String[] split = line.split("\\t+");
			wordNum = Integer.parseInt(split[WORD_NUM_INDEX]);
			form = canon(split[WORD_FORM_INDEX]);
			lemma = canon(split[LEMMA_INDEX]);
			cpos = canon(split[CPOS_INDEX]);
			pos = canon(split[POS_INDEX]);
			headNumIndex = Integer.parseInt(split[HEAD_NUM_INDEX]);
			dependency = canon(split[DEPENDENCY_INDEX]);
		}
	}
	
	@Override
	public void init(Map<String, String> params) {
		String value = params.get(PARAM_CANONICALIZE);
		if(value != null) {
			mCanonicalize = Boolean.parseBoolean(value);
		}
	}
	
	@Override
	public Parse readSentence(BufferedReader reader) throws IOException {
		Token root = new Token("[ROOT]", 0);
		List<Token> tokens = new ArrayList<Token>();
		List<Arc> arcs = new ArrayList<Arc>();
		
		String line = null;
		Map<Integer, Token> numToToken = new HashMap<Integer, Token>();
		numToToken.put(0, root);
		List<ConllxLine> lines = new ArrayList<ConllxLine>();
		
		while((line = reader.readLine()) != null) {
			if(line.trim().equals("")) {
				if(tokens.size() == 0) {
					continue;
				}
				else {
					break;
				}
			}
			else {
				ConllxLine conllxLine = new ConllxLine(line);
				lines.add(conllxLine);
				int tokenNum = lines.size();
				
				Token token = new Token(conllxLine.form, tokenNum);
				token.setCoarsePos(conllxLine.cpos);
				token.setPos(conllxLine.pos);
				
				tokens.add(token);
				numToToken.put(tokenNum, token);
			}
		}
		int numLines = lines.size();
		for(int lnum = 0; lnum < numLines; lnum++) {
			ConllxLine conllxLine = lines.get(lnum);
			int tokenNum = lnum+1;
			int head = conllxLine.headNumIndex;
			
			if(head != -1) {
				arcs.add(new Arc(numToToken.get(tokenNum), numToToken.get(head), conllxLine.dependency));
			}
		}
		
		Parse result = null;
		try {
			if(tokens.size() > 0) {
				result = new Parse(new Sentence(tokens), root, arcs);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			for(Token t : tokens) {
				System.err.println(t.getText());
			}
		}
		return result;
	}
	
	private String canon(String s) {
		String retValue;
		if(mCanonicalize) {
			retValue = mCanon.get(s);
			if(retValue == null) mCanon.put(s, retValue = s);
		}
		else {
			retValue = s;
		}
		return retValue;
	}
	
}