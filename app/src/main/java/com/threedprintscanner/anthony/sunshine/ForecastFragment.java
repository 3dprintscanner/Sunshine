package com.threedprintscanner.anthony.sunshine;

/**
 * Created by anthony on 3/12/2015.
 */

        import android.content.Intent;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.support.v4.app.Fragment;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.Menu;
        import android.view.MenuInflater;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.Adapter;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.ListView;
        import android.util.Log;
        import android.widget.Toast;

        import com.threedprintscanner.anthony.sunshine.R;

        import org.json.JSONException;

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.net.HttpURLConnection;
        import java.net.URL;
        import java.util.ArrayList;

public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> mForeCastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_refresh:
              GetWeatherInBackground weather = new GetWeatherInBackground();
                weather.execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final ArrayList teststrings = new ArrayList();
        teststrings.add( "Today Sunny 88/63 ");
        teststrings.add( "Today Windy 42/63 ");
        teststrings.add( "Today Rainy 53/21 ");
        teststrings.add( "Today Cloudy 76/84 ");
        teststrings.add( "Today Sunny 21/23 ");
        teststrings.add("Today Windy 83/33 ");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview, teststrings);
        ListView listView = (ListView) rootView.findViewById(R.id.listView_forecast);
        listView.setAdapter(mForeCastAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast = mForeCastAdapter.getItem(position);
                Toast.makeText(getActivity(), forecast, Toast.LENGTH_LONG ).show();
                Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }
        });



        return rootView;
    }
    class GetWeatherInBackground extends AsyncTask<String,Void,String[]> {
        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            if(strings != null) {
//                ArrayAdapter mForecastAdapter = new ArrayAdapter(getActivity(), R.layout.list_item_forecast);
                mForeCastAdapter.clear();
                for (String dayForecastString : strings){
                    mForeCastAdapter.add(dayForecastString);
                }
            }
        }

        @Override
        protected String[] doInBackground(String... params) {
//            String postcode = params[0];
              String postcode = "BS$8 2XD";
                String mode = "json";
                String units = "metric";
                String count = "7";
                final int numdays = 7;


            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            String baseurl = "http://api.openweathermap.org/data/2.5/forecast/daily";

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority("api.openweathermap.org")
                        .appendPath("data")
                        .appendPath("2.5")
                        .appendPath("forecast")
                        .appendPath("daily")
                        .appendQueryParameter("q",postcode)
                        .appendQueryParameter("mode",mode)
                        .appendQueryParameter("units",units)
                        .appendQueryParameter("count",count);

                String uri = builder.build().toString();

//                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?mode=json&units=metric&cnt=7&q=london");
                URL url = new URL(uri);
                Log.v("URI",url.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");

                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.d("ReturnedJSON", forecastJsonStr);
//                return formattedData;
            } catch (IOException e) {
                Log.e("ForecastFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("ForecastFragment", "Error closing stream", e);
                    }
                }
            }

            try {
                WeatherDataParser weatherData = new WeatherDataParser();
                String[] formattedData = weatherData.getWeatherDataFromJson(forecastJsonStr,numdays);
                return formattedData;
            } catch (JSONException e){
                Log.e("LOG ERROR", e.toString());
                e.printStackTrace();
            }
            return null;
        }
    }
}


