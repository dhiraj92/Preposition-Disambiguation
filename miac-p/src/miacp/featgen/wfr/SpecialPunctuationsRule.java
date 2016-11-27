package miacp.featgen.wfr;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import miacp.parse.types.Parse;
import miacp.parse.types.Token;

/**
 * Returns if the sentence had one of the punctuations from {,;!?}
 * This is purely on intuition and we want to test if it adds value
 * @author shashank
 *
 */
public class SpecialPunctuationsRule extends AbstractWordFindingRule{

	private static final long serialVersionUID = 1L;

	@Override
	public Set<Token> getProductions(List<Token> tokenList, Parse parse, int headIndex) {
		Set<String> punctuations = new HashSet<String>();
		punctuations.add(",");
		punctuations.add(";");
		punctuations.add("?");
		punctuations.add("!");
		Set<Token> results = new HashSet<Token>();
		for(Token tok : tokenList) {
            if(punctuations.contains(tok.getText())) {
                results.add(tok);
            }
        }
		return results;
	}	
}
