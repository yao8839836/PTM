# PTM

The dataset and implementation of Prescription Topic Model in our paper:

Liang Yao, Yin Zhang, Baogang Wei, Wenjin Zhang, Zhe Jin. (2018). "A Topic Modeling Approach for Traditional Chinese Medicine Prescriptions". IEEE Transactions on Knowledge and Data Engineering (TKDE) 30(6), pp.1007-1021. 


# Require
Java 7 or above, I use Java 8 in this project.

Eclipse


# Data

98,334 raw prescriptions with herbs and symptoms are in `/data/prescriptions.txt` . Each line is for a prescription, symptoms are on the left and herbs are on the right.

The preprocessed 33,765 prescriptions: `/data/pre_herbs.txt`, `/data/pre_symptoms.txt`. 

`Training set`: `/data/pre_herbs_train.txt`, `/data/pre_symptoms_train.txt`

`Test set`: `/data/pre_herbs_test.txt`, `/data/pre_symptoms_test.txt`

(Note: 
1. Each line in above files is for a prescription, the same line in `/data/pre_herbs_X.txt` and `/data/pre_symptoms_X.txt` (X is train or test or '') is for the same prescription.

2. Each number in above files means an herb or a symptom, each number is an index of the following herb list or symptom list. For example, '5' in /file/pre_herbs_train.txt means the 6th herb in the herb list /data/herbs_contains.txt, '17' in /file/pre_symptoms_train.txt means the 18th symptom in the symptom list /data/symptom_contains.txt. 
)

Herb list: `/data/herbs_contains.txt`

Symptom list: `/data/symptom_contains.txt`

TCM MeSH herb-symptom correspondence knowledge: `/data/symptom_herb_tcm_mesh.txt`

Symptom Category: `/data/symptom_category.txt`

# Demo

`PTM(a)`: /src/test/RunPTMa.java (reproducing prescribing patterns discovery results)

`PTM(b)`: /src/test/RunPTMb.java

`PTM(c)`: /src/test/RunPTMc.java

`PTM(d)`: /src/test/RunPTMd.java

# Herbs and symptoms prediction/recommendation tasks 
(reproducing herbs/symptoms predictive perplexity and precision@N results)

`PTM(a)`: /src/test/PTMaPredict.java

`PTM(b)`: /src/test/PTMbPredict.java

`PTM(c)`: /src/test/PTMcPredict.java

`PTM(d)`: /src/test/PTMdPredict.java

# Topic herb precision

/src/test/TopicPrecisionSymToHerb.java

# Prescription predictive perplexity

`PTM(a)`: src/perplexity/PTMaPerplexity.java

`PTM(b)`: src/perplexity/PTMbPerplexity.java

`PTM(c)`: src/perplexity/PTMcPerplexity.java

`PTM(d)`: src/perplexity/PTMdPerplexity.java
 
# Topic symptom coherence

/src/test/TopicKnowCoherence.java

