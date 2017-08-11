package test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import topic.PTMd;
import util.Corpus;
import util.EvaluationPTM;
import util.ReadWriteFile;

public class PTMdPredict {

	public static void main(String[] args) throws IOException {

		StringBuilder sb = new StringBuilder();

		int K = 20;

		int N = 5;

		for (int i = 0; i < 10; i++) {

			List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

			List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

			int[][] herbs_train = Corpus.getDocuments("file//pre_herbs_train.txt");

			int[][] symptoms_train = Corpus.getDocuments("file//pre_symptoms_train.txt");

			int[][] herbs_test = Corpus.getDocuments("file//pre_herbs_test.txt");

			int[][] symptoms_test = Corpus.getDocuments("file//pre_symptoms_test.txt");

			Map<String, Set<String>> sym_herb_map = TopicPrecisionSymToHerb
					.getSymptomHerbSetKnowledge("data//symptom_herb_tcm_mesh.txt");

			Map<Integer, Set<Integer>> symptom_herb = PTMcPredict.getSymptomHerbSetKnowledge(sym_herb_map, herbs_list,
					symptoms_list);

			Map<Integer, Set<Integer>> herb_symptom = PTMcPredict.getHerbSymptomSetKnowledge(symptom_herb);

			PTMd ptm = new PTMd(herbs_train, symptoms_train, herbs_list.size(), symptoms_list.size(), herb_symptom);

			double alpha = 1;
			double beta = 0.1;
			double eta = 1;
			double beta_bar = 0.1;
			int iterations = 1000;

			ptm.markovChain(K, alpha, beta, beta_bar, eta, iterations);

			double[][][] herb_topic = ptm.estimatePhi();

			double[][] symptom_topic = ptm.estimatePhiBar();

			double[][][] prescription_topic_role = ptm.estimatePi();

			double[][] prescription_topic = ptm.estimateTheta();

			double symptom_perplexity = EvaluationPTM.ptm_symptom_predictive_perplexity(herbs_test, symptoms_test,
					herb_topic, symptom_topic);

			System.out.println("PTM(d) symptom predictive perplexity : " + symptom_perplexity);

			double symptom_precision_k = EvaluationPTM.ptm_symptom_precision_k(herbs_test, symptoms_test, herb_topic,
					symptom_topic, N);

			System.out.println("PTM(d) symptom precision@" + N + ": " + symptom_precision_k);

			double symptom_recall_k = EvaluationPTM.ptm_symptom_recall_k(herbs_test, symptoms_test, herb_topic,
					symptom_topic, N);

			System.out.println("PTM(d) symptom recall@" + N + ": " + symptom_recall_k);

			double symptom_ndcg_k = EvaluationPTM.ptm_symptom_ndcg(herbs_test, symptoms_test, herb_topic, symptom_topic,
					N);

			System.out.println("PTM(d) symptom NDCG@" + N + ": " + symptom_ndcg_k);

			double herb_perplexity = EvaluationPTM.ptm_herb_predictive_perplexity(herbs_test, symptoms_test, herb_topic,
					symptom_topic, prescription_topic_role, prescription_topic);

			System.out.println("PTM(d) herb predictive perplexity : " + herb_perplexity);

			double herb_precision_k = EvaluationPTM.ptm_herb_precision_k(herbs_test, symptoms_test, herb_topic,
					symptom_topic, prescription_topic_role, prescription_topic, N);

			System.out.println("PTM(d) herb precision@" + N + ": " + herb_precision_k);

			double herb_recall_k = EvaluationPTM.ptm_herb_recall_k(herbs_test, symptoms_test, herb_topic, symptom_topic,
					prescription_topic_role, prescription_topic, N);

			System.out.println("PTM(d) herb recall@" + N + ": " + herb_recall_k);

			double herb_ndcg_k = EvaluationPTM.ptm_herb_ndcg(herbs_test, symptoms_test, herb_topic, symptom_topic,
					prescription_topic_role, prescription_topic, N);

			System.out.println("PTM(d) herb NDCG@" + N + ": " + herb_ndcg_k);

			sb.append(herb_perplexity + "," + herb_precision_k + "," + symptom_perplexity + "," + symptom_precision_k
					+ "\n");

		}

		ReadWriteFile.writeFile("file//ptm_d_" + K + "_" + N + ".csv", sb.toString());

	}

}
