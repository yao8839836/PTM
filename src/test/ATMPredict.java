package test;

import java.io.IOException;
import java.util.List;

import topic.ATM;
import util.Corpus;
import util.Evaluation;
import util.ReadWriteFile;

public class ATMPredict {

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

			ATM atm = new ATM(symptoms_train, herbs_train, symptoms_list.size(), herbs_list.size());

			double alpha = (double) 50 / K;
			double beta = 0.01;

			int iterations = 1000;

			atm.markovChain(K, alpha, beta, iterations);

			double[][] topic_symptom = atm.estimatePhi();

			double[][] herb_topic = atm.estimateTheta();

			double symptom_perplexity = Evaluation.atm_symptom_predictive_perplexity(herbs_test, symptoms_test,
					herb_topic, topic_symptom);

			System.out.println("ATM symptom predictive perplexity : " + symptom_perplexity);

			double symptom_precision_k = Evaluation.atm_symptom_precision_k(herbs_test, symptoms_test, herb_topic,
					topic_symptom, N);
			System.out.println("ATM symptom precision@" + N + ": " + symptom_precision_k);

			double symptom_recall_k = Evaluation.atm_symptom_recall_k(herbs_test, symptoms_test, herb_topic,
					topic_symptom, N);
			System.out.println("ATM symptom recall@" + N + ": " + symptom_recall_k);

			double symptom_ndcg_k = Evaluation.atm_symptom_ndcg(herbs_test, symptoms_test, herb_topic, topic_symptom,
					N);
			System.out.println("ATM symptom NDCG@" + N + ": " + symptom_ndcg_k);

			double herb_perplexity = Evaluation.atm_herb_predictive_perplexity(herbs_test, symptoms_test, herb_topic,
					topic_symptom);
			System.out.println("ATM herb predictive perplexity : " + herb_perplexity);

			double herb_precision_k = Evaluation.atm_herb_precision_k(herbs_test, symptoms_test, herb_topic,
					topic_symptom, N);
			System.out.println("ATM herb precision@" + N + ": " + herb_precision_k);

			double herb_recall_k = Evaluation.atm_herb_recall_k(herbs_test, symptoms_test, herb_topic, topic_symptom,
					N);
			System.out.println("ATM herb recall@" + N + ": " + herb_recall_k);

			double herb_ndcg_k = Evaluation.atm_herb_ndcg(herbs_test, symptoms_test, herb_topic, topic_symptom, N);
			System.out.println("ATM herb NDCG@" + N + ": " + herb_ndcg_k);

			sb.append(herb_perplexity + "," + herb_precision_k + "," + symptom_perplexity + "," + symptom_precision_k
					+ "\n");

		}

		ReadWriteFile.writeFile("file//atm_" + K + "_" + N + ".csv", sb.toString());

	}

}
