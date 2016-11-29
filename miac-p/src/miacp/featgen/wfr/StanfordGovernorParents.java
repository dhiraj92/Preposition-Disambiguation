package miacp.featgen.wfr;
/*
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.ling.IndexedWord;
import miacp.parse.types.Parse;
import miacp.parse.types.Token;
import miacp.util.stanfordClass;

public class StanfordGovernorParents extends AbstractWordFindingRule {
	
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
			Set<IndexedWord> Govchildren = (Set<IndexedWord>) depGraph.get("govParents");
			//System.out.println("govchild"+Govchildren);
			String governer = (String) depGraph.get("gov");
			for(IndexedWord v : Govchildren){
				//System.out.println(v);
				for (int i = 0; i<tokenList.size(); i++){
					
					Token tok = tokenList.get(i);
					String s = tok.getText();
					if(s.equals(v.originalText())){
						results.add(tok);
					}

					
				}

			}
		
		}
		return results;
	}
}*/