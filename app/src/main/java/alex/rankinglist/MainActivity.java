package alex.rankinglist;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

import alex.rankinglist.databinding.ActivityMainBinding;
import alex.rankinglist.util.RandomUtil;
import alex.rankinglist.widget.model.Rank;
import alex.rankinglist.widget.model.User;

public class MainActivity extends AppCompatActivity {
	private ActivityMainBinding binding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
		binding.lvRankings.setModel(getRanks(), getUsers());
	}

	private List<Rank> getRanks() {
		return Arrays.asList(
				new Rank("Newbie", 0, 30, R.drawable.icon_smile_3, Color.DKGRAY),
				new Rank("Good", 30, 85, R.drawable.icon_smile_5, Color.BLUE),
				new Rank("Best", 85, 100, R.drawable.icon_smile_6, Color.GREEN));
	}

	private List<User> getUsers() {
		return RandomUtil.GenerateUsersList(1000);
	}
}
