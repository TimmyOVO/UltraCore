package com.github.skystardust.ultracore.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.skystardust.ultracore.core.configuration.utils.ConfigurationFormatConverter;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FileUtils {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static boolean ENABLE_YAML_MODE = true;

    public static String readFileContent(File file) {
        String json = null;
        File yamlFile = new File(file.getPath() + ".yml");
        if (ENABLE_YAML_MODE && yamlFile.exists()){
            try {
                String jsonContent = readFile(yamlFile);
                json = ConfigurationFormatConverter.fromJson(jsonContent).toJSON();
                writeFile(file,json);
            } catch (FileNotFoundException | JsonProcessingException e) {
                e.printStackTrace();
            }
        }else {
            try {
                json = readFile(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return json;
    }

    public static void writeFileContent(File file, String content) {
        if (ENABLE_YAML_MODE){
            try {
                writeFile(new File(file.getPath()+".yml"),ConfigurationFormatConverter.fromJson(content).toYAML());
            } catch (JsonProcessingException ignored) {

            }
        }
        writeFile(file,content);
    }

    static String readFile(File file) throws FileNotFoundException {
        StringBuilder stringBuilder = new StringBuilder();
        Files.newReader(file, StandardCharsets.UTF_8).lines().forEach(str->{
            stringBuilder.append(str);
            stringBuilder.append("\n");
        });
        return stringBuilder.toString();
    }

    static void writeFile(File file,String content){
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = Files.newWriter(file, Charset.forName("UTF-8"));
            bufferedWriter.write(content);
            bufferedWriter.close();
        } catch (IOException var4) {
            var4.printStackTrace();
        }
    }
}

