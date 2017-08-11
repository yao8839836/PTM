package topic;

/**
 * 
 * Bilingual Biterm Topic Model (AAAI'16)
 * 
 * @author Liang Yao
 * @email yaoliang@zju.edu.cn
 *
 */
public class BiBTM {

	int[][] herb_pairs;

	int[][] symptom_pairs;

	int[][] herb_symptom_pairs;

	int H;

	int S;

	int K;

	double alpha;

	double beta;

	public int[] syndrome;

	public int[] treatment;

	public int[] syndrome_treatment;

	int[] np;

	int[][] ns;

	int[] nssum;

	int[][] nh;

	int[] nhsum;

	int iterations;

	public BiBTM(int[][] herb_pairs, int[][] symptom_pairs, int[][] herb_symptom_pairs, int H, int S) {

		this.herb_pairs = herb_pairs;

		this.symptom_pairs = symptom_pairs;

		this.herb_symptom_pairs = herb_symptom_pairs;

		this.H = H;

		this.S = S;

	}

	public void initialState() {

		np = new int[K];

		ns = new int[S][K];

		nssum = new int[K];

		nh = new int[H][K];

		nhsum = new int[K];

		syndrome = new int[symptom_pairs.length];

		treatment = new int[herb_pairs.length];

		syndrome_treatment = new int[herb_symptom_pairs.length];

		for (int i = 0; i < syndrome.length; i++) {

			int syn = (int) (Math.random() * K);

			syndrome[i] = syn;

			updateCountSymptom(syn, symptom_pairs[i], +1);

		}

		for (int i = 0; i < treatment.length; i++) {

			int treat = (int) (Math.random() * K);

			treatment[i] = treat;

			updateCountHerb(treat, herb_pairs[i], +1);

		}

		for (int i = 0; i < syndrome_treatment.length; i++) {

			int topic = (int) (Math.random() * K);

			syndrome_treatment[i] = topic;

			updateCountHerbSymptom(topic, herb_symptom_pairs[i], +1);

		}

	}

	public void markovChain(int K, double alpha, double beta, int iterations) {

		this.K = K;
		this.alpha = alpha;
		this.beta = beta;

		this.iterations = iterations;

		initialState();

		for (int i = 0; i < this.iterations; i++) {

			System.out.println("iteration : " + i);

		}

	}

	public void gibbs() {

		for (int i = 0; i < herb_pairs.length; i++) {

			int treat = sampleFullConditionalTreat(i);

			treatment[i] = treat;

		}

		for (int i = 0; i < symptom_pairs.length; i++) {

			int syn = sampleFullConditionalSyndrome(i);

			syndrome[i] = syn;

		}

		for (int i = 0; i < herb_symptom_pairs.length; i++) {

			int syn_treat = sampleFullConditionalTreatSyndrome(i);

			syndrome_treatment[i] = syn_treat;

		}

	}

	int sampleFullConditionalSyndrome(int i) {

		int syn = syndrome[i];

		updateCountSymptom(syn, symptom_pairs[i], +1);

		double[] pr = new double[K];

		for (int k = 0; k < K; k++) {
			pr[k] = (np[k] + alpha) * (ns[symptom_pairs[i][0]][k] + beta) / (nssum[k] + S * beta)
					* (ns[symptom_pairs[i][1]][k] + beta) / (nssum[k] + S * beta);
		}

		syn = sample(pr);

		updateCountSymptom(syn, symptom_pairs[i], -1);

		return syn;
	}

	int sampleFullConditionalTreat(int i) {

		int treat = treatment[i];

		updateCountHerb(treat, herb_pairs[i], +1);

		double[] pr = new double[K];

		for (int k = 0; k < K; k++) {
			pr[k] = (np[k] + alpha) * (nh[herb_pairs[i][0]][k] + beta) / (nhsum[k] + H * beta)
					* (nh[herb_pairs[i][1]][k] + beta) / (nhsum[k] + H * beta);
		}

		treat = sample(pr);

		updateCountHerb(treat, herb_pairs[i], -1);

		return treat;
	}

	int sampleFullConditionalTreatSyndrome(int i) {

		int syn_treat = syndrome_treatment[i];

		updateCountHerbSymptom(syn_treat, herb_symptom_pairs[i], +1);

		double[] pr = new double[K];

		for (int k = 0; k < K; k++) {
			pr[k] = (np[k] + alpha) * (nh[herb_symptom_pairs[i][0]][k] + beta) / (nhsum[k] + H * beta)
					* (ns[herb_symptom_pairs[i][1]][k] + beta) / (nssum[k] + S * beta);
		}

		syn_treat = sample(pr);

		updateCountHerbSymptom(syn_treat, herb_symptom_pairs[i], -1);

		return syn_treat;
	}

	int sample(double[] p) {

		int topic = 0;

		for (int k = 1; k < p.length; k++) {
			p[k] += p[k - 1];
		}
		double u = Math.random() * p[p.length - 1];
		for (int t = 0; t < p.length; t++) {
			if (u < p[t]) {
				topic = t;
				break;
			}
		}

		return topic;
	}

	void updateCountSymptom(int syndrome, int[] symptoms, int flag) {

		ns[symptoms[0]][syndrome] += flag;
		nssum[syndrome] += flag;

		ns[symptoms[1]][syndrome] += flag;
		nssum[syndrome] += flag;

		np[syndrome] += flag;

	}

	void updateCountHerb(int treat, int[] herbs, int flag) {

		nh[herbs[0]][treat] += flag;
		nhsum[treat] += flag;

		nh[herbs[1]][treat] += flag;
		nhsum[treat] += flag;

		np[treat] += flag;

	}

	void updateCountHerbSymptom(int topic, int[] herb_symptom, int flag) {

		nh[herb_symptom[0]][topic] += flag;
		nhsum[topic] += flag;

		ns[herb_symptom[1]][topic] += flag;
		nssum[topic] += flag;

		np[topic] += flag;

	}

	public double[] estimateTheta() {
		double[] theta = new double[K];

		for (int k = 0; k < K; k++) {
			theta[k] = (np[k] + alpha)
					/ (herb_pairs.length + symptom_pairs.length + herb_symptom_pairs.length + K * alpha);
		}

		return theta;
	}

	public double[][] estimatePhiBar() {
		double[][] phi_bar = new double[K][S];
		for (int k = 0; k < K; k++) {
			for (int s = 0; s < S; s++) {
				phi_bar[k][s] = (ns[s][k] + beta) / (nssum[k] + S * beta);
			}
		}
		return phi_bar;
	}

	public double[][] estimatePhi() {
		double[][] phi = new double[K][H];
		for (int k = 0; k < K; k++) {
			for (int h = 0; h < H; h++) {
				phi[k][h] = (nh[h][k] + beta) / (nhsum[k] + H * beta);
			}
		}
		return phi;
	}

}
