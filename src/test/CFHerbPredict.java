package test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import util.Common;
import util.Corpus;

public class CFHerbPredict {

	public static void main(String[] args) throws IOException, TasteException {

		List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

		List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

		int[][] herbs_test = Corpus.getDocuments("file//pre_herbs_test.txt");

		int[][] symptoms_test = Corpus.getDocuments("file//pre_symptoms_test.txt");

		DataModel model = new FileDataModel(new File("file//cf_symptoms_as_users.csv"));

		UserSimilarity similarity = new PearsonCorrelationSimilarity(model);

		UserNeighborhood neighborhood = new NearestNUserNeighborhood(10, similarity, model);

		UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

		// List<RecommendedItem> recommendations = recommender.recommend(3, 5);
		// for (RecommendedItem recommendation : recommendations) {
		// System.out.println(recommendation);
		//
		// }

		int U = symptoms_list.size();

		int I = herbs_list.size();

		int top_k = 5;

		double precision_k = 0;

		double[][] scores = new double[U][I];

		for (int u = 0; u < U; u++) {

			for (int i = 0; i < I; i++) {

				scores[u][i] = recommender.estimatePreference(u, i);

			}

		}

		for (int i = 0; i < symptoms_test.length; i++) {

			int[] herb_test = herbs_test[i];

			int[] symptom_test = symptoms_test[i];

			// Average

			double[] score_group_item = new double[I];

			for (int item = 0; item < I; item++) {

				for (int u : symptom_test) {
					score_group_item[item] += scores[u][item];
				}
				score_group_item[item] /= symptom_test.length;

			}

			// Least-Misery
			for (int item = 0; item < I; item++) {

				double min = scores[symptom_test[0]][item];

				for (int u : symptom_test) {

					if (scores[u][item] < min)
						min = scores[u][item];
				}
				score_group_item[item] = min;

			}

			Set<Integer> top_k_predict = new HashSet<>();

			for (int k = 0; k < top_k; k++) {

				int max_index = Common.maxIndex(score_group_item);
				top_k_predict.add(max_index);
				score_group_item[max_index] = 0;

			}

			Set<Integer> real_items = new HashSet<>();

			for (int h : herb_test) {

				real_items.add(h);
			}

			int hit_count = 0;
			for (int h : top_k_predict) {

				if (real_items.contains(h)) {
					hit_count++;
				}
			}

			precision_k += (double) hit_count / top_k;

		}

		precision_k /= symptoms_test.length;

		System.out.println("Herb Precision@" + top_k + " : " + precision_k);

	}

}
