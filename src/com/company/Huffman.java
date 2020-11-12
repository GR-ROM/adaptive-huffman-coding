package com.company;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class Huffman {

    private final int ESC = 0xFFFF;
    private final int EOF = 0x7FFF;
    private int numCode;
    private TreeNode root;
    private Map<Integer, Integer> frequency;
    private Map<Integer, TreeNode> codes;
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

    private void traverseTree(TreeNode node) {
        int code;
        List<TreeNode> stack = new ArrayList<>();
        stack.add(node);

        while (stack.size() != 0) {
            TreeNode parent;
            node = stack.remove(stack.size() - 1);
            if (node.getC() != null) {
                codes.put(node.getC(), node);
            } else {
                if (node.getLeft() != null) {
                    parent = node.getLeft().getParent();
                    code = parent.getCode();
                    code &= ~(1 << (31 - node.getCodeLen()));
                    node.getLeft().setCodeLen(parent.getCodeLen() + 1);
                    node.getLeft().setCode(code);
                    stack.add(node.getLeft());
                }
                if (node.getRight() != null) {
                    parent = node.getRight().getParent();
                    code = parent.getCode();
                    code |= (1 << (31 - node.getCodeLen()));
                    node.getRight().setCodeLen(parent.getCodeLen() + 1);
                    node.getRight().setCode(code);
                    stack.add(node.getRight());
                }
            }
        }
    }

    private TreeNode getRoot(TreeNode node) {
        while (node.getParent() != null) node = node.getParent();
        return node;
    }

    private boolean updateTree(TreeNode node) {
        boolean treeModified = false;
        boolean t = false;
        TreeNode inode = node;
        node.setWeight(node.getWeight() + 1);
        while (node.getParent() != null) {
            inode = node.getParent();
            inode.setWeight(inode.getWeight() + 1);
            for (int i = weights.indexOf(node) + 1; i != weights.size(); i++) {
                TreeNode cNode = weights.get(i);
                if (node.getWeight() > cNode.getWeight()) {
                    TreeNode.SwapNodes(node, cNode);
                    weights.set(i, node);
                    weights.set(weights.indexOf(node), cNode);
                    treeModified = true;
                    t = true;
                } else if (t) break;
            }
            if (inode != node.getParent())
                inode.setWeight(inode.getWeight() - 1);
            else {
                node = node.getParent();
                node.setWeight(node.getLeft().getWeight() + node.getRight().getWeight());
            }
        }
        return treeModified;
    }

    private TreeNode reBuildTree() {
        List<TreeNode> f = new ArrayList<>();
        weights.clear();
        for (Integer c : frequency.keySet()) {
            TreeNode node = new TreeNode(c, frequency.get(c));
            f.add(node);
            weights.add(node);
        }

        Collections.sort(f);
        while (f.size() > 1) {
            TreeNode left = f.remove(0);
            TreeNode right = f.remove(0);
            TreeNode node = new TreeNode(left, right);
            left.setParent(node);
            right.setParent(node);
            f.add(0, node);
            int c = 0;
            Boolean t = false;
            for (int i = 0; i != f.size(); i++) {
                if (f.get(c).getWeight() > f.get(i).getWeight()) {
                    Collections.swap(f, c++, i);
                    t = true;
                } else {
                    if (t) break;
                }
            }
           // Collections.sort(f);
            weights.add(node);
        }
        Collections.sort(weights);
        codes.clear();
        traverseTree(weights.get(weights.size() - 1));
        return f.get(0);
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
            bos.write((sym & 0x80)!=0 ? 1 : 0);
            sym<<=1;
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

    public List<Integer> encode(String sourceFileName, String destinationFileName) throws IOException {
        weights = new ArrayList<>();
        int c;
        int bytesRead = 0;
        byte[] inputBuffer = new byte[65536];
        frequency = new TreeMap<>();
        int CountReBuild = 0;
        int CountUpdate = 0;
        int CountTreeModifed = 0;
        codes = new HashMap<>();

        frequency.clear();
        frequency.put(ESC, 0);
        frequency.put(EOF, 1000000000);
        root = reBuildTree();
        traverseTree(weights.get(weights.size() - 1));
        TreePrinter tp = new TreePrinter();
        tp.printTree(getRoot(weights.get(0)));

        int timePutCodeToFile = 0;
        int timeTraverseTree = 0;
        int timeUpdateTree = 0;
        FileInputStream fis = new FileInputStream(new File(sourceFileName));
        FileOutputStream fos = new FileOutputStream(new File(destinationFileName));
        BitOutputStream bos = new BitOutputStream(fos);

        while (true) {
            bytesRead = fis.read(inputBuffer);
            if (bytesRead < 0) break;
            for (int i = 0; i != bytesRead; i++) {
                c = inputBuffer[i];
                Integer counter = frequency.get(c);
                frequency.put(c, counter == null ? 1 : counter + 1);
                if (!codes.containsKey(c)) {
                    putCodeToFile(bos, codes.get(ESC));
                    putSymbolToFile(bos, c);
                    reBuildTree();
                  //  tp.printTree(getRoot(weights.get(0)));
                    CountReBuild++;
                } else {
                    long s = System.currentTimeMillis();
                    putCodeToFile(bos, codes.get(c));
                    long e = System.currentTimeMillis();
                    timePutCodeToFile += e - s;
                    s = System.currentTimeMillis();
                    boolean treeModified = updateTree(codes.get(c));
                    e = System.currentTimeMillis();
                    timeTraverseTree += e - s;
                    if (treeModified) {
                        codes.clear();
                        s = System.currentTimeMillis();
                        traverseTree(weights.get(weights.size() - 1));
                        e = System.currentTimeMillis();
                        timeUpdateTree += e - s;
                        CountTreeModifed++;
                    }
                    CountUpdate++;
                }//reBuildTree();
            }
        }
        bos.close();
        fis.close();
        System.out.println("Counter of rebuilds: " + CountReBuild + ", counter of updates: " + CountUpdate + ", counter of tree modifications: " + CountTreeModifed);
        System.out.println("Total time of putCodeToFile: " + timePutCodeToFile);
        System.out.println("Total time of traverseTree: " + timeTraverseTree);
        System.out.println("Total time of UpdateTree: " + timeUpdateTree);
        //System.out.println(result.toString());
        return null;
    }

    private TreeNode findNode(int newCode, int len) {
        boolean match = false;
        int mask = 0;
        //for (int i=0;i!=len;i++) mask |= (1 << (31 - i));
        for (TreeNode node : weights) {
            int code = node.getCode();
            if (node.getC() != null) {
                if (len == node.getCodeLen()) {
                    match = true;
                    //     newCode&=mask;
                    //     code&=mask;
                    for (int i = 0; i != len; i++) {
                        if ((newCode & 1 << (31 - i)) != (code & 1 << (31 - i))) {
                            //if (newCode!=code){
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
        TreeNode node;
        weights = new ArrayList<>();
        byte[] bufOut = new byte[16];
        int bufCount = 0;
        int code = 0;
        int c;
        int codeLen = 0;
        frequency = new TreeMap<>();
        frequency.clear();
        frequency.put(ESC, 0);
        frequency.put(EOF, 1000000000);
        root = reBuildTree();

        TreePrinter tp = new TreePrinter();
        FileInputStream fis = new FileInputStream(new File(sourceFileName));
        FileOutputStream fos = new FileOutputStream(new File(destinationFileName));
        BitInputStream bis = new BitInputStream(fis);

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
                    codes.clear();
                    traverseTree(getRoot(weights.get(0)));
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
        fos.write(bufOut, 0, bufCount);
        fos.close();
        bis.close();
        tp.printTree(getRoot(weights.get(0)));
        return "";
    }
}