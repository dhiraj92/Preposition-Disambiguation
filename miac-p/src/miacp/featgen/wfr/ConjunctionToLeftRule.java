package miacp.featgen.wfr;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import miacp.parse.types.Parse;
import miacp.parse.types.Token;
import miacp.util.TreebankConstants;

/**
 * Presence of conjunctions to the left could be a good feature by intuition,
 * indicating whether there is a combination of two or more things.
 * @author dhiraj
 */
public class ConjunctionToLeftRule extends AbstractWordFindingRule{

	@Override
	public Set<Token> getProductions(List<Token> tokenList, Parse parse, int headIndex) {

		Set<Token> results = new HashSet<Token>();

        for(int i = headIndex-1; i >= 0; i--) {
            Token tok = tokenList.get(i);
            String pos = tok.getPos();
            if(TreebankConstants.COORDINATING_CONJUCTION.equals(pos)) {
                results.add(tok);
                // System.out.println("found conjunction:" + tok.getText());
                break;
            }
        }
        
        return results;
	}	

}