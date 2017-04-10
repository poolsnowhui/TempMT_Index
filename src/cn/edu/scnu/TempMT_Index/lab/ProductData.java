package cn.edu.scnu.TempMT_Index.lab;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

import cn.edu.scnu.TempMT_Index.service.Tuple;

/**
 * 
 * @author CXH
 * 生成时态数据
 */
public class ProductData {
	
//	private int noTimeDataCount = 5 ;//非时态个数
	private int dataCount = 10000;//元组总数
	private int singlePMCIDCount =2;//单个PM对应的最大CID数
	private int singleNoTimeCount = 20;//非时态元组重复最大个数
	private int cidMin = 100001;//课程id最小
	private int cidMax = 100100;//课程id最大
	private long vtMax = 0l;//有效时间最小19900101
	private long vtMin = 0l;//有效时间最大
//	private long vtMin = 400000000000l;//有效时间最小
//	private long vtMax = 1800000000000l;//有效时间最大
	
	private ArrayList<Tuple> alTuple = new ArrayList<Tuple>();//时态元组数组
	private int sid = 0;//非时态元组的序号自增
	
	private final int noTimeCount = 4;//非时态列数
	
	public ProductData() {
		try {
			vtMax = new SimpleDateFormat("YY-MM-DD").parse("2030-04-01").getTime();
			vtMin = new SimpleDateFormat("YY-MM-DD").parse("1980-04-01").getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	public int getNoTimeCount() {
		return noTimeCount;
	}
	public static final String[] FEMALE_FIRST_NAMES = {
	      "Mary", "Patricia", "Linda", "Barbara", "Elizabeth", "Jennifer", "Maria", "Susan",
	      "Margaret", "Dorothy", "Lisa", "Nancy", "Karen", "Betty", "Helen", "Sandra", "Donna",
	      "Carol", "Ruth", "Sharon", "Michelle", "Laura", "Sarah", "Kimberly", "Deborah", "Jessica",
	      "Shirley", "Cynthia", "Angela", "Melissa", "Brenda", "Amy", "Anna", "Rebecca", "Virginia",
	      "Kathleen", "Pamela", "Martha", "Debra", "Amanda", "Stephanie", "Carolyn", "Christine",
	      "Marie", "Janet", "Catherine", "Frances", "Ann", "Joyce", "Diane", "Alice", "Julie",
	      "Heather", "Teresa", "Doris", "Gloria", "Evelyn", "Jean", "Cheryl", "Mildred", "Katherine",
	      "Joan", "Ashley", "Judith", "Rose", "Janice", "Kelly", "Nicole", "Judy", "Christina",
	      "Kathy", "Theresa", "Beverly", "Denise", "Tammy", "Irene", "Jane", "Lori", "Rachel",
	      "Marilyn", "Andrea", "Kathryn", "Louise", "Sara", "Anne", "Jacqueline", "Wanda", "Bonnie",
	      "Julia", "Ruby", "Lois", "Tina", "Phyllis", "Norma", "Paula", "Diana", "Annie", "Lillian",
	      "Emily", "Robin", "Peggy", "Crystal", "Gladys", "Rita", "Dawn", "Connie", "Florence",
	      "Tracy", "Edna", "Tiffany", "Carmen", "Rosa", "Cindy", "Grace", "Wendy", "Victoria", "Edith",
	      "Kim", "Sherry", "Sylvia", "Josephine", "Thelma", "Shannon", "Sheila", "Ethel", "Ellen",
	      "Elaine", "Marjorie", "Carrie", "Charlotte", "Monica", "Esther", "Pauline", "Emma",
	      "Juanita", "Anita", "Rhonda", "Hazel", "Amber", "Eva", "Debbie", "April", "Leslie", "Clara",
	      "Lucille", "Jamie", "Joanne", "Eleanor", "Valerie", "Danielle", "Megan", "Alicia", "Suzanne",
	      "Michele", "Gail", "Bertha", "Darlene", "Veronica", "Jill", "Erin", "Geraldine", "Lauren",
	      "Cathy", "Joann", "Lorraine", "Lynn", "Sally", "Regina", "Erica", "Beatrice", "Dolores",
	      "Bernice", "Audrey", "Yvonne", "Annette", "June", "Samantha", "Marion", "Dana", "Stacy",
	      "Ana", "Renee", "Ida", "Vivian", "Roberta", "Holly", "Brittany", "Melanie", "Loretta",
	      "Yolanda", "Jeanette", "Laurie", "Katie", "Kristen", "Vanessa", "Alma", "Sue", "Elsie",
	      "Beth", "Jeanne"};
	public static final String[] MALE_FIRST_NAMES = {
	      "James", "John", "Robert", "Michael", "William", "David", "Richard", "Charles", "Joseph",
	      "Thomas", "Christopher", "Daniel", "Paul", "Mark", "Donald", "George", "Kenneth", "Steven",
	      "Edward", "Brian", "Ronald", "Anthony", "Kevin", "Jason", "Matthew", "Gary", "Timothy",
	      "Jose", "Larry", "Jeffrey", "Frank", "Scott", "Eric", "Stephen", "Andrew", "Raymond",
	      "Gregory", "Joshua", "Jerry", "Dennis", "Walter", "Patrick", "Peter", "Harold", "Douglas",
	      "Henry", "Carl", "Arthur", "Ryan", "Roger", "Joe", "Juan", "Jack", "Albert", "Jonathan",
	      "Justin", "Terry", "Gerald", "Keith", "Samuel", "Willie", "Ralph", "Lawrence", "Nicholas",
	      "Roy", "Benjamin", "Bruce", "Brandon", "Adam", "Harry", "Fred", "Wayne", "Billy", "Steve",
	      "Louis", "Jeremy", "Aaron", "Randy", "Howard", "Eugene", "Carlos", "Russell", "Bobby",
	      "Victor", "Martin", "Ernest", "Phillip", "Todd", "Jesse", "Craig", "Alan", "Shawn",
	      "Clarence", "Sean", "Philip", "Chris", "Johnny", "Earl", "Jimmy", "Antonio", "Danny",
	      "Bryan", "Tony", "Luis", "Mike", "Stanley", "Leonard", "Nathan", "Dale", "Manuel", "Rodney",
	      "Curtis", "Norman", "Allen", "Marvin", "Vincent", "Glenn", "Jeffery", "Travis", "Jeff",
	      "Chad", "Jacob", "Lee", "Melvin", "Alfred", "Kyle", "Francis", "Bradley", "Jesus", "Herbert",
	      "Frederick", "Ray", "Joel", "Edwin", "Don", "Eddie", "Ricky", "Troy", "Randall", "Barry",
	      "Alexander", "Bernard", "Mario", "Leroy", "Francisco", "Marcus", "Micheal", "Theodore",
	      "Clifford", "Miguel", "Oscar", "Jay", "Jim", "Tom", "Calvin", "Alex", "Jon", "Ronnie",
	      "Bill", "Lloyd", "Tommy", "Leon", "Derek", "Warren", "Darrell", "Jerome", "Floyd", "Leo",
	      "Alvin", "Tim", "Wesley", "Gordon", "Dean", "Greg", "Jorge", "Dustin", "Pedro", "Derrick",
	      "Dan", "Lewis", "Zachary", "Corey", "Herman", "Maurice", "Vernon", "Roberto", "Clyde",
	      "Glen", "Hector", "Shane", "Ricardo", "Sam", "Rick", "Lester", "Brent", "Ramon", "Charlie",
	      "Tyler", "Gilbert", "Gene"};
	public static final String[] LAST_NAMES = {
	      "Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller", "Wilson", "Moore",
	      "Taylor", "Anderson", "Thomas", "Jackson", "White", "Harris", "Martin", "Thompson", "Garcia",
	      "Martinez", "Robinson", "Clark", "Rodriguez", "Lewis", "Lee", "Walker", "Hall", "Allen",
	      "Young", "Hernandez", "King", "Wright", "Lopez", "Hill", "Scott", "Green", "Adams", "Baker",
	      "Gonzalez", "Nelson", "Carter", "Mitchell", "Perez", "Roberts", "Turner", "Phillips",
	      "Campbell", "Parker", "Evans", "Edwards", "Collins", "Stewart", "Sanchez", "Morris",
	      "Rogers", "Reed", "Cook", "Morgan", "Bell", "Murphy", "Bailey", "Rivera", "Cooper",
	      "Richardson", "Cox", "Howard", "Ward", "Torres", "Peterson", "Gray", "Ramirez", "James",
	      "Watson", "Brooks", "Kelly", "Sanders", "Price", "Bennett", "Wood", "Barnes", "Ross",
	      "Henderson", "Coleman", "Jenkins", "Perry", "Powell", "Long", "Patterson", "Hughes",
	      "Flores", "Washington", "Butler", "Simmons", "Foster", "Gonzales", "Bryant", "Alexander",
	      "Russell", "Griffin", "Diaz", "Hayes", "Myers", "Ford", "Hamilton", "Graham", "Sullivan",
	      "Wallace", "Woods", "Cole", "West", "Jordan", "Owens", "Reynolds", "Fisher", "Ellis",
	      "Harrison", "Gibson", "Mcdonald", "Cruz", "Marshall", "Ortiz", "Gomez", "Murray", "Freeman",
	      "Wells", "Webb", "Simpson", "Stevens", "Tucker", "Porter", "Hunter", "Hicks", "Crawford",
	      "Henry", "Boyd", "Mason", "Morales", "Kennedy", "Warren", "Dixon", "Ramos", "Reyes", "Burns",
	      "Gordon", "Shaw", "Holmes", "Rice", "Robertson", "Hunt", "Black", "Daniels", "Palmer",
	      "Mills", "Nichols", "Grant", "Knight", "Ferguson", "Rose", "Stone", "Hawkins", "Dunn",
	      "Perkins", "Hudson", "Spencer", "Gardner", "Stephens", "Payne", "Pierce", "Berry",
	      "Matthews", "Arnold", "Wagner", "Willis", "Ray", "Watkins", "Olson", "Carroll", "Duncan",
	      "Snyder", "Hart", "Cunningham", "Bradley", "Lane", "Andrews", "Ruiz", "Harper", "Fox",
	      "Riley", "Armstrong", "Carpenter", "Weaver", "Greene", "Lawrence", "Elliott", "Chavez",
	      "Sims", "Austin", "Peters", "Kelley", "Franklin", "Lawson"};
	public final static String[] EMAIL_SUFFIX={"qq.com","126.com","163.com","gmail.com",
		"163.net","msn.com","hotmail.com","yahoo.com.cn","sina.com","@mail.com","263.net","sohu.com",
		"21cn.com","sogou.com"
		};
	
	private final static <T>T nextValue(T[] array){
		assert (array!=null && array.length>0);
		return array[new Random().nextInt(array.length)];
	}
	//产生随机英文名
	public final static String getRandomEnglishName(){
		return getRandomEnglishFirstName()+" "+getRandomEnglishLastName();
	}
	private final static String getRandomEnglishFirstName(){
		return new Random().nextBoolean()?nextValue(FEMALE_FIRST_NAMES):nextValue(MALE_FIRST_NAMES);
	}
	private final static String getRandomEnglishLastName(){
		return nextValue(LAST_NAMES);
	}
	//产生邮箱名
	public final static String getRandomEmailAddress(){
		return getRandomEnglishFirstName()+getRandomEnglishLastName()+"@"+nextValue(EMAIL_SUFFIX);
	}
	
	//产生课程id
	private int getCourseID() {
		int id = (int)(cidMin+Math.random()*(cidMax-cidMin));
		return id;
	}
	//产生多个课程id
	private ArrayList<Object> getCouresID(int count) {
		ArrayList<Object> obj = new ArrayList<Object>();
		for (int i = 0; i < count; i++) {
			obj.add(getCourseID());
		}
		return obj;
	}
	private ArrayList<long[]> getValueTime(int count) {
		ArrayList<long[]> alValueTime = new ArrayList<long[]>();//有效时间对数组
		for(int i=0;i<count;i++){
			long [] tempValueTime = randomValueTime();
			alValueTime.add(tempValueTime);
//			System.out.println("s:"+alValueTime.get(i)[0]+" e:"+alValueTime.get(i)[1]);
		}
		return alValueTime;
	}

	//根据最大值，最小值随机产生一对有效时间
	private long[] randomValueTime() {
		long[] vt = new long[2];
		vt[0] = (long)(vtMin+Math.random()*(vtMax-vtMin));//有效时间开始
//		System.out.println(vt[0]);
		vt[1] = (long)(vt[0]+Math.random()*(vtMax-vt[0]));//有效时间结束
		return vt;
	}
	//产生个人信息
	private Object[] getRandomPersonMessage() {
		Object[] obj= new Object[noTimeCount-1];
		obj[0] = sid++;
		obj[1] = getRandomEnglishName();
		obj[2] = getRandomEmailAddress();
		return obj;
	}
	//产生一个非时态元组
	public Object[] randomNoTime() {
			Object[] obj= new Object[noTimeCount];
			Object[] objPM = getRandomPersonMessage();
			for (int i = 0; i < noTimeCount-1; i++) {
				obj[i] = objPM[i];
			}
			obj[noTimeCount-1] = getCourseID();
		return obj;
	}
	//产生所有的元组
	public ArrayList<Tuple> getAllTuple() {
		int l=0;
		for(int i=0;i<dataCount;i++){
//			Object[] ntObj = randomNoTime();
			//1.个人信息
			Object[] objPM = getRandomPersonMessage();
			int randomCidCount = (int)(1+(Math.random()*singlePMCIDCount));//个人信息的Cid个数
//			System.out.println(randomCidCount);
			//2.个人信息+CID
			ArrayList<Object> alCid = getCouresID(randomCidCount);//产生randomCidCount个CouresID
			for (int j = 0; j < alCid.size(); j++) {//个人信息和Cid组合
				Object[] ntObj= new Object[noTimeCount];
				for (int iPM = 0; iPM < noTimeCount-1; iPM++) {
					ntObj[iPM] = objPM[iPM];
				}
				ntObj[noTimeCount-1] = alCid.get(j);
				
				int randomVtCount = (int)(1+(Math.random()*singleNoTimeCount));//单非时态元组对应的有效时间个数
				//2.个人信息+CID+有效时间
				ArrayList<long[]> alVt = getValueTime(randomVtCount);//产生重复非时态元组的多个有效时间
//				System.out.println(randomVtCount);
				for (int k = 0; k < alVt.size(); k++) {
					if(l>=dataCount) break;//达到dataCount时，结束
					Tuple tuple = new Tuple(ntObj, alVt.get(k));
					alTuple.add(tuple);
//					System.out.println(alTuple.get(l));
					l++;
				}
				if(l>=dataCount) break;//达到dataCount时，结束
			}
			if(l>=dataCount) break;//达到dataCount时，结束
		}
		return alTuple;
	}

	public int getDataCount() {
		return dataCount;
	}
	public void setDataCount(int dataCount) {
		this.dataCount = dataCount;
	}
	public static void main(String[] args) {
		ProductData pd = new ProductData();
//		pd.getValueTime(10);
		pd.getAllTuple();
//		System.out.println(1000+Math.random()*(2000-1000));
		
	}
}
