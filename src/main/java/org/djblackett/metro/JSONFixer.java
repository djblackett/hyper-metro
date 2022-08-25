package org.djblackett.metro;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.LinkedList;

public class JSONFixer {
    public static Map<String, LinkedList<Vertex>> parseStationsFile(String filePath) throws IOException {
        Map<String, LinkedList<Vertex>> linesMap;
        Type type = new TypeToken<HashMap<String, LinkedList<Vertex>>>() {
        }.getType();
        Gson gson = new Gson();
        try (JsonReader reader = new JsonReader(new FileReader(filePath))) {
            linesMap = gson.fromJson(reader, type);
            return linesMap;
        } catch (com.google.gson.JsonIOException | com.google.gson.JsonSyntaxException e) {
            System.out.println("Incorrect file");
        }
        return null;
    }
}
