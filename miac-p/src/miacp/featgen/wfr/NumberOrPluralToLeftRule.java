package miacp.featgen.wfr;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import miacp.parse.types.Parse;
import miacp.parse.types.Token;
import miacp.util.TreebankConstants;

/**
 * Simple class to represent if there is a number present
 * anywhere in the left of the sentence. T is a sentinal value
 * standing for True (present)
 * @author shashank
 *
 */

public class NumberOrPluralToLeftRule extends AbstractWordFindingRule{
	
	@Override
	public Set<Token> getProductions(List<Token> tokenList, Parse parse, int headIndex) {
		Set<Token> results = new HashSet<Token>();
		for(int i = headIndex-1; i >= 0; i--) {
            Token tok = tokenList.get(i);
			if(TreebankConstants.CARDINAL_NUMBER.equals(tok.getPos()) || TreebankConstants.NOUN_PLURAL.equals(tok.getPos())) {
                results.add(new Token(tok.getPos().toString(), tok.getIndex()));
                // System.out.println("found Plural / Number : " + tok.getText());
            }
        }
		return results;
	}

}
