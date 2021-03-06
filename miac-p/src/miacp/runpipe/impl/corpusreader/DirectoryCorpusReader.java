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

package miacp.runpipe.impl.corpusreader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import miacp.runpipe.CorpusReader;
import miacp.runpipe.DocumentReadException;
import miacp.runpipe.TextDocument;
import miacp.runpipe.TextDocumentReader;
import miacp.runpipe.impl.TextDocumentImpl;


/**
 * Reads in all files under a given directory. 
 * Optionally (default: true) includes subdirectories.
 * Optionally (default: none) can filter filenames against a regular expression.
 */
public class DirectoryCorpusReader implements CorpusReader {
	
	public final static String PARAM_INPUT_DIR = "InputDirectories";
	public final static String PARAM_RECURSIVE = "IncludeSubdirs";
	public final static String PARAM_FILTER_PATTERN = "FilterPattern";
	
	protected List<File> mFiles = new ArrayList<File>();
	protected int mCurrentFileIndex;
	protected TextDocumentReader mDocumentReader;
	
	@Override
	public void initialize(Map<String, String> params) {
		String inputDirectoriesString = params.get(PARAM_INPUT_DIR);
		String[] inputDirectoriesArray = inputDirectoriesString.split(File.pathSeparator);
		
		String includeSubdirsString = params.get(PARAM_RECURSIVE);
		boolean includeSubdirs = includeSubdirsString == null ? Boolean.TRUE : Boolean.parseBoolean(includeSubdirsString);
		
		String filterPattern = params.get(PARAM_FILTER_PATTERN);
		Matcher fileNameMatcher = filterPattern == null ? null : Pattern.compile(filterPattern).matcher("");
		for(String inputDirectory : inputDirectoriesArray) {
			System.out.println(inputDirectory);
			addFiles(new File(inputDirectory), mFiles, includeSubdirs, fileNameMatcher);
		}
		Collections.sort(mFiles);
		Collections.reverse(mFiles);
	}
	
	private static void addFiles(File directory, List<File> fileList, boolean includeSubdirs, Matcher filter) {
		File[] files = directory.listFiles();
		
		//System.out.println("total file" + files.length);
		for(File file : files) {
				//System.out.println("in add files" + file.getAbsolutePath());
			
				if(file.isDirectory()) {
					if(includeSubdirs) {
						addFiles(file, fileList, includeSubdirs, filter);
					}
				}
				else {
					if(filter == null || filter.reset(file.getName()).matches()) {
						fileList.add(file);
					}
				}
			
		}
	}
	
	@Override
	public TextDocumentReader getDocumentReader() {
		return mDocumentReader;
	}
	
	@Override
	public void setDocumentReader(TextDocumentReader reader) {
		mDocumentReader = reader;
	}
	
	@Override
	public boolean hasNext() {
		return mCurrentFileIndex < mFiles.size();
	}
	
	@Override
	public TextDocument getNext() throws DocumentReadException, IOException {
		TextDocument doc = new TextDocumentImpl();
		File file = mFiles.get(mCurrentFileIndex++);
		System.err.println("Reading file " + mCurrentFileIndex + " of " + mFiles.size() + ": " + file.getName());
		doc.setUri(file.getAbsolutePath());
		InputStream is = new FileInputStream(file);
		mDocumentReader.hydrateDocument(is, doc);
		is.close();
		return doc;
	}
	
	
}
