package com.inj.rle;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * My own custom run length encoding.
 * It's not for compression purpose.
 * This is design as a PoC to distribute a file asynchronized via one or more channel
 */
public class RLEncoder {
    public Map<byte[], List<Integer>> encodeParallel(byte[] inputBytes, int parallelism) throws IOException {
        byte[] outputBytes = new byte[inputBytes.length];

        int sliceSize = Math.floorDiv(inputBytes.length, parallelism);
        if (sliceSize % 2 != 0) sliceSize -= 1;
        Map<Integer, Integer> slices = new HashMap<>();

        for (int i = 0; i < parallelism-1; i++) {
            byte[] slice = Arrays.copyOfRange(inputBytes, i * sliceSize , i * sliceSize + sliceSize);
            slices.put(i * sliceSize, sliceSize);
        }
        // last slice
        byte[] slice = Arrays.copyOfRange(inputBytes, (parallelism-1) * sliceSize , inputBytes.length);
        slices.put((parallelism-1) * sliceSize, (inputBytes.length - (parallelism-1) * sliceSize));

        System.out.println(slices);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Future<Map<byte[], List<Integer>>>> futures = new ArrayList<>();
        slices.entrySet().forEach( e -> {
            Future<Map<byte[], List<Integer>>> result  = executorService.submit( () -> encode(inputBytes, e.getKey(), e.getValue()));
            futures.add(result);
        });

        Map<byte[], List<Integer>> results = new HashMap<>();
        for (Future<Map<byte[], List<Integer>>> task : futures) {
            try {
                task.get().forEach( (k,v) -> {
                    if (results.get(k) == null) {
                        results.put(k, v);
                    } else {
                        results.get(k).addAll(v);
                    }
                });

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executorService.shutdown();
        return results;
    }

    public Map<byte[], List<Integer>> encode(byte[] inputBytes, int from, int length) throws IOException {
        assert from >= 0;
        assert length > 2;
        assert inputBytes != null;
        assert inputBytes.length >= length;

        byte[] outputBytes = new byte[length];

        Arrays.fill(outputBytes, (byte) 0);
        Map<byte[], List<Integer>> results = new HashMap<>();

        int tokenPointer = from;

        byte[] token = new byte[2];
        int count = 0;
        while(tokenPointer < (from + length) ) {

            if (outputBytes[tokenPointer - from] != 0) {
                tokenPointer += 2;
                continue;
            }

            token = Arrays.copyOfRange(inputBytes, tokenPointer, tokenPointer+2);

            int pointer = tokenPointer;
            List<Integer> offsets = new ArrayList<>();
            while(pointer < (from + length) ) {
                if (token[0] == inputBytes[pointer] &&
                        token[1] == inputBytes[pointer + 1]) {
                    offsets.add(pointer);
                    outputBytes[pointer - from] = token[0];
                    outputBytes[pointer - from + 1] = token[1];
                }
                pointer += 2;
            }
            results.put(token, offsets);
            tokenPointer += 2;
        }

//        int suffix = 0;
//        while (Files.exists(Paths.get("output.out." + suffix))) {
//            suffix++;
//        }
//        Files.write(Paths.get("output.out." + suffix), outputBytes);

        return results;

    }

    private void writeEncodedFile(Map<byte[], List<Integer>> results, String filename) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(filename))) {
            results.entrySet().stream().forEach( e -> {
                try {
                    bw.write("" + (char)e.getKey()[0] + (char)e.getKey()[1] + ':' + e.getValue());
                    bw.newLine();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
//                System.out.println(new String(e.getKey()) + ":" + e.getValue().size());
            });
        }
        System.out.println("No. of token: " + results.size());

    }

    public void encode(String filename) throws IOException, URISyntaxException {
        Path input = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
        System.out.println("Encoding file: " + input);

        StringBuilder sb = new StringBuilder();
        Files.readAllLines(input).forEach(sb::append);
        byte[] inputBytes = sb.toString().getBytes();

        Map<byte[], List<Integer>> results = encode(inputBytes, 0, inputBytes.length);
        System.out.println("Input size:   " + inputBytes.length);
        writeEncodedFile(results, filename + "_encoded.csv");
    }


    public void encodeParallel(String filename, int parallelism) throws IOException, URISyntaxException {
        Path input = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
        System.out.println("Encoding file: " + input);

        StringBuilder sb = new StringBuilder();
        Files.readAllLines(input).forEach(sb::append);
        byte[] inputBytes = sb.toString().getBytes();

        Map<byte[], List<Integer>> results = encodeParallel(inputBytes, parallelism);

        System.out.println("Input size:   " + inputBytes.length);
        writeEncodedFile(results, filename + "_encodedParallel.csv");
    }

}
