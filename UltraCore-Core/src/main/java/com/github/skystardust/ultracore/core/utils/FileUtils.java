package com.github.skystardust.ultracore.core.utils;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

public class FileUtils {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static String readFileContent(File file) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Files.newReader(file, Charset.forName("UTF-8")).lines().forEach(stringBuilder::append);
        } catch (FileNotFoundException var3) {
            var3.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static void writeFileContent(File file, String content) {
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
