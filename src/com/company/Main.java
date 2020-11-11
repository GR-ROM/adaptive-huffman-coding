package com.company;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Huffman huffman = new Huffman();
        List<Integer> s;
        try {
            s = huffman.encode("C:/Users/exp10/OneDrive/Documents/access.log", "C:/Users/exp10/OneDrive/Documents/compressed.bin");
            System.out.println(s);

            huffman.decode("C:/Users/exp10/OneDrive/Documents/compressed.bin", "C:/Users/exp10/OneDrive/Documents/decompressed.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
