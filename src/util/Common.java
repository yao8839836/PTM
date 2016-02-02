package util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Common {

	/**
	 * 返回数组中最大元素的下标
	 * 
	 * @param array
	 *            输入数组
	 * @return 最大元素的下标
	 */
	public static int maxIndex(double[] array) {
		double max = array[0];
		int maxIndex = 0;
		for (int i = 1; i < array.length; i++) {
			if (array[i] > max) {
				max = array[i];
				maxIndex = i;
			}

		}
		return maxIndex;

	}

	/**
	 * 返回数组中最小元素的下标
	 * 
	 * @param array
	 *            输入数组
	 * @return 最小元素的下标
	 */
	public static int minIndex(double[] array) {
		double min = array[0];
		int minIndex = 0;
		for (int i = 1; i < array.length; i++) {
			if (array[i] < min) {
				min = array[i];
				minIndex = i;
			}

		}
		return minIndex;

	}

	/**
	 * 返回数组中的最小值
	 * 
	 * @param array
	 *            输入数组
	 * @return
	 */
	public static double min(double[] array) {

		double min = array[0];

		for (int i = 0; i < array.length; i++) {
			if (array[i] < min)
				min = array[i];
		}

		return min;

	}

	/**
	 * 复制矩阵
	 * 
	 * @param array
	 *            矩阵
	 * @return
	 */
	public static double[][] makeCopy(double[][] array) {

		double[][] copy = new double[array.length][];

		for (int i = 0; i < copy.length; i++) {

			copy[i] = new double[array[i].length];

			for (int j = 0; j < copy[i].length; j++) {
				copy[i][j] = array[i][j];
			}
		}

		return copy;
	}

	/**
	 * 对象写文件
	 * 
	 * @param obj
	 * @throws IOException
	 */
	public static void writeObject(Object obj, String filename) throws IOException {

		FileOutputStream fos = new FileOutputStream(filename);
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		oos.writeObject(obj);
		oos.close();
		fos.close();

	}

	/**
	 * 读对象文件
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object readObject(String filename) throws IOException, ClassNotFoundException {

		FileInputStream fis = new FileInputStream(filename);
		ObjectInputStream ois = new ObjectInputStream(fis);
		Object obj = ois.readObject();

		ois.close();
		fis.close();

		return obj;
	}

}
