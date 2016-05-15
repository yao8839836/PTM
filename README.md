# PTM
A Topic Model for Traditional Chinese Medicine Presciptions

#Require
Java 7 or above, I use Java 8 in this project.

Eclipse


#Data

All 33,765 prescriptions: /file/pre_herbs.txt, file/pre_symptoms.txt

Training set: /file/pre_herbs_train.txt, /file/pre_symptoms_train.txt

Test set: /file/pre_herbs_test.txt, /file/pre_symptoms_test.txt

Herb list: /data/herbs_contains.txt

Symptom list: /data/symptom_contains.txt

TCM MeSH herb-symptom correspondence knowledge: /data/symptom_herb_tcm_mesh.txt

Symptom Category: /file/symptom_category.txt

#Demo

PTM(a): /src/test/RunPTM.java

PTM(b): /src/test/RunPTMMustLink.java

PTM(c): /src/test/RunPTMTreat.java

PTM(d): /src/test/RunPTMTreatMust.java

#Herbs and symptoms prediction tasks

PTM(a): /src/test/PTMPredict.java

PTM(b): /src/test/PTMMustPredict.java

PTM(c): /src/test/PTMTreatPredict.java

PTM(d): /src/test/PTMTreatMustPredict.java

# Topic herb precision

/src/test/TopicPrecisionSymToHerb.java
 
# Topic symptom coherence

/src/test/TopicKnowCoherence.java

