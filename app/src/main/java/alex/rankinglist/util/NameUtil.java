package alex.rankinglist.util;


import java.util.Random;

public class NameUtil {
	static private Random random = new Random();

	static public String GenerateName() {
		return Integer.toHexString(random.nextInt(0xFFFFFF));
	}
}
