package com.promise;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

public class Main {

    public static void main(String[] args) {

        fetch("http://www.randomnumberapi.com/api/v1.0/random?min=0&max=5&count=1")
                .then(Response::text)
                .then(System.out::println);

        System.out.println("Before the request's response!");

    }

    public static class Response {

        HttpURLConnection connection;

        public Response(HttpURLConnection connection) throws ProtocolException {
            this.connection = connection;
            connection.setRequestMethod("GET");
        }

        public String text() throws IOException {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            connection.disconnect();
            return content.toString();
        }

    }

    public static <C> Promise.ContinualPromise<Response, C> fetch(String url) {
        return new Promise.ContinualPromise<>(resolver -> {
            resolver.resolve(new Response((HttpURLConnection) new URL(url).openConnection()));
        });
    }

}
