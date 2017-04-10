package cn.edu.scnu.TempMT_Index.service;

import java.util.ArrayList;
import java.util.Stack;
/**
 * 
 * @author CXH
 *
 */
public class Format {
	private ArrayList<String> strarray = new ArrayList<String>();
	private Stack<Character> strstack = new Stack<Character>();
	private static final char[] blank = {' ','\t','\n','\r'};
	private static final char[] punctuation = {'(',')','[',']',',',';'};
	
	public ArrayList<String> format(String source) {
		String temp = "";
		int positionSemicolon = -1;
//		source = source.trim();
		for(int i=0; i<source.length();i++){
//			if(source.charAt(i)==';')
			strstack.push(source.charAt(i));
			//' ','\t','\n','\r','(',')','[',']',',',';'按这些字符串分裂数组
			if(!strstack.empty()&&!isBlank(strstack.peek())&&!isPunctuation(strstack.peek())){
					temp += strstack.pop();
			}else if(!strstack.empty()&&isBlank(strstack.peek())){
				strstack.pop();//空白字符扔掉
				if(temp!=""){
					strarray.add(temp);
					temp = "";					
				}
			}else if(!strstack.empty()&&isPunctuation(strstack.peek())){
				if(strstack.peek()==';')positionSemicolon = i;
				if(temp!=""){
					strarray.add(temp);
					temp = "";
				}
				//标点符号保存
				temp += strstack.pop();
				strarray.add(temp);
				temp = "";
			}
			
		}
		if(temp!=""){
			strarray.add(temp);
			temp = "";
		}
		if(positionSemicolon==-1) strarray.add(";");

		return strarray;
	}
	/**
	 * 是否空白字符
	 * @param peek
	 * @return
	 */
	private boolean isBlank(Character peek) {
		for (int i = 0; i < blank.length; i++) {
			if(peek==blank[i]) return true;
		}
		return false;
	}
	/**
	 * 是否标点符号
	 * @param peek
	 * @return
	 */
	private boolean isPunctuation(Character peek) {
		for (int i = 0; i < punctuation.length; i++) {
			if(peek==punctuation[i]) return true;
		}
		return false;
	}
	
	/**
	 * 这是一个过时方法，替代方法format
	 * @param source
	 * @return
	 */
	@Deprecated
	public ArrayList<String> formatOld(String source) {
		String temp = "";
//		source = source.trim();
		for(int i=0; i<source.length();i++){
//			if(source.charAt(i)==';')
			strstack.push(source.charAt(i));
			//' ','\t','\n','\r','(',')','[',']',',',';'按这些字符串分裂数组
			if(!strstack.empty()&&strstack.peek()!=' '&&strstack.peek()!='\t'&&strstack.peek()!='\n'&&strstack.peek()!='\r'&&strstack.peek()!='('&&strstack.peek()!=')'&&strstack.peek()!='['&&strstack.peek()!=']'&&strstack.peek()!=';'&&strstack.peek()!=','){
				temp += strstack.pop();
			}else if(!strstack.empty()&&(strstack.peek()==' '||strstack.peek()=='\t'||strstack.peek()=='\n'||strstack.peek()=='\r')){
				strstack.pop();
				if(temp!=""){
					strarray.add(temp);
//					System.out.println(temp);
					temp = "";					
				}
			}else if(!strstack.empty()&&(strstack.peek()=='('||strstack.peek()==')'||strstack.peek()=='['||strstack.peek()==']'||strstack.peek()==','||strstack.peek()==';')){
				if(temp==""){
					temp += strstack.pop();
					strarray.add(temp);
//					System.out.println(temp);
					temp = "";
				}else {
					strarray.add(temp);
//					System.out.println(temp);
					temp = "";
					temp += strstack.pop();
					strarray.add(temp);
//					System.out.println(temp);
					temp = "";
				}
			}
			
		}
		return strarray;
	}
}