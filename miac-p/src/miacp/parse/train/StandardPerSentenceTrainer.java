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

package miacp.parse.train;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import miacp.parse.NLParser;
import miacp.parse.ParseAction;
import miacp.parse.featgen.ParseFeatureGenerator;
import miacp.parse.ml.ParseModel;
import miacp.parse.types.Arc;
import miacp.parse.types.Token;
import miacp.parse.types.TokenPointer;
import miacp.parse.util.ParseConstants;
import miacp.types.IntArrayList;


/**
 * The standard <code>StandardPerSentenceTrainer</code>.
 * This code is not particularly easy to read. TODO: fix this
 *
 */
public class StandardPerSentenceTrainer implements PerSentenceTrainer {
	
	public final static double DEFAULT_MAX_UPDATE = 0.1;
	public final static int DEFAULT_MAX_ITERATIONS = 10;
	
	private double mMaxUpdate;
	private int mMaxIterations;
	
	// bad way to do logging...
	private PrintWriter mLog;
	
	public StandardPerSentenceTrainer() {
		mMaxUpdate = DEFAULT_MAX_UPDATE;
        mMaxIterations = DEFAULT_MAX_ITERATIONS;
        try {
        	//mLog = new PrintWriter(new FileWriter("trainingSteps.txt"));
        }
        catch(Exception e) {}
	}
	
	public IntArrayList getValues(ParseModel model, Set<String> fts, boolean addFeats) {
		IntArrayList values = new IntArrayList(fts.size());
		for(String f : fts) {
			int index = model.getIndex(f,addFeats);
			if(index != Integer.MIN_VALUE) {
				values.add(index);
			}
		}
		return values;
	}
	
	public static boolean hasAllItsDependents(Token topOfStack, List<Arc> arcListFull, List<Arc> arcListWorking) {
		int numFull = arcListFull == null ? 0 : arcListFull.size();
		int numWorking = arcListWorking == null ? 0 : arcListWorking.size();
		return numFull == numWorking;
	}
	
	private PenaltyFunction mPenaltyFunction = new DefaultPenaltyFunction();
	
	public TrainingResult train(
			         List<Token> sentence, 
			         List[] goldArcs, 
			         Arc[] goldTokenToHead, 
			         ParseModel model, 
			         ParseFeatureGenerator featGen, 
			         Token[] tokenToSubcomponentHead, 
			         int[] projectiveIndices) throws Exception {
		boolean maxIterationsReached = false, fatalError = false;
        
		// holder for action indices
		int[] indicesHolder = new int[model.getActions().size()];
		// holder for action scores
		double[] scores = new double[model.getActions().size()];
		
		// used for parameter averaging
		model.incrementCount();
		
		final int numTokens = sentence.size();
		boolean goldParseIsIncomplete = false;
		for(int i = 0; i < numTokens; i++) {
			if(goldTokenToHead[i+1] == null) {
				goldParseIsIncomplete = true;
				break;
			}
		}
		int numInvalids = 0;
		List[] currentArcs = new List[numTokens+1];
		
		// Create the linked-list data structure as well as the action-is-stale holder
		TokenPointer first = null;
		TokenPointer[] tokenToPtr = new TokenPointer[numTokens+1];
		boolean[] actionListStale = new boolean[numTokens+1];
		TokenPointer prev = null;
		for(int i = 0; i < numTokens; i++) {
			Token t = sentence.get(i);
			TokenPointer ptr = new TokenPointer(t, null, prev);
			if(first == null) first = ptr;
			tokenToPtr[t.getIndex()] = ptr;
			if(prev != null) {
				prev.next = ptr;
			}
			prev = ptr;
			actionListStale[i] = true;
		}
		actionListStale[numTokens] = true; // indices start at 1 for non-Root tokens
		
		Set[] tokenToStringFeats = new Set[numTokens+1];
		for(int i = 0; i < numTokens+1; i++) {
			tokenToStringFeats[i] = new HashSet<String>();
		}
		
		// Hold the possible parse actions for a given token given its current context
		Map<Token, List<ParseAction>> actionCache = new HashMap<Token, List<ParseAction>>();
		
		// Enter the training loop for this sentence
		int numIterations = 0;
		while(first.next != null) {			
			numIterations++;
			// Calculate weights for all the actions
			ParseAction highestScoredValidAction = null;
			
			ParseAction highestScoredInvalidAction = null;
			ParseAction lowestScoredValidAction = null;
			double lowestScoredValidActionScore = Double.POSITIVE_INFINITY;
			
			double highestScoredInvalidActionScore = Double.NEGATIVE_INFINITY;
			double highestScoredValidActionScore = Double.NEGATIVE_INFINITY;
			
			// Not currently using this, but it might be useful sometime
			List<ParseAction> invalidActions = new ArrayList<ParseAction>();
			//List<ParseAction> validActions = new ArrayList<ParseAction>();
			
			TokenPointer ptr = first;
			mainLoop:
			while(ptr != null) {
				Token token = ptr.tok;
				
				if(mLog != null) mLog.print(token.getText()+"\t\t");
				
				List<ParseAction> actions = actionCache.get(token);
				// First check if the actions need to be updated
				if(actionListStale[token.getIndex()]) {
					actions = null; // TODO: in the future it might make sense to reuse the same List
					
					// Generate the features and get their indices
					Set<String> feats = tokenToStringFeats[token.getIndex()];
					feats.clear();
					featGen.genFeats(feats, model, sentence, ptr, currentArcs);
					IntArrayList tokenFeatures = getValues(model, feats, false);
								
					// Get the list of possible actions
					List<String> actionNames = model.getActions(token, ptr.next == null ? null : ptr.next.tok, goldTokenToHead);
					boolean changesMade = addGoldActionIfNotPresent(actionNames, model, token, ptr.next == null ? null : ptr.next.tok, goldTokenToHead);
					if(changesMade) {
						// Should reset everything because it is possible that a new action is now available to
						// tokens outside the regular update range 
						for(TokenPointer tmpPtr = first; tmpPtr != null; tmpPtr = tmpPtr.next) {
							actionListStale[tmpPtr.tok.getIndex()] = true;
						}
						ptr = first;
						continue mainLoop;
					}
					
					// Score the actions
					model.scoreIntermediate(actionNames, tokenFeatures, indicesHolder, scores);
									
					if(actions == null) { // this will always be null (for now)
						actionCache.put(token, actions = new ArrayList<ParseAction>());
						final int numActions = actionNames.size();
						for(int i = 0; i < numActions; i++) {
							actions.add(new ParseAction(token, ptr, actionNames.get(i), scores[i]));
						}
					}
					else {
						final int numActions = actionNames.size();
						for(int i = 0; i < numActions; i++) {
							ParseAction action = actions.get(i);
							action.score = scores[i];
						}
					}
					// Ok, we've updated the actions for this token. No longer stale.
					actionListStale[token.getIndex()] = false;
				}
				
				// Time to evaluate the actions... now this is the UGLY part
				for(ParseAction action : actions) {
					if(mLog != null) mLog.print(action.actionName + ":"+action.score+"\t");
					
					TokenPointer tmpPtr = null;
					int tokenIndex = action.token.getIndex();
					double penalty = mPenaltyFunction.calculatePenalty(tokenToPtr[tokenIndex], action, goldParseIsIncomplete, goldTokenToHead, goldArcs, currentArcs, tokenToSubcomponentHead, projectiveIndices);
					if(penalty > 0 ||
							// a valid swap is invalidated by an earlier valid non-SWAP if the SWAP is not simple (has all its dependents and token+/-2 is its head)
						(
							lowestScoredValidAction != null &&
							(
								(action.actionName.equals(ParseConstants.SWAP_RIGHT_ACTION_NAME) && 
									!lowestScoredValidAction.actionName.equals(ParseConstants.SWAP_RIGHT_ACTION_NAME)
									&&!((tmpPtr = tokenToPtr[tokenIndex].next) != null &&
										(tmpPtr = tmpPtr.next) != null &&
										hasAllItsDependentsAndIsAMatch(action.token, tmpPtr.tok, goldTokenToHead[tokenIndex], goldTokenToHead, goldArcs[tokenIndex], currentArcs[tokenIndex])
										)
								)
								|| 
								(action.actionName.equals(ParseConstants.SWAP_LEFT_ACTION_NAME) && 
									!lowestScoredValidAction.actionName.equals(ParseConstants.SWAP_LEFT_ACTION_NAME)
									&&!((tmpPtr = tokenToPtr[tokenIndex].prev) != null &&
										(tmpPtr = tmpPtr.prev) != null &&
										hasAllItsDependentsAndIsAMatch(action.token, tmpPtr.tok, goldTokenToHead[tokenIndex], goldTokenToHead, goldArcs[tokenIndex], currentArcs[tokenIndex])
										)
								)
							)
						 )
						    
					) {
						// If we got here, the action must be invalid
						invalidActions.add(action);
						if(action.score > highestScoredInvalidActionScore) {
							highestScoredInvalidAction = action;
							highestScoredInvalidActionScore = action.score;							
						}
						
					}
					else {
						// If we get here, the action must be valid
						
						// Ok. Now let's check if the previous best action was some sort of SWAP, because we may want to invalidate it (SWAPs have low priority--weird things happen otherwise)
						if(lowestScoredValidAction != null) {
							tokenIndex = lowestScoredValidAction.token.getIndex();
							if(!action.actionName.startsWith("SWAP") &&
									(
											(lowestScoredValidAction.actionName.equals(ParseConstants.SWAP_RIGHT_ACTION_NAME)
												&& !((tmpPtr = tokenToPtr[tokenIndex].next) != null &&
													(tmpPtr = tmpPtr.next) != null &&
													hasAllItsDependentsAndIsAMatch(lowestScoredValidAction.token, tmpPtr.tok, goldTokenToHead[tokenIndex], goldTokenToHead, goldArcs[tokenIndex], currentArcs[tokenIndex])
													)
											)
									   ||
									   		(lowestScoredValidAction.actionName.equals(ParseConstants.SWAP_LEFT_ACTION_NAME)
									   			&& !((tmpPtr = tokenToPtr[tokenIndex].prev) != null &&
									   				(tmpPtr = tmpPtr.prev) != null &&
									   				hasAllItsDependentsAndIsAMatch(lowestScoredValidAction.token, tmpPtr.tok, goldTokenToHead[tokenIndex], goldTokenToHead, goldArcs[tokenIndex], currentArcs[tokenIndex])
									   				)
									   		)
									)
							  ) {
							
								invalidActions.add(lowestScoredValidAction);
								// no longer consider this swap to be valid because (it is non-simple AND) we found a new, non-SWAP valid action
								lowestScoredValidActionScore = Double.POSITIVE_INFINITY;
								highestScoredValidActionScore = Double.NEGATIVE_INFINITY;
								if(lowestScoredValidAction.score > highestScoredInvalidActionScore) {
									highestScoredInvalidAction = lowestScoredValidAction;
									highestScoredInvalidActionScore = lowestScoredValidAction.score;
									lowestScoredValidAction = null;
									highestScoredValidAction = null;
								}
							}
						}
						
						// Alright, time to update the lowest/highest scored action holders
						if(action.score < lowestScoredValidActionScore || lowestScoredValidActionScore == Double.MAX_VALUE) {
							lowestScoredValidAction = action;
							lowestScoredValidActionScore = action.score;
						}
						if(action.score > highestScoredValidActionScore || Double.isInfinite(highestScoredValidActionScore)) {
							highestScoredValidAction = action;
							highestScoredValidActionScore = action.score;
						}
					}
					
				} // end for(ParseAction action : actions)
				if(mLog != null) mLog.println();
				for(TokenPointer tptr = first; tptr != null; tptr = tptr.next) {
					if(mLog != null) mLog.print(tokenToStringFeats[tptr.tok.getIndex()].size()+" ");
				}
				if(mLog != null) mLog.println();
				// Moving on to the next token
				ptr = ptr.next;
			}
			
			if(lowestScoredValidAction == null) {
				if(goldParseIsIncomplete) {
					// Simply break. We may have non-projectivity that can't be handled without a complete parse
					break;
				}
				else {
					// We should not get in here... if we do, something bad happened
					printOutErrorMessage(sentence, first, goldTokenToHead, currentArcs, model, indicesHolder, scores);
					fatalError=true;
					break;
				}
			}

			// Determine if we should perform an action or if we should update the weights
			//if(highestScoredInvalidActionScore < lowestScoredValidAction.score || numIterations > mMaxIterations) {
			if(mLog != null) mLog.println();
			if(highestScoredInvalidActionScore < highestScoredValidAction.score || numIterations > mMaxIterations) {
				// Chosen action is valid, so let's take it
				if(numIterations > mMaxIterations) {
					// check to see if we've exceeded the max updates-in-a-row threshold for this sentence
					maxIterationsReached = true;
				}
				numIterations = 0;
				// Perform the action, conceivably this could alter the pointer to the front of the linked list
				if(mLog != null) mLog.println("PERFORM: " + highestScoredValidAction.tpr.tok.getText() + " " + highestScoredValidAction.actionName + " " + highestScoredValidActionScore);
				first = NLParser.performAction(sentence, first, tokenToPtr, highestScoredValidAction, actionListStale, currentArcs,-1,featGen.getContextWidth());
				
			}
			else {
				if(mLog != null) mLog.println("UPDATE: " + lowestScoredValidAction.tpr.tok.getText() + "_" + lowestScoredValidAction.actionName + "_" + lowestScoredValidActionScore + " vs " + highestScoredInvalidAction.tpr.tok.getText()+"_"+highestScoredInvalidAction.actionName+"_"+highestScoredInvalidActionScore);
				// Chosen action is invalid
				numInvalids++;
				// So, let's update the model
				
				performUpdate(lowestScoredValidAction, highestScoredInvalidAction, first, actionListStale, tokenToStringFeats, model);
			}
			if(mLog != null) mLog.println();
			if(mLog != null) mLog.flush();
		}
		return new TrainingResult(maxIterationsReached, fatalError, numInvalids);
	}
	
	/**
	 * Update the model parameters
	 */
	private void performUpdate(ParseAction lowestScoredValidAction, 
							   ParseAction maxInvalidAction,
							   TokenPointer first,
							   boolean[] actionListStale,
							   Set[] featureStrings,
							   ParseModel w) {
		ParseAction goodAction = lowestScoredValidAction;
		ParseAction badAction = maxInvalidAction;
		
		// Everything becomes stale because the model weights are changing
		for(TokenPointer tptr = first; tptr != null; tptr = tptr.next) {
			actionListStale[tptr.tok.getIndex()] = true;
		}
		
		// Hmm... Actually, would it make more sense to find the intersection of these and take the size of that?
		double denominator = featureStrings[badAction.token.getIndex()].size()+featureStrings[goodAction.token.getIndex()].size();//featureCache[badAction.token.getIndex()].size()+featureCache[goodAction.token.getIndex()].size();
		//double denominator = featureCache[badAction.token.getIndex()].size()+featureCache[goodAction.token.getIndex()].size();
		
		// uniform penalty of 1
		double change = 1;
		// magnitude of error is typically around .0055-.0035 
		double update = Math.min(mMaxUpdate, (badAction.score-goodAction.score+change)/denominator);
		
		
		if(mLog != null) mLog.println("UPDATE_CALC:\t"+badAction.score+"\t"+goodAction.score+"\t"+change+"\t"+denominator);
		for(TokenPointer tptr = first; tptr != null; tptr = tptr.next) {
			if(mLog != null) mLog.print(featureStrings[tptr.tok.getIndex()].size()+" ");
		}
		if(mLog != null) mLog.println();
		if(mLog != null) mLog.println("UPDATE:\t"+badAction.actionName+"\t"+featureStrings[badAction.token.getIndex()].size()+"\t"+featureStrings[goodAction.token.getIndex()].size()+"\t"+update);
		if(mLog != null) mLog.flush();
		// update feature vector weights
		w.update(badAction.actionName, featureStrings[badAction.token.getIndex()], -update);
		w.update(goodAction.actionName, featureStrings[goodAction.token.getIndex()], update);
	}
	
	private boolean hasAllItsDependentsAndIsAMatch(Token tokenToMove,
												   Token newNeighbor,
												   Arc goldHeadArc, 
												   Arc[] goldTokenToHead, 
												   List<Arc> goldArcs, 
												   List<Arc> currentArcs) {
		return goldHeadArc != null &&
		goldHeadArc.getHead() == newNeighbor &&
		hasAllItsDependents(tokenToMove, goldArcs, currentArcs);
	}
	
	private boolean addGoldActionIfNotPresent(List<String> actions, ParseModel model, Token tc, Token tr, Arc[] goldTokenToHead) {
		boolean changesMade = false;
		if(goldTokenToHead != null && tr != null) {
			Arc leftHead = goldTokenToHead[tc.getIndex()];
			Arc rightHead = goldTokenToHead[tr.getIndex()];
			if(leftHead != null && leftHead.getHead() == tr && !leftHead.getDependency().equals("*")) {
				String depend = leftHead.getDependency() + "l";
				if(!actions.contains(depend)) {
					model.addAction(tc.getPos(), tr.getPos(), depend);
					changesMade = true;					
				}
			}
			else if(rightHead != null && rightHead.getHead() == tc && !rightHead.getDependency().equals("*")) {
				String depend = rightHead.getDependency() + "r";
				if(!actions.contains(depend)) {
					model.addAction(tc.getPos(), tr.getPos(), depend);
					changesMade = true;
				}
			}
		}
		return changesMade;
	}
	
	private void printOutErrorMessage(List<Token> tokens, TokenPointer first, Arc[] goldTokenToHead, List[] currentArcs, ParseModel model, int[] indicesHolder, double[] scores) {
		System.err.println("ERROR: No Valid Action Found! Do cycles or multi-headed tokens exist? Moving on to next sentence...");
		DecimalFormat format = new DecimalFormat();
		format.setMaximumFractionDigits(4);
		TokenPointer ptr = first;
		while(ptr != null && ptr.tok != null) {
			List<String> actionNames = model.getActions(ptr.tok, ptr.next == null ? null : ptr.next.tok, goldTokenToHead);
	
			final int numActions = actionNames.size();
			System.err.print(ptr.tok.getText() + " " + ptr.tok.getPos() + " ");
			/*model.scoreIntermediate(actionNames2, tokenFeatures, indicesHolder, scores);
			for(int i = 0; i < numActions; i++) {
				String actionName = actionNames2.get(i);
				System.err.print(actionName + ":"+scores[i]+", ");
			}*/
			System.err.println();
			ptr = ptr.next;	
	}
	
		int numTokens = tokens.size();
		for(int i = 0; i < numTokens; i++) {
			System.err.print((i+1) + "\t" + tokens.get(i).getText()+"\t");
			System.err.println();
			List<Arc> children = currentArcs[i+1];
			if(children != null) {
				for(Arc child : children) {
					System.err.println("\t" + child.getDependency() + "\t" + child.getChild().getIndex() + "\t" + child.getHead().getIndex());
				}
			}
		}
	}
	
}