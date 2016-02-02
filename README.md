# PTM
A Topic Model for Traditional Chinese Medicine Presciption

#Require
Java 7 or above, I use Java 8 in this project.


#Data

All 33,765 prescriptions: /file/pre_herbs.txt, file/pre_symptoms.txt

Training set: /file/pre_herbs_train.txt, /file/pre_symptoms_train.txt

Test set: /file/pre_herbs_test.txt, /file/pre_symptoms_test.txt

Herb list: /data/herbs_contains.txt

Symptom list: /data/symptom_contains.txt

TCM MeSH herb-symptom correspondence knowledge: /data/symptom_herb_tcm_mesh.txt

#Demo

PTM(a): /src/test/RunPTM.java

PTM(b): /src/test/RunPTMMustLink.java

#Herbs and symptoms prediction tasks

PTM(a): /src/test/PTMPredict.java

PTM(b): /src/test/PTMMustPredict.java

 


