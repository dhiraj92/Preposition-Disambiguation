package miacp.featgen.fer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

public class Word2VecFER extends AbstractFeatureRule{

	/**
	 * Extract 5 nearest words for the given word with Google News trained word2vec model
	 */
	private static final long serialVersionUID = 1L;
	Word2Vec model;
	
	
	@Override
	public void init(Map<String, String> params) {
		File gModel = new File("/home/shashank/workspace/Preposition-Disambiguation/miac-p/data/GoogleNews-vectors-negative300.bin.gz");
		        
		try {
			model = (Word2Vec) WordVectorSerializer.loadGoogleModel(gModel, true);			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public Set<String> getProductions(String base, String type, Set<String> feats) throws FERProductionException {
		Collection<String> nearestWords = model.wordsNearest(base, 5);
		// System.out.println(nearestWords);
		return new HashSet<String>(nearestWords);
	}

}
