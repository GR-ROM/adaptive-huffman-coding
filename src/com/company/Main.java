package com.company;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Huffman huffman = new Huffman();
        List<Integer> s;
        try {
            if (args.length>1){
                if (args[0].equalsIgnoreCase("-e")){
                    huffman.encode(args[1], args[1]+".huf");
                }
                if (args[0].equalsIgnoreCase("-d")){
                    String ext=args[1].substring(args[1].lastIndexOf('.'), args[1].length());
                    if (ext.equalsIgnoreCase(".huf")) {
                        String fileName = args[1].substring(0, args[1].lastIndexOf('.'));
                        huffman.decode(args[1], fileName);
                    } else {
                        if (args.length>3){
                            if (args[2].equalsIgnoreCase("-o")) {
                                huffman.decode(args[1], args[3]);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
