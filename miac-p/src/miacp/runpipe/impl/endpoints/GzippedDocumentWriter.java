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

package miacp.runpipe.impl.endpoints;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import miacp.runpipe.EndPoint;
import miacp.runpipe.InitializationException;
import miacp.runpipe.ProcessException;
import miacp.runpipe.TextDocument;


public class GzippedDocumentWriter implements EndPoint {
	
	public final static String PARAM_OUTPUT_DIR = "OutputDir";
	
	private File mOutputDir;
	
	public void initialize(Map<String, String> params) throws InitializationException {
		mOutputDir = new File(params.get(PARAM_OUTPUT_DIR));
		mOutputDir.mkdirs();
	}
	
	public void process(TextDocument doc) throws ProcessException {
		String uri = doc.getUri();
		String docFilename = uri.substring(uri.lastIndexOf(File.separatorChar)+1);
		String outputFilename = docFilename + ".gz";
		try {
			File outputFile = new File(mOutputDir, outputFilename);
			ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(outputFile)));
			oos.writeObject(doc);
			oos.close();
		}
		catch(IOException ioe) {
			throw new ProcessException(ioe);
		}
	}
	
	public void batchFinished() {
		// Nothing to do
	}
	
}
