#!/bin/bash

# This script (parse.sh) can be used to 
# POS-tag, parse, and semantically-annotate files containing plain text sentences. Each line should contain exactly one sentence and
# the tokens should be separated by whitespace.
#
# Sample Usage:
# ./parse.sh input.txt output.txt 

#$1 input file/dir
#$2 outputdir

#Note: It doesn't actually need this much memory to run. 
MAX_MEMORY=-Xmx6000m

#Points to .jar file
CLASSPATH=

WORDNET=data/wordnet3/

SENTENCE_READER=miacp.parse.io.TokenizingSentenceReader
SENTENCE_WRITER=miacp.parse.io.DefaultSentenceWriter

POS_MODEL=posTaggingModel.gz
PARSE_MODEL=parseModel.gz

#POSSESSIVES_MODEL=""
#NOUN_COMPOUND_MODEL=""
#SRL_ARGS_MODELS=""
#SRL_PREDICATE_MODELS=""
#PREPOSITION_MODELS=""

POSSESSIVES_MODEL="-possmodel possessivesModel.gz"
NOUN_COMPOUND_MODEL="-nnmodel nnModel.gz"
SRL_ARGS_MODELS="-srlargsmodel srlArgsWrapper.gz"
SRL_PREDICATE_MODELS="-srlpredmodel srlPredWrapper.gz"
PREPOSITION_MODELS="-psdmodel psdModels.gz"

if [ $# -lt 2 ]; then
	OUTPUT_ARG=""
	INPUT_ARG="-input $1"
else	
	OUTPUT_ARG="-writeroptions output=$2"
fi

if [ $# -lt 1 ]; then
	INPUT_ARG=""
else
	INPUT_ARG="-input $1"
fi

java $MAX_MEMORY -cp $CLASSPATH miacp.parse.ParsingScript -wndir $WORDNET -posmodel $POS_MODEL -parsemodel $PARSE_MODEL $POSSESSIVES_MODEL $NOUN_COMPOUND_MODEL $PREPOSITION_MODELS $SRL_ARGS_MODELS $SRL_PREDICATE_MODELS -sreader $SENTENCE_READER -swriter $SENTENCE_WRITER ${OUTPUT_ARG} ${INPUT_ARG}
