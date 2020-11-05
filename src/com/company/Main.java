package com.company;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Huffman huffman = new Huffman();
        List<Integer> s;
        try {
            s=huffman.encode("C:/Users/exp10/Documents/input.txt", "C:/Users/exp10/Documents/compressed.bin");
            System.out.println(s);

            huffman.decode("C:/Users/exp10/Documents/compressed.bin", "C:/Users/exp10/Documents/decompressed.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
