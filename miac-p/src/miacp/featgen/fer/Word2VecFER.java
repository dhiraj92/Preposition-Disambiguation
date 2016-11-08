package miacp.featgen.fer;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

public class Word2VecFER extends AbstractFeatureRule{

	/**
	 * Extract 10 nearest words for the given word with Google News trained word2vec model
	 */
	private static final long serialVersionUID = 1L;
	Word2Vec model;
	Map<String, Collection<String>> simDict;
	
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
		// System.out.println(nearestWords);
		return new HashSet<String>(nearestWords);
	}

    @Override
    protected void finalize(){
        try{
            FileOutputStream fos = new FileOutputStream("data/similarWordsDict.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(simDict);
            System.out.printf("Serialized HashMap data is saved in data/similarWordsDict.ser");
            oos.close();
            fos.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
