package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import topic.PTMTreatMust;
import util.Corpus;
import util.Evaluation;

public class PTMTreatMustPredict {

	public static void main(String[] args) throws IOException {

		List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

		List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

		int[][] herbs_train = Corpus.getDocuments("file//pre_herbs_train.txt");

		int[][] symptoms_train = Corpus.getDocuments("file//pre_symptoms_train.txt");

		int[][] herbs_test = Corpus.getDocuments("file//pre_herbs_test.txt");

		int[][] symptoms_test = Corpus.getDocuments("file//pre_symptoms_test.txt");

		int[][] links = BlockLDAMustLinkPredict.getHerbLinksSet(herbs_train);

		System.out.println(links.length);

		Map<String, String> symptom_herb = TopicPrecisionSymToHerb
				.getSymptomHerbKnowledge("data//symptom_herb_tcm_mesh.txt");

		int[][] herb_symptom_links = PTMTreatPredict.getHerbSymptomLinksSet(symptom_herb, herbs_list, symptoms_list,
				herbs_train, symptoms_train);

		System.out.println(herb_symptom_links.length);

		PTMTreatMust ptm = new PTMTreatMust(herbs_train, symptoms_train, herbs_list.size(), symptoms_list.size(), links,
				herb_symptom_links);

		int K = 15;
		double alpha = 1;

		double alpha_l = 1;
		double alpha_t = 1;
		double beta = 0.1;
		double eta = 0.1;
		double beta_bar = 0.1;
		int iterations = 1000;

		ptm.markovChain(K, alpha, beta, beta_bar, eta, alpha_l, alpha_t, iterations);

		double[][][] herb_topic = ptm.estimatePhi();

		double[][] symptom_topic = ptm.estimatePhiBar();

		double[][] topic_role = ptm.estimatePsi();

		double symptom_perplexity = Evaluation.ptm_symptom_predictive_perplexity(herbs_test, symptoms_test, herb_topic,
				symptom_topic);

		System.out.println("PTM(d) symptom predictive perplexity : " + symptom_perplexity);

		double herb_perplexity = Evaluation.ptm_herb_predictive_perplexity(herbs_test, symptoms_test, herb_topic,
				symptom_topic, topic_role);

		System.out.println("PTM(d) herb predictive perplexity : " + herb_perplexity);

	}

	/**
	 * 从文件中获取herb-symptom关联
	 * 
	 * @param filename
	 * @param herbs_list
	 * @param symptoms_list
	 * @return
	 * @throws IOException
	 */
	public static int[][] getHerbSymptomLinksSet(String filename, List<String> herbs_list, List<String> symptoms_list)
			throws IOException {

		File f = new File(filename);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
		String line = "";

		Set<List<String>> link_set = new HashSet<>();

		while ((line = reader.readLine()) != null) {

			String[] temp = line.split("\t");

			if (temp.length == 2) {

				String symptom = temp[0];

				String[] herbs = temp[1].split(" ");

				for (String herb : herbs) {

					List<String> herb_symptom_pair = new ArrayList<>();

					herb_symptom_pair.add(herb);

					herb_symptom_pair.add(symptom);

					link_set.add(herb_symptom_pair);

				}

			}

		}

		reader.close();

		List<List<String>> link_list = new ArrayList<>(link_set);

		int[][] herb_symptom_links = new int[link_list.size()][2];

		for (int i = 0; i < herb_symptom_links.length; i++) {

			herb_symptom_links[i][0] = herbs_list.indexOf(link_list.get(i).get(0));

			herb_symptom_links[i][1] = symptoms_list.indexOf(link_list.get(i).get(1));

		}

		return herb_symptom_links;
	}

}
