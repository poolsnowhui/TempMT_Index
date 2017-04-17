package cn.edu.scnu.TempMT_Index.lab;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.edu.scnu.TempMT_Index.service.DQL;
import cn.edu.scnu.TempMT_Index.service.FUNCTION;
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

	public static void labProjection(int... dataCount) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String format = simpleDateFormat.format(new Date());
		String fileName = "projection_" + format + ".lab";
		FileHelper fh = FileHelper.getInstance();
		for (int i = 0; i < dataCount.length; i++) {
			double sum = 0;
			int copy = 50;
			for (int j = 0; j < copy; j++) {
				String sql = "VALIDTIME PROJECTION SELECT  * FROM student" + (dataCount[i] / 1000) + "k;";
				DQL project = new DQL();
				project.translate(sql, null);
				ReMessage reMessage = project.getReMessage();
				sum += Integer.valueOf(reMessage.getTimeExpense());
			}
			String data = dataCount[i] + " " + (double) (sum / copy) + " \n";
			fh.write(fileName, data);
		}
	}

	public static void labInterval(int... dataCount) {
		String format = FUNCTION.getCurrDate();
		String fileName = "interval_" + format + ".lab";
		FileHelper fh = FileHelper.getInstance();
		for (int i = 0; i < dataCount.length; i++) {
			double sum = 0;
			int copy = 50;
			for (int j = 0; j < copy; j++) {
				String sql = "VALIDTIME INTERVAL year > 24 SELECT * FROM student" + (dataCount[i] / 1000) + "k;";
				DQL interval = new DQL();
				interval.translate(sql, null);
				ReMessage reMessage = interval.getReMessage();
				sum += Integer.valueOf(reMessage.getTimeExpense());
			}
			String data = dataCount[i] + " " + (double) (sum / copy) + " \n";
			fh.write(fileName, data);
		}
	}

	public static void labSnap(int... dataCount) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String format = simpleDateFormat.format(new Date());
		String fileName = "snap_" + format + ".lab";
		FileHelper fh = FileHelper.getInstance();
		for (int i = 0; i < dataCount.length; i++) {
			double sum = 0;
			int copy = 50;
			for (int j = 0; j < copy; j++) {
				String sql = "VALIDTIME SNAPSHOT SELECT * FROM student" + (dataCount[i] / 1000) + "k;";
				DQL snap = new DQL();
				snap.translate(sql, null);
				ReMessage reMessage = snap.getReMessage();
				sum += Integer.valueOf(reMessage.getTimeExpense());
			}
			String data = dataCount[i] + " " + (double) (sum / copy) + " \n";
			fh.write(fileName, data);
		}
	}

	public static void labPeriodMysql(int strategy[], int... dataCount) {
		LOB period = new LOB();
		double[] interval = { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6 };
		String format = FUNCTION.getCurrDate();
		FileHelper fh = FileHelper.getInstance();
		// List<ArrayList<Tuple>> result = new ArrayList<>();
		// ArrayList<Tuple> alTuple = new ArrayList<>();
		// ImportAndExportDataBase importAndExportDataBase = new
		// ImportAndExportDataBase();
		for (int i = 0; i < dataCount.length; i++) {
			// 1.从数据库中来
			String tableName = "student" + (dataCount[i] / 1000) + "k";
			// importAndExportDataBase = new ImportAndExportDataBase();
			// importAndExportDataBase.setDataCount(dataCount[i]);
			// alTuple = importAndExportDataBase.ExportD();
			// result = period.create(alTuple);
			int copy = 50;
			for (int j = 0; j < interval.length; j++) {
				ValidTime[] query = new ValidTime[copy];
				try {
					long vtMax = new SimpleDateFormat("YY-MM-DD").parse("2030-04-01").getTime();
					long vtMin = new SimpleDateFormat("YY-MM-DD").parse("1980-04-01").getTime();

					for (int k = 0; k < copy; k++) {// 随机生成repeatCount个查询窗口
						long qvts = (long) ((vtMax - vtMin) * (1 - interval[j]) * Math.random() + vtMin);
						long qvte = (long) (qvts + (vtMax - vtMin) * interval[j]);
						query[k] = new ValidTime(qvts, qvte);
					} // copy个查询窗口
				} catch (ParseException e) {
					e.printStackTrace();
				}
				for (int m = 0; m < strategy.length; m++) {
					String fileName = "period_" + strategy[m] + "_" + format + ".lab";
					double sum = 0;
					String data = "";
					for (int k = 0; k < copy; k++) {
						switch (strategy[m]) {
						case 0:
							period.queryNoLOB(tableName, query[k]);
							// case 8:
							// period.queryTraversal(result, query[k]);
							// break;
							// case 9:
							// period.queryBinaryChop(result, query[k]);
							// break;
							// case 10:
							// period.querySequence(result, query[k]);
							// break;
						default:
							break;
						}
						System.out.println(period.getTime()[4]);
						sum += period.getTime()[4];
					}
					data += dataCount[i] + " " + interval[j] + " " + " " + strategy[m] + " " + (double) (sum / copy)
							+ "\n";
					System.out.println((double) sum / copy + "ms");
					fh.write(fileName, data);
				}

			}
		}

	}

	public static void labDisk(int... dataCount) {
		LOB disk = new LOB();
		double[] interval = { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6 };
		int[] strategy = { 8 };
		String format = FUNCTION.getCurrDate();
		FileHelper fh = FileHelper.getInstance();
		for (int i = 0; i < dataCount.length; i++) {
			String tableName = "student" + (dataCount[i] / 1000) + "k";
			disk.setFromDisk(true);
			disk.setTableName(tableName);
			int copy = 50;
			for (int j = 0; j < interval.length; j++) {
				ValidTime[] query = new ValidTime[copy];
				try {
					long vtMax = new SimpleDateFormat("YY-MM-DD").parse("2030-04-01").getTime();
					long vtMin = new SimpleDateFormat("YY-MM-DD").parse("1980-04-01").getTime();

					for (int k = 0; k < copy; k++) {// 随机生成repeatCount个查询窗口
						long qvts = (long) ((vtMax - vtMin) * (1 - interval[j]) * Math.random() + vtMin);
						long qvte = (long) (qvts + (vtMax - vtMin) * interval[j]);
						query[k] = new ValidTime(qvts, qvte);
					} // copy个查询窗口
				} catch (ParseException e) {
					e.printStackTrace();
				}
				for (int m = 0; m < strategy.length; m++) {
					disk.setStrategy(strategy[m]);
					String fileName = "period_disk_" + strategy[m] + "_" + format + ".lab";
					double sum = 0;
					String data = "";
					for (int k = 0; k < copy; k++) {
						disk.setQuery(query[k]);
						disk.queryFromDisk(query[k]);
						// disk.queryProcess();
						System.out.println(disk.getTime()[3] + disk.getTime()[4]);
						sum += disk.getTime()[3] + disk.getTime()[4];
					}
					data += dataCount[i] + " " + interval[j] + " " + " " + strategy[m] + " "
							+ (double) (double) (sum / copy) + "\n";
					System.out.println((double) sum / copy + "ms");
					fh.write(fileName, data);
				}
			}
		}
	}

	/**
	 * 
	 * @param strategy
	 * @param dataCount
	 */
	public static void labPeriod(int strategy[], int... dataCount) {
		LOB period = new LOB();
		double[] interval = { 0.1 };
		String format = FUNCTION.getCurrDate();
		FileHelper fh = FileHelper.getInstance();
		List<ArrayList<Tuple>> result = new ArrayList<>();
		ArrayList<Tuple> alTuple = new ArrayList<>();
		ImportAndExportDataBase importAndExportDataBase = new ImportAndExportDataBase();
		for (int i = 0; i < dataCount.length; i++) {
			// 1.从数据库中来
			// String tableName = "student" + (dataCount[i] / 1000) + "k";
			importAndExportDataBase = new ImportAndExportDataBase();
			importAndExportDataBase.setDataCount(dataCount[i]);
			alTuple = importAndExportDataBase.ExportD();
			result = period.create(alTuple);
			int copy = 50;
			for (int j = 0; j < interval.length; j++) {
				ValidTime[] query = new ValidTime[copy];
				try {
					long vtMax = new SimpleDateFormat("YY-MM-DD").parse("2030-04-01").getTime();
					long vtMin = new SimpleDateFormat("YY-MM-DD").parse("1980-04-01").getTime();

					for (int k = 0; k < copy; k++) {// 随机生成repeatCount个查询窗口
						long qvts = (long) ((vtMax - vtMin) * (1 - interval[j]) * Math.random() + vtMin);
						long qvte = (long) (qvts + (vtMax - vtMin) * interval[j]);
						query[k] = new ValidTime(qvts, qvte);
					} // copy个查询窗口
				} catch (ParseException e) {
					e.printStackTrace();
				}
				for (int m = 0; m < strategy.length; m++) {
					String fileName = "period_memory_" + format + ".lab";
					double sum = 0;
					String data = "";
					for (int k = 0; k < copy; k++) {
						switch (strategy[m]) {
						// case 0:
						// period.queryNoLOB(tableName, query[k]);
						case 8:
							period.queryTraversal(result, query[k]);
							break;
						case 9:
							period.queryBinaryChop(result, query[k]);
							break;
						case 10:
							period.querySequence(result, query[k]);
							break;
						default:
							break;
						}
						System.out.println(period.getTime()[4]);
						sum += period.getTime()[4];
					}
					data += dataCount[i] + " " + interval[j] + " " + " " + strategy[m] + " " + (double) (sum / copy)
							+ "\n";
					System.out.println((double) sum / copy + "ms");
					fh.write(fileName, data);
				}

			}
		}

	}

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

		// double interval ={0.1};
		// int threadCount[] ={1,2,4,8};
		// int dataCount[] ={5000000};
		LOB thread = new LOB();
		thread.setSql(null);
		int currThread = Runtime.getRuntime().availableProcessors();
		int copy = 50;// 500条区间
		String format = FUNCTION.getCurrDate();
		String fileName = "";
		FileHelper fh = FileHelper.getInstance();
		int[] strategy = { 12, 13, 14, 15 };
		for (int i = 0; i < dataCount.length; i++) {
			// 1.从数据库中来
			ImportAndExportDataBase importAndExportDataBase = new ImportAndExportDataBase();
			importAndExportDataBase.setDataCount(dataCount[i]);
			ArrayList<Tuple> alTuple = importAndExportDataBase.ExportD();
			List<ArrayList<Tuple>> result = thread.create(alTuple);
			for (int j = 0; j < interval.length; j++) {
				ValidTime[] query = new ValidTime[copy];
				try {
					long vtMax = new SimpleDateFormat("YY-MM-DD").parse("2030-04-01").getTime();
					long vtMin = new SimpleDateFormat("YY-MM-DD").parse("1980-04-01").getTime();

					for (int k = 0; k < copy; k++) {// 随机生成repeatCount个查询窗口
						long qvts = (long) ((vtMax - vtMin) * (1 - interval[j]) * Math.random() + vtMin);
						long qvte = (long) (qvts + (vtMax - vtMin) * interval[j]);
						query[k] = new ValidTime(qvts, qvte);
					} // copy个查询窗口
				} catch (ParseException e) {
					e.printStackTrace();
				}
				double sum = 0;
				String data = "";
				for (int m = 0; m < strategy.length; m++) {
					fileName = "period_thread_" + currThread + "_" + format + ".lab";
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
								+ (double) (sum / copy) + "\n";
						fh.write(fileName, data);
					}

				}
			}
		}

	}

	public static void main(String[] args) {
		int c = 1000000;
		// int[] s = { 0 };
		// int[] s8910 = { 8, 9, 10 };
		double[] interval = { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6 };
		int threadCount[] = { 8 };
		for (int i = 1; i <= 6; i++) {
			// labInterval(i * c );
			// System.out.println("labInterval " + (i * c ));
			// labDisk(i * c);
			// System.out.println("labDisk " + (i * c));
			// labPeriodMysql(s, i * c);
			// System.out.println("labPeriod " + (i * c));
			// labDisk(i * c);
			// System.out.println("labDisk " + (i * c));
			// labPeriod(s8910, i * c);
			// System.out.println("labPeriod " + (i * c));
			// labProjection(i * c );
			// System.out.println("labProjection " + (i * c ));
			// labSnap(i * c );
			// System.out.println("labSnap " + (i * c ));
			labQueryThread(interval, threadCount, i * c);
			System.out.println("labQueryThread " + (i * c));
		}

	}
}
