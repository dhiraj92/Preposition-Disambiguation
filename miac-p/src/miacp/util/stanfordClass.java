package miacp.util;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.simple.*;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.TypedDependency;

public class stanfordClass {


	 public static void main(String[] args) { // TODO Auto-generated method
	 System.out.println("hello"); 
	 int headIndex = 7; 
	 String sentence =	 "He led in amalgamating the Royal Clyde with the Royal Northern . "; 
	 HashMap m = getDepGraph(sentence,8,"with","with");
	 }
	 

	public static HashMap getDepGraph(String sentence, int headIndex, String prepName, String prep) {
		HashMap depObj = new HashMap();

		Sentence sent = new Sentence(sentence);
		// System.out.println(sent.word(headIndex));
		String DirectoryPath = "data/stanfordDep";
		File theDir = new File(DirectoryPath);
		if (!theDir.exists())
			theDir.mkdir();
		String filePath = "data/stanfordDep/" + prepName + ".ser";
		SemanticGraph graph = null;
		File save = new File(filePath);
		if (!save.exists()) {
			// System.out.println("Saving SemanticGraph");
			try {
				graph = sent.dependencyGraph();
				FileOutputStream fileOut = new FileOutputStream(filePath);
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				out.writeObject(graph);
				out.close();
				fileOut.close();
				// System.out.println("Serialized data is saved in" + filePath);
			} catch (IOException i) {
				i.printStackTrace();
				// return null;
			}

		}

		else {
			// System.out.println("Reading SemanticGraph");
			try {
				FileInputStream fileIn = new FileInputStream(filePath);
				ObjectInputStream in = new ObjectInputStream(fileIn);
				graph = (SemanticGraph) in.readObject();
				in.close();
				fileIn.close();
			} catch (IOException i) {
				i.printStackTrace();
				return null;
			} catch (ClassNotFoundException c) {
				System.out.println("SemanticGraph class not found");
				c.printStackTrace();
				return null;
			}
		}

		// System.out.println(graph.toList());
		Collection<TypedDependency> sentDepCollection = graph.typedDependencies();

		// System.out.println(graph.typedDependencies());
		// IndexedWord pp = graph.getNodeByIndex(headIndex);
		// System.out.println(graph.relns(pp));
		// graph.outgoingEdgeList(pp);
		// System.out.println(graph.parentPairs(pp));
		// System.out.println(graph.vertexListSorted());
		IndexedWord governer = null;
		IndexedWord depedent = null;
		Set<IndexedWord> Govchildren = null;
		Set<IndexedWord> Govparents = null;
		for (TypedDependency dep : sentDepCollection) {
			//System.out.println(dep);
			// && dep.reln().getSpecific().equals("at")
			if (dep != null && dep.reln() != null && dep.reln().getLongName() != null) {
				if (dep.reln().getLongName().equals("nmod_preposition") && dep.reln().getSpecific().equals(prep)) {
					governer = dep.gov();
					depedent = dep.dep();
					//System.out.println(graph.relns(governer));
					Govchildren = graph.getChildren(governer);
					Govparents = graph.getParents(governer);
/*					for(IndexedWord v : Govchildren){
						System.out.println(v.originalText());
						System.out.println(v.index());
						
					}*/
					break;

				}
			}
		}
		// System.out.println(governer.originalText());
		// System.out.println(depedent.originalText());
		if (governer == null) {
			// System.out.println("didn't get a gov");
			return null;
		} else {
			depObj.put("gov", governer.originalText());
			depObj.put("obj", depedent.originalText());
			depObj.put("govParents", Govparents);
			depObj.put("govChildren", Govchildren);

			return depObj;

		}

	}

}
