package com.inj.rle;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class RLDecoder {
    public byte[] decode(List<String> input)  {

        Map<byte[], List<Integer>> results = input.stream().filter(l -> !l.isEmpty()).map(line -> {
            String[] tokenValuePair = line.split(":",  -1);
            String[] positions = tokenValuePair[1].replaceAll("[]\\[ ]", "").split(",");
            List<Integer> pos = Arrays.stream(positions).map(Integer::parseInt).collect(Collectors.toList());
            return new AbstractMap.SimpleEntry<>(tokenValuePair[0].isEmpty() ? new byte[]{ 13, 10} : tokenValuePair[0].getBytes(), pos);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        int max = results.values().stream()
                .flatMap(List::stream)
                .max(Comparator.naturalOrder()).orElse(0) + 2;

        System.out.println("Encoded file read. Size: " + results.size() + " output file size: " + max);

        byte[] outputBytes = new byte[max];
        results.forEach((token, value) -> value.forEach(i -> {
            outputBytes[i] = token[0];
            outputBytes[i + 1] = token[1];
        }));

        return outputBytes;
    }

    public void decode(String filename) throws IOException, URISyntaxException {
        Path encodedFile = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
        List<String> input = Files.readAllLines(encodedFile);

        byte[] outputBytes = decode(input);
        Files.write(Paths.get(filename + "_decoded.b64"), outputBytes);
    }

}
