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

package miacp.runpipe.impl.docreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import miacp.runpipe.TextDocument;
import miacp.runpipe.TextDocumentReader;
import miacp.runpipe.annotations.Sentence;



public class OneSentencePerLineDocReader implements TextDocumentReader {
	

	public void initialize(Map<String, String> params) {
		
	}
	
	public void hydrateDocument(InputStream istream, TextDocument doc) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
		String l = null;
		List<String> sentences = new ArrayList<String>();
		while((l = reader.readLine()) != null) {
			sentences.add(l);
		}
		StringBuffer docText = new StringBuffer();
		int sentenceNum = 1;
		int start = 0;
		for(String sentence : sentences) {
			int length = sentence.length();
			Sentence newSentence = new Sentence(doc, start, start+length);
			newSentence.setSentenceNum(sentenceNum++);
			doc.addAnnotation(newSentence);
			docText.append(sentence);
			start+=length;				
		}
		doc.setText(docText.toString());
		reader.close();
	}
}
