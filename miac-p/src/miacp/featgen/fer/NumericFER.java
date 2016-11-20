package miacp.featgen.fer;

import java.util.HashSet;
import java.util.Set;

/**
 * This is a dummy rule for numeric features.
 * Numeric features are WordExtractionRules that return numbers (in String form).
 * This feature just returns the same number in string form
 * @author shashank
 *
 */
public class NumericFER extends AbstractFeatureRule {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Set<String> getProductions(String base, String type, Set<String> feats) throws FERProductionException {
		HashSet<String> token = new HashSet<String>();
		token.add(base);
		return token; 
	}

}
