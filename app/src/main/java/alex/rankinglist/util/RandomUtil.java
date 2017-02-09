package alex.rankinglist.util;


import android.support.annotation.IntRange;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import alex.rankinglist.widget.model.User;

public class RandomUtil {
	private static boolean ENABLE_RANDOM = false;
	private static Randomizer randomizer = new Randomizer();

	public static String GenerateName() {
		return Integer.toHexString(randomizer.nextInt(0xFFFFFF));
	}

	public static User GenerateUser() {
		float score = randomizer.nextFloat(100);
		return new User(GenerateName(), score);
	}

	public static List<User> GenerateUsersList(@IntRange(from=0) int count) {
		List<User> users = new ArrayList<>(count);
		if (!ENABLE_RANDOM) {
			for (int i = 0; i <= 10; ++i) {
				users.add(new User(GenerateName(), i * 5));
			}

//			users.add(new User(GenerateName(), 1));
//			users.add(new User(GenerateName(), 10));
//			users.add(new User(GenerateName(), 17));
//
//			users.add(new User(GenerateName(), 1));
//			users.add(new User(GenerateName(), 20));
//			users.add(new User(GenerateName(), 47));
//
			users.add(new User(GenerateName(), 80));
			users.add(new User(GenerateName(), 85));
//			users.add(new User(GenerateName(), 75));
		} else {
			for (int i = 0; i < count; ++i) {
				users.add(GenerateUser());
			}
		}
		return users;
	}



	private static class Randomizer {
		private Random random = new Random();

		int nextInteger = 0, iNextFloat = 0;
		float[] floats = {0, 5, 10, 15, 20, 25};

		public int nextInt(int range) {
			if (ENABLE_RANDOM) {
				return random.nextInt(range);
			} else {
				return nextInteger++;
			}
		}

		public float nextFloat(float range) {
			if (ENABLE_RANDOM) {
				return random.nextFloat() * range;
			} else {
				iNextFloat %= floats.length;
				return floats[iNextFloat++];
			}
		}
	}
}
