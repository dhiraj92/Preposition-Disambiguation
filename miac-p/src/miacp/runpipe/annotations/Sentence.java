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

package miacp.runpipe.annotations;

import miacp.runpipe.TextDocument;
import miacp.runpipe.impl.AnnotationImpl;

/**
 * Sentence annotation
 */
public class Sentence extends AnnotationImpl {
	
	public final static long serialVersionUID = 2;
	
	private String mId;
	private int mNum;
	private String mParseString = "";
	
	public Sentence(TextDocument doc, int start, int end) {
		super(doc, start, end);
	}
	
	public Sentence(TextDocument doc) {
		super(doc);
	}
	
	public void setId(String id) {
		mId = id;
	}
	
	public String getId() {
		return mId;
	}
	
	public void setSentenceNum(int num) {
		mNum = num;
	}
	
	public int getSentenceNum() {
		return mNum;
	}
	
	public void setParseString(String parseString) {
		mParseString = parseString;
	}
	
	public String getParseString() {
		return mParseString;
	}
	
}
