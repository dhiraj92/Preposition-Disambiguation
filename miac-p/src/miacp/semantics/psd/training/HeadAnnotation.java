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

import miacp.runpipe.TextDocument;
import miacp.runpipe.impl.AnnotationImpl;

/**
 * Annotation class for a preposition instance (used for feature extraction for preposition sense disambiguation)
 *
 */
public class HeadAnnotation extends AnnotationImpl {
	
	public static final long serialVersionUID = 6910373698687872271l;
	
	private String mId;
	private String mSenseId;
	
	public HeadAnnotation(TextDocument doc) {
		super(doc);
	}
	
	public HeadAnnotation(TextDocument doc, int start, int end) {
		super(doc, start, end);
	}
	
	public void setId(String id) {
		mId = id;
	}
	
	public void setSenseId(String senseId) {
		mSenseId = senseId;
	}
	
	public String getId() {
		return mId;
	}
	
	public String getSenseId() {
		return mSenseId;
	}
	
}
