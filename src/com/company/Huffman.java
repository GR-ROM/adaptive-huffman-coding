package com.company;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class Huffman {

    public final static int ESC = 0xFFFF;
    public final static int EOB = 0x7FFF;

    int CountReBuild = 0;
    int CountUpdate = 0;
    int CountTreeModified = 0;
    int timePutCodeToFile = 0;
    int timeTraverseTree = 0;
    int timeUpdateTree = 0;

    private boolean verbose = false;
    private final HuffmanTree tree;

    public Huffman() {
        tree = new HuffmanTree();
    }

    public void initCoder() {
        // debug/profiling values
        CountReBuild = 0;
        CountUpdate = 0;
        CountTreeModified = 0;
        timePutCodeToFile = 0;
        timeTraverseTree = 0;
        timeUpdateTree = 0;
    }

    private int putCodeToBuffer(byte[] buffer, int bitPos, TreeNode codeNode) {
        int code = codeNode.getCode();
        for (int i = 0; i != codeNode.getCodeLen(); i++) {
            if ((code & 0x80000000) != 0) {
                buffer[bitPos / 8] |= 1 << (7 - (bitPos % 8));
            } else {
                buffer[bitPos / 8] &= ~(1 << (7 - bitPos % 8));
            }
            code <<= 1;
            bitPos++;
        }
        return bitPos;
    }

    private int putSymbolToBuffer(byte[] buffer, int bitPos, int sym) {
        for (int i = 0; i != 8; i++) {
            if ((sym & 0x80) != 0) {
                buffer[bitPos / 8] |= 1 << (7 - (bitPos % 8));
            } else {
                buffer[bitPos / 8] &= ~(1 << (7 - (bitPos % 8)));
            }
            sym <<= 1;
            bitPos++;
        }
        return bitPos;
    }

    private void putCodeToFile(BitOutputStream bos, TreeNode codeNode) throws IOException {
        int code = codeNode.getCode();
        for (int i = 0; i != codeNode.getCodeLen(); i++) {
            bos.write((code & 0x80000000) != 0 ? 1 : 0);
            code <<= 1;
        }
    }

    private void putSymbolToFile(BitOutputStream bos, int sym) throws IOException {
        for (int i = 0; i != 8; i++) {
            bos.write((sym & 0x80) != 0 ? 1 : 0);
            sym <<= 1;
        }
    }

    private int getSymbolFromFile(BitInputStream bis) throws IOException {
        byte sym = 0;
        for (int i = 0; i != 8; i++) {
            sym <<= 1;
            sym |= (bis.read() > 0) ? 1 : 0;
        }
        return new Byte(sym).intValue();
    }

    public void encodeFile(String sourceFileName, String destinationFileName) throws IOException {
        int bytesRead = 0;
        byte[] inBuffer = new byte[65536];
        byte[] outBuffer = new byte[65536];
        int outBufferBitLength;
        File in = new File(sourceFileName);
        File out = new File(destinationFileName);
        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        initCoder();
        Random rand = new Random();
        long sec = System.currentTimeMillis();
        while (true) {
            bytesRead = fis.read(inBuffer);
            if (bytesRead <= 0) break;
            initCoder();
            outBufferBitLength = encodeBlock(inBuffer, bytesRead, outBuffer);
            fos.write(outBuffer, 0, outBufferBitLength/8);
            System.out.println(outBufferBitLength);
        }
        sec = System.currentTimeMillis() - sec;

        fis.close();
        fos.close();

        System.out.println("File encoded successfully.");
        if (this.verbose) {
            System.out.println("Total tree rebuilds: " + CountReBuild + ", total tree updates: " + CountUpdate + ", total tree modifications: " + CountTreeModified);
            System.out.println("Total time of putCodeToFile: " + timePutCodeToFile + " ms");
            System.out.println("Total time of traverseTree: " + timeTraverseTree + " ms");
            System.out.println("Total time of UpdateTree: " + timeUpdateTree + " ms");
            System.out.println("Input file size: " + String.format("%,d kilobytes", in.length() / 1024));
            System.out.println("Output file size: " + String.format("%,d kilobytes", out.length() / 1024));
            System.out.println("Compression rate: " + String.format("%.2f", (float) in.length() / out.length()));
            System.out.println("Compression speed: " + String.format("%.2f kb/s", ((float)in.length()/1024)/((float)sec/1000)));
        }
    }

    public int encodeBlock(byte[] inData, int inLength, byte[] compressedData) throws IOException {
        int c;
        int outBitPos=0;
        for (int i = 0; i != inLength; i++) {
            c = inData[i];
            Integer counter = tree.getFrequency().get(c);
            tree.getFrequency().put(c, counter == null ? 1 : counter + 1);
            if (!tree.getCodes().containsKey(c)) {
                outBitPos = putCodeToBuffer(compressedData, outBitPos, tree.getCodeNode(ESC));
                outBitPos = putSymbolToBuffer(compressedData, outBitPos, c);
                tree.reBuildTree();
                CountReBuild++;
            } else {
                long s = System.currentTimeMillis();
                outBitPos = putCodeToBuffer(compressedData, outBitPos, tree.getCodeNode(c));
                long e = System.currentTimeMillis();
                timePutCodeToFile += e - s;
                s = System.currentTimeMillis();
                tree.updateTree(c);
                e = System.currentTimeMillis();
                timeUpdateTree += e - s;
                CountUpdate++;
            }
        }
        outBitPos = putCodeToBuffer(compressedData, outBitPos, tree.getCodeNode(EOB)); // put end of block
        if (outBitPos % 8 != 0) outBitPos += 8 - (outBitPos % 8); // byte padding
        return outBitPos;
    }

    public void decode(String sourceFileName, String destinationFileName) throws IOException {
        TreeNode node;
        byte[] bufOut = new byte[16];
        int bufCount = 0;
        int code = 0;
        int c;
        int codeLen = 0;

        TreePrinter tp = new TreePrinter();
        FileInputStream fis = new FileInputStream(new File(sourceFileName));
        FileOutputStream fos = new FileOutputStream(new File(destinationFileName));
        BitInputStream bis = new BitInputStream(fis);
/*
        do {
            if (bis.read() != 0) code |= 1 << (31 - codeLen);
            codeLen++;
            node = findNode(code, codeLen);
            if (node != null) {
                if (node.getC() == ESC) {
                    c = getSymbolFromFile(bis);
                    Integer counter = frequency.get(c);
                    frequency.put(c, counter == null ? 1 : counter + 1);
                    reBuildTree();
                    //tp.printTree(getRoot(weights.get(0)));
                } else {
                    c = node.getC();
                    Integer counter = frequency.get(c);
                    frequency.put(c, counter == null ? 1 : counter + 1);
                    updateTree(codes.get(c));
                }
                bufOut[bufCount++] = (byte) c;
                c = 0;
                code = 0;
                codeLen = 0;
                if (bufCount == 16) {
                    fos.write(bufOut, 0, bufCount);
                    bufCount = 0;
                }
            }
        } while (!(bis.isEndOfStream()));
  */      fos.write(bufOut, 0, bufCount);
        fos.close();
        bis.close();
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}