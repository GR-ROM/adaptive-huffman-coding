package com.company;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Huffman huffman = new Huffman();
        LZW lzw=new LZW();
        String dataIn="";
        String dataOut="";
        /*try {
            lzw.encodeFile("C:/Users/exp10/Desktop/LAND2..BMP", "C:/Users/exp10/Desktop/LAND2.BMP.COMP");
        }
        catch (IOException e) {
            e.printStackTrace();
        }*/
        List<Integer> s;
        try {
                            huffman.encodeFile(args[0], args[1]);
                         //   huffman.decodeFile(args[3], args[1] + ".out");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
