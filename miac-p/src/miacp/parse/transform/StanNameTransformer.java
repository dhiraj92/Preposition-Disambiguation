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

package miacp.parse.transform;

import miacp.parse.types.Arc;
import miacp.parse.types.Parse;
import miacp.parse.util.ParseConstants;

public class StanNameTransformer implements ParseTransform {
	
	@Override
	public void performTransformation(Parse parse) {
		for(Arc a : parse.getHeadArcs()) {
			if(a != null) {
				String dep = a.getDependency();
				if(dep.equals(ParseConstants.ROOT_DEP)) {
					dep = ParseConstants.STANFORD_ROOT_DEP;
				}
				else if(dep.equals(ParseConstants.INFINITIVE_MARKER_DEP)) {
					dep = ParseConstants.AUXILLARY_DEP;
				}
				else if(dep.equals(ParseConstants.WH_ADVERBIAL_DEP)) {
					dep = ParseConstants.ADVERBIAL_DEP;
				}
				else if(dep.equals(ParseConstants.COMBO_DEP)) {
					dep = ParseConstants.UNSPECIFIED_DEP;
				}
				else if(dep.equals(ParseConstants.AGENT_DEP)) {
					dep = ParseConstants.PREP_MOD_DEP; // stanford basic doesn't seem to have 'agent'
				}
				else if(dep.equals(ParseConstants.INITIAL_COORDINATING_CONJUNCTION_DEP)) {
					dep = ParseConstants.COORDINATING_CONJUNCTION_DEP;
				}
				else if(dep.equals(ParseConstants.EXTRAPOSED_IT_DEP)) {
					dep = ParseConstants.CLAUSAL_COMPLEMENT_WO_SUBJ_DEP;
				}
				else if(dep.equals(ParseConstants.CLEFT_DEP)) {
					dep = ParseConstants.UNSPECIFIED_DEP;
				}
				else if(dep.equals(ParseConstants.POSTERIOR_LOCATION_DEP)) {
					dep = ParseConstants.APPOSITIVE_DEP;
				}
				a.setDependency(dep);
			}
		}
	}
}
