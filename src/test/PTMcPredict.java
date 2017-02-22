package test;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import topic.PTMc;
import util.Corpus;
import util.EvaluationPTM;
import util.ReadWriteFile;

public class PTMcPredict {

	public static void main(String[] args) throws IOException {

		StringBuilder sb = new StringBuilder();

		int K = 15;

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

			Map<Integer, Set<Integer>> symptom_herb = getSymptomHerbSetKnowledge(sym_herb_map, herbs_list,
					symptoms_list);

			Map<Integer, Set<Integer>> herb_symptom = getHerbSymptomSetKnowledge(symptom_herb);

			PTMc ptm = new PTMc(herbs_train, symptoms_train, herbs_list.size(), symptoms_list.size(), herb_symptom);

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

			System.out.println("PTM(c) symptom predictive perplexity : " + symptom_perplexity);

			double symptom_precision_k = EvaluationPTM.ptm_symptom_precision_k(herbs_test, symptoms_test, herb_topic,
					symptom_topic, N);

			System.out.println("PTM(c) symptom precision@" + N + ": " + symptom_precision_k);

			double symptom_recall_k = EvaluationPTM.ptm_symptom_recall_k(herbs_test, symptoms_test, herb_topic,
					symptom_topic, N);

			System.out.println("PTM(c) symptom recall@" + N + ": " + symptom_recall_k);

			double symptom_ndcg_k = EvaluationPTM.ptm_symptom_ndcg(herbs_test, symptoms_test, herb_topic, symptom_topic,
					N);

			System.out.println("PTM(c) symptom NDCG@" + N + ": " + symptom_ndcg_k);

			double herb_perplexity = EvaluationPTM.ptm_herb_predictive_perplexity(herbs_test, symptoms_test, herb_topic,
					symptom_topic, prescription_topic_role, prescription_topic);

			System.out.println("PTM(c) herb predictive perplexity : " + herb_perplexity);

			double herb_precision_k = EvaluationPTM.ptm_herb_precision_k(herbs_test, symptoms_test, herb_topic,
					symptom_topic, prescription_topic_role, prescription_topic, N);

			System.out.println("PTM(c) herb precision@" + N + ": " + herb_precision_k);

			double herb_recall_k = EvaluationPTM.ptm_herb_recall_k(herbs_test, symptoms_test, herb_topic, symptom_topic,
					prescription_topic_role, prescription_topic, N);

			System.out.println("PTM(c) herb recall@" + N + ": " + herb_recall_k);

			double herb_ndcg_k = EvaluationPTM.ptm_herb_ndcg(herbs_test, symptoms_test, herb_topic, symptom_topic,
					prescription_topic_role, prescription_topic, N);

			System.out.println("PTM(c) herb NDCG@" + N + ": " + herb_ndcg_k);

			sb.append(herb_perplexity + "," + herb_precision_k + "," + symptom_perplexity + "," + symptom_precision_k
					+ "\n");

		}

		ReadWriteFile.writeFile("file//ptm_c_" + K + "_" + N + ".csv", sb.toString());

	}

	/**
	 * 症状-单味药关系转化为数字形式
	 * 
	 * @param sym_herb_map
	 * @param herb_list
	 * @param symptom_list
	 * @return
	 */
	public static Map<Integer, Set<Integer>> getSymptomHerbSetKnowledge(Map<String, Set<String>> sym_herb_map,
			List<String> herb_list, List<String> symptom_list) {

		Map<Integer, Set<Integer>> symptom_herb_set = new HashMap<>();

		for (String sym : sym_herb_map.keySet()) {

			Set<String> herb_strs = sym_herb_map.get(sym);

			Set<Integer> herbs = new HashSet<>();

			for (String herb_str : herb_strs) {

				int herb = herb_list.indexOf(herb_str);

				herbs.add(herb);

			}

			int symptom = symptom_list.indexOf(sym);

			symptom_herb_set.put(symptom, herbs);

		}

		return symptom_herb_set;

	}

	/**
	 * 转为单味药-症状关系
	 * 
	 * @param symptom_herb
	 * @return
	 */
	public static Map<Integer, Set<Integer>> getHerbSymptomSetKnowledge(Map<Integer, Set<Integer>> symptom_herb) {

		Map<Integer, Set<Integer>> herb_symptom = new HashMap<>();

		for (int s : symptom_herb.keySet()) {

			Set<Integer> herbs = symptom_herb.get(s);

			for (int herb : herbs) {

				if (!herb_symptom.containsKey(herb)) {

					Set<Integer> symptoms = new HashSet<>();
					symptoms.add(s);
					herb_symptom.put(herb, symptoms);

				} else {
					Set<Integer> symptoms = herb_symptom.get(herb);
					symptoms.add(s);
				}

			}

		}
		return herb_symptom;

	}

}
