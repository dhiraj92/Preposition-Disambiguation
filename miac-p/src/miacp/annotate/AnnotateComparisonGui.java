/*
 * Copyright 2012 Stephen Tratz
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

package miacp.annotate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import miacp.parse.io.ConllxSentenceReader;
import miacp.parse.types.Arc;
import miacp.parse.types.Parse;
import miacp.parse.types.Sentence;
import miacp.parse.types.Token;


/**
 * A rather crude annotation GUI for attachment decisions made by two different parsers.
 *
 */
public class AnnotateComparisonGui extends JFrame {
	
	private JButton mSaveButton = new JButton("Done");
	private JButton mSkipButton = new JButton("Skip");
	
	private JTextPane mTextArea = new JTextPane();
	private JTextField mRelationField = new JTextField();
	
	private PrintWriter mWriter;
	ConllxSentenceReader mSreader = new ConllxSentenceReader();
	
	List<Token> tokenList = new ArrayList<Token>();
	private Map<Integer, Token> mCaretIndexToToken = new HashMap<Integer, Token>();
	private Map<Token, Integer> mTokenToFirstStart = new HashMap<Token, Integer>();
	private Token mRootToken = new Token("ROOT", 0);
	private Token mAttachmentPoint;
	private Token mAttachmentHighlight1;
	private Token mAttachmentHighlight2;
	private Token mChild;
	
	private SimpleAttributeSet attachAtts, selfAtts, attachHighlight, plainAtts;
	
	private class Diff {
		public Sentence sentence;
		public int sentenceNum;
		public int childIndex;
		public int headIndex1;
		public int headIndex2;
		public Diff(Sentence sentence, int sentenceNum, int childIndex, int headIndex1, int headIndex2) {
			this.sentence = sentence;
			this.sentenceNum = sentenceNum;
			this.childIndex = childIndex;
			this.headIndex1 = headIndex1;
			this.headIndex2 = headIndex2;
		}
	}
	Diff currentDiff;
	List<Diff> allDiffs = new ArrayList<Diff>();
	int diffIndex;
	
	public AnnotateComparisonGui(BufferedReader reader, BufferedReader reader2, BufferedReader goldReader, PrintWriter writer) throws IOException {
		int sNo = 0;
		Parse parse1 = null, parse2 = null, goldParse = null;
		while((parse1 = mSreader.readSentence(reader)) != null) {
			parse2 = mSreader.readSentence(reader2);
			goldParse = mSreader.readSentence(goldReader);
			
			Arc[] arcs1 = parse1.getHeadArcs();
			Arc[] arcs2 = parse2.getHeadArcs();
			Arc[] goldArcs = goldParse.getHeadArcs();
			for(int i = 0; i < goldArcs.length; i++) {
				if(goldArcs[i] == null) {
					if(arcs1[i] != null && arcs2[i] != null && arcs1[i].getHead().getIndex() != arcs2[i].getHead().getIndex()
							&& (!arcs1[i].getDependency().equals("punct") || !arcs2[i].getDependency().equals("punct"))) {
						allDiffs.add(new Diff(parse1.getSentence(), sNo, i, arcs1[i].getHead().getIndex(), arcs2[i].getHead().getIndex()));
					}
				}
			}
			sNo++;
		}
		diffIndex = 0;
		System.err.println("Num diffs: " + allDiffs.size());
		
		mWriter = writer;
		createLayout();
		createListeners();
		setSize(1200, 768);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		loadNextDiff();
		
	}
	
	private void createLayout() {
		
		selfAtts = new SimpleAttributeSet();
		StyleConstants.setBackground(selfAtts, Color.pink);
		
		attachHighlight = new SimpleAttributeSet();
		StyleConstants.setBackground(attachHighlight, Color.yellow);
		
		attachAtts = new SimpleAttributeSet();
		StyleConstants.setBackground(attachAtts, Color.green);
		
		plainAtts = new SimpleAttributeSet();
		StyleConstants.setBackground(plainAtts, Color.white);
		
		Container contentPane = getContentPane();
		contentPane.add(new JScrollPane(mTextArea));
		mTextArea.setFont(new Font("AlBattar", Font.BOLD, 20));
		JPanel buttonPanel = new JPanel();
		contentPane.add(buttonPanel, BorderLayout.NORTH);
		contentPane.add(mRelationField, BorderLayout.SOUTH);
		buttonPanel.add(mSaveButton);
		buttonPanel.add(mSkipButton);
	}
	
	private void createListeners() {
		mSkipButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mSkipButton_actionPerformed(e);
			}
		});
		mSaveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mSaveButton_actionPerformed(e);
			}
		});
		mTextArea.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				mTextArea_mouseClicked(me);
			}
		});
	}
	
	private void mTextArea_mouseClicked(MouseEvent me) {
		
		int caretPosition = mTextArea.getCaretPosition();
		System.err.println("Caret at: " + caretPosition);
		Token clickedToken = mCaretIndexToToken.get(caretPosition);
		if(clickedToken != null) {
			int tokenStart = mTokenToFirstStart.get(clickedToken);
			System.err.println("Clicked: " + clickedToken.getText());
			if(me.getButton() == MouseEvent.BUTTON1) {
				if(mAttachmentPoint != null) {
					int start = mTokenToFirstStart.get(mAttachmentPoint);
					System.err.println("Applying: " + start + " " + mAttachmentPoint.getText().length());
					mTextArea.getStyledDocument().setCharacterAttributes(start, mAttachmentPoint.getText().length(), plainAtts, true);
				}
				mAttachmentPoint = clickedToken;
				mTextArea.getStyledDocument().setCharacterAttributes(tokenStart, clickedToken.getText().length(), attachAtts, true);
			}
			
		}
	}
	
	private void mSkipButton_actionPerformed(ActionEvent e) {
		mWriter.println("SKIP");
		mWriter.flush();
		loadNextDiff();
	}
	
	private void mSaveButton_actionPerformed(ActionEvent e) {
		writeChange();
		loadNextDiff();
	}
	
	private void writeChange() {
		mWriter.println(currentDiff.sentenceNum + "\t" + currentDiff.childIndex + "\t" + mAttachmentPoint.getIndex());
		mWriter.flush();
	}
	
	private void loadNextDiff() {
		try {
			mRelationField.setText("ignore");
			mCaretIndexToToken.clear();
			mTokenToFirstStart.clear();
			tokenList.clear();
			mChild = null;
			mAttachmentPoint = null;
			
			int caretPosition = 0;
			StringBuilder buf = new StringBuilder();
			
			System.err.println("Loading...");
			if(diffIndex < allDiffs.size()) { 
				
				currentDiff = allDiffs.get(diffIndex);
				tokenList.addAll(currentDiff.sentence.getTokens());
				
				List<Token> tokenList = currentDiff.sentence.getTokens();
				if(tokenList.get(0) != mRootToken) {
					tokenList.add(0, mRootToken);
				}
				for(Token t : tokenList) {
					int tokLen = t.getText().length();
					mTokenToFirstStart.put(t, caretPosition);
					for(int i = caretPosition; i <= caretPosition+tokLen; i++) {
						mCaretIndexToToken.put(i, t);
					}
					buf.append(t.getText()).append(" ");
					caretPosition += t.getText().length()+1;
					
					mChild = tokenList.get(currentDiff.childIndex);
					mAttachmentHighlight1 = tokenList.get(currentDiff.headIndex1);
					mAttachmentHighlight2 = tokenList.get(currentDiff.headIndex2);
				}
				
				mTextArea.setText(buf.toString());
				
				
				
				mTextArea.getStyledDocument().setCharacterAttributes(0, mTextArea.getText().length(), plainAtts, true);
				if(mChild != null) {
					int start = mTokenToFirstStart.get(mChild);
					mTextArea.getStyledDocument().setCharacterAttributes(start, mChild.getText().length(), selfAtts, true);
				}
				if(mAttachmentHighlight1 != null) {
					int start = mTokenToFirstStart.get(mAttachmentHighlight1);
					System.err.println("Applying: " + start + " " + mAttachmentHighlight1.getText().length());
					mTextArea.getStyledDocument().setCharacterAttributes(start, mAttachmentHighlight1.getText().length(), attachHighlight, true);
				}
				if(mAttachmentHighlight2 != null) {
					int start = mTokenToFirstStart.get(mAttachmentHighlight2);
					System.err.println("Applying: " + start + " " + mAttachmentHighlight2.getText().length());
					mTextArea.getStyledDocument().setCharacterAttributes(start, mAttachmentHighlight2.getText().length(), attachHighlight, true);
				}
								
				diffIndex++;
			}
			else {
				mTextArea.setText("THAT'S ALL FOLKS");
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static void main(final String[] args) throws Exception {
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				String inFile = args[0];
				String inFile2 = args[1];
				String goldFile = args[2];
				String outFile = args[3];
				try {
					if(!new File(outFile).exists()) {
						BufferedReader reader = new BufferedReader(new FileReader(inFile));
						BufferedReader reader2 = new BufferedReader(new FileReader(inFile2));
						BufferedReader goldReader = new BufferedReader(new FileReader(goldFile));
						PrintWriter writer = new PrintWriter(new FileWriter(outFile));
				
						AnnotateComparisonGui gui = new AnnotateComparisonGui(reader, reader2, goldReader, writer);
						gui.setVisible(true);
					}
					else {
						System.err.println("Error: output file already exists");
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
}