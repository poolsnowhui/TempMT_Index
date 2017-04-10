package cn.edu.scnu.TempMT_Index.lab;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.edu.scnu.TempMT_Index.service.DQL;
import cn.edu.scnu.TempMT_Index.service.LOB;
import cn.edu.scnu.TempMT_Index.service.ReMessage;
import cn.edu.scnu.TempMT_Index.service.Tuple;
import cn.edu.scnu.TempMT_Index.service.ValidTime;

/**
 * 实验
 * 
 * @author chixh
 *
 */
public class Lab {

	/**
	 * 
	 * @param dataCount
	 * @param interval
	 * @param repeatCount
	 * @param threadCount
	 *            平均策略划分 交叉策略划分 前部平均策略划分 前部交叉策略划分
	 * 
	 */
	public static void labQueryThread(double interval[], int threadCount[], int... dataCount) {

		// double interval ={0.1,0.2};
		// int threadCount[] ={1,2,4,8};
		// int dataCount[] ={1000000,2000000,3000000};
		LOB thread = new LOB();
		thread.setSql(null);

		int currThread = Runtime.getRuntime().availableProcessors();
		int copy = 500;// 500条区间
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String format = simpleDateFormat.format(new Date());
		String fileName = "thread_" + currThread + "_" + format + ".lab";
		FileHelper fh = FileHelper.getInstance();
		int[] strategy = { 12, 13, 14, 15 };
		for (int i = 0; i < dataCount.length; i++) {
			// 1.从数据库中来
			ImportAndExportDataBase importAndExportDataBase = new ImportAndExportDataBase();
			importAndExportDataBase.setDataCount(dataCount[i]);
			ArrayList<Tuple> alTuple = importAndExportDataBase.ExportD();
			List<ArrayList<Tuple>> result = thread.create(alTuple);
			for (int j = 0; j < interval.length; j++) {
				long vtMax = result.get(0).get(0).getRight();
				long vtMin = result.get(0).get(0).getLeft();
				ValidTime[] query = new ValidTime[copy];
				for (int k = 0; k < copy; k++) {// 随机生成repeatCount个查询窗口
					long qvts = (long) ((vtMax - vtMin) * (1 - interval[j]) * Math.random() + vtMin);
					long qvte = (long) (qvts + (vtMax - vtMin) * interval[j]);
					query[k] = new ValidTime(qvts, qvte);
				} // 500个查询窗口
				int sum = 0;
				String data = "";
				for (int m = 0; m < strategy.length; m++) {
					for (int l = 0; l < threadCount.length; l++) {
						thread.setThreadCount(threadCount[l]);
						sum = 0;
						data = "";
						for (int k = 0; k < copy; k++) {
							long after = System.currentTimeMillis();
							switch (strategy[m]) {
							case 12:
								thread.queryThreadSeries(result, query[k]);
								break;
							case 13:
								thread.queryThreadCross(result, query[k]);
								break;
							case 14:
								thread.queryThreadFrontSeries(result, query[k]);
								break;
							case 15:
								thread.queryThreadFrontCross(result, query[k]);
								break;
							default:
								break;
							}
							long expense = System.currentTimeMillis() - after;
							sum += expense;
						}
						data += dataCount[i] + " " + interval[j] + " " + threadCount[l] + " " + strategy[m] + " "
								+ (sum / copy) + "/n";
						fh.write(fileName, data);
					}
					
				}
			}
		}

	}

	public static void labProjection(int... dataCount) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String format = simpleDateFormat.format(new Date());
		String fileName = "projection_" + format + ".lab";
		FileHelper fh = FileHelper.getInstance();
		for (int i = 0; i < dataCount.length; i++) {
			int sum = 0;
			int copy = 500;
			for (int j = 0; j < copy; j++) {
				String sql = "VALIDTIME PROJECTION SELECT  * FROM student" + (dataCount[i] / 1000) + "k;";
				DQL project = new DQL();
				project.translate(sql, null);
				ReMessage reMessage = project.getReMessage();
				sum += Integer.valueOf(reMessage.getTimeExpense());
			}
			String data = dataCount[i] + " " + (sum / copy) + " \n";
			fh.write(fileName, data);
		}
	}

	public static void labInterval(int... dataCount) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String format = simpleDateFormat.format(new Date());
		String fileName = "interval_" + format + ".lab";
		FileHelper fh = FileHelper.getInstance();
		for (int i = 0; i < dataCount.length; i++) {
			int sum = 0;
			int copy = 500;
			for (int j = 0; j < copy; j++) {
				String sql = "VALIDTIME INTERVAL year > 24 SELECT * FROM student" + (dataCount[i] / 1000) + "k;";
				DQL interval = new DQL();
				interval.translate(sql, null);
				ReMessage reMessage = interval.getReMessage();
				sum += Integer.valueOf(reMessage.getTimeExpense());
			}
			String data = dataCount[i] + " " + (sum / copy) + " \n";
			fh.write(fileName, data);
		}
	}

	public static void labSnap(int... dataCount) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String format = simpleDateFormat.format(new Date());
		String fileName = "snap_" + format + ".lab";
		FileHelper fh = FileHelper.getInstance();
		for (int i = 0; i < dataCount.length; i++) {
			int sum = 0;
			int copy = 500;
			for (int j = 0; j < copy; j++) {
				String sql = "VALIDTIME SNAPSHOT SELECT * FROM student" + (dataCount[i] / 1000) + "k;";
				DQL snap = new DQL();
				snap.translate(sql, null);
				ReMessage reMessage = snap.getReMessage();
				sum += Integer.valueOf(reMessage.getTimeExpense());
			}
			String data = dataCount[i] + " " + (sum / copy) + " \n";
			fh.write(fileName, data);
		}
	}

	public static void labPeriod(int strategy, int... dataCount) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String format = simpleDateFormat.format(new Date());
		String fileName = "period_" + strategy + "_" + format + ".lab";
		FileHelper fh = FileHelper.getInstance();
		for (int i = 0; i < dataCount.length; i++) {
			int sum = 0;
			int copy = 500;
			for (int j = 0; j < copy; j++) {
				String sql = "VALIDTIME PERIOD [ DATE '2013-1-1' - DATE '2016-1-1' ] SELECT * FROM student"
						+ (dataCount[i] / 1000) + "k;";
				DQL period = new DQL();
				period.setStrategy(strategy);
				period.translate(sql, null);
				ReMessage reMessage = period.getReMessage();
				sum += Integer.valueOf(reMessage.getTimeExpense());
			}
			String data = dataCount[i] + " " + (sum / copy) + " \n";
			fh.write(fileName, data);
		}
	}

	public static void main(String[] args) {
		int c = 20000;
		for (int i = 46; i < 50; i++) {
//			labInterval(i * c + c);
//			System.out.println("labInterval " + (i * c + c));
//			labPeriod(0, i * c + c);
//			System.out.println("labPeriod " + (i * c + c));
			labProjection(i * c + c);
			System.out.println("labProjection " + (i * c + c));
//			labSnap(i * c + c);
//			System.out.println("labSnap " + (i * c + c));
		}
	}
}
