package com.example.kursa;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Parser {
    public List<Word> parseSkyengWords() throws IOException {
        String url = "https://skyeng.ru/articles/samye-populyarnye-slova-v-anglijskom-yazyke/";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        Response response = client.newCall(request).execute();
        Document document = Jsoup.parse(response.body().string());

        List<Word> words = new ArrayList<>();
        Elements rows = document.select("table tbody tr");

        for (Element row : rows) {
            Elements columns = row.select("td");
            if (columns.size() >= 4) {
                String word = columns.get(1).text().trim();
                String definition = columns.get(3).text().trim();
                    words.add(new Word(word, definition));
            }
        }

        return words;
    }
}
