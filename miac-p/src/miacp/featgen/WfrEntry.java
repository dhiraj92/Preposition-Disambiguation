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

package miacp.featgen;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import miacp.featgen.fer.FeatureExtractionRule;
import miacp.featgen.wfr.WordFindingRule;


public class WfrEntry implements Serializable {
		
		private static final long serialVersionUID = 1L;
		private WordFindingRule mRule;
		
		private String mName;
		private String mPrefix;
		
		private List<FeatureExtractionRule> mFERs;
		
		public String getName() {return mName;}
		public String getPrefix() {return mPrefix;}
		public List<FeatureExtractionRule> getFERs() {return mFERs;}
		
		public WfrEntry(String name, 
				           String prefix, 
				           List<FeatureExtractionRule> fers, 
				           String className,
				           Map<String, String> params) throws Exception {
			mName = name;
			mPrefix = prefix;
			mFERs = fers;
			mRule = (WordFindingRule)Class.forName(className).newInstance();
			mRule.init(params);
		}
		
		public WfrEntry(String name, 
		           String prefix, 
		           List<FeatureExtractionRule> fers, 
		           WordFindingRule wfr) {
			mName = name;
			mPrefix = prefix;
			mFERs = fers;
			mRule = wfr;
		}
		
		public WordFindingRule getWfrRule() {
			return mRule;
		}
	}
