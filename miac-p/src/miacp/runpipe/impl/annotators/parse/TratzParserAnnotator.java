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

package miacp.runpipe.impl.annotators.parse;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import miacp.jwni.WordNet;
import miacp.parse.NLParser;
import miacp.parse.featgen.ParseFeatureGenerator;
import miacp.parse.ml.ParseModel;
import miacp.parse.transform.VchTransformer;
import miacp.parse.types.Arc;
import miacp.parse.util.NLParserUtils;
import miacp.runpipe.Annotator;
import miacp.runpipe.InitializationException;
import miacp.runpipe.ProcessException;
import miacp.runpipe.TextDocument;
import miacp.runpipe.annotations.Sentence;
import miacp.runpipe.annotations.Token;
import miacp.runpipe.util.RunpipeUtils;




public class TratzParserAnnotator implements Annotator {
	
	public final static String PARAM_MODEL_FILE = "ModelFile";
	public final static String PARAM_WORDNET_LOCATION = "WordNetDir";
	public final static String PARAM_ENABLE_VCH_TRANSFORM = "VchTransform";
	
	private NLParser mNlParser;
	private boolean mVchTransform = false;
	
	public void initialize(Map<String, String> args) throws InitializationException {
		try {
			System.err.print("Loading model...");
			long startTime = System.currentTimeMillis();
			String modelFile = args.get(PARAM_MODEL_FILE);
			InputStream is = new BufferedInputStream(new FileInputStream(modelFile));
			if(modelFile.endsWith(".gz")) {
				is = new GZIPInputStream(is);
			}
			ObjectInputStream ois = new ObjectInputStream(is);
			ParseModel model = (ParseModel)ois.readObject();
			ParseFeatureGenerator featGen = (ParseFeatureGenerator)ois.readObject();
			ois.close();
			System.err.println("loaded");
			System.err.println("Model loading took: " + (System.currentTimeMillis()-startTime)/1000.0 + " seconds.");
			
			mNlParser = new NLParser(model, featGen);
			String wnLocation = args.get(PARAM_WORDNET_LOCATION);
			if(wnLocation != null) {
				new WordNet(new File(wnLocation));
			}
			String enableVchTransformParam = args.get(PARAM_ENABLE_VCH_TRANSFORM);
			if(enableVchTransformParam != null) {
				mVchTransform = Boolean.parseBoolean(enableVchTransformParam);
			}
		}
		catch(ClassNotFoundException cnfe) {
			throw new InitializationException(cnfe);
		}
		catch(IOException ioe) {
			throw new InitializationException(ioe);
		}
	}
	
	public void process(TextDocument doc) throws ProcessException {
		List<Sentence> sentences = (List<Sentence>)doc.getAnnotationList(Sentence.class);
		List<Token> tokens = (List<Token>)doc.getAnnotationList(Token.class);
		try {
			if(sentences != null && tokens != null) {
				VchTransformer vchTransform = new VchTransformer();
				for(Sentence sentence : sentences) {
					
					List<Token> sentenceTokens = RunpipeUtils.getSublist(sentence, tokens);
					
					List<miacp.parse.types.Token> parserTokens = new ArrayList<miacp.parse.types.Token>();
					
					int numTokens = sentenceTokens.size();
					for(int i = 0; i < numTokens; i++) {
						Token tok = sentenceTokens.get(i);
						miacp.parse.types.Token parseToken = new miacp.parse.types.Token(tok.getAnnotText(), i+1);
						parseToken.setPos(tok.getPos());
						parserTokens.add(parseToken);
					}
					
					// TODO: Remove at some point
					for(miacp.parse.types.Token t : parserTokens) {
						t.setLemma(NLParserUtils.getLemma(t, WordNet.getInstance()));
					}
					
					miacp.parse.types.Sentence inputSentence = new miacp.parse.types.Sentence(parserTokens);
					miacp.parse.types.Parse parse = mNlParser.parseSentence(inputSentence);
					if(parse != null) {
					if(mVchTransform) {
						vchTransform.performTransformation(parse);
					}
					for(miacp.parse.types.Token parseToken : parserTokens) {
						List<Arc> toChildren =  parse.getDependentArcLists()[parseToken.getIndex()];
						Token parent = sentenceTokens.get(parseToken.getIndex()-1);
						if(toChildren != null) {
							for(Arc childArc : toChildren) {
								Token child = sentenceTokens.get(childArc.getChild().getIndex()-1);
								Token.Arc depArc = new Token.Arc(parent, child, childArc.getDependency());
								child.setParentArc(depArc);
								parent.addDependent(depArc);
							}
						}
					}
					}
				}
			}
		}
		catch(Exception e) {
			throw new ProcessException(e);
		}
	}
	
}