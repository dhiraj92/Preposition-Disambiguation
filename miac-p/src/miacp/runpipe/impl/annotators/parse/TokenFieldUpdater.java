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

package miacp.runpipe.impl.annotators.parse;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import miacp.jwni.IndexEntry;
import miacp.jwni.POS;
import miacp.jwni.WordNet;
import miacp.runpipe.InitializationException;
import miacp.runpipe.ProcessException;
import miacp.runpipe.TextDocument;
import miacp.runpipe.annotations.Token;
import miacp.runpipe.impl.AnnotatorImpl;
import miacp.util.TreebankConstants;



public class TokenFieldUpdater extends AnnotatorImpl implements TreebankConstants {
	
	public static final String PARAM_WORDNET_DIR = "WordNetDir";
	
	public void initialize(Map<String, String> params) throws InitializationException {
		String wordNetPath = params.get(PARAM_WORDNET_DIR);
		if(WordNet.getInstance() == null) {
			try {
				String path = new File(wordNetPath).getAbsolutePath();
				new WordNet(new File(path).getAbsoluteFile().toURI().toString());
			}
			catch(IOException ioe) {
				throw new InitializationException(ioe);
			}
		}
	}
	
	public void process(TextDocument doc) throws ProcessException {
		List<Token> tokens = (List)doc.getAnnotationList(Token.class);
		if(tokens != null) {
			for(Token token : tokens) {
				updateField(token);
			}
		}
	}
	
	private void updateField(Token tok) {
		String xPos = tok.getPos();
		POS jpos = getPosForType(xPos);
		if(jpos != null) {
			IndexEntry ie = null;
			try {
				for(POS speechPart : new POS[]{jpos, POS.NOUN, POS.VERB, POS.ADJECTIVE}) {
					if(ie == null) {
						ie = WordNet.getInstance().lookupIndexEntry(speechPart, tok.getAnnotText().replace(' ', '_'));
					}
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			if(ie != null) {
				tok.setLemma(ie.getLemma());
			}
			else {
				tok.setLemma(tok.getAnnotText().toLowerCase());
			}
		}
		else {
			tok.setLemma(tok.getAnnotText().toLowerCase());
		}
	}
	
	protected POS getPosForType(String type) {
		POS pos = null;
		if(TreebankConstants.NOUN_LABELS.contains(type)) {
			pos = POS.NOUN;
		}
		else if(TreebankConstants.VERB_LABELS.contains(type)) {
			pos = POS.VERB;
		}
		else if(TreebankConstants.ADJ_LABELS.contains(type)) {
			pos = POS.ADJECTIVE;
		}
		else if(TreebankConstants.ADV_LABELS.contains(type)) {
			pos = POS.ADVERB;
		}
		return pos;
	}
	
}
