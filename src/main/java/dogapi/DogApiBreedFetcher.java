package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DogApiBreedFetcher implements BreedFetcher {
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public List<String> getSubBreeds(String breed) throws BreedNotFoundException {
        String url = "https://dog.ceo/api/breed/" + breed + "/list";
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new BreedNotFoundException("API request failed");
            }
            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);
            String status = json.optString("status", "");
            if (status.equals("error")) {
                int code = json.optInt("code", 0);
                if (code == 404) throw new BreedNotFoundException("Breed not found");
                throw new BreedNotFoundException("Unknown API error");
            }
            if (!status.equals("success")) {
                throw new BreedNotFoundException("Unexpected response");
            }
            JSONArray arr = json.getJSONArray("message");
            List<String> result = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                result.add(arr.getString(i));
            }
            return result;
        } catch (IOException e) {
            throw new BreedNotFoundException("Network error: " + e.getMessage());
        }
    }
}
