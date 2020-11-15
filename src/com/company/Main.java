package com.company;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Huffman huffman = new Huffman();
        List<Integer> s;
        try {
            if (args.length > 0) if (args[args.length - 1].equalsIgnoreCase("-v")) huffman.setVerbose(true);
            if (args.length > 1) {
                if (args[0].equalsIgnoreCase("-e")) {
                    if (args.length > 3) {
                        if (args[2].equalsIgnoreCase("-o")) {
                            huffman.encodeFile(args[1], args[3]);
                            huffman.decodeFile(args[3], args[1] + ".out");
                        }
                    } else System.out.println("Output filename is not specified!");
                }
                if (args[0].equalsIgnoreCase("-d")) {
                    if (args.length > 3) {
                        if (args[2].equalsIgnoreCase("-o")) {
                            //
                        }
                    } else System.out.println("Output filename is not specified!");
                }
            } else {
                if (args.length == 0) System.out.println("Use -? to list options.");
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("-?")) {
                        System.out.println("Using, encode file: huffman -e /test/input.file -o /test/output.file");
                        System.out.println("decode file: huffman -d /test/input.file -o /test/output.file");
                        System.out.println("Options:");
                        System.out.println("-e encode file");
                        System.out.println("-d decode file");
                        System.out.println("-o output file");
                        System.out.println("-v verbose output");
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
