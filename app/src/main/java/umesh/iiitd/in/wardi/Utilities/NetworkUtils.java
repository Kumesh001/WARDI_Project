package umesh.iiitd.in.wardi.Utilities;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {
    final static String GOOGLE_DIRECTION_API =
            "https://maps.googleapis.com/maps/api/directions/json";

    final static String PARAM_QUERY_ORIGIN = "origin";
    final static String PARAM_QUERY_destination = "destination";
    final static String PARAM_QUERY_API_KEY = "key";
    final static String PARAM_QUERY_MODE="mode";
    final static String WALKING="walking";

    final static String API_KEY="AIzaSyBnIz58aCxsqez_9kzYKlEZbneTvDSxuU0";
    final static String API_KEY_NEW="AIzaSyATMcsb6J8Clg-YuqPIfnUf4u-Q2YQwZVs";

    public static URL buildUrl(String src,String des) {
        Uri builtUri = Uri.parse(GOOGLE_DIRECTION_API).buildUpon()
                .appendQueryParameter(PARAM_QUERY_ORIGIN, src)
                .appendQueryParameter(PARAM_QUERY_destination, des)
                .appendQueryParameter(PARAM_QUERY_MODE,WALKING)
                .appendQueryParameter(PARAM_QUERY_API_KEY,API_KEY_NEW)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
