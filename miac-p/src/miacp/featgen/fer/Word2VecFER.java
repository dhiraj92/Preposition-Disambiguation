package miacp.featgen.fer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

public class Word2VecFER extends AbstractFeatureRule{

	/**
	 * Extract 10 nearest words for the given word with Google News trained word2vec model
	 */
	private static final long serialVersionUID = 1L;
	Word2Vec model;
	Map<String, Collection<String>> simDict;
	static int count;

	@Override
	public void init(Map<String, String> params) {
		        
		try {
            File gModel = new File("data/GoogleNews-vectors-negative300.bin.gz");
            
            File serDictFile = new File("data/similarWordsDict.ser");
            if (serDictFile.exists() && !serDictFile.isDirectory()){
                FileInputStream fis = new FileInputStream(serDictFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                simDict = (HashMap) ois.readObject();
                ois.close();
                fis.close();
            }
            else{
                simDict = new HashMap<String, Collection<String>>();
            }
			model = (Word2Vec) WordVectorSerializer.loadGoogleModel(gModel, true);			
			count = 0;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public Set<String> getProductions(String base, String type, Set<String> feats) throws FERProductionException {
        
        Collection<String> nearestWords = simDict.get(base);
        if(nearestWords == null){
            nearestWords = model.wordsNearest(base, 10);
            simDict.put(base, nearestWords);
        }

        count++;
        if (count % 500 == 0){
            write();
        }
        feats.addAll(nearestWords);
		return feats;
	}

    private void write(){
        try{
            FileOutputStream fos = new FileOutputStream("data/similarWordsDict.ser", false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(simDict);
            oos.close();
            fos.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
