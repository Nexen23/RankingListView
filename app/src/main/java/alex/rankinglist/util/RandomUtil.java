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
		for (int i = 7; i <= 17; ++i) {
			//users.add(GenerateUser());
			users.add(new User(GenerateName(), i * 5));
		}

//		users.add(new User(GenerateName(), 1));
//		users.add(new User(GenerateName(), 10));
//		users.add(new User(GenerateName(), 17));

//		users.add(new User(GenerateName(), 1));
//		users.add(new User(GenerateName(), 20));
//		users.add(new User(GenerateName(), 47));

//		users.add(new User(GenerateName(), 80));
//		users.add(new User(GenerateName(), 95));
		return users;
	}
}
