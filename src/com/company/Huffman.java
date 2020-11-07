package com.company;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class Huffman {

    private final char ESC = 0xFF;
    private int minCodeLen;
    private TreeNode root;
    private Map<Character, Integer> frequency;
    private Map<Character, TreeNode> codes;
    private List<TreeNode> weights;

    private void addNewNode(TreeNode newNode) {
        TreeNode escNode = weights.get(0);
        TreeNode parent = escNode.getParent();
        TreeNode middle = new TreeNode(null, escNode.getWeight() + newNode.getWeight());
        if (parent != null) {
            if (parent.getLeft() == escNode) parent.setLeft(middle);
            else parent.setRight(middle);
        }
        middle.setLeft(escNode);
        middle.setRight(newNode);
        escNode.setParent(middle);
        newNode.setParent(middle);
        weights.add(newNode);
        Collections.sort(weights);
    }

    private void traverseTree(TreeNode node, byte[] code, int codeLen) {
        if (node.getLeft() != null){
            code[codeLen] = 0;
            traverseTree(node.getLeft(), code, codeLen + 1);
        }
        if (node.getRight() != null) {
            code[codeLen] = 1;
            traverseTree(node.getRight(), code, codeLen + 1);
        }
        if (node.getC() != null) {
            node.setCodeLen(codeLen + 1);
            if (minCodeLen > codeLen + 1) minCodeLen = codeLen + 1;
            node.setCode(code);
            codes.put(node.getC(), node);
        }
    }

    private TreeNode getRoot(TreeNode node) {
        while (node.getParent() != null) node = node.getParent();
        return node;
    }

    private void updateTree(TreeNode node) {
        if (node.getParent() == null) return;
        TreeNode inode = node;
        node.setWeight(node.getWeight() + 1);
        while (node.getParent() != null) {
            inode = node.getParent();
            inode.setWeight(inode.getWeight() + 1);
            for (int i = weights.indexOf(node) + 1; i != weights.size(); i++) {
                TreeNode cnode = weights.get(i);
                if (node.getWeight() > cnode.getWeight()) {
                    TreeNode.SwapNodes(node, cnode);
                    Collections.sort(weights);
                }
            }
            if (inode != node.getParent()) inode.setWeight(inode.getWeight() - 1);
            else {
                node = node.getParent();
                node.setWeight(node.getLeft().getWeight() + node.getRight().getWeight());
            }
        }
        codes.clear();
        minCodeLen = 32;
        traverseTree(getRoot(weights.get(0)), new byte[32], 0);
    }

    private TreeNode reBuildTree() {
        List<TreeNode> f = new ArrayList<>();
        weights.clear();
        for (Character c : frequency.keySet()) {
            TreeNode node = new TreeNode(c, frequency.get(c));
            f.add(node);
            weights.add(node);
        }
        TreeNode left;
        TreeNode right;
        while (f.size() > 1) {
            Collections.sort(f);
            left = f.get(0);
            f.remove(0);
            right = f.get(0);
            f.remove(0);
            TreeNode node = new TreeNode(left, right);
            f.add(node);
            weights.add(node);
        }
        Collections.sort(weights);
        codes.clear();
        minCodeLen = 32;
        traverseTree(f.get(0), new byte[32], 0);
        return f.get(0);
    }

    private void putCodeToFile(BitOutputStream bos, TreeNode codeNode) throws IOException {
        byte[] code = codeNode.getCode();
        for (int i = 0; i != codeNode.getCodeLen(); i++) {
            bos.write(code[i]);
        }
    }

    private void putSymbolToFile(BitOutputStream bos, char sym) throws IOException {
        for (int i = 0; i != 8; i++) {
            bos.write((sym & 0x80) >> 7);
            sym <<= 1;
        }
    }

    private char getSymbolFromFile(BitInputStream bis) throws IOException {
        char sym = 0;
        for (int i = 0; i != 8; i++) {
            sym <<= 1;
            if (bis.read() > 0)
                sym |= 1;
        }
        return sym;
    }

    public List<Integer> encode(String sourceFileName, String destinationFileName) throws IOException {
        weights = new ArrayList<>();
        char c;
        int bytesRead = 0;
        byte[] inputBuffer = new byte[16];
        frequency = new TreeMap<>();
        frequency.clear();
        frequency.put(ESC, 0);
        codes = new HashMap<>();
        root = reBuildTree();
        TreePrinter tp = new TreePrinter();
        tp.printTree(getRoot(weights.get(0)));
        List<Integer> result = new ArrayList<>();

        FileInputStream fis = new FileInputStream(new File(sourceFileName));
        FileOutputStream fos = new FileOutputStream(new File(destinationFileName));
        BitOutputStream bos = new BitOutputStream(fos);
int count=0;
        while (true) {
            bytesRead = fis.read(inputBuffer);

            if (bytesRead < 0) break;
            for (int i = 0; i != bytesRead; i++) {
                c = (char) inputBuffer[i];
                Integer counter = frequency.get(c);
                frequency.put(c, counter == null ? 1 : counter + 1);
                if (!codes.containsKey(c)) {
                    putCodeToFile(bos, codes.get(ESC));
                    result.add(codes.get(ESC).getIntCode());
                    putSymbolToFile(bos, c);
                    result.add((int) c);
                    addNewNode(new TreeNode(c, 1));
                    reBuildTree();
                } else {
                    putCodeToFile(bos, codes.get(c));
                    result.add(codes.get(c).getIntCode());
                    updateTree(codes.get(c));
                }
                if (count>=20)
                    System.out.println(weights.toString());//tp.printTree(getRoot(weights.get(0)));
                count++;
            }
        }
        bos.close();
        fis.close();
        System.out.println(result.toString());
        return result;
    }

    private TreeNode findNode(byte[] newCode, int len) {
        boolean match = false;
        for (TreeNode node : weights) {
            byte[] code = node.getCode();
            if (node.getC() != null) {
                if (len == node.getCodeLen()) {
                    match = true;
                    for (int i = 0; i != code.length; i++) {
                        if (newCode[i] != code[i]) {
                            match = false;
                            break;
                        }
                    }
                }
            }
            if (match) return node;
        }
        return null;
    }

    public String decode(String sourceFileName, String destinationFileName) throws IOException {
        List<Integer> result = new ArrayList<>();
        TreeNode node = null;
        TreePrinter tp=new TreePrinter();
        weights = new ArrayList<>();
        byte[] bufOut = new byte[64];
        int bufCount = 0;
        byte[] code = new byte[32];
        char c;
        int codeLen = 0;
        frequency = new TreeMap<>();
        frequency.clear();
        frequency.put(ESC, 0);
        root = reBuildTree();

        FileInputStream fis = new FileInputStream(new File(sourceFileName));
        FileOutputStream fos = new FileOutputStream(new File(destinationFileName));
        BitInputStream bis = new BitInputStream(fis);

        while(!bis.isEndOfStream()) {
            code[codeLen++] = (byte) bis.read();
            if (codeLen >= minCodeLen) {
                node = findNode(code, codeLen);
                if (node != null) {
                    if (node.getC() == ESC) {
                        c = getSymbolFromFile(bis);
                        Integer counter = frequency.get(c);
                        frequency.put(c, counter == null ? 1 : counter + 1);
                        reBuildTree();
                    } else {
                        c = node.getC();
                        Integer counter = frequency.get(c);
                        frequency.put(c, counter == null ? 1 : counter + 1);
                        updateTree(codes.get(c));
                    }//  reBuildTree();
                    bufOut[bufCount++] = (byte) c;
                    c = 0;
                    Arrays.fill(code, (byte) 0);
                    codeLen = 0;
                    if (bufCount == 20) {
                        tp.printTree(getRoot(weights.get(0)));
                        fos.write(bufOut, 0, bufCount);
                        bufCount = 0;
                    }
                }
            }
        }
        fos.write(bufOut, 0, bufCount);
        fos.close();
        bis.close();
        return "";
    }
}