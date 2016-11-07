/* Note: This message is to inform you that this code was modified by Stephen Tratz in early 2012 and that therefore this code will
 * be somewhat different from that made available at the Information Sciences Institute's website (unless similar changes are made there).
 * This message is here to comply with the terms of the Apache license ("You must cause any modified files to carry prominent notices stating that You changed the files").
 */

/*
 * Copyright 2011 University of Southern California 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 *      
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package miacp.parse.featgen;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import miacp.featgen.InitException;
import miacp.parse.ml.ParseModel;
import miacp.parse.types.Arc;
import miacp.parse.types.Token;
import miacp.parse.types.TokenPointer;
import miacp.parse.util.ParseConstants;
import miacp.types.ChecksumMap;


/**
 * Feature generation class for parsing. 
 * A bit on the ugly side (understatement).
 * It seems as though the order in which the features are added to the Set 
 * may affect training, which is strange... that would only make sense if either 
 * 1) there is a bug or 2) there is some sort of numerical issue creeping up
 * 
 * Chinese and other foreign characters used to make for smaller feature strings
 */
public class DefaultEnParseFeatureGenerator implements Serializable, ParseFeatureGenerator {
	
	public final static long serialVersionUID = 1;
	
	private final static String DUMMY_PART_OF_SPEECH = "na";
	transient TokenPointer mDummy = new TokenPointer(new Token(null, DUMMY_PART_OF_SPEECH, 0), null, null);
	
	
	public DefaultEnParseFeatureGenerator() throws Exception {
		loadMap();
	}
	
	// load the Brown clusters (not very good to have this hard-coded like this)
	private void loadMap() throws Exception {
		mFeatMap2 = new ChecksumMap<String>();
		int minOccurrence = 1;
		int maxDepth = Integer.MAX_VALUE;
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("data/brownClusters175.gz"))));
			String line = null;
			mBCs = new ArrayList<String>();
			String lastClass = null;
			while((line = reader.readLine()) != null) {
				String[] split = line.split("\\t+");
				String path = split[0];
				String token = split[1];
				int occurrences = Integer.parseInt(split[2]);
				if(occurrences >= minOccurrence) {
					String clazz = Integer.toHexString(Integer.parseInt(path.substring(0, Math.min(path.length(), maxDepth)),2));
					if(!clazz.equals(lastClass)) {
						mBCs.add(clazz);
						lastClass = clazz;
					}
					mFeatMap2.put(token, mBCs.size()-1);
				}
			}
			reader.close();
		}
		catch(IOException ioe) {
			// TODO: This should not be an FERInitException...
			throw new InitException(ioe);
		}
	}
	
	private String getBC(String s) {
		if(mFeatMap2 == null) {
			try {
				// "Strange... didn't save the BC map... problem seems fixed now,
				// must have been a serialization issue. Can probably take this out now
				loadMap();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		String retValue;
		if(s==null) {
			retValue = "-2";
		}
		else {
			String numericized = s.replaceAll("[0-9]", "ƞ");
			if(s.equals("an")) {
				s = "a";
			}
			if(!mFeatMap2.containsKey(numericized)) {
				String lower = numericized.toLowerCase();
				if(!mFeatMap2.containsKey(lower)) retValue = "-1";
				else retValue = mBCs.get(mFeatMap2.get(lower));
			}
			else retValue = mBCs.get(mFeatMap2.get(numericized));
		}
		return retValue;
	}
	
	private List<String> mBCs;
	private ChecksumMap<String> mFeatMap2 = new ChecksumMap<String>();
	
	private String getTag(TokenPointer ptr) {
		return getTag(ptr.tok);
	}
	
	private String getTag(Token tok) {
		String tag = tok.getPos();
		// WDTs as well?
		if(tag.equals("IN")||tag.equals("WRB")||tag.equals("WP")) tag = tag+getLemma(tok);
		return tag;
	}
	
	private String getLemma(TokenPointer ptr) {
		return getLemma(ptr.tok);
	}
	
	private String getLemma(Token tok) {
		String text = tok.getLemma();
		if(text == null) {
			text = tok.getText();
			if(text != null) {
				System.err.println("No lemma for: " + text + " " + tok.getPos());
			}
		}
		if("\"".equals(text)) {
			text = tok.getPos();
		}
		return	text == null ? text : text.toLowerCase();
	}
	
	private String getForm(Token tok) {
		String text = tok.getText();
		if("\"".equals(text)) {
			text = tok.getPos();
		}
		return	text == null ? text : text.toLowerCase();
	}
	
	private int length(TokenPointer ptr, Token[] kids) {
		int leftside = (kids[0] == mDummy.tok) ? ptr.tok.getIndex() : kids[0].getIndex();
		int rightside = (kids[1] == mDummy.tok) ? ptr.tok.getIndex() : kids[1].getIndex();
		return rightside-leftside;
	}
	
	private String getCpos(Token t) {
		String pos = t.getPos();
		return (pos.length() > 2 && !pos.endsWith("$")) ? pos.substring(0,2) : pos;
	}
	
	private final static int L3=0,L2=1,L1=2,L0=3,R0=4,R1=5,R2=6,R3=7;
	private final static int NTOK = 8;
	private final static Set<String> inbetweenWordsOfInterest = new HashSet<String>(Arrays.asList(new String[]{",","``",";",":","--","(",")"}));
	
	private final int[] lseq = new int[]{L1,L1,L0,R0,L0},
						rseq = new int[]{R0,L0,R0,R1,R1};
	
	private final String[]	tt = new String[]{"⼿","⽀","⽁","⽂","⽃"},
							ww = new String[]{"⽄","⽕","⽖","⽗","⽘"},
							wt = new String[]{"⽙","⽚","⽛","⽜","⽭"},
							tw = new String[]{"⽮","⽯","⽰","⽱","⽲"};
	private final String[]  ttlclc = new String[]{"⽳","⽴","⾅","⾆","⾇"},
							ttlcrc = new String[]{"⾈","⾉","⾊","⾋","⾌"},
							ttrclc = new String[]{"⾝","⾞","⾟","⾠","⾡"},
							ttrcrc = new String[]{"⾢","⾣","⾤","⾵","⾶"};
	// unigram
	private final String[] uniFrmPrefixes	= new String[]{"*","⻯","⻰","⼍","⼎","⼏","⼐","~"}, // Form indicators
						   uniCposPrefixes 	= new String[]{"*","⼩","⼪","⼫","⼬","⼽","⼾","~"}, // Coarse POS indicators
						   uniNoChildPrefix = new String[]{"*","⺴","⺵","⺶","⺷","⺸","⺹","~"}, // No_child indicators
						   uniTagPrefixes 	= new String[]{"*","⼑","⼒","⼓","⼔","⼥","⼦","~"}, // Tag indicators
						   uniLengthPrefix  = new String[]{"*","⺺","⺻","⺼","⺽","⺾","⺿","~"};

	private final String[] deprelSymbols = new String[]{"*","㋟","⻪","⻫","⻬","⻭","㋠","~"};
	
	private final String[] deltaP = new String[]{"*","⻀","⻑","⻒","⻓","⻔","~"};
	private final String[] dt = new String[]{"*","⻕","⻖","⻗","⻘","⻩","~"};
	
	//Unused symbols 㑇鬻鬴顁⿋魙鬮騛騜騝騞騟
	
	public class PreCalculations {
		// Word forms, tags, pos, cpos
		final String[] frm = new String[NTOK], tag = new String[NTOK];
		final String[] pos = new String[NTOK], cpos = new String[NTOK];
			
		final Token[][] kids = new Token[NTOK][];
		// Child pos
		final String[] lcpos = new String[NTOK], rcpos = new String[NTOK];
		// Child forms
		final String[] rcfrm = new String[NTOK];
		final String[] rccpos = new String[NTOK];
			
		final String[] bc = new String[NTOK];
		final String[] bcrc = new String[NTOK];
	 }
	 
	public void genFeats(Set<String> fts, ParseModel model, List<Token> tokens, TokenPointer ptr, List[] currentArcs) {
		if(mDummy == null) {
			mDummy = new TokenPointer(new Token(null, DUMMY_PART_OF_SPEECH, 0), null, null);
		}
		
		PreCalculations pc = new PreCalculations();
		
		final TokenPointer[] tp = new TokenPointer[NTOK];
		tp[L0] = ptr;
		tp[L1] = (tp[L0].prev != null) ? tp[L0].prev : mDummy; 
		tp[L2] = (tp[L1].prev != null) ? tp[L1].prev : mDummy;
		tp[L3] = (tp[L2].prev != null) ? tp[L2].prev : mDummy;
		
		tp[R0] = (ptr.next != null) ? ptr.next : mDummy;
		tp[R1] = (tp[R0].next != null) ? tp[R0].next : mDummy;
		tp[R2] = (tp[R1].next != null) ? tp[R1].next : mDummy;
		tp[R3] = (tp[R2].next != null) ? tp[R2].next : mDummy;
		
		// Fill out arrays and features that are created for all tokens
		for(int i = 0; i < NTOK; i++) {
			pc.frm[i] = getLemma(tp[i]);
			pc.tag[i] = getTag(tp[i]);
			pc.pos[i] = tp[i].tok.getPos();
			pc.cpos[i] = getCpos(tp[i].tok);
			pc.kids[i] = getLRchildren(tp[i].tok, currentArcs); 
			pc.lcpos[i] = pc.kids[i][0].getPos();
			pc.rcpos[i] = pc.kids[i][1].getPos();
			pc.rcfrm[i] = getLemma(pc.kids[i][1]);
			pc.rccpos[i] = getCpos(pc.kids[i][1]);
			pc.bc[i] = getBC(getForm(tp[i].tok));
			pc.bcrc[i] = getBC(getForm(pc.kids[i][1]));
		}
		
		for(int i = L2; i <= R2; i++) {
			String posi = pc.pos[i];
			// noun vs verb pos tag error possibility indicator
			if(posi.equals("NNS") || posi.equals("VBZ")) fts.add("⼩nz"+(i-1));
			if(posi.equals("NN") || posi.equals("VB")) fts.add("⼩nv"+(i-1));
			
			// vbd vs vbn POS tag error possibility indicator
			if(posi.equals("VBN") || posi.equals("VBD")) fts.add("⼑1"+(i-1)); else fts.add("⼑2"+(i-1));
			
			if(posi.equals("DT")) {
				// is article indicator
				if(pc.frm[i].equals("the") || pc.frm[i].equals("a") || pc.frm[i].equals("an")) {
					fts.add("⼓1"+(i-1)); 
				}
				else {
					fts.add("⼓2"+(i-1));
				}
			}
			
			// Superlative indicator
			if(posi.equals("RBS") || posi.equals("JJS")) fts.add((i-1)+"⼩s");
			// Comparative indicator
			if(posi.equals("RBR") || posi.equals("JJR")) fts.add((i-1)+"⼩c");
			// Possessive pronoun indicator
			if(posi.equals("WP$") || posi.equals("PRP$")) fts.add((i-1)+"⼩p");
			
			
			// Features
			fts.add(pc.tag[i]+uniTagPrefixes[i]);
			fts.add(pc.cpos[i]+uniCposPrefixes[i]);
			fts.add(pc.bc[i]+uniTagPrefixes[i]);
			
			if(pc.frm[i] != null) {
				fts.add(pc.frm[i]+uniFrmPrefixes[i]);
			}
			//fts.add(soP[i]+(verbsPoses.contains(tag[i]) ? hasSoModifier(tp[i].tok, currentArcs) : false));
			fts.add(pc.tag[i]+(pc.kids[i][0] == mDummy.tok && pc.kids[i][1] == mDummy.tok)+uniNoChildPrefix[i]);	
			fts.add(pc.tag[i]+uniLengthPrefix[i]+length(tp[i], pc.kids[i]));
		}
		
		for(int i = L2; i <= R1; i++) {
			boolean delta = Math.abs(tp[i].tok.getIndex()-tp[i+1].tok.getIndex())>1;
			fts.add(deltaP[i]+delta);
			fts.add(delta+"+"+pc.tag[i]+dt[i]+pc.tag[i+1]);
		}
		
		// Dependency relation features: note: l2 and r2 seem to be useful
		Set[] deprels = new Set[NTOK];
		for(int i = L2; i <= R2; i++) {
			deprels[i] = addAllDeprels(deprelSymbols[i], tp[i].tok, currentArcs);
			for(String s : (Set<String>)deprels[i]) {
				fts.add(s);
			}
		}
		
		// in betweens (potentially big).. should be limited to a fixed window for theoretical
		// complexity reasons
		int l0i = tp[L0].tok.getIndex();
		int r0i = tp[R0].tok.getIndex();
		int rldiff = r0i-l0i;
		if(l0i != 0 && r0i != 0 && rldiff > 0) {
			final int minIndex = Math.max(l0i, r0i-10);
			for(int i = r0i; i > minIndex; i--) {
				Token t = tokens.get(i-1);
				String text = getLemma(t);
				fts.add(getTag(t)+"⺎");
				if(inbetweenWordsOfInterest.contains(text)) {
					fts.add(text+"⺎");
				}
			}
		}
		
		crossSelf("ᅄ", "⿅", fts, L0, deprels, pc);
		crossSelf("ᄹ",  "⿆", fts, R0, deprels, pc);
		
		for(Object deprell0 : deprels[L0]) {
			// l0 x r0
			String newFeat = deprell0+"⿁";
			fts.add(deprell0+pc.tag[R0]+"⿇");
			fts.add(deprell0+pc.bc[R0]+"⿇");
			String l0tdr0 = pc.tag[L0]+"⿈";
			for(Object deprelr0 : deprels[R0]) {
				// l0t x dp(r0)
				fts.add(l0tdr0+deprelr0);
				fts.add(pc.bc[L0]+"+"+deprelr0);
				// dp(l0) x dp(r0)
				fts.add(newFeat+deprelr0);
			}
			String newFeat2 = deprell0+"⿂";
			for(Object deprell1 : deprels[L1]) {
				// dp(l1) x dp(l0) 
				fts.add(newFeat2+deprell1);
			}
		}
		for(Object deprelr1 : deprels[R1]) {
			String prefix = deprelr1+"⿃";
			for(Object deprelr0 : deprels[R0]) {
				// dp(r0) x dp(r1)
				fts.add(prefix+deprelr0);
			}
		}

		if(r0i < tokens.size()-1) {
			String tag1 = getTag(tokens.get(r0i));
			String tag2 = getTag(tokens.get(r0i+1));
			//String tag3 = r0i < tokens.size()-2 ? getTag(tokens.get(r0i+2)) : DUMMY_PART_OF_SPEECH;
			String twotags = tag1+"+"+tag2;
			fts.add("㑆"+tag1);
			fts.add("㑆"+twotags);
		}
		
		// Find new characters for X and Y
		fts.add("鬵"+pc.tag[L0]+"+"+pc.tag[R0]+"+"+pc.bcrc[R0]);
		fts.add("⿉"+pc.tag[L0]+"+"+pc.tag[R0]+"+"+pc.bcrc[L0]);
		// coarse tags
		
		// Ambiguous L1,L2 cpos and bc feats 
		fts.add("⼧"+pc.cpos[L2]);fts.add(pc.bc[L2]+"⼧");
		fts.add("⼧"+pc.cpos[L1]);fts.add(pc.bc[L1]+"⼧");
		// Ambiguous R1,R2 cpos and bc feats
		fts.add("⼨"+pc.cpos[R1]);fts.add(pc.bc[R1]+"⼨");
		fts.add("⼨"+pc.cpos[R2]);fts.add(pc.bc[R2]+"⼨");
		
		if(!pc.cpos[L2].equals("VB") && !pc.cpos[L1].equals("VB") && 
				!pc.cpos[R1].equals("VB") && !pc.cpos[R2].equals("VB")) {
			if(!pc.cpos[L0].equals("VB") && pc.cpos[R0].equals("VB")) {
				fts.add("r0lv");
			}
			else if(pc.cpos[L0].equals("VB") && !pc.cpos[R0].equals("VB")) {
				fts.add("l0lv");
			}
		}
		
		// Might be useless....
		if(pc.tag[L0].equals("JJ") || pc.tag[L0].startsWith("NN")) {
			if(hasTooModifier(tp[L0].tok, currentArcs)) fts.add("ᅃ");
		}
		// no unigram tag+child tag entries..... interesting
		
		
		for(int i = 0; i < lseq.length; i++) {
			int lseqi = lseq[i], rseqi = rseq[i];
			String lf = pc.frm[lseqi], rf = pc.frm[rseqi], lt = pc.tag[lseqi], rt = pc.tag[rseqi];
			String t2 = lt+"+"+rt+"+";
			// tag-tag and word-word
			fts.add(tt[i]+t2); fts.add(lf+ww[i]+rf);
			// tag-word / word-tag
			fts.add(lf+wt[i]+rt); fts.add(rf+tw[i]+lt);
			fts.add(tt[i]+pc.bc[lseqi]+"+"+pc.bc[rseqi]);
			fts.add(t2+pc.lcpos[lseqi]+ttlclc[i]+pc.lcpos[rseqi]);
			fts.add(t2+pc.lcpos[lseqi]+ttlcrc[i]+pc.rcpos[rseqi]);
			fts.add(t2+pc.rcpos[lseqi]+ttrclc[i]+pc.lcpos[rseqi]);
			fts.add(t2+pc.rcpos[lseqi]+ttrcrc[i]+pc.rcpos[rseqi]);
		}
		// More tag-tag
		fts.add("⿌"+pc.tag[L1]+"+"+pc.tag[R1]);

		// ROOT-related features
		if(pc.tag[L1].equals(DUMMY_PART_OF_SPEECH) && pc.tag[R1].equals(DUMMY_PART_OF_SPEECH)) {
			fts.add("VR"+pc.bc[L0]+"+"+pc.bc[R0]);
			fts.add("⿍"+pc.tag[L0]+"+"+pc.tag[R0]);
		}
		
		// Preposition features
		addPrepositionFeats(fts, pc.cpos, pc.tag, pc.frm, pc.rcpos, pc.rcfrm, pc.bc, pc.bcrc);
		// Coordinated conjunction features
		addCcFeats(fts, currentArcs, deprels, pc, tp);
		
	}
	
	private void crossSelf(String prefix, String prefix2, Set<String> fts, int index, Set[] deprels, PreCalculations pc) {
		Set deps = deprels[index];
		final int numDeprels = deps.size();
		if(numDeprels > 1) {
			List<String> deprelsList = new ArrayList<String>(deps);
			// Why are we sorting if we start j at 0?
			//Collections.sort(deprelsList);
			String tag = pc.tag[index];
			String bc = pc.bc[index];
			for(int i = 0; i < numDeprels; i++) {
				String deprel = deprelsList.get(i);
				String newFeat = prefix2+deprel;
				fts.add(prefix+tag+"+"+deprel);
				fts.add(prefix+bc+"+"+deprel);
				// Why does this start at 0?? If we sort, shouldn't we start at i+1?
				for(int j = 0; j < numDeprels; j++) {
					if(i != j) {
						fts.add(newFeat + deprelsList.get(j));
					}
				}
			}
		}
	}
	
	private void addCcFeats(Set<String> fts, List[] tokenToArcs, Set[] deprels, PreCalculations pc, TokenPointer[] tp) {
// no features of interest for pos[L2].equals("CC") (yet :) )
		if("CC".equals(pc.pos[L2])) {
			addL2CCFeats(pc.kids[L2][1], fts, tokenToArcs, deprels, pc, tp);
		}
		if("CC".equals(pc.pos[L1])) {
			addL1CCFeats(pc.kids[L1][1], fts, deprels, pc);
		}
		if("CC".equals(pc.pos[L0])) {
			addL0CCFeats(pc.kids[L0][1], fts, tokenToArcs, pc, tp);
		}
		if("CC".equals(pc.pos[R0])) {
			addR0CCFeats(pc.kids[R0][1], fts, tokenToArcs, deprels, pc, tp);
		}
		if("CC".equals(pc.pos[R1])) {
			addR1CCFeats(pc.kids[R1][1], fts, tokenToArcs, deprels, pc);
		}
		if("CC".equals(pc.pos[R2])) {
			addR2CCFeats(pc.kids[R2][1], fts, tokenToArcs, deprels, pc);
		}
	}
	
	private void addSimilarityFeats(
						    Set<String> fts, 
							String[] frm, 
							String[] pos, 
							String[] cpos,
							String[] bc,
							String[] frm2,
							String[] pos2,
							String[] cpos2,
							String[] bc2,
							int ccindex,
							int index1,
							int index2,
							int isChild) {
		String prefix = "騚"+ccindex+index1+index2+isChild;
		if(frm2[index2] != null && frm[index1] != null) {
			String longForm, shortForm;
			if(frm2[index2].length() > frm[index1].length()) {
				longForm = frm2[index2];
				shortForm = frm[index1];
			}
			else {
				longForm = frm[index1];
				shortForm = frm2[index2];
			}
			//frm2[index2].equals(frm[index1])
			if(longForm.startsWith(shortForm) || longForm.endsWith(shortForm)) {
				fts.add(prefix+"f");
				if(longForm.equals(shortForm)) {
					fts.add(prefix+"fp_"+cpos[index1].equals(cpos2[index2]));
					fts.add(prefix+"fp");
				}
				else {
					fts.add(prefix+"nfp");
				}
			}
			else {
				fts.add(prefix+"nf");
			}
		}
		
		if(bc[index1].equals(bc2[index2])) {
			fts.add(prefix+"bc");
		}
		
		if(cpos2[index2].equals(cpos[index1])) {
			fts.add(prefix+"c");
			if(pos2[index2].equals(pos[index1])) {
				fts.add(prefix+"p");
			}
			else {
				fts.add(prefix+"np");
			}
			
			/*if(rcpos[R0].equals(pos[L0])) {
				fts.add("ZC==");
			}
			else {
				fts.add("ZC!=");
			}
			if(rccpos[R0].equals("VB")) {
				fts.add("ZC_L1POS="+pos[L1]);
			}*/
		}
		else {
			fts.add(prefix+"nc");
		}
	}
	
	private void addL2CCFeats(Token kidL2_1, 
			Set<String> fts, 
			List[] currentArcs,
			Set[] deprels,
			PreCalculations pc,
			TokenPointer[] tp) {
		if(kidL2_1 != mDummy.tok) {
			
		}
		else {
			addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.frm, pc.pos, pc.cpos, pc.bc, L2, L0, L3, 0);
			addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.frm, pc.pos, pc.cpos, pc.bc, L2, R0, L3, 0);
		}
	}
	
	private void addL1CCFeats(Token kidL1_1,
							  Set<String> fts,
							  Set[] deprels,
			 				  PreCalculations pc) {
		fts.add(pc.frm[L1]+"⺒"+pc.rcpos[L1]);
		fts.add("l1CChasChild"+(kidL1_1 == mDummy.tok));
		
		
		
		// What is this feature for? Shouldn't it probably be rcpos[L1], not rcpos[L2]! Hmm...
		fts.add(pc.tag[L2]+"+"+pc.tag[L0]+"+"+pc.tag[R0]+"+"+pc.rcpos[L2]+"⺖"+pc.lcpos[L2]);
		
		if(kidL1_1 != mDummy.tok) {
			//New feat for helping with cases where preposition comes after CC and gets attached to a verb on the right
			// instead of being attached to CC
			// ?/l3 ?/l2 CC/l1 IN/l0 ?VB?/r0
			if((pc.pos[L0].equals("IN") || pc.pos[L0].equals("TO"))
					&& (pc.cpos[R0].equals("VB") || pc.cpos[R0].equals("MD")) 
					&& !pc.cpos[L2].equals("VB") && !pc.cpos[L3].equals("VB")) {
				// discourage rightward attachment
				fts.add("#%1");
			}
			
			// May not hurt to change these equivalence features to be simple combo features
			// L0 CC L2 (matches of pos, cpos, frm)
			/*fts.add(pc.pos[L0].equals(pc.pos[L2])+"⿐1"+pc.tag[R0]);
			fts.add(pc.cpos[L0].equals(pc.cpos[L2])+"⿑1"+pc.tag[R0]);
			//fts.add(pc.frm[L0].equals(pc.frm[L2])+"⿒1"+pc.tag[R0]);
			
			// R0 CC L2 (matches of pos, cpos, frm) AMBIGUOUS with above features - did I mean them to be???
			fts.add(pc.pos[R0].equals(pc.pos[L2])+"⿐1"+pc.tag[R0]);
			fts.add(pc.cpos[R0].equals(pc.cpos[L2])+"⿑1"+pc.tag[R0]);
			//if(pc.frm[R0] != null) fts.add(pc.frm[R0].equals(pc.frm[L2])+"⿒1"+pc.tag[R0]);*/
			
			addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.rcfrm, pc.rcpos, pc.rccpos, pc.bcrc, L1, L2, L1, 1);
			addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.rcfrm, pc.rcpos, pc.rccpos, pc.bcrc, L1, L3, L1, 1);
		}
		else {
			// May not hurt to change these equivalence features to be simple combo features
			// L0 CC L2 (matches of pos, cpos, frm)
			/*fts.add(pc.pos[L0].equals(pc.pos[L2])+"⿐2"+pc.tag[R0]);
			fts.add(pc.cpos[L0].equals(pc.cpos[L2])+"⿑2"+pc.tag[R0]);
			fts.add(pc.frm[L0].equals(pc.frm[L2])+"⿒2"+pc.tag[R0]);
			
			// R0 CC L2 (matches of pos, cpos, frm) AMBIGUOUS with above features - did I mean them to be???
			fts.add(pc.pos[R0].equals(pc.pos[L2])+"⿐2"+pc.tag[R0]);
			fts.add(pc.cpos[R0].equals(pc.cpos[L2])+"⿑2"+pc.tag[R0]);
			if(pc.frm[R0] != null) fts.add(pc.frm[R0].equals(pc.frm[L2])+"⿒2"+pc.tag[R0]);*/
			
			addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.frm, pc.pos, pc.cpos, pc.bc, L1, L0, L2, 0);
			addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.frm, pc.pos, pc.cpos, pc.bc, L1, R0, L2, 0);
			
			for(Object l0dep : deprels[L0]) {
				for(Object l2deprel : deprels[L2]) fts.add(l0dep+"*2%"+l2deprel);
			}
		}
			
		// EXTREMELY PRODUCTIVE.. achieves its goal.. and more(perhaps some not so good things?)
		fts.add("⺙"+pc.tag[L2]+"+"+pc.tag[L0]+"+"+pc.tag[R0]);
	}
	
	private void addL0CCFeats(Token kidL0_1, Set<String> fts, List[] currentArcs, PreCalculations pc, TokenPointer[] tp) {
		fts.add(pc.frm[L0]+"⺓"+pc.rcpos[L0]);
		fts.add("l0HasChild"+(kidL0_1 == mDummy.tok));
		if(kidL0_1 == mDummy.tok) {
			// consider adding this one later
			//fts.add("zig:"+pos[L1]+"_"+pos[L2]+"_"+pos[R1]+"_"+pos[R2]);
			// also consider adding these
			/*if(cpos[R1].equals("VB") || cpos[R1].equals("MD") ) {
				if(cpos[L2].equals("VB") || cpos[L2].equals("MD")) {
					fts.add("zag2:"+pos[L2] + "_"+pos[R2]);
				}
				if(cpos[L3].equals("VB") || cpos[L3].equals("MD")) {
					fts.add("zag3:"+pos[L3] + "_"+pos[R3]);
				}
			}*/
			if((pc.cpos[R1].equals("VB") || pc.cpos[R1].equals("MD")) && null==getDeprel(tp[R1].tok, currentArcs, ParseConstants.SUBJECT_DEP)) {
				fts.add("R1sbjless:"+ (null==getDeprel(tp[R1].tok, currentArcs, ParseConstants.SUBJECT_DEP)));
			}
			
			/*fts.add(pos[R0].equals(pos[L1])+"⿔");fts.add(cpos[R0].equals(cpos[L1])+"頤");fts.add((frm[R0]!=null&&frm[R0].equals(frm[L1]))+"頤頤");
			fts.add(pos[R0].equals(pos[L2])+"⿓");fts.add(cpos[R0].equals(cpos[L2])+"頦");fts.add((frm[R0]!=null&&frm[R0].equals(frm[L2]))+"⿓⿓");
			
			fts.add(pos[R1].equals(pos[L1])+"⿕");fts.add(cpos[R1].equals(cpos[L1])+"⿕2");fts.add((frm[R1]!=null&&frm[R1].equals(frm[L1]))+"⿕3");
			fts.add(pos[R1].equals(pos[L2])+"々");fts.add(cpos[R1].equals(cpos[L2])+"々2");fts.add((frm[R1]!=null&&frm[R1].equals(frm[L2]))+"々3");
			
			fts.add(pos[R2].equals(pos[L1])+"〇");fts.add(cpos[R2].equals(cpos[L1])+"頣");fts.add((frm[R2]!=null&&frm[R2].equals(frm[L1]))+"頣3");
			fts.add(pos[R2].equals(pos[L2])+"〣");fts.add(cpos[R2].equals(cpos[L2])+"頡");fts.add((frm[R2]!=null&&frm[R2].equals(frm[L2]))+"頡3");*/
			addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.frm, pc.pos, pc.cpos, pc.bc, L0, R0, L1, 0);
			addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.frm, pc.pos, pc.cpos, pc.bc, L0, R0, L2, 0);
			
			addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.frm, pc.pos, pc.cpos, pc.bc, L0, R1, L1, 0);
			addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.frm, pc.pos, pc.cpos, pc.bc, L0, R1, L2, 0);
			
			addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.frm, pc.pos, pc.cpos, pc.bc, L0, R2, L1, 0);
			addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.frm, pc.pos, pc.cpos, pc.bc, L0, R2, L2, 0);
			
			// ?/l2 CC/l1 ?/l0 ?/r0 ?/r1 ?/r2
			fts.add(pc.pos[L2]+"+"+pc.pos[L1]+"+"+pc.pos[R0]);
			
			fts.add(pc.cpos[L2]+"⺢"+pc.cpos[R0]);
		}
		else {
			addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.rcfrm, pc.rcpos, pc.rccpos, pc.bcrc, L0, L1, L0, 1);
			addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.rcfrm, pc.rcpos, pc.rccpos, pc.bcrc, L0, L2, L0, 1);
		}
		
	}
	
	private void addR0CCFeats(Token kidR0_1, 
			Set<String> fts, 
			List[] currentArcs,
			Set[] deprels,
			PreCalculations pc,
			TokenPointer[] tp) {
		fts.add(pc.frm[R0]+"⺔"+pc.rcpos[R0]);
		// r0 has child
		fts.add((kidR0_1 != mDummy.tok)+"頩");
		if(kidR0_1 != mDummy.tok) {
			Set<String> r0cDeprels = addAllDeprels("", kidR0_1, currentArcs);
			//Set<String> deprelsl2 = addAllDeprels("",  tp[L2].tok, currentArcs);
			for(String dep : r0cDeprels) {
				for(Object l0deprel : deprels[L0]) fts.add(dep+"㐲"+l0deprel);
				for(Object l1deprel : deprels[L1]) fts.add(dep+"㐁"+l1deprel);
				for(Object l2deprel : deprels[L2]) fts.add(dep+"㐴"+l2deprel);
			}
			
			//*** NOTE: These features won't work because of deprel prefixes...
			if(kidR0_1.getPos().equals("CD")) {
				fts.add("numL0:"+deprels[L0].contains(ParseConstants.NUMERAL_MOD_DEP));
				fts.add("numL1:"+deprels[L1].contains(ParseConstants.NUMERAL_MOD_DEP));
				fts.add("numL2:"+deprels[L2].contains(ParseConstants.NUMERAL_MOD_DEP));
			}
			if(r0cDeprels.contains(ParseConstants.SUBJECT_DEP) || r0cDeprels.contains(ParseConstants.EXPLETIVE_THERE_DEP)) {
				fts.add("l0HasSubj:"+(deprels[L0].contains(ParseConstants.SUBJECT_DEP) || deprels[L0].contains(ParseConstants.EXPLETIVE_THERE_DEP)));
				fts.add("l1HasSubj:"+(deprels[L1].contains(ParseConstants.SUBJECT_DEP) || deprels[L1].contains(ParseConstants.EXPLETIVE_THERE_DEP)));
				fts.add("l2HasSubj:"+(deprels[L2].contains(ParseConstants.SUBJECT_DEP) || deprels[L2].contains(ParseConstants.EXPLETIVE_THERE_DEP)));
			}
			
			addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.rcfrm, pc.rcpos, pc.rccpos, pc.bcrc, R0, L0, R0, 1);
			addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.rcfrm, pc.rcpos, pc.rccpos, pc.bcrc, R0, L1, R0, 1);
			addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.rcfrm, pc.rcpos, pc.rccpos, pc.bcrc, R0, L2, R0, 1);
		}
		
		
		Token r0RCd = getDeterminer(kidR0_1, currentArcs);
		Token l0d = getDeterminer(tp[L0].tok, currentArcs);
		Token l2d = getDeterminer(tp[L2].tok, currentArcs);
		
		if(pc.frm[R0].equals("but") && l0d != null) {
			if(l0d.getText().toLowerCase().equals("no")) {
				fts.add("ZCbutno");
			}
		}
		
		boolean isDefiniter0RCd = isDefinite(r0RCd);
		boolean isDefinitel0d = isDefinite(l0d);
		boolean isDefinitel2d = isDefinite(l2d);
		
		boolean reql0 = r0RCd!=null&&l0d!=null&&r0RCd.getText()!=null&&r0RCd.getText().equalsIgnoreCase(l0d.getText());
		boolean reql2 = r0RCd!=null&&l2d!=null&&r0RCd.getText()!=null&&r0RCd.getText().equalsIgnoreCase(l2d.getText());
		// ZC15:,16,17,18 // ONLY AFFECT cc
		// 'true' is POSITIVE for cc-right (good, same determiner=>they are a good match)
		if(reql0) fts.add("⺞"+reql0);
		// 'true' is VERY NEGATIVE for cc-right (good, next one has the same determiner=>wait a bit)
		if(reql2) fts.add("⺟"+reql2);
		// VERY NEGATIVE FOR cc-right (good, next one has same determiner but immediate left does not=>definitely wait)
		if(reql2 && !reql0) fts.add("⺠");
		
		// Affects many things, appears to be useful
		fts.add("⺡" + isDefinitel2d + "+"+isDefinitel0d+"+"+isDefiniter0RCd);
	}
	
	private void addR1CCFeats(Token kidsR1_1, 
		      Set<String> fts, 
		      List[] currentArcs,
		      Set[] deprels,
		      PreCalculations pc) {
			fts.add(pc.frm[R1]+"⺕"+pc.rcpos[R1]);
			// r1 has child
			fts.add((kidsR1_1 != mDummy.tok) + "頴");
			if(kidsR1_1 != mDummy.tok) {
				Set<String> r1cDeprels = addAllDeprels("", kidsR1_1, currentArcs);
				for(String dep : r1cDeprels) {
					for(Object r0deprel : deprels[R0]) fts.add(dep+"㑀"+r0deprel);
					for(Object l0deprel : deprels[L0]) fts.add(dep+"㑁"+l0deprel);
					for(Object l1deprel : deprels[L1]) fts.add(dep+"㑃"+l1deprel);
				}
				addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.rcfrm, pc.rcpos, pc.rccpos, pc.bcrc, R1, L1, R1, 1);
				addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.rcfrm, pc.rcpos, pc.rccpos, pc.bcrc, R1, L0, R1, 1);
				addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.rcfrm, pc.rcpos, pc.rccpos, pc.bcrc, R1, R0, R1, 1);
			}
			else {
				// X/l0 U/r0 or/r1 Y/r2
				// SOMEWHAT USEFUL FEATS
				addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.frm, pc.pos, pc.cpos, pc.bc, R1, R0, R2, 0);
				addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.frm, pc.pos, pc.cpos, pc.bc, R1, R0, R3, 0);
			}
			
			// ****VERY PRODUCTIVE RULE***** MAYBE TOO MUCH SO... ROOM FOR IMPROVEMENT?
			fts.add(pc.tag[L0]+"+"+pc.tag[R0]+"+"+pc.rcpos[R1]+"⺛"+pc.tag[R2]);
			
			// Very productive...
			fts.add(pc.pos[L0]+"+"+pc.pos[R0]+"⿏"+pc.pos[R2]);
		
	}
	
	private void addR2CCFeats(Token kidsR2_1, 
						      Set<String> fts, 
						      List[] currentArcs, 
						      Set[] deprels,
						      PreCalculations pc) {
		if(kidsR2_1 != mDummy.tok) {
			Set<String> r2cDeprels = addAllDeprels("",  kidsR2_1, currentArcs);
			for(String dep : r2cDeprels) {
				for(Object r0deprel : deprels[R0]) fts.add(dep+"顂"+r0deprel);
				for(Object l0deprel : deprels[L0]) fts.add(dep+"顄"+l0deprel);
			}
			
			// ? ? . ? ? CC->x
			/*if(rcfrm[R2].equals(frm[L0])) fts.add("ZC12frml0");
			// ? ? ? . ? CC->x
			if(rcfrm[R2].equals(frm[R0])) fts.add("ZC12");
			// ? ? ? ? . CC->x
			if(rcfrm[R2].equals(frm[R1])) fts.add("ZC12frm1");
			
			
			if(rccpos[R2].equals(cpos[R1])) {
				fts.add("dCCcpos1");
				if(rcpos[R2].equals(pos[R1])) {
					fts.add("dCCpos1");
				}
			}
			if(rccpos[R2].equals(cpos[R0])) {
				fts.add("dCCcpos0");
				if(pos[R1].equals(",") || pos[R1].equals(":")) {
					fts.add("dCCcpos0x2");
				}
				if(rcpos[R2].equals(pos[R0])) {
					fts.add("dCCpos0");
					if(pos[R1].equals(",") || pos[R1].equals(":") || pos[R1].equals("(")) {
						fts.add("dCCpos0x2");
					}
				}
			}*/
			/*if(rccpos[R2].equals(cpos[L0])) {
				fts.add("dCCcposl0");
				if(rcpos[R2].equals(pos[L0])) {
					fts.add("dCCposl0");
				}
			}*/
			addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.rcfrm, pc.rcpos, pc.rccpos, pc.bcrc, R2, L0, R2, 1);
			addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.rcfrm, pc.rcpos, pc.rccpos, pc.bcrc, R2, R0, R2, 1);
			addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.rcfrm, pc.rcpos, pc.rccpos, pc.bcrc, R2, R1, R2, 1);
			
			// VBZ/L0 VBN/R0 , CC->VBD (might be worth adding a super productive feature template)
			if(pc.pos[R1].equals(",") || pc.pos[R1].equals(":") || pc.pos[R1].equals("(")) {
				fts.add("r2cc_"+pc.pos[L0]+"_"+pc.pos[R0]+"_"+pc.rccpos[R2]);
			}
		}
		else {
			fts.add("dCCr1");
			addSimilarityFeats(fts, pc.frm, pc.pos, pc.cpos, pc.bc, pc.frm, pc.pos, pc.cpos, pc.bc, R2, R0, R3, 0);
		}
	}
	
	private void addPrepositionFeats(Collection<String> fts, String[] cpos, String[] tag, String[] frm, 
			String[] rcpos, String[] rcfrm, String[] bc, String[] bcrc) {
		if(tag[L0].startsWith("IN")) {
			fts.add(frm[L1]+"+"+frm[L0]+"⾷"+rcpos[L0]);
			fts.add(frm[L0]+"+"+tag[L1]+"⾸"+rcfrm[L0]);
			fts.add("⾷"+bc[L1]+"+"+bc[L0]+"+"+bcrc[L0]);
			fts.add(tag[L1]+"+"+bc[L0]+"⾸"+bcrc[L0]);
		}
		if(tag[R0].startsWith("IN")) {
			// Ambiguous for L1 and L2
			fts.add(frm[L2]+"+"+frm[R0]+"⾹"+rcpos[R0]);	fts.add(frm[R0]+"+"+tag[L2]+"⾺"+rcfrm[R0]);
			fts.add(frm[L1]+"+"+frm[R0]+"⾹"+rcpos[R0]);	fts.add(frm[R0]+"+"+tag[L1]+"⾺"+rcfrm[R0]);
			
			fts.add(frm[L0]+"+"+frm[R0]+"⾻"+rcpos[R0]);	fts.add(frm[R0]+"+"+tag[L0]+"⾼"+rcfrm[R0]);
			
			fts.add("⾻"+bc[L0]+"+"+bc[R0]+"+"+bcrc[R0]);	fts.add("⾼"+tag[L0]+"+"+bc[R0]+"+"+bcrc[R0]);
			
			if(!rcpos[R0].equals(DUMMY_PART_OF_SPEECH)) {
				fts.add(frm[L1]+"+"+cpos[L0]+"ᅍ"+frm[R0]);
				fts.add(frm[L1]+"+"+tag[L0]+"ᅎ"+frm[R0]);
				fts.add("haschildᅏ");
				fts.add(frm[L0]+"+"+frm[R0]+"ᅐ"+tag[R1]);
				
				// Ambiguous
				fts.add(frm[L2]+"⾹"+frm[R0]);	fts.add(tag[L2]+"⾺"+frm[R0]);
				fts.add(frm[L1]+"⾹"+frm[R0]);	fts.add(tag[L1]+"⾺"+frm[R0]);
				
				fts.add(frm[L0]+"⾻"+frm[R0]);	fts.add(tag[L0]+"⾼"+frm[R0]);
			}
		}
		if(tag[R1].startsWith("IN")) {
			fts.add(frm[R0]+"+"+frm[R1]+"⾽"+rcpos[R1]);	fts.add(frm[R1]+"+"+tag[R0]+"⾾"+rcfrm[R1]);
			fts.add(frm[L0]+"+"+frm[R1]+"⾿"+rcpos[R1]);	fts.add(frm[R1]+"+"+tag[L0]+"⿀"+rcfrm[R1]);
			
			// Was this supposed to have the bc of the right child instead?
			//fts.add("⾿"+bc[L0]+"+"+bc[R1]);	
			fts.add(tag[L0]+"⿀"+bc[R1]);
			fts.add(bc[L0]+"⾿"+bc[R1]+"+"+bcrc[R1]);	
			fts.add(tag[L0]+"⿀"+bc[R1]+"+"+bcrc[R1]);
		}
	}
	
	private boolean hasTooModifier(Token t, List[] currentArcs) {
		boolean hasTooModifier = false;
		List<Arc> arcs = currentArcs[t.getIndex()];
		if(arcs != null) {
			for(Arc arc : arcs) {
				String text = arc.getChild().getText().toLowerCase();
				if(text.equals("too") || text.equals("enough")) {
					hasTooModifier = true;
					break;
				}
			}
		}
		return hasTooModifier;
	}
	
	private Arc getDeprel(Token t, List[] currentArcs, String deprelString) {
		Arc deprel = null;
		List<Arc> arcs = currentArcs[t.getIndex()];
		if(arcs != null) {
			for(Arc a : arcs) {
				if(a.getDependency().equals(deprelString)) {
					deprel = a;
					break;
				}
			}
		}
		return deprel;
	}
	
	
	// TODO: need to fix handling of possessives, arbitrary possessors aren't being identified as definite!
	private Set<String> definiteDeterminers = new HashSet<String>(Arrays.asList(new String[]{"the","these","those","this","that","'s","'","what","whose"}));
	private boolean isDefinite(Token t) {
		return (t == null) ? false : definiteDeterminers.contains(t.getText().toLowerCase()) || t.getPos().equals("PRP$");
	}
	private Token getDeterminer(Token t, List[] currentArcs) {
		Token determiner = null;
		List<Arc> arcs = currentArcs[t.getIndex()];
		int ti = t.getIndex();
		if(arcs != null) {
			for(Arc arc : arcs) {
				String arcDependency = arc.getDependency();
				if(arcDependency.equals(ParseConstants.DETERMINER_DEP) || arcDependency.equals(ParseConstants.POSSESSOR_DEP)) {
					Token child = arc.getChild();
					if(child.getIndex() < ti) {
						String childPos = child.getPos();
						if(childPos.endsWith("DT")) {
							determiner = child; break;
						}
						else if(childPos.equals("PRP$")||childPos.equals("WP$")) {
							determiner = child; break;
						}
						else {
							List<Arc> grandChildArcs = currentArcs[child.getIndex()];
							if(grandChildArcs != null) {
								for(Arc grandDependent : grandChildArcs) {
									if(grandDependent.getDependency().equals(ParseConstants.POSSESSIVE_MARKER)){
										determiner = grandDependent.getChild(); break;
									}
								}
							}
						}
					}
				}
			}
		}
		return determiner;
	}
	
	private final static Set<String> COMMON_RELATIVE_PRON_POSITIONS = new HashSet<String>(Arrays.asList(new String[]{ParseConstants.SUBJECT_DEP,ParseConstants.DETERMINER_DEP,ParseConstants.POSSESSOR_DEP,ParseConstants.ADVERBIAL_DEP,ParseConstants.PREP_MOD_DEP}));
	private Set<String> addAllDeprels(String prefix, 
									 Token t, 
									 List[] tokenToArcs) {
		Set<String> additions = new HashSet<String>();
		List<Arc> arcs = tokenToArcs[t.getIndex()];

		if(arcs != null) {
			String ldep = "l"+prefix;
			String rdep = "r"+prefix;
			int tIndex = t.getIndex();
			for(Arc arc : arcs) {
				String dep = arc.getDependency();
				Token child = arc.getChild();
				String childPos = child.getPos();
				String feat = (tIndex<arc.getChild().getIndex()?rdep:ldep)+dep;
				if(dep.equals(ParseConstants.PUNCTUATION_DEP)||dep.equals(ParseConstants.PREP_MOD_DEP)){
					feat = feat + getLemma(child);
				}
				else if(dep.equals(ParseConstants.ADVERBIAL_DEP)) {
					feat = feat + getBC(getLemma(child));
					
					//if(child.getText().toLowerCase().startsWith("how")) {
					//	additions.add("whadvmod");
					//}
				}
				if( (COMMON_RELATIVE_PRON_POSITIONS.contains(dep) &&
				  //(childPos.equals("WP") || childPos.equals("WDT") || hasSecondLevelRelativePronoun(child, tokenToArcs)))) {
						(childPos.startsWith("W") || hasSecondLevelRelativePronoun(child, tokenToArcs)))) {
					additions.add(prefix+"⿎");
				}
				additions.add(feat);
			}
		}
		
		return additions;
	}
	
	private boolean hasSecondLevelRelativePronoun(Token t, List[] tokenToArcs) {
		boolean hasSecondLevelRelativePronoun = false;
		List<Arc> arcs = tokenToArcs[t.getIndex()];
		if(arcs != null) {
			outer:
			for(Arc a : arcs) {
				String dependency = a.getDependency();
				if(dependency.equals(ParseConstants.POSSESSOR_DEP)) {
					if(a.getChild().getPos().equals("WP$")) {
						hasSecondLevelRelativePronoun = true;
					}
					break;
				}
				else if(dependency.equals(ParseConstants.PREP_MOD_DEP)) {
					List<Arc> subarcs = tokenToArcs[a.getChild().getIndex()];
					if(subarcs != null) {
						for(Arc subarc : subarcs) {
							String subArcChildPos = subarc.getChild().getPos();
							if(subArcChildPos.equals("WP") || subArcChildPos.equals("WDT")) {
								hasSecondLevelRelativePronoun = true;
								break outer;
							}
						}
					}
				}
			}
		}
		return hasSecondLevelRelativePronoun;
	}
	
	private Token[] getLRchildren(Token t, List[] tokenToArcs) {
		Token[] result = new Token[]{mDummy.tok, mDummy.tok};
		List<Arc> arcs = tokenToArcs[t.getIndex()];
		Arc leftmostArc = null;
		Arc rightmostArc = null;
		if(arcs != null && arcs.size() > 0) {
			// if we knew Arcs were sorted, we could avoid this
			final int tokenIndex = t.getIndex();
			for(Arc arc : arcs) {
				final int arcChildIndex = arc.getChild().getIndex();
				if(arcChildIndex < tokenIndex) {
					if((leftmostArc == null || arcChildIndex < leftmostArc.getChild().getIndex())) {
						// Farther left than leftmost child
						leftmostArc = arc;
					}
				}
				else {
					if((rightmostArc == null || arcChildIndex > rightmostArc.getChild().getIndex())) {
						// Farther right than rightmost child
						rightmostArc = arc;
					}
				}
			}
		}
		if(leftmostArc != null) {
			result[0] = leftmostArc.getChild();
		}
		if(rightmostArc != null) {
			result[1] = rightmostArc.getChild();
		}
		return result;
	}
	
	@Override
	public int getContextWidth() {
		return 8;
	}
	
}