package com.company;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class Huffman {

    public final static int ESC = 0xFFFF;
    public final static int EOB = 0x7FFF;

    private final static int NO_DATA= 0x7FF;
    private final static int DATA_READY=0xFF;

    //decoder context
    private TreeNode node;
    private int code = 0;
    private int c;
    private int codeLen = 0;
    private int inBitCount;
    private int outByteCount;

    private boolean verbose = false;
    private final HuffmanTree tree;

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public Huffman() {
        tree = new HuffmanTree();
    }

    public void initCoder() {
        code=0;
        codeLen=0;
        c=0;
        inBitCount=0;
        outByteCount=0;
        tree.reset();
    }

    private int putBitsToBuffer(byte[] buffer, int bitPos, int value, int valueBitLength, int mask) {
        for (int i = 0; i != valueBitLength; i++) {
            if ((value & mask) != 0) buffer[bitPos / 8] |= 1 << (7 - (bitPos % 8));
            else buffer[bitPos / 8] &= ~(1 << (7 - bitPos % 8));
            value <<= 1;
            bitPos++;
        }
        return bitPos;
    }

    private int getBitsFromBuffer(byte[] buffer, int bitPos, int valueBitLength){
        int result=0;
        for (int i=0;i!=valueBitLength;i++){
            if ((buffer[bitPos / 8] & 1<<(7-bitPos % 8))!=0)
                result|=1<<(7-i);
            else
                result&=~(1<<(7-i));
            bitPos++;
        }
        return result;
    }

    public void encodeFile(String sourceFileName, String destinationFileName) throws IOException {
        int bytesRead = 0;
        byte[] inBuffer = new byte[256*1024];
        byte[] outBuffer = new byte[256*1024];
        int outBufferBitLength;
        File in = new File(sourceFileName);
        File out = new File(destinationFileName);
        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        initCoder();

        long sec = System.currentTimeMillis();
        int blocks=0;
        while (true) {
            bytesRead = fis.read(inBuffer);
            if (bytesRead <= 0) break;
            outBufferBitLength = encodeBlock(inBuffer, bytesRead, outBuffer);
            if (blocks==0) tree.printTree();
            blocks++;
            fos.write(outBuffer, 0, outBufferBitLength/8);
            System.out.println(String.format("%.2f kilobytes", (float)outBufferBitLength/8/1024));
        }
        sec = System.currentTimeMillis() - sec;

        fis.close();
        fos.close();

        System.out.println("File encoded successfully.");
        if (this.verbose) {
            System.out.println("Input file size: " + String.format("%,d kilobytes", in.length() / 1024));
            System.out.println("Output file size: " + String.format("%,d kilobytes", out.length() / 1024));
            System.out.println("Compression rate: " + String.format("%.2f", (float) in.length() / out.length()));
            System.out.println("Compression speed: " + String.format("%.2f kb/s", ((float)in.length()/1024)/((float)sec/1000)));
        }
    }

    public void decodeFile(String sourceFileName, String destinationFileName) throws IOException {
        int bytesRead = 0;
        byte[] inBuffer = new byte[512*1024];
        byte[] outBuffer = new byte[256*1024];
        int outByteCount=0;
        File in = new File(sourceFileName);
        File out = new File(destinationFileName);
        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        initCoder();
        bytesRead = fis.read(inBuffer, 0, 256*1024);
        while (true) {
            inBitCount=0;
            outByteCount = decodeBlock(inBuffer, bytesRead, outBuffer);
            fos.write(outBuffer, 0, outByteCount);
            int pos=(inBitCount/8);
            int c=0;
            for (int i=pos;i!=bytesRead;i++){
                inBuffer[c++]=inBuffer[i];
            }
            bytesRead=fis.read(inBuffer, (256*1024-pos), 256*1024);
            if (bytesRead<=0) break;
        }
        fis.close();
        fos.close();
    }

    public int encodeBlock(byte[] inData, int inLength, byte[] compressedData) {
        int c;
        int outBitPos=0;
        for (int i = 0; i != inLength; i++) {
            c = inData[i];
            Integer counter = tree.getFrequency().get(c);
            tree.getFrequency().put(c, counter == null ? 1 : counter + 1);
            if (!tree.getCodes().containsKey(c)) {
                outBitPos = putBitsToBuffer(compressedData,
                        outBitPos,
                        tree.getCodeNode(ESC).getCode(),
                        tree.getCodeNode(ESC).getCodeLen(),
                        0x80000000);  // put ESC mark
                outBitPos = putBitsToBuffer(compressedData,
                        outBitPos,
                        c,
                        8,
                        0x80); // put new symbol as is
                tree.reBuildTree();
            } else {
                outBitPos = putBitsToBuffer(compressedData,
                        outBitPos,
                        tree.getCodeNode(c).getCode(),
                        tree.getCodeNode(c).getCodeLen(),
                        0x80000000); // put code of existing symbol
                tree.updateTree(c);
            }
        }
        outBitPos = putBitsToBuffer(compressedData,
                outBitPos,
                tree.getCodeNode(EOB).getCode(),
                tree.getCodeNode(EOB).getCodeLen(),
                0x80000000);  // put EOB mark
        int outBytePos=(outBitPos / 8)+1;
        outBitPos=outBytePos * 8;
        return outBitPos;
    }

    public int decodeBlock(byte[] inData, int inLength, byte[] deCompressedData){
        int outByteCount=0;
        while (true){
            if ((inData[inBitCount / 8] & (1<<(7-(inBitCount % 8))))!=0) code|=1<<(31-codeLen++);
            else code&=~(1<<(31-codeLen++));
            inBitCount++;
            node = tree.findNode(code, codeLen);
            if (node != null) {
                if (node.getC()==EOB) {
                    tree.printTree();
                    code = 0;
                    codeLen = 0;
                    int inBytePos=(inBitCount / 8)+1;
                    inBitCount=inBytePos * 8;
                    break;
                }
                if (node.getC() == ESC) {
                    c = getBitsFromBuffer(inData, inBitCount, 8);
                    inBitCount+=8;
                    Integer counter = tree.getFrequency().get(c);
                    tree.getFrequency().put(c, counter == null ? 1 : counter + 1);
                    tree.reBuildTree();
                } else {
                    c = node.getC();
                    Integer counter = tree.getFrequency().get(c);
                    tree.getFrequency().put(c, counter == null ? 1 : counter + 1);
                    tree.updateTree(node);
                }
                deCompressedData[outByteCount++] = (byte) c;
                code = 0;
                codeLen = 0;
            }
        }
        return outByteCount;
    }

}