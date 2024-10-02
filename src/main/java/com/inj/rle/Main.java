package com.inj.rle;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {

    private void encode(String filename) throws URISyntaxException, IOException {
        RLEncoder encoder = new RLEncoder();
        encoder.encode(filename);
        encoder.encodeParallel(filename, 8);

        RLDecoder decoder = new RLDecoder();
        decoder.decode(filename + "_encoded.csv");
        decoder.decode(filename + "_encodedParallel.csv");
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        System.out.println("Hello and RLE!");
        if (args.length < 1) {
            System.out.println("Usage: java -jar com.inj.rle.Main [filename]");
            System.exit(1);
        }
        String filename = args[0];

        Main app = new Main();
        app.encode(filename);

    }
}
