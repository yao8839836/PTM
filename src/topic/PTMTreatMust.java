package topic;

public class PTMTreatMust {

	int[][] herbs;

	int[][] symptoms;

	int H;

	int S;

	int K;

	int X = 4;

	double alpha;

	double beta;

	double beta_bar;

	double eta;

	int[][] syndrome;

	/**
	 * treatment method assignments
	 */
	int[][] treatment;

	/**
	 * jun-chen-zuo-shi assignments
	 */
	int[][] x;

	int[][][] kxh;

	int[][] ns;

	int[][] np;

	int[] npsum;

	int[][] nhsum;

	int[] nssum;

	int[] nxsum;

	int iterations;

	/*
	 * herb symptom treatment part
	 */
	int[][] treat_links;

	int[] n_treat_links;

	int[] z_treat_links;

	int[] x_treat_links;

	double alpha_t;

	/*
	 * herb links
	 * 
	 */

	int[][] links;

	int[] nlinks;

	int[] z_links;

	int[][] x_links;

	double alpha_l;

	public PTMTreatMust(int[][] herbs, int[][] symptoms, int H, int S, int[][] links, int[][] treat_links) {

		this.herbs = herbs;
		this.symptoms = symptoms;

		this.H = H;
		this.S = S;

		this.links = links;

		this.treat_links = treat_links;
	}

	public void initialState() {

		int P = herbs.length;

		np = new int[P][K];
		npsum = new int[P];

		ns = new int[S][K];
		nssum = new int[K];

		kxh = new int[K][X][H];
		nhsum = new int[K][X];
		nxsum = new int[K];

		syndrome = new int[P][];
		treatment = new int[P][];
		x = new int[P][];

		for (int p = 0; p < P; p++) {

			// herbs
			int Hp = herbs[p].length;
			treatment[p] = new int[Hp];
			x[p] = new int[Hp];

			for (int n = 0; n < Hp; n++) {

				int treatment_method = (int) (Math.random() * K);
				treatment[p][n] = treatment_method;

				int jun_chen_zuo_shi = (int) (Math.random() * X);
				x[p][n] = jun_chen_zuo_shi;

				updateCountHerb(p, treatment_method, jun_chen_zuo_shi, herbs[p][n], +1);

			}

			int Sp = symptoms[p].length;
			syndrome[p] = new int[Sp];

			for (int m = 0; m < Sp; m++) {

				int syn = (int) (Math.random() * K);
				syndrome[p][m] = syn;

				updateCountSymptom(p, syn, symptoms[p][m], +1);
			}

		}

		// links

		nlinks = new int[K];

		z_links = new int[links.length];

		x_links = new int[links.length][2];

		for (int i = 0; i < links.length; i++) {

			int topic = (int) (Math.random() * K);

			z_links[i] = topic;

			int role_1 = (int) (Math.random() * X);

			int role_2 = (int) (Math.random() * X);

			x_links[i][0] = role_1;

			x_links[i][1] = role_2;

			updateLinkEntityCount(topic, x_links[i], links[i], +1);

		}

		// herb symptom treatment

		n_treat_links = new int[K];

		z_treat_links = new int[treat_links.length];

		x_treat_links = new int[treat_links.length];

		for (int i = 0; i < treat_links.length; i++) {

			int topic = (int) (Math.random() * K);

			z_treat_links[i] = topic;

			int role = (int) (Math.random() * X);

			x_treat_links[i] = role;

			updateHerbSymptomLinkCount(topic, role, treat_links[i], +1);

		}

	}

	public void markovChain(int K, double alpha, double beta, double beta_bar, double eta, double alpha_l,
			double alpha_t, int iterations) {

		this.K = K;
		this.alpha = alpha;
		this.beta = beta;
		this.beta_bar = beta_bar;
		this.iterations = iterations;
		this.eta = eta;

		this.alpha_l = alpha_l;

		this.alpha_t = alpha_t;

		initialState();

		for (int i = 0; i < this.iterations; i++) {

			System.out.println("iteration : " + i);
			gibbs();
		}

	}

	public void gibbs() {

		for (int p = 0; p < herbs.length; p++) {

			for (int m = 0; m < symptoms[p].length; m++) {

				int syn = sampleFullConditionalSyndrome(p, m);

				syndrome[p][m] = syn;

			}

			for (int n = 0; n < herbs[p].length; n++) {

				int[] treat_role = sampleFullConditionalTreatRole(p, n);
				treatment[p][n] = treat_role[0];

				x[p][n] = treat_role[1];

			}

		}

		// herb symptom links

		for (int i = 0; i < treat_links.length; i++) {

			int[] topic_role = sampleFullConditionalHerbSymptomLink(i);

			z_treat_links[i] = topic_role[0];

			x_treat_links[i] = topic_role[1];

		}

		// links

		for (int i = 0; i < links.length; i++) {

			int topic = sampleFullConditionalLink(i);

			z_links[i] = topic;

		}
	}

	int sampleFullConditionalSyndrome(int p, int m) {

		int syn = syndrome[p][m];

		updateCountSymptom(p, syn, symptoms[p][m], -1);

		double[] pr = new double[K];

		for (int k = 0; k < K; k++) {

			pr[k] = (np[p][k] + alpha) / (npsum[p] + K * alpha) * (ns[symptoms[p][m]][k] + beta_bar)
					/ (nssum[k] + S * beta_bar);
		}

		syn = sample(pr);

		updateCountSymptom(p, syn, symptoms[p][m], +1);

		return syn;

	}

	int[] sampleFullConditionalTreatRole(int p, int n) {

		int[] treat_role = new int[2];

		int treat = treatment[p][n];

		int role = x[p][n];

		updateCountHerb(p, treat, role, herbs[p][n], -1);

		double[][] pr = new double[K][X];

		for (int k = 0; k < K; k++) {

			for (int r = 0; r < X; r++) {

				pr[k][r] = (np[p][k] + alpha) / (npsum[p] + K * alpha) * (nhsum[k][r] + eta) / (nxsum[k] + X * eta)
						* (kxh[k][r][herbs[p][n]] + beta) / (nhsum[k][r] + H * beta);

			}
		}

		double[] pr_sum = new double[K * X];

		for (int k = 0; k < K; k++) {

			for (int r = 0; r < X; r++) {
				pr_sum[k * X + r] = pr[k][r];
			}

		}

		int index = sample(pr_sum);

		treat = index / X;
		role = index % X;

		treat_role[0] = treat;
		treat_role[1] = role;

		// System.out.println(treat + "\t" + role);

		updateCountHerb(p, treat, role, herbs[p][n], +1);

		return treat_role;
	}

	int[] sampleFullConditionalHerbSymptomLink(int i) {

		int topic = z_treat_links[i];

		int role = x_treat_links[i];

		int[] syndrome_role = new int[2];

		updateHerbSymptomLinkCount(topic, role, treat_links[i], -1);

		double[][] pr = new double[K][X];

		for (int k = 0; k < K; k++) {

			for (int r = 0; r < X; r++) {

				pr[k][r] = (n_treat_links[k] + alpha_t) * (ns[treat_links[i][1]][k] + beta_bar)
						/ (nssum[k] + S * beta_bar) * (nhsum[k][r] + eta) / (nxsum[k] + X * eta)
						* (kxh[k][r][treat_links[i][0]] + beta) / (nhsum[k][r] + H * beta);

			}
		}

		double[] pr_sum = new double[K * X];

		for (int k = 0; k < K; k++) {

			for (int r = 0; r < X; r++) {
				pr_sum[k * X + r] = pr[k][r];
			}

		}

		int index = sample(pr_sum);

		topic = index / X;
		role = index % X;

		updateHerbSymptomLinkCount(topic, role, treat_links[i], +1);

		syndrome_role[0] = topic;
		syndrome_role[1] = role;

		return syndrome_role;

	}

	int sampleFullConditionalLink(int i) {

		int topic = z_links[i];

		updateLinkEntityCount(topic, x_links[i], links[i], -1);

		double[][][] pr = new double[K][X][X];

		for (int k = 0; k < K; k++) {

			for (int r = 0; r < X; r++) {

				for (int r1 = 0; r1 < X; r1++) {

					pr[k][r][r1] = (nlinks[k] + alpha_l) * (nhsum[k][r] + eta) / (nxsum[k] + X * eta)
							* (kxh[k][r][links[i][0]] + beta) / (nhsum[k][r] + H * beta) * (nhsum[k][r1] + eta)
							/ (nxsum[k] + X * eta) * (kxh[k][r1][links[i][1]] + beta) / (nhsum[k][r1] + H * beta);

				}
			}

		}

		double[] pr_sum = new double[K * X * X];

		for (int k = 0; k < K; k++) {

			for (int r = 0; r < X; r++) {

				for (int r1 = 0; r1 < X; r1++) {

					pr_sum[k * X * X + r * X + r1] = pr[k][r][r1];

				}

			}

		}

		int index = sample(pr_sum);

		topic = index / (X * X);

		x_links[i][0] = (index - topic * X * X) / X;

		x_links[i][1] = index % X;

		// System.out.println(index + "\t" + topic + "\t" + x_links[i][0] + "\t"
		// + x_links[i][1]);

		updateLinkEntityCount(topic, x_links[i], links[i], +1);

		return topic;

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

	void updateCountSymptom(int p, int syndrome, int symptom, int flag) {

		ns[symptom][syndrome] += flag;
		nssum[syndrome] += flag;

		np[p][syndrome] += flag;
		npsum[p] += flag;

	}

	void updateCountHerb(int p, int treatment, int jczs, int herb, int flag) {

		np[p][treatment] += flag;
		npsum[p] += flag;

		kxh[treatment][jczs][herb] += flag;
		nhsum[treatment][jczs] += flag;
		nxsum[treatment] += flag;

	}

	void updateLinkEntityCount(int treatment, int[] role, int[] link, int flag) {

		nlinks[treatment] += flag;

		kxh[treatment][role[0]][link[0]] += flag;
		nhsum[treatment][role[0]] += flag;
		nxsum[treatment] += flag;

		kxh[treatment][role[1]][link[1]] += flag;
		nhsum[treatment][role[1]] += flag;
		nxsum[treatment] += flag;

	}

	void updateHerbSymptomLinkCount(int treatment, int role, int[] herb_symptom_link, int flag) {

		n_treat_links[treatment] += flag;

		// 0 is herb

		kxh[treatment][role][herb_symptom_link[0]] += flag;
		nhsum[treatment][role] += flag;
		nxsum[treatment] += flag;

		// 1 is symptom

		ns[herb_symptom_link[1]][treatment] += flag;
		nssum[treatment] += flag;
	}

	public double[][] estimateTheta() {
		double[][] theta = new double[herbs.length][K];
		for (int p = 0; p < herbs.length; p++) {
			for (int k = 0; k < K; k++) {
				theta[p][k] = (np[p][k] + alpha) / (npsum[p] + K * alpha);
			}
		}
		return theta;
	}

	public double[][] estimatePhiBar() {
		double[][] phi_bar = new double[K][S];
		for (int k = 0; k < K; k++) {
			for (int s = 0; s < S; s++) {
				phi_bar[k][s] = (ns[s][k] + beta_bar) / (nssum[k] + S * beta_bar);
			}
		}
		return phi_bar;
	}

	public double[][][] estimatePhi() {
		double[][][] phi = new double[K][X][H];
		for (int k = 0; k < K; k++) {
			for (int r = 0; r < X; r++) {
				for (int h = 0; h < H; h++) {

					phi[k][r][h] = (kxh[k][r][h] + beta) / (nhsum[k][r] + H * beta);

				}
			}
		}
		return phi;
	}

	public double[][] estimatePsi() {
		double[][] psi = new double[K][X];
		for (int k = 0; k < K; k++) {
			for (int r = 0; r < X; r++) {
				psi[k][r] = (nhsum[k][r] + eta) / (nxsum[k] + X * eta);
			}
		}
		return psi;
	}

}
