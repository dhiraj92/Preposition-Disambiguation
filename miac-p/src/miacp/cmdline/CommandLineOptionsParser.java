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

package miacp.cmdline;

import java.util.List;

import miacp.cmdline.CommandLineOptions.Option;


public class CommandLineOptionsParser {
	
	public ParsedCommandLine parseOptions(CommandLineOptions options, String[] args) throws CommandLineOptionsParsingException {
		ParsedCommandLine commandLine = new ParsedCommandLine();
		
		List<Option> optionsList = options.getOptions();
		
		for(int i = 0; i < args.length; i+=2) {
			String argumentIdentifier = args[i];
			if(!argumentIdentifier.startsWith("-") || argumentIdentifier.length() <= 1) {
				throw new CommandLineOptionsParsingException("unexpected argument name: " + argumentIdentifier);
			}
			else {
				String argIdName = argumentIdentifier.substring(1);
				boolean matched = false;
				for(Option opt : optionsList) {
					if(opt.getName().equals(argIdName)) {
						matched = true;
						commandLine.setArgumentValue(argIdName, args[i+1]);
					}
				}
				if(!matched) {
					throw new CommandLineOptionsParsingException("unexpected argument name: " + argumentIdentifier + "\n" + options.getArgumentsDescriptionString());
				}
			}
		}
		
		return commandLine;
	}
	
}
