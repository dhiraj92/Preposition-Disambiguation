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

package miacp.pos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Crude script for combining Penn Treebank .pos files
 */
public class PosToConll {
	
	public static void main(String[] args) throws Exception {
		String posFilesDir = "Treebank_3/tagged/pos";
		String combinedFile = "combinedPosFile";
		
		List<File> files = new ArrayList<File>();
		collectFiles(files, new File(posFilesDir));
		
		PrintWriter writer = new PrintWriter(new FileWriter(combinedFile));
		Collections.sort(files);
		
		for(File f : files) {
			System.err.println(f.getName());
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String line = null;
			int tokenNum = 0;
			String prevToken = "PREVIOUS_TOKEN";
			while((line = reader.readLine()) != null) {
				if(line.startsWith("*x*")) continue;
				
				line = line.trim();
				if(!line.equals("")) {
					//line = line.replaceAll("\\[\\]", "");
					String[] tokens = line.split("\\s+");
					for(int i = 0; i < tokens.length; i++) {
						String token = tokens[i];
						int slashIndex = -1;
						if((slashIndex = token.indexOf("/")) > -1) {
							String text = token.substring(0, slashIndex);
							String pos = token.substring(slashIndex+1, token.length());
							if(!(prevToken.equals(text) && prevToken.matches("\\!|\\?|;|\\.|:|,"))) {
								tokenNum++;
								writer.println(tokenNum+"\t"+text+"\t_\t_\t"+pos+"\t_\t0\tROOT");
								prevToken = text;
							}
						}
						
					}
				}
				if(line.equals("==================================") && tokenNum > 0) {
					tokenNum = 0;
					writer.println();
				}
			}
			reader.close();
		}
		writer.close();
	}
	
	private static void collectFiles(List<File> files, File dir) {
		for(File f : dir.listFiles()) {
			if(f.isDirectory()) {
				collectFiles(files, f);
			}
			else {
				if(f.getName().endsWith(".pos")) {
					files.add(f);
				}
			}
		}
	}
	
}