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

package miacp.parse.ml;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import miacp.parse.types.Arc;
import miacp.parse.types.Token;
import miacp.parse.util.ParseConstants;


abstract public class AbstractParseModel implements ParseModel {
	
	public final static long serialVersionUID = 1;
	
	protected List<String> mActions;
	
	protected Map<String, Map<String, List<String>>> mPosPosActs = new HashMap<String, Map<String,List<String>>>(); 
	
	protected Map<String, Integer> mActionToIndex;
	
	@Override
	public List<String> getActions() {
		return mActions;
	}
	
	@Override
	public void incrementCount() {
		throw new UnsupportedOperationException("Not implemented for this model: " + this.getClass().getCanonicalName());
	}
	
	@Override
	public void update(String action, Collection<String> features, double change) {
		throw new UnsupportedOperationException("Not implemented for this model: " + this.getClass().getCanonicalName());
	}
	
	@Override
	public void updateFeature(int actionIndex, int feat, double change) {
		throw new UnsupportedOperationException("Not implemented for this model: " + this.getClass().getCanonicalName());
	}
	
	
	public int getActionIndex(String action, boolean addIfNecessary) {
		Integer actionIndex = mActionToIndex.get(action);
		if(actionIndex == null && addIfNecessary) {
			mActionToIndex.put(action, actionIndex = mActionToIndex.size()+1);
		}
		return actionIndex;
	}
	
	private List<String> EMPTY_LIST = new ArrayList<String>();
	
	@Override
	public void addAction(String pos1, String pos2, String action) {
		Map<String, List<String>> actsMap = mPosPosActs.get(pos1);
		actsMap.get(pos2).add(action);
	}
	
	@Override
	public List<String> getActions(Token tc, Token tr, Arc[] goldTokenToHead) {
		
		if(EMPTY_LIST == null) {
			EMPTY_LIST = new ArrayList<String>();
		}
		List<String> actions;
		Map<String, List<String>> acts = mPosPosActs.get(tc.getPos());
		if(acts == null) {
			mPosPosActs.put(tc.getPos(), acts = new HashMap<String, List<String>>());
		}
		actions = tr != null ? acts.get(tr.getPos()) : EMPTY_LIST;
		if(actions == null) {
			acts.put(tr.getPos(), actions = new ArrayList<String>());
			
			actions.add(ParseConstants.SWAP_RIGHT_ACTION_NAME);
			actions.add(ParseConstants.SWAP_LEFT_ACTION_NAME);
				
			actions.add(ParseConstants.UNSPECIFIED_DEP+"r");
			actions.add(ParseConstants.UNSPECIFIED_DEP+"l");
				
		}
		return actions;
	}
	
}