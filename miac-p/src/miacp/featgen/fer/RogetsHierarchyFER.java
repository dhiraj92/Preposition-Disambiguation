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

package miacp.featgen.fer;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import miacp.featgen.InitException;
import miacp.rogi.Roget;
import miacp.rogi.Roget.Division;
import miacp.rogi.Roget.Entry;
import miacp.rogi.Roget.POS;
import miacp.rogi.Roget.Term;
import miacp.util.TreebankConstants;


/**
 * Feature extraction rule for creating features based on Roget's Thesaurus hierarchy
 *
 */
public class RogetsHierarchyFER extends AbstractFeatureRule {
	
	private static final long serialVersionUID = 1L;
	
	
	public final static String PARAM_DEFAULT_POS = "DefaultPos";
	
	private String mDefaultPos;
	
	@Override
	public void init(Map<String, String> params) throws InitException {
		super.init(params);
		mDefaultPos = params.get(PARAM_DEFAULT_POS);
	}

	@Override
	public Set<String> getProductions(String input, String type, Set<String> results) {
		if(Roget.getInstance() == null) {
			try {
				Roget.init();
			}
			catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
		if(type == null) {
			type = mDefaultPos;
		}
		POS pos = null;
		if(TreebankConstants.NOUN_LABELS.contains(type)) {
			pos = POS.N;
		}
		else if(TreebankConstants.VERB_LABELS.contains(type)) {
			pos = POS.V;
		}
		else if(TreebankConstants.ADJ_LABELS.contains(type)) {
			pos = POS.ADJ;
		}
		if(pos != null) {
			Set<Term> terms = Roget.getInstance().getTerms(pos, input);
			if(terms != null) {
				for(Term t : terms) {
					Entry entry = t.getEntry();
					results.add("re:"+entry.getNum());
					Division div = entry.getDivision();
					while(div != null) {
						results.add("rd:"+div.getName().hashCode());
						div = div.getParent();
					}
				}
			}
		}
		return results;
	}
}
