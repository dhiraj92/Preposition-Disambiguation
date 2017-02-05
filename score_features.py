import os
import math
from collections import Counter
featureSelectionDir="featuresSelected"

files = []
for dirname, dirnames, filenames in os.walk(featureSelectionDir):
    for filename in filenames:
        files.append(os.path.join(dirname, filename))

feature_names = {'wv':'Word2Vec', 'w':'Word Itself', 'c':'Capitalized', 'l':'Lemma', 'pos':'Part-Of-Speech',\
        'wc': 'Word-Class', 'af':'Affix', 'h': 'Hypernyms', 'ah': 'Hypernyms', 's':'Synonyms',\
        'as' : 'Synonyms', 'g': 'Gloss Terms', 'ln': 'LexName', 'ri': 'Rule Itself', 'n': 'Numeric'
        }
features = Counter()

for f in files:
    with open(f) as fs_file:
        ctr = 0
        for line in fs_file:
            ctr += 1
            if ctr <= 2:
                continue
            elif ctr == 1001:
                break
            splits = line.split('\t')
            feature_full = splits[0]
            feature = feature_full.split(':')[1]
            score = float(splits[1])
            features[feature_names[feature]] += score

for key in features:
    features[key] /= len(files)

for key, value in features.most_common():
    print(key,math.floor(value), sep='\t')
