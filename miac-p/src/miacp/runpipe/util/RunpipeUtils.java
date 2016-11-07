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

package miacp.runpipe.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import miacp.runpipe.Annotation;
import miacp.runpipe.TextDocument;


public class RunpipeUtils {
	
	public static <T extends Annotation> List<T> getSublist(Annotation boundaryAnnot, List<T> list) {
		final int start = boundaryAnnot.getStart();
		final int end = boundaryAnnot.getEnd();
		
		int result = Collections.binarySearch(list, boundaryAnnot);
		result = -(result + 1);
		result = Math.max(0, result-1);
		List<T> subList = new ArrayList<T>();
		final int numAnnots = list.size();
		for(int i = 0; i < numAnnots; i++) {
			T annot = list.get(i);
			final int annotStart = annot.getStart();
			if(annotStart >= start) {
				if(annotStart >= end) {
					break;
				}
				if(annot.getEnd() <= end) {
					subList.add(annot);
				}
			}
		}
		return subList;
	}

	public static List<Annotation> getAllAnnotsInList(TextDocument doc,
			Class[] listOfTypes) {
		List<Annotation> annotations = new ArrayList<Annotation>();
		for (Class clazz : listOfTypes) {
			Set<Annotation> annots = (Set<Annotation>)doc.getAnnotationSet(clazz);
			if (annots != null) {
				annotations.addAll(annots);
			}
		}
		Collections.sort(annotations);			
		return annotations;
	}
	
}
