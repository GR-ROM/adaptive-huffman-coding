package com.company;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class LZW {
    private int currentCode;
    private int codeLen=9;
    private int strIndex=0;

    private List<Integer> str=new ArrayList<>();
    private Map<List<Integer>, Integer> dictionary=new HashMap<>();

    public void encodeFile(String sourceFileName, String destinationFileName) throws IOException {
        int bytesRead = 0;
        byte[] inBuffer = new byte[128 * 1024];
        byte[] outBuffer = new byte[256 * 1024];
        int outBufferByteLength;
        File in = new File(sourceFileName);
        File out = new File(destinationFileName);
        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);

        long sec = System.currentTimeMillis();
        currentCode=this.initDictionary(dictionary, 8);
        bytesRead = fis.read(inBuffer);
        int i=inBuffer[0] & 0xFF;
        str.add(strIndex++, i);
        outBufferByteLength = encodeBlock(inBuffer, 1, bytesRead, outBuffer);
        fos.write(outBuffer, 0, outBufferByteLength);
        while (true) {
            bytesRead = fis.read(inBuffer);
            if (bytesRead <= 0) break;
            outBufferByteLength = encodeBlock(inBuffer, 0, bytesRead, outBuffer);
            fos.write(outBuffer, 0, outBufferByteLength);
            System.out.println(String.format("%.2f kilobytes", (float)outBufferByteLength / 1024));
        }
        sec = System.currentTimeMillis() - sec;
        fis.close();
        fos.close();

        System.out.println("File encoded successfully.");
        System.out.println("Input file size: " + String.format("%,d kilobytes", in.length() / 1024));
        System.out.println("Output file size: " + String.format("%,d kilobytes", out.length() / 1024));
        System.out.println("Compression rate: " + String.format("%.2f", (float) in.length() / out.length()));
        System.out.println("Compression speed: " + String.format("%.2f kb/s", ((float) in.length() / 1024) / ((float) sec / 1000)));
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

    private int initDictionary(Map<List<Integer>, Integer> dictionary, int maxCodeLen){
        int i;
        maxCodeLen=(1<<maxCodeLen);
        for (i=0;i!=maxCodeLen;i++){
            List<Integer> tmp=new ArrayList<>();
            tmp.add(i);
            dictionary.put(tmp, i);
        }
        return i;
    }

    private int initDictionary(List<String> dictionary){
        int i;
        for (i=0;i!=255;i++){
            dictionary.add(i, ""+(char)i);
        }
        return i;
    }

    private int getOrder(int value){
        int i=8;
        value>>=8;
        while (value!=0){
            value>>=1;
            i++;
        }
        return i;
    }

    public int encodeBlock(byte inBuffer[], int startIndex, int inBufLength, byte compressedBuffer[]){
        int sym;
        int outBitPos=0;
        for (int i=startIndex;i!=inBufLength;i++){
            sym=inBuffer[i] & 0xFF;
            str.add(strIndex++, sym);
            if (!dictionary.containsKey(str)){
                str.remove(--strIndex);
                Integer code=dictionary.get(str);
                if (code==null){
                    System.out.println("iter: "+i);
                }
                outBitPos=this.putBitsToBuffer(compressedBuffer, outBitPos, code, codeLen);
                str.add(strIndex++, sym);
                dictionary.put(new ArrayList<>(str), currentCode++);
                if (codeLen<getOrder(currentCode)) {
                    codeLen=getOrder(currentCode);
                    if (codeLen>12) {
                        outBitPos=this.putBitsToBuffer(compressedBuffer, outBitPos, 1<<codeLen, codeLen);
                        dictionary.clear();
                        initDictionary(dictionary, 8);
                        codeLen=9;
                    }

                }
                str.clear();
                strIndex=0;
                str.add(strIndex++, sym);
            }
        }
        return (outBitPos+8)/8;
    }

    public int decodeBlock(byte inBuffer[], int inBufLength, String decodedData){
        int oldCode;
        int newCode;
        int inBitLength=inBufLength*8;
        String str="";
        char sym;
        int currentCode;
        int codeLen=9;
        int inBitPos=0;
        List<String> dictionary=new ArrayList<>();
        currentCode=this.initDictionary(dictionary);
        oldCode=getBitsFromBuffer(inBuffer, inBitPos, codeLen);
        inBitPos+=codeLen;
        sym=(char)oldCode;
        decodedData=""+sym;
        while (inBitPos<inBitLength){
            newCode=getBitsFromBuffer(inBuffer, inBitPos, codeLen);
            inBitPos+=codeLen;
            if (dictionary.size()>newCode){
                str=dictionary.get(newCode);
            } else {
                str=dictionary.get(oldCode);
                str=str+sym;
            }
            decodedData=decodedData+str;
            sym=str.charAt(0);
            dictionary.add(currentCode++, dictionary.get(oldCode)+sym);
            if (codeLen<getOrder(currentCode+1))
                codeLen=getOrder(currentCode+1);
            oldCode=newCode;
        }
        return 0;
    }

}