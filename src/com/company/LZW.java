package com.company;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class LZW {
    private final static int CC=0x100;
    private final static int EOD=0x101;

    private int currentCode;
    private int codeLen=9;
    int maxCode=0;

    private List<Integer> str=new ArrayList<>();
    private Map<List<Byte>, Integer> dictionary=new HashMap<>();
    private Map<Integer, List<Byte>> revDictionary=new HashMap<>();

    public void encodeFile(String sourceFileName, String destinationFileName) throws IOException {
        int bytesRead = 0;
        byte[] raw = new byte[64 * 1024];
        byte[] compressed = new byte[64 * 1024];
        byte[] outBuffer = new byte[64 * 1024];
        byte[] decomp = new byte[64 * 1024];
        int outBufferByteLength;
        File in = new File(sourceFileName);
        File out = new File(destinationFileName);
        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);

        long sec = System.currentTimeMillis();
        currentCode=this.initDictionary(dictionary, 8)+1;
        codeLen=9;
        maxCode=512;
        bytesRead = fis.read(raw);

        outBufferByteLength = encodeBlock(raw, 0, bytesRead, compressed);
        currentCode=this.initRevDictionary(revDictionary, 8)+1;
        codeLen=9;
        maxCode=512;
        decomp=decodeBlock(compressed, outBufferByteLength, decomp);
        fos.write(decomp, 0, 64 * 1024);
        fos.close();
        while (true) {
            bytesRead = fis.read(raw);
            if (bytesRead <= 0) break;
            outBufferByteLength = encodeBlock(raw, 0, bytesRead, outBuffer);


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

    private int putBitsToBuffer(byte[] buffer, int bitPos, int code, int valueBitLength) {
        int bytePos=bitPos / 8;
        byte val=buffer[bytePos];
        int bPos=bitPos % 8;
        int mask_pos=0;
        int counter=valueBitLength;
        while(counter-->0) {
            val|=(((code & (1 << mask_pos++))!=0) ? 1 : 0) << bPos++;
            if (bPos==8){
                bPos=0;
                buffer[bytePos++]=val;
                val=0;
            }
        }
        buffer[bytePos++]=val;
        return bitPos+valueBitLength;
    }

    private int getBitsFromBuffer(byte[] buffer, int bitPos, int valueBitLength) {
        int bytePos=bitPos / 8;
        int code=0;
        int val=buffer[bytePos++];
        bitPos=bitPos % 8;
        int mask_pos=0;
        while(valueBitLength-->0) {
            code|=(((val & (1 << bitPos++))!=0) ? 1 : 0) << mask_pos++;
            if (bitPos==8){
                bitPos=0;
                val=buffer[bytePos++];
            }
        }
        return code;
    }

    private int initDictionary(Map<List<Byte>, Integer> dictionary, int maxCodeLen){
        int i;
        dictionary.clear();
        maxCodeLen=(1<<maxCodeLen);
        for (i=0;i!=maxCodeLen;i++){
            ArrayList<Byte> t=new ArrayList<>();
            t.add(0, (byte)i);
            dictionary.put(t, i);
        }
        return i;
    }

    private int initRevDictionary(Map<Integer, List<Byte>> dictionary, int maxCodeLen){
        int i;
        dictionary.clear();
        maxCodeLen=(1<<maxCodeLen);
        for (i=0;i!=maxCodeLen;i++){
            List<Byte> s=new ArrayList<>();
            s.add(0, (byte)i);
            dictionary.put(i, s);
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
        byte sym=0;
        int i=1;
        int outBitPos=0;
        List<Byte> str=new ArrayList<>();
        str.add(0, inBuffer[0]);
        while(i!=inBufLength){
            sym=inBuffer[i++];
            List<Byte> istr=new ArrayList<>(str);
            istr.add(istr.size(), sym);
            if (!dictionary.containsKey(istr)){
                Integer code=dictionary.get(str);
                outBitPos=this.putBitsToBuffer(compressedBuffer, outBitPos, code, codeLen);
                dictionary.put(istr, currentCode++);
                if (currentCode==maxCode) {
                    maxCode=1<<(++codeLen);
                    if (codeLen>12) {
                        outBitPos=this.putBitsToBuffer(compressedBuffer, outBitPos, 256, codeLen-1);
                        currentCode=initDictionary(dictionary, 8)+1;
                        codeLen=9;
                        maxCode=512;
                        i--;
                        str.clear();
                        str.add(0, inBuffer[i++]);
                        continue;
                    }
                }
                str.clear();
                str.add(0, sym);
            } else str=istr;
        }
        return (outBitPos+8)/8;
    }

    public byte[] decodeBlock(byte inBuffer[], int inBufLength, byte decodedData[]){
        int inBitLength=inBufLength*8;
        int inBitPos=0;
        int outBytePos=0;
        int newCode;
        int oldCode=getBitsFromBuffer(inBuffer, inBitPos, codeLen)  & 0x0FFF;
        inBitPos+=codeLen;
        int sym=oldCode;
        decodedData[outBytePos++]=(byte)sym;
        while (inBitPos<inBitLength){
            if (currentCode+1==maxCode)
                maxCode=1<<++codeLen;
            if (codeLen>12) {
                newCode = getBitsFromBuffer(inBuffer, inBitPos, codeLen - 1) & 0x0FFF;
                inBitPos+=codeLen-1;
            }
            else {
                newCode = getBitsFromBuffer(inBuffer, inBitPos, codeLen) & 0x0FFF;
                inBitPos += codeLen;
            }
            if(newCode==256){
                currentCode=initRevDictionary(revDictionary, 8)+1;
                maxCode=512;
                codeLen=9;
                oldCode=getBitsFromBuffer(inBuffer, inBitPos, codeLen)  & 0x0FFF;
                inBitPos+=codeLen;
                sym=oldCode;
                decodedData[outBytePos++]=(byte)sym;
                continue;
            }
            List<Byte> str;
            if (revDictionary.containsKey(newCode)){
                str=new ArrayList<>(revDictionary.get(newCode));
            } else {
                str=new ArrayList<>(revDictionary.get(oldCode));
                str.add(str.size(), (byte)sym);
            }
            for (int i=0;i!=str.size();i++)
                decodedData[outBytePos++]=str.get(i);
            sym=str.get(0);
            List<Byte> t=new ArrayList<>(revDictionary.get(oldCode));
            t.add(t.size(), (byte)sym);
            revDictionary.put(currentCode++, t);

            oldCode=newCode;
        }
        return decodedData;
    }

}