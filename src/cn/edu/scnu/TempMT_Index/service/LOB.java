package cn.edu.scnu.TempMT_Index.service;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import cn.edu.scnu.TempMT_Index.dao.DAOFactory;
import cn.edu.scnu.TempMT_Index.lab.FileHelper;

/**
 * @author CXH
 *
 */
public class LOB {
	// 初始
	public static final String indexFolder = CONTANT.indexPath;// 索引磁盘存放路径
	private String labRecordFile = "-result" + ".data";// 存放的数据
	private String indexType;// 索引类型：磁盘索引（二分）索引类型：磁盘索引（非二分）索引类型：内存索引（二分）索引类型：内存索引（非二分）

	private int strategy = 10;// 默认单线程分支内部二分
	private ValidTime query;// 查询区间
	private String sql = "";// 不带时态的常规SQL（暂时针对内存实时查询）
	private String tableName = "student";// 默认表前缀名
	private long sourceCount;// 原记录数
	private boolean isFromDisk = false;// 查询时是使用磁盘索引查，默认不使用
	private int threadCount = Runtime.getRuntime().availableProcessors();// 线程个数默认为本机个数；
	// 内部
	private long branchCount;// 分支个数
	// 结果
	private long[] time = new long[5];// 各阶段时间
	private long expense;// 时间开销
	private int resultNum;// 结果个数

	/**
	 * 0 SQL查 1 时态索引 0SQL 8单线程 12线程 14前部 10 单线程 11多线程 1001 分支内部遍历
	 * 1010分支内部二分1011分支起始序列 110实际 111前部 1100连续 1101交叉 1110前部连续 1111前部交叉
	 */
	public LOB() {
	}

	public LOB(String sql, ValidTime query, String tableName) {
		this.sql = sql;
		this.query = query;
		this.tableName = tableName;
	}

	/**
	 * @param sql
	 * @param query
	 * @param tableName
	 * @param isLOB
	 * @param isDichotomy
	 * @param isSequence
	 * @param isFromDisk
	 * @param isThread
	 * @param threadCount
	 */
	public LOB(String sql, ValidTime query, String tableName, boolean isLOB, boolean isDichotomy, boolean isSequence,
			boolean isFromDisk, boolean isThread, int threadCount) {
		super();
		this.sql = sql;
		this.query = query;
		this.tableName = tableName;
		this.isFromDisk = isFromDisk;
		this.threadCount = threadCount;
	}

	public LOB(int strategy, String sql, ValidTime query, String tableName, boolean isFromDisk, int threadCount) {
		super();
		this.strategy = strategy;
		this.sql = sql;
		this.query = query;
		this.tableName = tableName;
		this.isFromDisk = isFromDisk;
		this.threadCount = threadCount;
	}

	/**
	 * 0.SQL查
	 * 
	 * @return
	 */
	private List<Tuple> sqlQuery() {
		long start = System.currentTimeMillis();
		try {
			if (sql == "")
				sql = "select * from " + tableName;
			List<Object[]> source;
			try {
				source = DAOFactory.getInstance().executeQuery(sql);
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
			List<Tuple> alTuple = new ArrayList<Tuple>();
			for (int i = 1; i < source.size(); i++) {
				alTuple.add(new Tuple(source.get(i)));
			}
			time[0] = System.currentTimeMillis() - start;
			return alTuple;
		} catch (Exception e) {
			e.printStackTrace();// 控制台输出异常，供开发使用
			throw new RuntimeException(e.getMessage());// 抛出运行异常，供错误提示使用
		}
	}

	/**
	 * 1.建索引（LOB）
	 * 
	 * @param alTuple
	 * @return
	 */
	public List<ArrayList<Tuple>> create(List<Tuple> alTuple) {
		long start = System.currentTimeMillis();
		sourceCount = alTuple.size();
		// 开始构造线序
		// System.out.println("开始构造线序");
		// long st = System.currentTimeMillis();
		Tuple[] arTuple = (Tuple[]) alTuple.toArray(new Tuple[alTuple.size()]);// ArrayList变成数组
		alTuple = null;
		Arrays.sort(arTuple);// 深度排序（Tuple中的toCompare的特性）

		List<ArrayList<Tuple>> result = new ArrayList<ArrayList<Tuple>>();// 多个分支
		List<Tuple> p = new ArrayList<Tuple>();
		boolean flag; // 为true说明分支结束了，为false说明分支还没结束
		for (int i = 0; i < arTuple.length; i++) {
			flag = true;
			for (int j = 0; j < result.size(); j++) {
				p = result.get(j);
				// 单个分支构造
				if (arTuple[i].getVt().getRight() <= p.get(p.size() - 1).getVt().getRight()) {// 是否达到分支的结尾
					p.add(arTuple[i]);// 添加一条记录进分支里
					// System.out.println(arTuple[i]);
					flag = false;
					break;
				}
			}
			// 建立新分支
			if (flag) {
				ArrayList<Tuple> temp = new ArrayList<Tuple>();
				temp.add(arTuple[i]);// 添加一条记录进分支里
				result.add(temp);
			}
		}
		// System.out.println("结束构造线序！" + (System.currentTimeMillis() - st) +
		// "ms");
		branchCount = result.size();
		time[1] = System.currentTimeMillis() - start;
		return result;
	}

	/**
	 * 2.索引存磁盘
	 * 
	 * @return
	 */
	private boolean storeToDisk(List<ArrayList<Tuple>> alalTuple) {
		long start = System.currentTimeMillis();
		// 存磁盘
		// String lobindex = System.getProperty("user.dir") +
		// "\\WebRoot\\lobindex";// 存盘路径
		System.out.println("索引磁盘路径" + indexFolder);
		// 写文件
		DataOutputStream dos = null;
		try {
			File file = new File(indexFolder, tableName + ".idx");
			file.getParentFile().mkdirs();// 建目录
			file.createNewFile();// 建文件
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			dos = new DataOutputStream(bos);
			String data = "";
			for (int i = 0, len = alalTuple.size(); i < len; i++) {
				data = i + "\n";
				dos.writeBytes(data);
				for (int j = 0, lenj = alalTuple.get(i).size(); j < lenj; j++) {
					data = alalTuple.get(i).get(j).toString() + "\n";
					dos.writeBytes(data);
					dos.flush();
				}
			}
			System.out.println("存磁盘success.索引位置：" + tableName + ".idx");
			// dos.writeBytes(data);

		} catch (IOException e) {
			Logger.getLogger(LOB.class).error(e);
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());// 抛出运行异常，供错误提示使用
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
					Logger.getLogger(LOB.class).error(e);
					e.printStackTrace();
					throw new RuntimeException(e.getMessage());// 抛出运行异常，供错误提示使用
				}
			}
		}
		// File f = new File(indexFolder, tableName + ".idx");
		// f.getParentFile().mkdirs();
		// FileOutputStream fos = null;
		// try {
		// f.createNewFile();
		// fos = new FileOutputStream(f);
		// String temp = "";
		// byte[] bytes;
		// for (int i = 0, len = alalTuple.size(); i < len; i++) {
		// temp = i + "\n";
		// bytes = temp.getBytes();
		// fos.write(bytes);
		// for (int j = 0, lenj = alalTuple.get(i).size(); j < lenj; j++) {
		// temp = alalTuple.get(i).get(j).toString() + "\n";
		// bytes = temp.getBytes();
		// fos.write(bytes);
		// fos.flush();
		// }
		// }
		// System.out.println("存磁盘成功//将索引结果放到" + tableName + ".idx");
		// } catch (IOException e) {
		// e.printStackTrace();// 控制台输出异常，供开发使用
		// throw new RuntimeException(e.getMessage());// 抛出运行异常，供错误提示使用
		// } finally {
		// try {
		// fos.close();
		// } catch (IOException e) {
		// e.printStackTrace();// 控制台输出异常，供开发使用
		// throw new RuntimeException(e.getMessage());// 抛出运行异常，供错误提示使用
		// }
		// }
		time[2] = System.currentTimeMillis() - start;
		return true;
	}

	/**
	 * 磁盘建索引的过程
	 */
	public boolean disk012Create() {
		// long start = System.currentTimeMillis();
		List<Tuple> result1 = sqlQuery();// 1.全表查
		// time1 = System.currentTimeMillis() - start;
		List<ArrayList<Tuple>> result2 = create(result1);// 2.建索引
		result1 = null;
		// time2 = System.currentTimeMillis() - start - time1;
		storeToDisk(result2);// 3.索引存磁盘
		// time3 = System.currentTimeMillis() - start - time1 - time2;
		return true;
	}

	/**
	 * 3.从磁盘中读索引
	 * 
	 * @return
	 */

	private ArrayList<ArrayList<Tuple>> readFromDisk() {
		long start = System.currentTimeMillis();
		File f = new File(indexFolder, tableName + ".idx");
		ArrayList<ArrayList<Tuple>> alalTuple = new ArrayList<ArrayList<Tuple>>();

		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(f);
			br = new BufferedReader(fr);
			String line = "";
			for (int i = 0; (line = br.readLine()) != null;) {
				if (FUNCTION.isInteger(line) && Integer.valueOf(line) == i) {// 取出来的值是数字，代表是第几个线序分支
					// System.out.println(line);//第几分支
					alalTuple.add(new ArrayList<Tuple>());
					i++;

				} else {// 取出来的值不是空，而且也不是数字
					Object[] obj = line.split(",");// 取得单个validtime对象
					// System.out.println(line);//第几分支
					alalTuple.get(i - 1).add(new Tuple(obj));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();// 控制台输出异常，供开发使用
			throw new RuntimeException(e.getMessage());// 抛出运行异常，供错误提示使用
		} catch (IOException e) {
			e.printStackTrace();// 控制台输出异常，供开发使用
			throw new RuntimeException(e.getMessage());// 抛出运行异常，供错误提示使用
		} finally {
			try {
				br.close();
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();// 控制台输出异常，供开发使用
				throw new RuntimeException(e.getMessage());// 抛出运行异常，供错误提示使用
			}
		}
		time[3] = System.currentTimeMillis() - start;
		return alalTuple;
	}

	/**
	 * 4.根据索引得结果 线序分支查询（分支使用二分法查找）
	 * 
	 * @param resultLOB
	 * @param query
	 * @return
	 */
	public List<Tuple> queryBinaryChop(List<ArrayList<Tuple>> resultLOB, ValidTime query) {
		long start = System.currentTimeMillis();
		// 左时间比右时间大时，查询区间有误返回null
		if (query.getLeft() > query.getRight()) {
			return null;
		}
		// 开始查询
		ArrayList<Tuple> queryResult = new ArrayList<Tuple>();// 查询结果
		for (int i = 0; i < resultLOB.size(); i++) {
			// 索引的分支数，i分支
			// 1
			if (resultLOB.get(i).get(0).getVt().getLeft() > query.getLeft()) {
				// 所有分支都不是，退出查询
				// System.out.println("实际个数"+i);
				break;
			} else {
				// 2.1
				if (resultLOB.get(i).get(0).getVt().getRight() < query.getRight()) {
					// 整条分支都不是，头就不包含
					continue;
				} else if (resultLOB.get(i).get(resultLOB.get(i).size() - 1).getVt().getLeft() <= query.getLeft()
						&& resultLOB.get(i).get(resultLOB.get(i).size() - 1).getVt().getRight() >= query.getRight()) {
					// 2.2
					// 尾部区间包含查询区间时，说明整条分支都是。
					queryResult.addAll(resultLOB.get(i));// 整条分支放入查询结果集中
					continue;
				} else {
					// 2.3
					// 部分分支是（使用二分法查找）
					int low = 0;
					int high = resultLOB.get(i).size() - 1;
					for (; low <= high;) {
						int mid = (low + high) / 2;
						// System.out.println(mid);
						if (resultLOB.get(i).get(mid).getVt().getLeft() <= query.getLeft()
								&& resultLOB.get(i).get(mid).getVt().getRight() >= query.getRight()) {
							for (int k = low; k <= mid; k++) {
								// System.out.println("开始查询...");
								queryResult.add(resultLOB.get(i).get(k));
							}
							low = mid + 1;
						} else {
							high = mid - 1;
						}
					}
				}
			}
		}
		// System.out.println("查询成功！共"+queryResult.size()+"个区间");
		time[4] = System.currentTimeMillis() - start;
		return queryResult;
	}

	public List<Tuple> querySequence(List<ArrayList<Tuple>> alalTuple, ValidTime query) {
		// TODO
		long start = System.currentTimeMillis();
		time[4] = System.currentTimeMillis() - start;
		return null;
	}

	/**
	 * 线序分支查询（分支逐个查）
	 * 
	 * @param alalTuple
	 * @param query
	 * @return
	 */
	public List<Tuple> queryTraversal(List<ArrayList<Tuple>> alalTuple, ValidTime query) {
		long start = System.currentTimeMillis();
		// 左时间比右时间大时，查询区间有误返回null
		if (query.getLeft() > query.getRight()) {
			return null;
		}
		// 开始查询
		ArrayList<Tuple> queryResult = new ArrayList<Tuple>();// 查询结果
		for (int i = 0; i < alalTuple.size(); i++) {
			// 索引的分支数，i分支
			// 1
			if (alalTuple.get(i).get(0).getVt().getLeft() > query.getLeft()) {
				// 所有分支都不是，退出查询
				break;
			} else {
				// 2.1
				if (alalTuple.get(i).get(0).getVt().getRight() < query.getRight()) {
					// 整条分支都不是，头就不包含
					continue;
				} else if (alalTuple.get(i).get(alalTuple.get(i).size() - 1).getVt().getLeft() <= query.getLeft()
						&& alalTuple.get(i).get(alalTuple.get(i).size() - 1).getVt().getRight() >= query.getRight()) {
					// 2.2
					// 尾部区间包含查询区间时，说明整条分支都是。
					queryResult.addAll(alalTuple.get(i));// 整条分支放入查询结果集中
					continue;
				} else {
					// 2.3
					// 部分分支是
					for (int j = 0; j < alalTuple.get(i).size()
							&& alalTuple.get(i).get(j).getVt().getLeft() <= query.getLeft()
							&& alalTuple.get(i).get(j).getVt().getRight() >= query.getRight(); j++) {
						queryResult.add(alalTuple.get(i).get(j));
					}
				}
			}

		}
		System.out.println("查询成功！共" + queryResult.size() + "个区间");
		time[4] = System.currentTimeMillis() - start;
		return queryResult;

	}

	/**
	 * 线序分支查询多线程优化，平均划分策略 8线程，16分支时，线程0：0，1。线程1：2，3.。。线程7：14，15
	 * 
	 * @param resultLOB
	 * @param query
	 * @return
	 */
	public List<Tuple> queryThreadSeries(List<ArrayList<Tuple>> resultLOB, ValidTime query) {
		long start = System.currentTimeMillis();
		// 索引为空，时间期间无效返回空
		if (resultLOB == null || query.getLeft() > query.getRight()) {
			return null;
		}
		int branchCount = resultLOB.size();// 分支个数
		ToolsBranch.setNew(threadCount);
		BranchThread[] branchThreads = new BranchThread[threadCount];
		List<ArrayList<ArrayList<Tuple>>> alalBranch = new ArrayList<ArrayList<ArrayList<Tuple>>>();
		for (int i = 0; i < threadCount; i++) {
			alalBranch.add(new ArrayList<ArrayList<Tuple>>());
		}
		// long s = System.currentTimeMillis();
		int colomns = (branchCount - 1) / threadCount + 1;// 行數
		int rows = threadCount;// 列數
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < colomns; j++) {
				if (branchCount > (j + i * colomns)) {
					alalBranch.get(i).add(resultLOB.get(j + i * colomns));
				}
			}
		}
		System.out.println("分支个数" + branchCount);
		for (int i = 0; i < threadCount; i++) {
			ServiceBranch serviceBranch = new ServiceBranch(alalBranch.get(i), query);
			branchThreads[i] = new BranchThread(serviceBranch);
			branchThreads[i].start();
		}

		ArrayList<Tuple> result = null;
		try {
			ToolsBranch.countDownLatch.await();
			result = ToolsBranch.setAndGetResult(null);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			for (int i = 0; i < branchThreads.length; i++) {
				branchThreads[i] = null;
			}
			ToolsBranch.setNull();// 释放资源
		}
		time[4] = System.currentTimeMillis() - start;
		return result;
	}

	/**
	 * 多线程优化查询交叉策略 8线程，16分支时，线程0：0，8。线程1：1，9.。。线程7：7，15
	 * 
	 * @param resultLOB
	 * @param query
	 * @return
	 */
	public List<Tuple> queryThreadCross(List<ArrayList<Tuple>> resultLOB, ValidTime query) {
		long start = System.currentTimeMillis();
		// 索引为空，时间期间无效返回空
		if (resultLOB == null || query.getLeft() > query.getRight()) {
			return null;
		}
		int branchCount = resultLOB.size();// 分支个数
		// int threadCount = Runtime.getRuntime().availableProcessors();//
		// 线程个数默认为系统个数32，系统默认cpu8*4个；
		ToolsBranch.setNew(threadCount);
		BranchThread[] branchThreads = new BranchThread[threadCount];
		ArrayList<ArrayList<ArrayList<Tuple>>> alalBranch = new ArrayList<ArrayList<ArrayList<Tuple>>>();
		for (int i = 0; i < threadCount; i++) {
			alalBranch.add(new ArrayList<ArrayList<Tuple>>());
		}
		int rows = (branchCount - 1) / threadCount + 1;// 行數
		int colomns = threadCount;// 列數
		for (int i = 0; i < colomns; i++) {
			for (int j = 0; j < rows; j++) {
				if (branchCount > (i + j * colomns)) {
					alalBranch.get(i).add(resultLOB.get(i + j * colomns));
				}
			}
		}
		System.out.println("分支个数" + branchCount);
		// System.out.println("分段"+(System.currentTimeMillis()-s)+"ms");
		for (int i = 0; i < threadCount; i++) {
			ServiceBranch serviceBranch = new ServiceBranch(alalBranch.get(i), query);
			branchThreads[i] = new BranchThread(serviceBranch);
			branchThreads[i].start();
		}

		ArrayList<Tuple> result = null;
		try {
			ToolsBranch.countDownLatch.await();
			result = ToolsBranch.setAndGetResult(null);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			for (int i = 0; i < branchThreads.length; i++) {
				branchThreads[i] = null;
			}
			ToolsBranch.setNull();// 释放资源
		}
		time[4] = System.currentTimeMillis() - start;
		return result;
	}

	/**
	 * 线序分支查询多线程优化2 找出实际需要查找的个数，然后进行放到线程里跑 //平均划分策略
	 * 
	 * @param resultLOB
	 * @param query
	 * @return
	 */
	public List<Tuple> queryThreadFrontSeries(List<ArrayList<Tuple>> resultLOB, ValidTime query) {
		long start = System.currentTimeMillis();
		// 索引为空，时间期间无效返回空
		if (resultLOB == null || query.getLeft() > query.getRight()) {
			return null;
		}
		int branchCount = resultLOB.size();// 分支个数
		// int threadCount =
		// Runtime.getRuntime().availableProcessors();//线程个数默认为系统个数32，系统默认cpu8*4个；
		int factBranchCount = 0;// 需处理的分支个数
		int low = 0, high = branchCount - 1, mid = high;

		if (resultLOB.get(0).get(0).getVt().getLeft() > query.getLeft()) {
			// 所有分支都不是，退出查询
			factBranchCount = 0;
		} else {
			// System.out.println("有分支是:");
			if (resultLOB.get(branchCount - 1).get(0).getVt().getLeft() > query.getLeft()) {
				while (high != low + 1) {
					if (resultLOB.get(mid).get(0).getVt().getLeft() > query.getLeft()) {
						high = mid;
						// System.out.println("high:" + high);
					} else {
						low = mid;
						// System.out.println("low:" + low);
					}
					mid = (low + high) / 2;
					// System.out.println("mid:" + mid);
				}
				factBranchCount = mid + 1;
			} else {
				factBranchCount = branchCount;
				// System.out.println("所有分支都是：" + factBranchCount);
			}
		}

		// factBranchCount = factBranchCount + threadCount;

		ToolsBranch.setNew(threadCount);
		// ToolsBranch.countDownLatch = new CountDownLatch(threadCount);
		// ToolsBranch.result = new ArrayList<Tuple>();
		BranchThread[] branchThreads = new BranchThread[threadCount];
		ArrayList<ArrayList<ArrayList<Tuple>>> alalBranch = new ArrayList<ArrayList<ArrayList<Tuple>>>();
		for (int i = 0; i < threadCount; i++) {
			alalBranch.add(new ArrayList<ArrayList<Tuple>>());
		}
		int colomns = (factBranchCount - 1) / threadCount + 1;// 行數
		int rows = threadCount;// 列數
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < colomns; j++) {
				if (factBranchCount > (j + i * colomns)) {
					alalBranch.get(i).add(resultLOB.get(j + i * colomns));
				}
			}
		}
		System.out.println("实际个数:" + factBranchCount);
		System.out.println("分支个数" + branchCount);
		for (int i = 0; i < threadCount; i++) {
			ServiceBranch serviceBranch = new ServiceBranch(alalBranch.get(i), query);
			branchThreads[i] = new BranchThread(serviceBranch);
			branchThreads[i].start();
		}

		ArrayList<Tuple> result = null;
		try {
			ToolsBranch.countDownLatch.await();
			result = ToolsBranch.setAndGetResult(null);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			for (int i = 0; i < branchThreads.length; i++) {
				branchThreads[i] = null;
			}
			ToolsBranch.setNull();// 释放资源
		}
		time[4] = System.currentTimeMillis() - start;
		return result;
	}

	/**
	 * 线序分支查询多线程优化2 找出实际需要查找的个数，然后进行放到线程里跑 //平均划分策略
	 * 
	 * @param alalTuple
	 * @param query
	 * @return
	 */
	public List<Tuple> queryThreadFrontCross(List<ArrayList<Tuple>> resultLOB, ValidTime query) {
		long start = System.currentTimeMillis();
		// 索引为空，时间期间无效返回空
		if (resultLOB == null || query.getLeft() > query.getRight()) {
			return null;
		}
		int branchCount = resultLOB.size();// 分支个数
		int factBranchCount = 0;// 需处理的分支个数
		int low = 0, high = branchCount - 1, mid = high;

		if (resultLOB.get(0).get(0).getVt().getLeft() > query.getLeft()) {
			// 所有分支都不是，退出查询
			factBranchCount = 0;
		} else {
			// System.out.println("有分支是:");
			if (resultLOB.get(branchCount - 1).get(0).getVt().getLeft() > query.getLeft()) {
				while (high != low + 1) {
					if (resultLOB.get(mid).get(0).getVt().getLeft() > query.getLeft()) {
						high = mid;
						// System.out.println("high:" + high);
					} else {
						low = mid;
						// System.out.println("low:" + low);
					}
					mid = (low + high) / 2;
					// System.out.println("mid:" + mid);
				}
				factBranchCount = mid + 1;
			} else {
				factBranchCount = branchCount;
				// System.out.println("所有分支都是：" + factBranchCount);
			}
		}
		System.out.println("開始分配：" + threadCount);
		ToolsBranch.setNew(threadCount);
		BranchThread[] branchThreads = new BranchThread[threadCount];
		List<ArrayList<ArrayList<Tuple>>> alalBranch = new ArrayList<ArrayList<ArrayList<Tuple>>>();
		for (int i = 0; i < threadCount; i++) {
			alalBranch.add(new ArrayList<ArrayList<Tuple>>());
		}
		int rows = (factBranchCount - 1) / threadCount + 1;// 行數
		int colomns = threadCount;// 列數
		for (int i = 0; i < colomns; i++) {
			for (int j = 0; j < rows; j++) {
				if (factBranchCount > (i + j * colomns)) {
					alalBranch.get(i).add(resultLOB.get(i + j * colomns));
				}
			}
		}
		// for (int i = 0; i < threadCount; i++) {
		// for (int j = 0; j < (factBranchCount / threadCount); j++) {
		// alalBranch.get(i).add(resultLOB.get(i + j * threadCount));
		// }
		// }
		// for (int i = 0; i < alalBranch.size(); i++) {
		// for (int j = 0; j < alalBranch.get(i).size(); j++) {
		// System.err.println("--------------------");
		// for (int k = 0; k < alalBranch.get(i).get(j).size(); k++) {
		// System.out.println(alalBranch.get(i).get(j).get(k) + " ");
		// }
		// }
		// }
		// 划分策略，交叉策略划分
		System.out.println("分支个数" + branchCount);
		System.out.println("实际个数:" + factBranchCount);
		for (int i = 0; i < threadCount; i++) {
			ServiceBranch serviceBranch = new ServiceBranch(alalBranch.get(i), query);
			branchThreads[i] = new BranchThread(serviceBranch);
			branchThreads[i].start();
		}

		List<Tuple> result = null;
		try {
			ToolsBranch.countDownLatch.await();
			result = ToolsBranch.setAndGetResult(null);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			for (int i = 0; i < branchThreads.length; i++) {
				branchThreads[i] = null;
			}
			ToolsBranch.setNull();// 释放资源
		}
		time[4] = System.currentTimeMillis() - start;
		return result;
	}

	/**
	 * 不带索引的查
	 * 
	 * @param tableName
	 * @param query
	 * @return
	 */
	public ArrayList<Tuple> queryNoLOB(String tableName, ValidTime query) {
		long start = System.currentTimeMillis();

		String mysql = "select * from " + tableName + "where vts_timeDB <= " + query.getLeftString()
				+ " and vte_timeDB >= " + query.getRightString() + ";";
		// System.out.println(mysql);
		List<Object[]> source = null;
		try {
			source = DAOFactory.getInstance().executeQuery(mysql);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		ArrayList<Tuple> result5 = new ArrayList<Tuple>();
		for (int i = 1; i < source.size(); i++) {
			result5.add(new Tuple(source.get(i)));
		}
		resultNum = result5.size();
		time[4] = System.currentTimeMillis() - start;
		return result5;
	}

	public List<Tuple> queryProcess() {
		// long start = System.currentTimeMillis();
		List<Tuple> result5 = new ArrayList<>();
		List<ArrayList<Tuple>> resultLOB;
		if ((strategy & 8) == 8) {
			// 1.时态索引查
			// 1.1获取索引
			if (!isFromDisk) {
				// 1.1.1不从磁盘
				List<Tuple> result1 = sqlQuery();// 1.已经进行非时态筛选的数据
				// time1 = System.currentTimeMillis() - start;
				resultLOB = create(result1);// 2.建线序分支
				System.out.println("create");
				for (int i = 0; i < resultLOB.size(); i++) {
//					System.out.println(resultLOB.get(i));
					for (int j = 0; j < resultLOB.get(i).size(); j++) {
//						System.out.println(resultLOB.get(i).get(j));
					}
				}
				// time2 = System.currentTimeMillis() - time1;
				result1 = null;

			} else {
				// 1.1.2从磁盘(暂时只支持全表的源数据)
				resultLOB = readFromDisk();// 4.从磁盘读索引
				// time4 = System.currentTimeMillis() - start;

			}
			// 1.2调用策略查
			if ((strategy & 12) != 12) {
				// 1.2.1不使用线程
				if (strategy == 9) {
					// 1.2.1.1 分支内二分查找
					result5 = queryBinaryChop(resultLOB, query);
					// time5 = System.currentTimeMillis() - start - time1 -
					// time2 - time3 - time4;
				} else if (strategy == 10) {
					// 1.2.1.2分支内起始序列查找
					result5 = querySequence(resultLOB, query);
					// time5 = System.currentTimeMillis() - start - time1 -
					// time2 - time3 - time4;
				} else if (strategy == 8) {

					// 1.2.1.3分支内遍历查找
					result5 = queryTraversal(resultLOB, query);
					// time5 = System.currentTimeMillis() - start - time1 -
					// time2 - time3 - time4;

				} else {

				}
			} else {
				System.out.println("1.2.2使用綫程");
				// 1.2.2使用线程
				if (strategy == 12) {
					// 1.2.2.1 连续
					result5 = queryThreadSeries(resultLOB, query);
					// time5 = System.currentTimeMillis() - start - time1 -
					// time2 - time3 - time4;
				}
				if (strategy == 13) {
					// 1.2.2.2 交叉
					result5 = queryThreadCross(resultLOB, query);
					// time5 = System.currentTimeMillis() - start - time1 -
					// time2 - time3 - time4;
				}
				if (strategy == 14) {
					// 1.2.2.3 前部连续
					result5 = queryThreadFrontSeries(resultLOB, query);
					// time5 = System.currentTimeMillis() - start - time1 -
					// time2 - time3 - time4;
				}
				if (strategy == 15) {
					// 1.2.2.4前部交叉
					result5 = queryThreadFrontCross(resultLOB, query);
					// time5 = System.currentTimeMillis() - start - time1 -
					// time2 - time3 - time4;
				}
			}
		} else {
			// 2.无时态索引
			result5 = queryNoLOB(tableName, query);
			// time5 = System.currentTimeMillis() - start - time1 - time2 -
			// time3 - time4;
		}
		expense = getTotalTime();
		resultNum = result5.size();
		return result5;
	}

	public void insert() {
		long start = System.currentTimeMillis();
		// TODO 待定
		time[4] = System.currentTimeMillis() - start;
	}

	public void update() {
		long start = System.currentTimeMillis();
		// TODO 待定
		time[4] = System.currentTimeMillis() - start;
	}

	public void delete() {
		long start = System.currentTimeMillis();
		// TODO 待定
		time[4] = System.currentTimeMillis() - start;
	}

	public void getMessage() {
		if ((strategy & 8) == 8) {
			if (isFromDisk) {
				indexType = "在磁盘中";
			} else {
				indexType = "在内存中";
			}
			if (strategy == 8) {
				indexType += "(内部遍历)";
			} else if (strategy == 9) {
				indexType += "(内部二分)";
			} else if (strategy == 10) {
				indexType += "(起始序列)";
			}
		} else {
			indexType = "常规";
		}
		expense = getTotalTime();

		System.out.print("表名：" + tableName);
		System.out.print(" 索引类型：" + indexType);
		System.out.print(" 结果个数：" + resultNum);
		System.out.println(" 查询区间：" + query.getLeftString() + " " + query.getRightString());
		System.out.print("时间开销：总开销：" + getTotalTime() + "ms\t");
		System.out.print("全表查出:\t" + time[0] + "ms\t");
		System.out.print("建索引:\t" + time[1] + "ms" + "\t");
		System.out.print("索引存磁盘:\t" + time[2] + "ms\t");
		System.out.print("磁盘读索引:\t" + time[3] + "ms\t");
		System.out.print("通过索引查询:\t" + time[4] + "ms\t");
		System.out.print("磁盘建\t" + (getTime()[0] + getTime()[1] + getTime()[2]) + "ms\t");
		System.out.println("磁盘查:\t" + (getTime()[3] + getTime()[4]) + "ms\t");
		// 写文件
		FileHelper fh = FileHelper.getInstance();
		String fileName = tableName + labRecordFile;
		System.out.println(fileName);
		String str = "";
		str += " 索引类型：" + indexType;
		str += " 结果个数：" + resultNum;
		str += " 查询区间：" + query.getLeftString() + " " + query.getRightString();
		str += "\n";
		str += getTotalTime() + "\t";
		str += time[0] + "\t";
		str += time[1] + "\t";
		str += time[2] + "\t";
		str += time[3] + "\t";
		str += time[4] + "\t";
		str += (getTime()[0] + getTime()[1] + getTime()[2]) + "\t";
		str += (getTime()[3] + getTime()[4]) + "\t";
		str += "\n";

		fh.write(fileName, str);

	}

	/*********************************************************************/
	/* 辅助函数 */

	private long getTotalTime() {
		int t = 0;
		for (int i = 0; i < time.length; i++)
			t += time[i];
		return t;
	}
	/*********************************************************************/
	/* 辅助函数 */
	//
	// /**
	// * 判断字符串是否是整数
	// */
	// public static boolean isInteger(String value) {
	// try {
	// Integer.parseInt(value);
	// return true;
	// } catch (NumberFormatException e) {
	// return false;
	// }
	// }
	//
	// /**
	// * 判断字符串是否是浮点数
	// */
	// public static boolean isDouble(String value) {
	// try {
	// Double.parseDouble(value);
	// if (value.contains("."))
	// return true;
	// return false;
	// } catch (NumberFormatException e) {
	// return false;
	// }
	// }
	//
	// /**
	// * 判断字符串是否是数字
	// */
	// public static boolean isNumber(String value) {
	// return isInteger(value) || isDouble(value);
	// }

	/**
	 * @return the indexfolder
	 */
	public static String getIndexfolder() {
		return indexFolder;
	}

	/**
	 * @return the labRecordFile
	 */
	public String getLabRecordFile() {
		return labRecordFile;
	}

	/**
	 * @return the indexType
	 */
	public String getIndexType() {
		return indexType;
	}

	/**
	 * @return the strategy
	 */
	public int getStrategy() {
		return strategy;
	}

	/**
	 * @return the query
	 */
	public ValidTime getQuery() {
		return query;
	}

	/**
	 * @return the sql
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @return the sourceCount
	 */
	public long getSourceCount() {
		return sourceCount;
	}

	/**
	 * @return the isFromDisk
	 */
	public boolean isFromDisk() {
		return isFromDisk;
	}

	/**
	 * @return the threadCount
	 */
	public int getThreadCount() {
		return threadCount;
	}

	/**
	 * @return the branchCount
	 */
	public long getBranchCount() {
		return branchCount;
	}

	/**
	 * @return the time
	 */
	public long[] getTime() {
		return time;
	}

	/**
	 * @return the expense
	 */
	public long getExpense() {
		return expense;
	}

	/**
	 * @return the resultNum
	 */
	public int getResultNum() {
		return resultNum;
	}

	/**
	 * @param indexType
	 *            the indexType to set
	 */
	public void setIndexType(String indexType) {
		this.indexType = indexType;
	}

	/**
	 * @param strategy
	 *            the strategy to set
	 */
	public void setStrategy(int strategy) {
		this.strategy = strategy;
	}

	/**
	 * @param query
	 *            the query to set
	 */
	public void setQuery(ValidTime query) {
		this.query = query;
	}

	/**
	 * @param sql
	 *            the sql to set
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}

	/**
	 * @param tableName
	 *            the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * @param isFromDisk
	 *            the isFromDisk to set
	 */
	public void setFromDisk(boolean isFromDisk) {
		this.isFromDisk = isFromDisk;
	}

	/**
	 * @param threadCount
	 *            the threadCount to set
	 */
	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	/* 辅助函数 */
	/*************************************************************/

	public static void queryMain() {
		String[] tableName = { "student1800k", "student2000k", "student2200k", "student2400k", "student2600k", };
		String[] queryStr = { "2013-01-01", "2014-07-01" };
		ValidTime query = new ValidTime(queryStr[0], queryStr[1]);
		int testTimes = 5;
		LOB lobQuery0;
		LOB lobQuery1;
		LOB lobQuery2;
		LOB lobQuery3;
		LOB lobQuery4;
		for (int i = 0, len = tableName.length; i < len; i++) {
			lobQuery0 = new LOB(0, "", query, tableName[i], false, 0);

			for (int j = 0; j < testTimes; j++) {
				lobQuery0.queryNoLOB(tableName[i], query);
				lobQuery0.getMessage();
			}
		}
		lobQuery0 = null;
		for (int i = 0, len = tableName.length; i < len; i++) {
			lobQuery1 = new LOB(8, "", query, tableName[i], false, 0);

			for (int j = 0; j < testTimes; j++) {
				lobQuery1.queryProcess();
				lobQuery1.getMessage();
			}
		}
		lobQuery1 = null;
		for (int i = 0, len = tableName.length; i < len; i++) {
			lobQuery2 = new LOB(9, "", query, tableName[i], false, 0);
			for (int j = 0; j < testTimes; j++) {
				lobQuery2.queryProcess();
				lobQuery2.getMessage();
			}
		}
		lobQuery2 = null;
		for (int i = 0, len = tableName.length; i < len; i++) {
			lobQuery3 = new LOB(8, "", query, tableName[i], true, 0);
			for (int j = 0; j < testTimes; j++) {
				lobQuery3.disk012Create();
				lobQuery3.queryProcess();
				lobQuery3.getMessage();
			}
		}
		lobQuery3 = null;
		for (int i = 0, len = tableName.length; i < len; i++) {
			lobQuery4 = new LOB(9, "", query, tableName[i], true, 0);
			for (int j = 0; j < testTimes; j++) {
				lobQuery4.disk012Create();
				lobQuery4.queryProcess();
				lobQuery4.getMessage();
			}
		}
		lobQuery4 = null;

	}

	public static void main(String[] args) {
	}

}

/**
 * 单例模式 线程间共享资源
 * 
 * @author CXH
 *
 */
class ToolsBranch {
	private static ToolsBranch instance = null;

	private ToolsBranch() {

	}

	static {
		instance = new ToolsBranch();
	}

	public static ToolsBranch getInstance() {
		return instance;
	}

	public static CountDownLatch countDownLatch = new CountDownLatch(8);;
	public static ArrayList<Tuple> result = new ArrayList<Tuple>();

	/**
	 * 同步查询结果赋值
	 * 
	 * @param result
	 * @return
	 */
	synchronized public static ArrayList<Tuple> setAndGetResult(ArrayList<Tuple> results) {
		if (results != null) {
			result.addAll(results);
		}
		return result;
	}

	/**
	 * 给类成员初始化
	 * 
	 * @param threadCount
	 */
	public static void setNew(int threadCount) {
		countDownLatch = new CountDownLatch(threadCount);
		result = new ArrayList<Tuple>();
	}

	/**
	 * 给类成员释放资源
	 */
	public static void setNull() {
		result = null;
		countDownLatch = null;
	}

}

/**
 * 分支
 * 
 * @author CXH
 *
 */
class ServiceBranch {
	private ArrayList<ArrayList<Tuple>> alalTuple;
	private ValidTime query;

	public ServiceBranch(ArrayList<ArrayList<Tuple>> alalTuple, ValidTime query) {
		this.alalTuple = alalTuple;
		this.query = query;
	}

	public void query() {
		long s = System.currentTimeMillis();
		ArrayList<Tuple> queryResult = new ArrayList<Tuple>();// 查询结果
		// 左时间比右时间大时，查询区间有误返回null
		if (query.getLeft() > query.getRight()) {
			queryResult = null;
		}
		// 开始查询
		for (int i = 0; i < alalTuple.size(); i++) {
			// 索引的分支数，i分支
			// 1
			if (alalTuple.get(i).get(0).getVt().getLeft() > query.getLeft()) {
				// 所有分支都不是，退出查询
				break;
			} else {
				// 2.1
				if (alalTuple.get(i).get(0).getVt().getRight() < query.getRight()) {
					// 整条分支都不是，头就不包含
					continue;
				} else if (alalTuple.get(i).get(alalTuple.get(i).size() - 1).getVt().getLeft() <= query.getLeft()
						&& alalTuple.get(i).get(alalTuple.get(i).size() - 1).getVt().getRight() >= query.getRight()) {
					// 2.2
					// 尾部区间包含查询区间时，说明整条分支都是。
					queryResult.addAll(alalTuple.get(i));// 整条分支放入查询结果集中
					continue;
				} else {
					// 2.3
					// 部分分支是（使用二分法查找）
					int low = 0;
					int high = alalTuple.get(i).size() - 1;
					for (; low <= high;) {
						int mid = (low + high) / 2;
						// System.out.println(mid);
						if (alalTuple.get(i).get(mid).getVt().getLeft() <= query.getLeft()
								&& alalTuple.get(i).get(mid).getVt().getRight() >= query.getRight()) {
							for (int k = low; k <= mid; k++) {
								// System.out.println("开始查询...");
								queryResult.add(alalTuple.get(i).get(k));
							}
							low = mid + 1;
						} else {
							high = mid - 1;
						}
					}
				}
			}
		}
		ToolsBranch.setAndGetResult(queryResult);
		System.out.println("线程" + Thread.currentThread().getName() + "开销" + "" + (System.currentTimeMillis() - s) + "ms"
				+ "查询成功！共" + queryResult.size() + "个记录");
		ToolsBranch.countDownLatch.countDown();
	}
}

/**
 * 给一个分支，线程得到该分支的查询结果
 * 
 * @author CXH
 *
 */
class BranchThread extends Thread {
	private ServiceBranch serviceBranch;

	public BranchThread() {
	}

	public BranchThread(ServiceBranch serviceBranch) {
		this.serviceBranch = serviceBranch;
	}

	@Override
	public void run() {
		serviceBranch.query();
	}
}
