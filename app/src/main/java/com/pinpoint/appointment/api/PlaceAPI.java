package com.pinpoint.appointment.api;



import android.os.AsyncTask;

import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.activities.RegisterActivity;
import com.pinpoint.appointment.adapter.AdpPlacesNew;
import com.pinpoint.appointment.models.Places;
import com.pinpoint.appointment.utils.Debug;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created on 26-07-2017.
 */

public class PlaceAPI {
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";

    private static final String OUT_JSON = "/json";

//    public ArrayList<Places> autocomplete(String input) {
//        ArrayList<Places> resultList = null;
//        HttpURLConnection conn = null;
//        StringBuilder jsonResults = new StringBuilder();
//
//        try {
//            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
//            sb.append("?key=" + ServerConfig.MAP_API_KEY);
//            sb.append("&input=" + URLEncoder.encode(input, "utf8"));
//            sb.append("&types=(cities)");
//            sb.append("&components=country:ind");
//
//            Debug.trace("Input:" + input);
//            Debug.trace("URL:" + sb.toString());
//
//            URL url = new URL(sb.toString());
//            conn = (HttpURLConnection) url.openConnection();
//            InputStreamReader in = new InputStreamReader(conn.getInputStream());
//
//            // Load the results into a StringBuilder
//            int read;
//            char[] buff = new char[1024];
//            while ((read = in.read(buff)) != -1) {
//                jsonResults.append(buff, 0, read);
//            }
//        } catch (MalformedURLException e) {
//            Debug.trace("Error processing Places API URL:" + e.getMessage());
//            return resultList;
//        } catch (IOException e) {
//            Debug.trace("Error connecting to Places API:" + e.getMessage());
//            return resultList;
//        } finally {
//            if (conn != null) {
//                conn.disconnect();
//            }
//        }
//
//        try {
//            Debug.trace("JSON Result:" + jsonResults.toString());
//            // Create a JSON object hierarchy from the results
//            JSONObject jsonObj = new JSONObject(jsonResults.toString());
//            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
//
//            // Extract the Place descriptions from the results
//            resultList = new ArrayList<Places>(predsJsonArray.length());
//            Places mPlace;
//            for (int i = 0; i < predsJsonArray.length(); i++) {
//                mPlace = new Places();
//                mPlace.setDescription(predsJsonArray.getJSONObject(i).getString("description"));
//                mPlace.setPlace_id(predsJsonArray.getJSONObject(i).getString("place_id"));
//                resultList.add(mPlace);
//            }
//        } catch (JSONException e) {
//            Debug.trace("Cannot process JSON results:" + e.getMessage());
//        }
//        return resultList;
//    }

    public ArrayList<Places> autocomplete(String input) {
        ArrayList<Places> resultList = null;
        try {
            resultList=new getPlaces().execute(input).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    private class getPlaces extends AsyncTask<String, Void, ArrayList<Places>> {
        private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
        private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
        private static final String OUT_JSON = "/json";
        ArrayList<Places> resultList = null;

        @Override
        protected ArrayList<Places> doInBackground(String... voids) {
            HttpURLConnection conn = null;
            StringBuilder jsonResults = new StringBuilder();
            if(resultList==null)
            {
                resultList=new ArrayList<>();
            }
            try {
//                String sb = PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON + "?key=" + ServerConfig.MAP_API_KEY +
//                        "&input=" + URLEncoder.encode(voids[0], "utf8") + "" +
//                        "&types=(cities)" +
//                        "&components=country:ind";

                String sb = PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON +"?input="+ URLEncoder.encode(voids[0], "utf8") + "" +
                        "&types=address" +
                        "&location=" + BaseConstants.LATITUDE+","+BaseConstants.LONGITUDE+
                        "&radius="+BaseConstants.RADIUS+
                        "&strictbounds"+
                        "&key=" + ServerConfig.MAP_API_KEY ;

                URL url = new URL(sb);
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());

                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
            } catch (MalformedURLException ignored) {


            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            try {
//                Debug.trace("JSON Result:" + jsonResults.toString());
                // Create a JSON object hierarchy from the results
                JSONObject jsonObj = new JSONObject(jsonResults.toString());
                JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

                // Extract the Place descriptions from the results
                resultList = new ArrayList<>(predsJsonArray.length());
                Places mPlace;
                for (int i = 0; i < predsJsonArray.length(); i++) {
                    mPlace = new Places();
                    mPlace.setDescription(predsJsonArray.getJSONObject(i).getString("description"));
                    mPlace.setPlace_id(predsJsonArray.getJSONObject(i).getString("place_id"));
                    resultList.add(mPlace);
                }
            } catch (JSONException e) {
                Debug.trace("Cannot process JSON results:" + e.getMessage());
            }
            return resultList;
        }

//        @Override
//        protected void onPostExecute(Void result) {
//            placeAdapter = new AdpPlacesNew(RegisterActivity.this, resultList);
//            mAcTxtCityState.setAdapter(placeAdapter);
//
//        }
    }
}