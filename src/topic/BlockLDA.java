package topic;

public class BlockLDA {

	int[][] documents;

	int[][] entities;

	int V;

	int E;

	int K;

	double alpha;

	double alpha_l;

	double beta;

	double beta_bar;

	int[][] z;

	int[][] z_bar;

	int[][] nw;

	int[][] ne;

	int[][] nd;

	int[][] links;

	int[][] nlinks;

	int[][] z_links;

	int[] nwsum;

	int[] nesum;

	int[] ndsum;

	int iterations;

	public BlockLDA(int[][] documents, int[][] entities, int V, int E, int[][] links) {

		this.documents = documents;
		this.entities = entities;
		this.V = V;
		this.E = E;

		this.links = links;
	}

	public void initialState() {

		int D = documents.length;

		nd = new int[D][K];
		ndsum = new int[D];

		nw = new int[V][K];
		nwsum = new int[K];

		ne = new int[E][K];
		nesum = new int[K];

		nlinks = new int[K][K];

		z = new int[D][];
		z_bar = new int[D][];

		for (int d = 0; d < D; d++) {

			// words
			int Nd = documents[d].length;
			z[d] = new int[Nd];

			for (int n = 0; n < Nd; n++) {

				int topic = (int) (Math.random() * K);
				z[d][n] = topic;

				updateCount(d, topic, documents[d][n], +1);
			}

			// entities
			int Ed = entities[d].length;
			z_bar[d] = new int[Ed];

			for (int m = 0; m < Ed; m++) {

				int topic = (int) (Math.random() * K);
				z_bar[d][m] = topic;

				updateEntityCount(d, topic, entities[d][m], +1);
			}

		}
		// links

		z_links = new int[links.length][];

		for (int i = 0; i < links.length; i++) {

			z_links[i] = new int[links[i].length];

			int topic = (int) (Math.random() * K * K);

			z_links[i][0] = topic / K;

			z_links[i][1] = topic % K;

			updateLinkEntityCount(z_links[i][0], z_links[i][1], links[i], +1);

		}

	}

	public void markovChain(int K, double alpha, double beta, double beta_bar, double alpha_l, int iterations) {

		this.K = K;
		this.alpha = alpha;
		this.beta = beta;
		this.beta_bar = beta_bar;
		this.alpha_l = alpha_l;
		this.iterations = iterations;

		initialState();

		for (int i = 0; i < this.iterations; i++) {

			System.out.println("iteration : " + i);
			gibbs();
		}
	}

	public void gibbs() {

		for (int d = 0; d < documents.length; d++) {

			// words
			for (int n = 0; n < z[d].length; n++) {

				int topic = sampleFullConditional(d, n);
				z[d][n] = topic;

			}
			// entities
			for (int m = 0; m < z_bar[d].length; m++) {

				int topic = sampleFullConditionalEntity(d, m);
				z_bar[d][m] = topic;

			}
		}

		// links

		for (int i = 0; i < links.length; i++) {

			int[] topic_pair = sampleFullConditionalLink(i);

			z_links[i][0] = topic_pair[0];
			z_links[i][1] = topic_pair[1];

		}
	}

	int sampleFullConditional(int d, int n) {

		int topic = z[d][n];

		updateCount(d, topic, documents[d][n], -1);

		double[] p = new double[K];

		for (int k = 0; k < K; k++) {

			p[k] = (nd[d][k] + alpha) / (ndsum[d] + K * alpha) * (nw[documents[d][n]][k] + beta)
					/ (nwsum[k] + V * beta);
		}

		topic = sample(p);

		updateCount(d, topic, documents[d][n], +1);

		return topic;

	}

	int sampleFullConditionalEntity(int d, int m) {

		int topic = z_bar[d][m];

		updateEntityCount(d, topic, entities[d][m], -1);

		double[] p = new double[K];

		for (int k = 0; k < K; k++) {

			p[k] = (nd[d][k] + alpha) / (ndsum[d] + K * alpha) * (ne[entities[d][m]][k] + beta_bar)
					/ (nesum[k] + E * beta_bar);
		}
		topic = sample(p);

		updateEntityCount(d, topic, entities[d][m], +1);

		return topic;

	}

	int[] sampleFullConditionalLink(int i) {

		int topic_0 = z_links[i][0];

		int topic_1 = z_links[i][1];

		int[] topic_pair = new int[2];

		updateLinkEntityCount(topic_0, topic_1, links[i], -1);

		double[][] p = new double[K][K];

		for (int k = 0; k < K; k++) {

			for (int r = 0; r < K; r++) {

				p[k][r] = (nlinks[k][r] + alpha_l) * (nw[links[i][0]][k] + beta) / (nwsum[k] + V * beta)
						* (nw[links[i][1]][r] + beta) / (nwsum[r] + V * beta);

			}

		}

		double[] pr_sum = new double[K * K];

		for (int k = 0; k < K; k++) {

			for (int r = 0; r < K; r++) {
				pr_sum[k * K + r] = p[k][r];
			}

		}

		int index = sample(pr_sum);

		topic_0 = index / K;
		topic_1 = index % K;

		updateLinkEntityCount(topic_0, topic_1, links[i], +1);

		topic_pair[0] = topic_0;
		topic_pair[1] = topic_1;

		return topic_pair;

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

	void updateCount(int d, int topic, int word, int flag) {

		nd[d][topic] += flag;
		ndsum[d] += flag;

		nw[word][topic] += flag;
		nwsum[topic] += flag;
	}

	void updateEntityCount(int d, int topic, int entity, int flag) {

		nd[d][topic] += flag;
		ndsum[d] += flag;

		ne[entity][topic] += flag;
		nesum[topic] += flag;
	}

	void updateLinkEntityCount(int topic_0, int topic_1, int[] link, int flag) {

		nw[link[0]][topic_0] += flag;
		nwsum[topic_0] += flag;

		nw[link[1]][topic_1] += flag;
		nwsum[topic_1] += flag;

		nlinks[topic_0][topic_1] += flag;

	}

	public double[][] estimateTheta() {
		double[][] theta = new double[documents.length][K];
		for (int d = 0; d < documents.length; d++) {
			for (int k = 0; k < K; k++) {
				theta[d][k] = (nd[d][k] + alpha) / (ndsum[d] + K * alpha);
			}
		}
		return theta;
	}

	public double[][] estimatePhi() {
		double[][] phi = new double[K][V];
		for (int k = 0; k < K; k++) {
			for (int w = 0; w < V; w++) {
				phi[k][w] = (nw[w][k] + beta) / (nwsum[k] + V * beta);
			}
		}
		return phi;
	}

	public double[][] estimatePhiBar() {
		double[][] phi_bar = new double[K][E];
		for (int k = 0; k < K; k++) {
			for (int e = 0; e < E; e++) {
				phi_bar[k][e] = (ne[e][k] + beta_bar) / (nesum[k] + E * beta_bar);
			}
		}
		return phi_bar;
	}

}
