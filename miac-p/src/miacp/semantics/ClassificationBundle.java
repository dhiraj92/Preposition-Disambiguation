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

package miacp.semantics;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import miacp.featgen.MultiStepFeatureGenerator;
import miacp.ml.ClassScoreTuple;
import miacp.ml.LinearClassificationModel;
import miacp.parse.types.Parse;
import miacp.parse.types.Token;


public class ClassificationBundle implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected MultiStepFeatureGenerator mFeatGen;
	protected Map<String, LinearClassificationModel> mModelMap;
	protected LinearClassificationModel mSingleModel;
	protected ModelNameLookup mModelNameLookup;
	
	public ClassificationBundle(MultiStepFeatureGenerator featGen, 
								Map<String, LinearClassificationModel> models,
								ModelNameLookup modelNameLookup) {
		mFeatGen = featGen;
		mModelMap = models;
		mModelNameLookup = modelNameLookup;
	}
	
	public ClassificationBundle(MultiStepFeatureGenerator featGen, LinearClassificationModel model) {
		mFeatGen = featGen;
		mSingleModel = model;
	}
	
	public ClassScoreTuple[] getPredictions(String arcType, List<Token> tokens, Parse parse, int wordIndex) {
		ClassScoreTuple[] retValue = null;
		Set<String> features = mFeatGen.generateFeatures(tokens, parse, wordIndex);
		if(mSingleModel != null) {
			retValue = mSingleModel.getDecision(features);
		}
		else {
			String modelName = mModelNameLookup.determineModelName(arcType, tokens, parse, wordIndex);
			if(modelName != null) {
				LinearClassificationModel model = mModelMap.get(modelName);
				if(model != null) {
					retValue = model.getDecision(features);
				}
			}
		}
		return retValue;
	}
	
}
