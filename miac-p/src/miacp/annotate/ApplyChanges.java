/*
 * Copyright 2012 Stephen Tratz
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

package miacp.annotate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import miacp.parse.io.ConllxSentenceReader;
import miacp.parse.io.DefaultSentenceWriter;
import miacp.parse.types.Arc;
import miacp.parse.types.Parse;
import miacp.parse.types.Token;


/**
 * A simple script for applying the changes identified when using the <code>AnnotationComparisonGui</code>
 *
 */
public class ApplyChanges {
	
	public static void main(String[] args) throws Exception {
		File sentencesFileOriginal = new File(args[0]);
		File changes = new File(args[1]);
		File newSentencesFile = new File(args[2]);
		
		List<Parse> parses = readParses(sentencesFileOriginal);
		applyChanges(changes, parses);
		writeParses(parses, newSentencesFile);
	}
	
	public static void writeParses(List<Parse> parses, File newSentencesFile) throws Exception {
		DefaultSentenceWriter swriter = new DefaultSentenceWriter();
		Map<String, String> args = new HashMap<String, String>();
		args.put(swriter.PARAM_OUTPUT_FILE, newSentencesFile.getAbsolutePath());
		args.put(swriter.PARAM_SKIP_SEMATICS, "true");
		swriter.initialize(args);
		for(Parse parse : parses) {
			swriter.appendSentence(parse.getSentence(), parse);
		}
		swriter.flush();
		swriter.close();
	}
	
	public static List<Parse> readParses(File sentencesFileOriginal) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(sentencesFileOriginal));
		ConllxSentenceReader sreader = new ConllxSentenceReader();
		List<Parse> parses = new ArrayList<Parse>();
		Parse parse = null;
		while((parse = sreader.readSentence(reader)) != null) {
			parses.add(parse);
		}
		reader.close();
		return parses;
	}
	
	public static void applyChanges(File changesFile, List<Parse> parses) throws Exception {
		int numSkips = 0;
		BufferedReader changesReader = new BufferedReader(new FileReader(changesFile));
		String line = null;
		while((line = changesReader.readLine()) != null) {
			if(line.startsWith("SKIP")) {
				numSkips++;
			}
			else {
				String[] parts = line.split("\\t+");
				int snum = Integer.parseInt(parts[0]);
				int childIndex = Integer.parseInt(parts[1]);
				int headIndex = Integer.parseInt(parts[2]);
				Parse parse = parses.get(snum);

				
				Arc[] headArcs = parse.getHeadArcs();
				List<Token> tokens = parse.getSentence().getTokens();
				System.err.println(tokens.size() + " " + childIndex + " " + headIndex);
				Token childToken = tokens.get(childIndex-1);
				Token headToken = headIndex == 0 ? parse.getRoot() : tokens.get(headIndex-1);
				Arc newArc = new Arc(childToken, headToken, "*");
				headArcs[childIndex] = newArc;
				// technically, one should remove any replaced arc...
			}
		}
		changesReader.close();
	}
	
}