package com.company;

public class Main {

    public static void main(String[] args) {
	    Huffman huffman=new Huffman();
	    String str="AAAABBAAAAAAAAAAAAAAAABBBBCCDDDBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";
	    huffman.encode(str);
    }
}
