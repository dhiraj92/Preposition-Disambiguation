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

package miacp.featgen.wfr;

import java.util.List;
import java.util.Map;
import java.util.Set;

import miacp.featgen.InitException;
import miacp.parse.types.Arc;
import miacp.parse.types.Parse;
import miacp.parse.types.Token;


/**
 * Interface for a word-finding rule. Word-finding rules are used to identify
 * words that can be used for feature generation.
 */
public interface WordFindingRule {
	
	abstract public void init(Map<String, String> params) throws InitException;
	abstract public Set<Token> getProductions(List<Token> tokenList, Parse parse, int tokenIndex);
	
}
