package alex.rankinglist.util;


import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import alex.rankinglist.widget.model.User;

public class RandomUtil {
	static private Random random = new Random();

	static public String GenerateName() {
		return Integer.toHexString(random.nextInt(0xFFFFFF));
	}

	static public User GenerateUser() {
		@FloatRange(from=0, to=100) float score = random.nextFloat() * 100;
		return new User(GenerateName(), score);
	}

	static public List<User> GenerateUsersList(@IntRange(from=0) int count) {
		// FIXME: 26.01.2017 return real impl
		List<User> users = new ArrayList<>(count);
		/*count = 21;
		for (int i = 0; i < count; ++i) {
			//users.add(GenerateUser());
			users.add(new User(GenerateName(), i * 5));
		}*/
		//users.add(new User(GenerateName(), 0));
		users.add(new User(GenerateName(), 31));
		//users.add(new User(GenerateName(), 45));
		//users.add(new User(GenerateName(), 50));
		return users;
	}
}
