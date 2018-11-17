package de.thro.inf.prg3.a07;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.thro.inf.prg3.a07.api.OpenMensaAPI;
import de.thro.inf.prg3.a07.model.Meal;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

	private Button btn;
	private CheckBox cb;
	private ListView lv;

	private OpenMensaAPI openMensaAPI;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// this will inflate the layout from res/layout/activity_main.xml
		setContentView(R.layout.activity_main);

		// get the view elements
		lv = (ListView) findViewById(R.id.menu_listview);
		cb = (CheckBox) findViewById(R.id.vegetarian_checkbox);
		btn = (Button) findViewById(R.id.refresh_button);

		OkHttpClient client = new OkHttpClient.Builder().build();

		Retrofit retrofit = new Retrofit.Builder()
			.addConverterFactory(GsonConverterFactory.create())
			.baseUrl("http://openmensa.org/api/v2/")
			.client(client)
			.build();

		openMensaAPI = retrofit.create(OpenMensaAPI.class);

		updateList(false);


		btn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				updateList(cb.isChecked());
			}
		});

		cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				updateList(isChecked);
			}
		});
	}

	private void updateList(final boolean vegetarian)
	{
		// get the date if its a saturday or sunday set it to the next monday
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

		Date d = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		if (cal.DAY_OF_WEEK == Calendar.SATURDAY)
		{
			cal.add(Calendar.DATE, 2);
		}
		else if (cal.DAY_OF_WEEK == Calendar.SUNDAY)
		{
			cal.add(Calendar.DATE, 1);
		}
		String sDate = sdf.format(cal.getTime());


		// preparing call
		Call<List<Meal>> call = openMensaAPI.getMeals(sDate);

		// executing the call synchronously and unwrap the body
		call.enqueue(new Callback<List<Meal>>()
		{
			@Override
			public void onResponse(Call<List<Meal>> call, Response<List<Meal>> response)
			{
				if (response.isSuccessful())
				{
					List<Meal> meals = response.body();

					List<String> sList = new ArrayList<>();
					for (Meal m : meals)
					{
						if (!vegetarian || m.isVegetarian())
						{
							sList.add(m.toString());
						}
					}
					String[] sData = new String[sList.size()];
					sData = sList.toArray(sData);

					lv.setAdapter(new ArrayAdapter<>(
						MainActivity.this,
						R.layout.meal_entry,
						sData
					));
				}
			}

			@Override
			public void onFailure(Call<List<Meal>> call, Throwable t)
			{
				lv.setAdapter(new ArrayAdapter<>(
					MainActivity.this,
					R.layout.meal_entry,
					new String[] {"ERROR"}
				));
			}
		});
	}
}
