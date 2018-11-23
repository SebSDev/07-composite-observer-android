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

public class MainActivity extends AppCompatActivity
{
	// UI elements
	private Button btn;
	private CheckBox cb;
	private ListView lv;

	// api instance
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

		apiSetup();

		// set initial list data
		updateList(false);

		// update the list if the button gets clicked
		btn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				updateList(cb.isChecked());
			}
		});

		// update the list with the changed checkbox
		cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				updateList(isChecked);
			}
		});
	}

	/**
	 * Updates the List of available meals
	 * @param vegetarian if true only vegetarian meals are added to the list
	 */
	private void updateList(final boolean vegetarian)
	{
		// preparing api call
		Call<List<Meal>> call = openMensaAPI.getMeals(getDateString());

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

					// add the meals to the list.
					// if the vegetarian flag is set we only add vegetarian meals
					for (Meal m : meals)
					{
						if (!vegetarian || m.isVegetarian())
						{
							sList.add(m.toString());
						}
					}
					// convert the list to an array
					String[] sData = new String[sList.size()];
					sData = sList.toArray(sData);

					// set the adapter for the listview
					lv.setAdapter(new ArrayAdapter<>(
						MainActivity.this,
						R.layout.meal_entry, // layout for single list entries (standard text view)
						sData // the string array of meals
					));
				}
			}

			@Override
			public void onFailure(Call<List<Meal>> call, Throwable t)
			{
				lv.setAdapter(new ArrayAdapter<>(
					MainActivity.this,
					R.layout.meal_entry,
					new String[]{"ERROR"} // just add one entry with "ERROR"
				));
			}
		});
	}

	/**
	 * sets up retrofit with the openmensa api
	 */
	private void apiSetup()
	{
		Retrofit retrofit = new Retrofit.Builder()
			.addConverterFactory(GsonConverterFactory.create())
			.baseUrl("http://openmensa.org/api/v2/")
			.build();

		openMensaAPI = retrofit.create(OpenMensaAPI.class);
	}

	/**
	 * Gets the current Date as a String
	 * @return the date string
	 */
	private String getDateString()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		Date d = new Date();
		return sdf.format(d);
	}
}
