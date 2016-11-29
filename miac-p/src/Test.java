import java.util.ArrayList;
import java.util.HashMap;

import jigsaw.JIGSAW;
import module.graph.SentenceToGraph;
import module.graph.helper.ClassesResource;
import module.graph.helper.GraphPassingNode;

public class Test {

	public static void main(String args[]) {
		SentenceToGraph sg = new SentenceToGraph();
		GraphPassingNode gpn = sg.extractGraph("John met Sally at Starbucks.", false, true);
		ClassesResource cr = gpn.getConClassRes();
		HashMap<String, String> map = cr.getSuperclassesMap();
		System.out.println(map.get("at-4"));
		
		JIGSAW js = new JIGSAW();
		HashMap<String, ArrayList<String>> arrList = js.getWordSenses("John met Sally", 0);
		System.out.println(arrList);
	}

}
