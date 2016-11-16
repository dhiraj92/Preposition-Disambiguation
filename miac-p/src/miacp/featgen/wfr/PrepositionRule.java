package miacp.featgen.wfr;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import miacp.parse.types.Parse;
import miacp.parse.types.Token;

public class PrepositionRule extends AbstractWordFindingRule{

	/**
	 * Kind of a hack. Ideally, should return a percentage showing relative position
	 * of the preposition from the start. To take advantage of the configurability
	 * and pipeline, currently return a string casted float
	 */
	@Override
	public Set<Token> getProductions(List<Token> tokenList, Parse parse, int tokenIndex) {
		Set<Token> results = new HashSet<Token>();
		Token token = new Token(String.valueOf(tokenIndex), tokenIndex);
		results.add(token);
		return results;
	}

}
