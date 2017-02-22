package topic;

/**
 * COM, In KDD 2014
 * 
 * @author: Liang Yao
 * @email yaoliang@zju.edu.cn
 */

public class COM {

	int[][] users;

	int[] items;

	int K;

	int U;

	int I;

	int[][] zu;

	int[] nzusum;

	int[][] ui;

	int[] nuisum;

	int[][] zi;

	int[] nzisum;

	int[][] gz;

	int[] ngzsum;

	int[][] uc;

	double alpha;

	double beta;

	double gamma;

	double rou;

	double eta;

	int[][] z;

	int[][] c;

	int iterations;

	public COM(int[][] users, int[] items, int U, int I) {

		this.users = users;

		this.items = items;

		this.U = U;

		this.I = I;

	}

	public void initialState() {

		int G = users.length;

		gz = new int[G][K];

		ngzsum = new int[G];

		zu = new int[K][U];

		nzusum = new int[K];

		ui = new int[U][I];

		nuisum = new int[U];

		zi = new int[K][I];

		nzisum = new int[K];

		uc = new int[U][2];

		z = new int[G][];

		c = new int[G][];

		for (int g = 0; g < G; g++) {

			int group_size = users[g].length;

			z[g] = new int[group_size];

			c[g] = new int[group_size];

			for (int gn = 0; gn < group_size; gn++) {

				int c_assign = (int) (Math.random() * 2);

				int topic = (int) (Math.random() * K);

				c[g][gn] = c_assign;

				z[g][gn] = topic;

				if (c_assign == 0) {
					updateCountSwitch(users[g][gn], items[g], +1);
					updateCountTopic(g, topic, users[g][gn], +1);
				} else {
					updateCountSwitch(users[g][gn], items[g], topic, +1);

					updateCountTopic(g, topic, users[g][gn], items[g], +1);
				}

			}

		}

	}

	public void markovChain(int K, double alpha, double beta, double gamma, double rou, double eta, int iterations) {

		this.K = K;
		this.alpha = alpha;
		this.beta = beta;

		this.gamma = gamma;
		this.rou = rou;
		this.eta = eta;
		this.iterations = iterations;

		initialState();

		for (int i = 0; i < this.iterations; i++) {

			System.out.println("iteration : " + i);
			gibbs();
		}

	}

	public void gibbs() {

		for (int g = 0; g < users.length; g++) {

			// Step 1, for topics z
			for (int gn = 0; gn < users[g].length; gn++) {

				int topic = sampleFullConditionalTopic(g, gn);

				z[g][gn] = topic;

			}
			// Step 2, for switches c
			for (int gn = 0; gn < users[g].length; gn++) {

				int c_assign = sampleFullConditionalSwitch(g, gn);

				c[g][gn] = c_assign;
			}

		}

	}

	int sampleFullConditionalTopic(int g, int n) {

		int topic = z[g][n];

		if (c[g][n] == 1) {

			updateCountTopic(g, topic, users[g][n], items[g], -1);

			double[] pr = new double[K];

			for (int k = 0; k < K; k++) {

				pr[k] = (gz[g][k] + alpha) / (ngzsum[g] + K * alpha) * (zu[k][users[g][n]] + beta)
						/ (nzusum[k] + U * beta) * (zi[k][items[g]] + eta) / (nzisum[k] + I * eta);

			}

			topic = sample(pr);

			updateCountTopic(g, topic, users[g][n], items[g], +1);

		} else if (c[g][n] == 0) {

			updateCountTopic(g, topic, users[g][n], -1);

			double[] pr = new double[K];

			for (int k = 0; k < K; k++) {

				pr[k] = (gz[g][k] + alpha) / (ngzsum[g] + K * alpha) * (zu[k][users[g][n]] + beta)
						/ (nzusum[k] + U * beta);

			}

			topic = sample(pr);

			updateCountTopic(g, topic, users[g][n], +1);

		}

		return topic;

	}

	int sampleFullConditionalSwitch(int g, int n) {

		int c_assign = c[g][n];

		if (c_assign == 0) {
			updateCountSwitch(users[g][n], items[g], -1);
		} else {
			updateCountSwitch(users[g][n], items[g], z[g][n], -1);
		}

		double[] pr = new double[2];

		pr[0] = (uc[users[g][n]][0] + gamma) / (uc[users[g][n]][0] + uc[users[g][n]][1] + 2 * gamma)
				* (ui[users[g][n]][items[g]] + rou) / (nuisum[users[g][n]] + I * rou);

		pr[1] = (uc[users[g][n]][1] + gamma) / (uc[users[g][n]][0] + uc[users[g][n]][1] + 2 * gamma)
				* (zi[z[g][n]][items[g]] + eta) / (nzisum[z[g][n]] + I * eta);

		c_assign = sample(pr);

		if (c_assign == 0) {
			updateCountSwitch(users[g][n], items[g], +1);
		} else {
			updateCountSwitch(users[g][n], items[g], z[g][n], +1);
		}

		return c_assign;

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

	void updateCountTopic(int g, int topic, int user, int item, int flag) {

		gz[g][topic] += flag;
		ngzsum[g] += flag;

		zu[topic][user] += flag;
		nzusum[topic] += flag;

		zi[topic][item] += flag;
		nzisum[topic] += flag;

	}

	void updateCountTopic(int g, int topic, int user, int flag) {

		gz[g][topic] += flag;
		ngzsum[g] += flag;

		zu[topic][user] += flag;
		nzusum[topic] += flag;

	}

	void updateCountSwitch(int user, int item, int flag) {

		uc[user][0] += flag;

		ui[user][item] += flag;

		nuisum[user] += flag;

	}

	void updateCountSwitch(int user, int item, int topic, int flag) {

		uc[user][1] += flag;

		zi[topic][item] += flag;

		nzisum[topic] += flag;

	}

	public double[][] estimatePhiZU() {

		double[][] phi = new double[K][U];

		for (int k = 0; k < K; k++) {

			double prob_sum = 0;
			for (int u = 0; u < U; u++) {
				phi[k][u] = (zu[k][u] + beta) / (nzusum[k] + U * beta);

				prob_sum += phi[k][u];
			}
			if (prob_sum - 1 > 0.0001 || prob_sum - 1 < -0.0001)
				System.out.println("ZU概率之和不为1 : " + prob_sum);

		}

		return phi;
	}

	public double[][] estimatePhiUI() {

		double[][] phi = new double[U][I];

		for (int u = 0; u < U; u++) {

			double prob_sum = 0;
			for (int i = 0; i < I; i++) {
				phi[u][i] = (ui[u][i] + rou) / (nuisum[u] + I * rou);
				prob_sum += phi[u][i];

			}

			if (prob_sum - 1 > 0.0001 || prob_sum - 1 < -0.0001)
				System.out.println("UI概率之和不为1 : " + prob_sum);

		}

		return phi;
	}

	public double[][] estimatePhiZI() {

		double[][] phi = new double[K][I];

		for (int k = 0; k < K; k++) {

			double prob_sum = 0;
			for (int i = 0; i < I; i++) {

				phi[k][i] = (zi[k][i] + eta) / (nzisum[k] + I * eta);
				prob_sum += phi[k][i];
			}

			if (prob_sum - 1 > 0.0001 || prob_sum - 1 < -0.0001)
				System.out.println("ZI概率之和不为1 : " + prob_sum);

		}

		return phi;
	}

	public double[] estimateLambada() {

		double[] lambda = new double[U];

		for (int u = 0; u < U; u++) {
			lambda[u] = (uc[u][1] + gamma) / (uc[u][0] + uc[u][1] + 2 * gamma);

			double prob_sum = 0;

			prob_sum += lambda[u];

			prob_sum += (uc[u][0] + gamma) / (uc[u][0] + uc[u][1] + 2 * gamma);

			if (prob_sum - 1 > 0.0001 || prob_sum - 1 < -0.0001)
				System.out.println("Lambada概率之和不为1 : " + prob_sum);
		}

		return lambda;

	}
}
