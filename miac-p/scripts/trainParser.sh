#!/bin/bash

#This is a rudimentary script to help you train a parsing model

#The training file(s)
TRAINING_FILES = $0

#The development file (used to evaluate model after each iteration)
DEVELOPMENT_FILE = $1

#Number of training iterations
NUM_OF_ITERATIONS = $2

#The name of the parsing model
MODEL_NAME = $3

#The name of the class to used for feature generation
FEATURE_GENERATOR = miacp.parse.featgen.DefaultEnParseFeatureGenerator

#Name of the class in charge of reading the data
SENTENCE_READER = miacp.parse.io.ConllxSentenceReader

#Don't write models to disk until this training iteration
FIRST_ITERATION_TO_SAVE = 10

#Determine whether to save after each iteration or only after a new best result is achieved
#WHEN_TO_SAVE = new_best
WHEN_TO_SAVE = all

JAR = 
MEMORY_ARG = -Xmx12000m

#TRAIN THE PARSER 
#NOTE: To train a Penn Treebank model, you will need a lot of RAM (12GB should be good)

java ${MEMORY_ARG} -cp ${JAR} miacp.parse.train.OnlineParserTrainer \
	-infiles ${TRAINING_FILES} \
    -out ${MODEL_NAME} \
    -log ${MODEL_NAME}.log \
    -iterations $2 \
    -sentencereader ${SENTENCE_READER} \
    -featgen ${FEATURE_GENERATOR} \
    -devfile ${DEVELOPMENT_FILES} \
    -modelclass miacp.parse.ml.TrainablePerceptron \
    -trainingclass miacp.parse.train.StandardPerSentenceTrainer \
    -first_save_iteration ${FIRST_ITERATION_TO_SAVE} \
    -saveiterations ${WHEN_TO_SAVE}

#CREATE THE FINALIZED MODEL (faster and requires less memory)
#Requires over 7GB free to perform for a typical PTB model

#Usually some of the features have very little importance and can be removed without harming model performance
#TRIM_FACTOR = 0.3
#TRIM_FACTOR = 0.0

#java ${MEMORY_ARG} -cp ${JAR}
