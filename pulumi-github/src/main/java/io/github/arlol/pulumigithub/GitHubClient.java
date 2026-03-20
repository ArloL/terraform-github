package io.github.arlol.pulumigithub;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class GitHubClient {

    private final HttpClient http = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String token;
    private final String owner;

    public GitHubClient(String token, String owner) {
        this.token = token;
        this.owner = owner;
    }

    public List<String> listRepositoryNames() throws IOException, InterruptedException {
        var names = new ArrayList<String>();
        String url = "https://api.github.com/users/" + owner + "/repos?type=owner&per_page=100";
        while (url != null) {
            var response = http.send(
                    HttpRequest.newBuilder(URI.create(url))
                            .header("Authorization", "Bearer " + token)
                            .header("Accept", "application/vnd.github+json")
                            .header("X-GitHub-Api-Version", "2022-11-28")
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "GitHub API returned " + response.statusCode() + ": " + response.body());
            }
            var repos = gson.fromJson(response.body(), JsonArray.class);
            for (var repo : repos) {
                names.add(repo.getAsJsonObject().get("name").getAsString());
            }
            url = nextUrl(response.headers().firstValue("Link").orElse(null));
        }
        return names;
    }

    static String nextUrl(String linkHeader) {
        if (linkHeader == null) {
            return null;
        }
        for (var part : linkHeader.split(",")) {
            var segments = part.trim().split(";");
            if (segments.length == 2 && segments[1].trim().equals("rel=\"next\"")) {
                return segments[0].trim().replaceAll("^<|>$", "");
            }
        }
        return null;
    }

}
