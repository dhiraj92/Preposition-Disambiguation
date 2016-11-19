package miacp.featgen.wfr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import miacp.parse.types.Arc;
import miacp.parse.types.Parse;
import miacp.parse.types.Token;
import miacp.parse.util.ParseConstants;
import miacp.util.stanfordClass;

public class StanfordGoverner extends AbstractWordFindingRule {
	
	private static final long serialVersionUID = 1L;

	@Override
	public Set<Token> getProductions(List<Token> tokenList, Parse parse, int headIndex) {
		Set<Token> results = new HashSet<Token>();
		String sentence = "";
		for (int i = 0; i<tokenList.size(); i++){
			Token tok = tokenList.get(i);
			String s = tok.getText();
			sentence += s + " ";
			
		}

		//System.out.println(sentence);
		stanfordClass stanford = new stanfordClass();
		String Filename = sentence.substring(0,15);
		Filename = Filename.replaceAll("[^a-zA-Z0-9.-]", "_");
		HashMap depGraph = stanford.getDepGraph(sentence, headIndex, Filename,tokenList.get(headIndex).getText());
		if(depGraph != null){
			String governer = (String) depGraph.get("gov");
			//System.out.println(governer);
			
			for(int i = headIndex-1; i >= 0; i--) {
				Token tok = tokenList.get(i);
				String pos = tok.getPos();
				//System.out.println(tokenList);
				//handle null return case
				String t = tok.getText();
				if(t.equals(governer)) {
					
					results.add(tok);
					break;
				}
			}		
		
		}
		return results;
	}
	
}

