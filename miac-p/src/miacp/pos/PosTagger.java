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

package miacp.pos;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import miacp.ml.ClassScoreTuple;
import miacp.ml.LinearClassificationModel;
import miacp.parse.types.Token;
import miacp.pos.featgen.PosFeatureGenerator;


public class PosTagger {
	
	protected PosFeatureGenerator mFeatureGenerator; 
	protected LinearClassificationModel mDecisionModule;
	
	public PosTagger(File modelFile) throws IOException, ClassNotFoundException {
		InputStream is = new FileInputStream(modelFile);
		if(modelFile.getName().endsWith(".gz")) {
			is = new GZIPInputStream(is);
		}
		ObjectInputStream ois = new ObjectInputStream(is);
		mDecisionModule = (LinearClassificationModel)ois.readObject();
		mFeatureGenerator = (PosFeatureGenerator)ois.readObject();
		ois.close(); 
	}
	
	// For Don
	public static List<Token> makeMeSomeTokens(String[] wordsInSentence) {
		List<Token> tokens = new ArrayList<Token>();
		for(int i = 0; i < wordsInSentence.length; i++) {
			tokens.add(new Token(wordsInSentence[i], (i+1))); // 0 is reserved for ROOT
		}
		return tokens;
	}
	
	public void posTag(List<Token> sentenceTokens) {
		posTag(sentenceTokens, false);
	}
	
	public void posTag(List<Token> sentenceTokens, boolean reverseDirection) {
		final int numTokens = sentenceTokens.size();
		if(reverseDirection) {
			for(int j = numTokens-1; j >= 0; j--) {
				posTag(sentenceTokens, j);
			}
		}
		else {
			for(int j = 0; j < numTokens; j++) {
				posTag(sentenceTokens, j);
			}
		}
	}
	
	private void posTag(List<Token> sentenceTokens, int index) {
		Token t = sentenceTokens.get(index);
		Set<String> feats = mFeatureGenerator.getFeats(sentenceTokens, index);
		// Ranking of part-of-speech tags
		ClassScoreTuple[] classRankings = mDecisionModule.getDecision(feats);
		// Use highest rated tag for this token
		t.setPos(classRankings[0].clazz);
	}
}
