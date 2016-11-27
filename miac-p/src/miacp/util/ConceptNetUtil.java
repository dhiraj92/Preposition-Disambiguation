package miacp.util;

import java.util.Collection;
import java.util.HashSet;

public class ConceptNetUtil {
	
	public static Collection<String> getRelationWords(String id, String relation, String word){
		HashSet<String> words = new HashSet<String>();
		int start = id.indexOf("[");
		int end = id.indexOf("]");
		String listString = id.substring(start + 1, end - 1);
		String[] tokens = listString.split(",");
		for(String token: tokens){
			if(token.startsWith("/r/")){
				if(!token.contains(relation)){
					words.clear();
					break;
				}
			}else if(token.startsWith("/c/en/")){
				// System.out.println(token);
				String[] allTokens = token.split("/");
				String tokenWord = allTokens[3];
				if(!tokenWord.equals(word)){
					words.add(tokenWord);
				}
			}
		}
		//if(words.size() > 0)
			//System.out.println(word + " " + relation + words);
		
		return words;
	}
}
