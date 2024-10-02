package com.inj.rle;

import java.io.IOException;
import java.net.URISyntaxException;
//TIPS
//TEST
public class Main {

    private void run() throws URISyntaxException, IOException {
        //TIP
        // test
        //
        RLEncoder encoder = new RLEncoder();
        encoder.encode("sample.b64");
        encoder.encodeParallel("sample.b64", 8);

        RLDecoder decoder = new RLDecoder();
        decoder.decode("sample.b64_encoded.csv");
        decoder.decode("sample.b64_encodedParallel.csv");
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        System.out.println("Hello and RLE!");

        Main app = new Main();
        app.run();

    }
}
