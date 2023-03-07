package com.promise;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main {

    public static void main(String[] args) {

        fetch("http://www.randomnumberapi.com/api/v1.0/random?min=100&max=1000&count=5")
                .thenc(Response::text)
                .then(System.out::println);

    }

    public static class Response {

        HttpURLConnection connection;

        public Response(String location) throws IOException {
            connection = (HttpURLConnection) new URL(location).openConnection();
            connection.setRequestMethod("GET");
        }

        private Promise<String> get() {
            return Promise.async(() -> {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                connection.disconnect();
                return content.toString();
            });
        }

        public Promise<String> text() {
            return Promise.async(this::get);
        }

    }

    public static Promise<Response> fetch(String url) {
        return Promise.async(() -> new Response(url));
    }

}