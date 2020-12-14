package com.company;

import java.io.IOException;
import java.util.List;

public class Main {

    public static byte[] hexStringToByteArray(String s) {

        s = s.replaceAll("\\s+","");
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static void main(String[] args) throws IOException {
        Huffman huffman = new Huffman();
        byte[] test=hexStringToByteArray("00 A7 65 93 F6 6A 1A B6 69 AF A4 65 4B 75 30 E1 C2 86 0A 19 22 8C 88 ED D5 C2 6C AF B0 A5 CA E6 EA 62 C7 54 74 EA 10 A1 F3 86 0E 9D 22 74 DC D4 A1 43 A4 8E 1B 3A FB 62 6A CC 36 D3 15 C2 86 38 6F EA CC 78 F3 D5 AB 54 1D 5D F9 E4 98 2A E3 50 57 2E E9 2C 71 A3 A4 E4 C9 94 75 94 B8 59 1A 73 DA C7 8A 56 B3 65 65 58 F0 6A D7 87 05 8D 62 B4 88 CA A2 59 8E 08 87 68 28 A9 32 E5 52 A5 2F 5D AE DC 77 71 26 CE 8D 33 EB 6E 7C F8 10 21 46 82 D9 50 61 B4 29 74 A3 50 22 49 88 28 11 F9 F4 E9 54 A6 53 F7 75 24 88 D0 EA C4 BE 36 ED 4A 4C 68 D1 66 67 B3 1D CD 56 CC 48 84 C8 90 91 4E D9 32 96 9B D2 2F C1 86 03 C9 62 DC 3A 33 23 DE 8D AF 3C 07 C6 C8 55 70 58 C4 A5 D5 3E 6D 1A 72 E4 D2 C5 93 2D 66 15 BC 9B 2F C3 BF CE 2B 56 1C 3C 76 34 46 8E AF 4E 03 57 42 E4 65 49 A9 24 A1 BA");
        byte[] output=new byte[4096];
        byte[] decompressed=new byte[4096];
        byte[] dataIn;
        String s="TOBEORNOTTOBEORTOBEORNOT";
        dataIn=s.getBytes();
        LZW lzw=new LZW();
        lzw.encodeFile("C:\\Users\\exp10\\Desktop\\LAND2.BMP", "C:\\Users\\exp10\\Desktop\\LAND2.BMP.LZW");
//        lzw.encodeBlock(dataIn, 0, dataIn.length, output);
   //     lzw.decodeBlock(test, test.length, decompressed);
        /*try {
            lzw.encodeFile("C:/Users/exp10/Desktop/LAND2..BMP", "C:/Users/exp10/Desktop/LAND2.BMP.COMP");
        }
        catch (IOException e) {
            e.printStackTrace();
        }*/
        try {
                            huffman.encodeFile(args[0], args[1]);
                         //   huffman.decodeFile(args[3], args[1] + ".out");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
