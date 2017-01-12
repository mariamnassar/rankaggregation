import java.awt.List;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import javax.print.attribute.HashAttributeSet;

public class Test {
	
	
	public static void main(String [ ] args){
		HashMap<Float, String> map = new HashMap<Float, String>();
		map.put((float) 9.8 , "k");
		map.put((float) 7.4 , "h");
		map.put((float) 8.1 , "a");
		Map<Float, String> maps = new TreeMap<Float, String>(map);
		System.out.println(maps);
	}

}
