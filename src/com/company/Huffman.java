package com.company;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Huffman {

    public final static int ESC = 0xFFFF;
    public final static int EOB = 0x7FFF;

    private final static int NO_DATA = 0x7FF;
    private final static int DATA_READY = 0xFF;
    private final HuffmanTree treeComp;
    private final HuffmanTree treeDecomp;
    //decoder context
    private TreeNode node;
    private int code = 0;
    private int c;
    private int codeLen = 0;
    private int inBitCount;
    private int outByteCount;
    private boolean verbose = false;

    public Huffman() {
        treeComp = new HuffmanTree();
        treeDecomp=new HuffmanTree();
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void initCoder() {
        code = 1;
        codeLen = 0;
        c = 0;
        inBitCount = 0;
        outByteCount = 0;
        treeComp.reset();
        treeDecomp.reset();
    }

    private int putBitsToBuffer(byte[] buffer, int bitPos, int value, int valueBitLength) {
        for (int i = 0; i != valueBitLength; i++) {
            if ((value & (1<<(valueBitLength-1))) != 0) buffer[bitPos / 8] |= 1 << (7 - (bitPos % 8));
            else buffer[bitPos / 8] &= ~(1 << (7 - bitPos % 8));
            value <<= 1;
            bitPos++;
        }
        return bitPos;
    }

    private int getBitsFromBuffer(byte[] buffer, int bitPos, int valueBitLength) {
        int result = 0;
        for (int i = 0; i != valueBitLength; i++) {
            result<<=1;
            if ((buffer[bitPos / 8] & 1 << (7 - bitPos % 8)) != 0)
                result |= 1;
            bitPos++;
        }
        return result;
    }

    public void encodeFile(String sourceFileName, String destinationFileName) throws IOException {
        int bytesRead = 0;
        byte[] inBuffer = new byte[64];
        byte[] outBuffer = new byte[128];
        byte[] decodedBuffer = new byte[128];
        int outBufferByteLength;
        File in = new File(sourceFileName);
        File out = new File(destinationFileName);
        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        initCoder();

        long sec = System.currentTimeMillis();
        int blocks = 0;
        while (true) {
            bytesRead = fis.read(inBuffer);
            if (bytesRead <= 0) break;

            outBufferByteLength = encodeBlock(inBuffer, bytesRead, outBuffer);
            decodeBlock(outBuffer, outBufferByteLength, decodedBuffer);

            System.out.println(String.format("%.2f kilobytes", (float)outBufferByteLength / 1024));
        }
        sec = System.currentTimeMillis() - sec;

        fis.close();
        fos.close();

        System.out.println("File encoded successfully.");
        if (this.verbose) {
            System.out.println("Input file size: " + String.format("%,d kilobytes", in.length() / 1024));
            System.out.println("Output file size: " + String.format("%,d kilobytes", out.length() / 1024));
            System.out.println("Compression rate: " + String.format("%.2f", (float) in.length() / out.length()));
            System.out.println("Compression speed: " + String.format("%.2f kb/s", ((float) in.length() / 1024) / ((float) sec / 1000)));
        }
    }

    public void decodeFile(String sourceFileName, String destinationFileName) throws IOException {
        int bytesRead = 0;
        byte[] inBuffer = new byte[512 * 1024];
        byte[] outBuffer = new byte[256 * 1024];
        int outByteCount = 0;
        File in = new File(sourceFileName);
        File out = new File(destinationFileName);
        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        initCoder();
        bytesRead = fis.read(inBuffer, 0, 256 * 1024);

        while (true) {
            inBitCount = 0;
            outByteCount = decodeBlock(inBuffer, bytesRead, outBuffer);
            fos.write(outBuffer, 0, outByteCount);
            int pos = (inBitCount / 8);
            int c = 0;
            for (int i = pos; i != bytesRead; i++) inBuffer[c++] = inBuffer[i];
            bytesRead = fis.read(inBuffer, bytesRead - pos, 256 * 1024);
            if (bytesRead <= 0) break;
        }
        fis.close();
        fos.close();
    }

    public int encodeBlock(byte[] inData, int inLength, byte[] compressedData) {
        int c;
        int outBitPos = 0;
        for (int i = 0; i != inLength; i++) {
            c = inData[i];
            Integer counter = treeComp.getFrequency().get(c);
            treeComp.getFrequency().put(c, counter == null ? 1 : counter + 1);
            if (i==61){
                System.out.println("============================================");
                treeComp.printTree();
            }

            if (!treeComp.getCodes().containsKey(c)) {
                outBitPos = putBitsToBuffer(compressedData,
                        outBitPos,
                        treeComp.getCodeNode(ESC).getCode(),
                        treeComp.getCodeNode(ESC).getCodeLen());  // put ESC mark
                outBitPos = putBitsToBuffer(compressedData,
                        outBitPos,
                        c,
                        8); // put new symbol as is
                treeComp.rebuild();
               // tree.printTree();
            } else {
                outBitPos = putBitsToBuffer(compressedData,
                        outBitPos,
                        treeComp.getCodeNode(c).getCode(),
                        treeComp.getCodeNode(c).getCodeLen()); // put code of existing symbol
                treeComp.rebuild();
                //treeComp.update(c);
            }
        }
        outBitPos = putBitsToBuffer(compressedData,
                outBitPos,
                treeComp.getCodeNode(EOB).getCode(),
                treeComp.getCodeNode(EOB).getCodeLen());  // put EOB mark
        outBitPos += 8-(outBitPos % 8);
        outBitPos+=8;
        return outBitPos / 8;
    }

    public int decodeBlock(byte[] inData, int inLength, byte[] deCompressedData) {
        int outByteCount = 0;
        inBitCount=0;
        code=0;
        codeLen=0;
        while (true) {
            code<<=1;
            if ((inData[inBitCount / 8] & (1 << (7 - (inBitCount % 8)))) != 0) code |= 1;
            codeLen++;
            inBitCount++;
            node = treeDecomp.findNode(code | 1<<codeLen, codeLen);
            if (node != null) {
                if (node.getC() == EOB) {
                    treeDecomp.printTree();
                    code = 0;
                    codeLen = 0;
                    inBitCount += 8-(inBitCount % 8);
                    inBitCount+=8;
                    break;
                } else
                if (node.getC() == ESC) {
                    c = getBitsFromBuffer(inData, inBitCount, 8);
                    inBitCount += 8;
                    Integer counter = treeDecomp.getFrequency().get(c);
                    treeDecomp.getFrequency().put(c, counter == null ? 1 : counter + 1);
                    treeDecomp.rebuild();
                } else {
                    c = node.getC();
                    Integer counter = treeDecomp.getFrequency().get(c);
                    treeDecomp.getFrequency().put(c, counter == null ? 1 : counter + 1);
                   // treeDecomp.update(node);
                    treeComp.rebuild();
                }
                deCompressedData[outByteCount++] = (byte) c;
                if (outByteCount==61){
                    System.out.println("-------------------------------------------------------");
                    treeComp.printTree();
                }
                code = 0;
                codeLen = 0;
            }
        }
        return outByteCount;
    }

}