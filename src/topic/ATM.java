package topic;

public class ATM {

	int[][] documents;

	int[][] authors;

	int V;

	int A;

	int K;

	double alpha;

	double beta;

	int[][] z;

	int[][] x;

	int[][] nw;

	int[] nwsum;

	int[][] at;

	int[] atsum;

	int iterations;

	public ATM(int[][] documents, int[][] authors, int V, int A) {

		this.documents = documents;
		this.authors = authors;
		this.V = V;
		this.A = A;

	}

	public void initialState() {

		int D = documents.length;

		nw = new int[V][K];
		nwsum = new int[K];

		at = new int[A][K];

		atsum = new int[A];

		z = new int[D][];

		x = new int[D][];

		for (int d = 0; d < D; d++) {

			int Nd = documents[d].length;
			z[d] = new int[Nd];
			x[d] = new int[Nd];

			for (int n = 0; n < Nd; n++) {

				int topic = (int) (Math.random() * K);
				z[d][n] = topic;

				updateTopicWordCount(topic, documents[d][n], +1);

				int author = (int) (Math.random() * A);
				x[d][n] = author;

				updateAuthorTopicCount(author, topic, +1);

			}
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
			gibbs();
		}

	}

	public void gibbs() {

		for (int d = 0; d < documents.length; d++) {

			for (int n = 0; n < z[d].length; n++) {

				int[] topic_author = sampleFullConditionalTopicAuthor(d, n);

				z[d][n] = topic_author[0];
				x[d][n] = topic_author[1];
			}

		}

	}

	int[] sampleFullConditionalTopicAuthor(int d, int n) {

		int[] topic_author = new int[2];

		int topic = z[d][n];

		updateTopicWordCount(topic, documents[d][n], -1);

		int author = x[d][n];

		updateAuthorTopicCount(author, topic, -1);

		double[][] pr = new double[K][A];

		for (int k = 0; k < K; k++) {

			for (int a = 0; a < A; a++) {

				pr[k][a] = (nw[documents[d][n]][k] + beta) / (nwsum[k] + V * beta) * (at[a][k] + alpha)
						/ (atsum[a] + K * alpha);

			}

		}

		double[] pr_sum = new double[K * A];

		for (int k = 0; k < K; k++) {

			for (int a = 0; a < A; a++) {
				pr_sum[k * A + a] = pr[k][a];
			}

		}

		int index = sample(pr_sum);

		topic = index / A;
		author = index % A;

		topic_author[0] = topic;
		topic_author[1] = author;

		updateAuthorTopicCount(author, topic, +1);

		updateTopicWordCount(topic, documents[d][n], +1);

		return topic_author;
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

	void updateTopicWordCount(int topic, int word, int flag) {

		nw[word][topic] += flag;
		nwsum[topic] += flag;
	}

	void updateAuthorTopicCount(int author, int topic, int flag) {

		at[author][topic] += flag;
		atsum[author] += flag;
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

	public double[][] estimateTheta() {

		double[][] theta = new double[A][K];

		for (int a = 0; a < A; a++) {

			for (int k = 0; k < K; k++) {
				theta[a][k] = (at[a][k] + alpha) / (atsum[a] + K * alpha);
			}

		}

		return theta;
	}

}
