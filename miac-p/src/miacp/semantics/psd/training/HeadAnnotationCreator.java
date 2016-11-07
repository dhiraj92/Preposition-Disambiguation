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

package miacp.semantics.psd.training;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import miacp.runpipe.ProcessException;
import miacp.runpipe.TextDocument;
import miacp.runpipe.annotations.Token;
import miacp.runpipe.impl.AnnotatorImpl;


public class HeadAnnotationCreator extends AnnotatorImpl {
	
	private Set<String> mPrepositionsOfInterest = new HashSet<String>(Arrays.asList(new String[]{
			"about","above","across","after","against","along","among","around","as","at","before","behind","beneath","beside","between","by", "down", "during",
			"for", "from", "in", "inside", "into", "like", "of", "off", "on", "onto", "over", "round", "through", "to", "towards", "with"}));
		
	
	public void process(TextDocument doc) throws ProcessException {
		List<Token> tokens = (List<Token>)doc.getAnnotationList(Token.class);
		for(Token tok : tokens) {
			if(mPrepositionsOfInterest.contains(tok.getAnnotText().toLowerCase())) {
				doc.addAnnotation(new HeadAnnotation(doc, tok.getStart(), tok.getEnd()));
			}	
		}
	}
	
}
