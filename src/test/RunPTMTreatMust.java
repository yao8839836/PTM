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
import util.Common;
import util.Corpus;
import util.ReadWriteFile;

public class RunPTMTreatMust {

	public static void main(String[] args) throws IOException {

		List<String> herbs_list = Corpus.getVocab("data//herbs_contains.txt");

		List<String> symptoms_list = Corpus.getVocab("data//symptom_contains.txt");

		int[][] herbs = Corpus.getDocuments("file//pre_herbs.txt");

		int[][] symptoms = Corpus.getDocuments("file//pre_symptoms.txt");

		Map<String, String> symptom_herb = TopicPrecisionSymToHerb
				.getSymptomHerbKnowledge("data//symptom_herb_tcm_mesh.txt");

		int[][] herb_symptom_links = PTMTreatPredict.getHerbSymptomLinksSet(symptom_herb, herbs_list, symptoms_list,
				herbs, symptoms);

		System.out.println(herb_symptom_links.length);

		int[][] links = BlockLDAMustLinkPredict.getHerbLinksSet(herbs);

		System.out.println(links.length);

		PTMTreatMust linklda = new PTMTreatMust(herbs, symptoms, herbs_list.size(), symptoms_list.size(), links,
				herb_symptom_links);

		int K = 25;
		double alpha = 1;
		double alpha_l = 1;
		double alpha_t = 1;
		double beta = 0.1;
		double beta_bar = 0.1;

		double eta = 0.1;
		int iterations = 1000;

		linklda.markovChain(K, alpha, beta, beta_bar, eta, alpha_l, alpha_t, iterations);

		double[][] phi_bar = linklda.estimatePhiBar();

		double[][] phi_bar_for_write = Common.makeCopy(phi_bar);

		double[][] psi = linklda.estimatePsi();

		double[][][] phi = linklda.estimatePhi();

		StringBuilder sb = new StringBuilder();

		for (int k = 0; k < phi_bar.length; k++) {

			double[] topic_herb = new double[phi[0][0].length];

			for (int x = 0; x < phi[0].length; x++) {
				for (int h = 0; h < topic_herb.length; h++) {
					topic_herb[h] += phi[k][x][h] * psi[k][x];
				}

			}
			StringBuilder herb_str = new StringBuilder();

			for (int i = 0; i < 10; i++) {

				int max_index = Common.maxIndex(topic_herb);

				herb_str.append(herbs_list.get(max_index) + " ");

				topic_herb[max_index] = 0;

			}

			double[] phi_bar_k = phi_bar_for_write[k];

			StringBuilder symptom_str = new StringBuilder();

			for (int i = 0; i < 10; i++) {

				int max_index = Common.maxIndex(phi_bar_k);

				symptom_str.append(symptoms_list.get(max_index) + " ");

				phi_bar_k[max_index] = 0;

			}

			sb.append(herb_str.toString().trim() + "\t" + symptom_str.toString().trim() + "\n");

		}

		String filename = "result//topic_ptm_3d(d)_25.txt";
		ReadWriteFile.writeFile(filename, sb.toString());

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
