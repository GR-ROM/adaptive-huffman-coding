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
        byte[] code;
        List<TreeNode> stack = new LinkedList<>();
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
                    code[node.getCodeLen()] = 0;
                    node.getLeft().setCodeLen(parent.getCodeLen() + 1);
                    node.getLeft().setCode(code);
                    stack.add(node.getLeft());
                }
                if (node.getRight() != null) {
                    parent = node.getRight().getParent();
                    code = parent.getCode();
                    code[node.getCodeLen()] = 1;
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
        boolean treeModified=false;
        TreeNode inode = node;
        node.setWeight(node.getWeight() + 1);
        while (node.getParent() != null) {
            inode = node.getParent();
            inode.setWeight(inode.getWeight() + 1);
            for (int i = weights.indexOf(node) + 1; i != weights.size(); i++) {
                TreeNode cnode = weights.get(i);
                if (node.getWeight() > cnode.getWeight()) {
                    TreeNode.SwapNodes(node, cnode);
                    weights.set(i, node);
                    weights.set(weights.indexOf(node), cnode);
                    treeModified=true;
                }
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
        TreeNode left;
        TreeNode right;

        while (f.size() > 1) {
            Collections.sort(f);
            left = f.get(0);
            f.remove(0);
            right = f.get(0);
            f.remove(0);
            TreeNode node = new TreeNode(left, right);
            left.setParent(node);
            right.setParent(node);
            f.add(node);
            weights.add(node);
        }
        Collections.sort(weights);
        codes.clear();
        traverseTree(weights.get(weights.size()-1));
        return f.get(0);
    }

    private void putCodeToFile(BitOutputStream bos, TreeNode codeNode) throws IOException {
        byte[] code = codeNode.getCode();
        for (int i = 0; i != codeNode.getCodeLen(); i++) {
            bos.write((code[i] != 0) ? 1 : 0);
        }
    }

    private void putSymbolToFile(BitOutputStream bos, int sym) throws IOException {
        for (int i = 0; i != 8; i++) {
            int l = (sym & (1 << i)) >> i;
            bos.write(l);
        }
    }

    private char getSymbolFromFile(BitInputStream bis) throws IOException {
        int bitLen = 8;
        char sym = 0;
        for (int i = 0; i != bitLen; i++) {
            sym >>= 1;
            if (bis.read() > 0)
                sym |= 0x80;
        }
        return sym;
    }

    public List<Integer> encode(String sourceFileName, String destinationFileName) throws IOException {
        weights = new ArrayList<>();
        int c;
        int bytesRead = 0;
        byte[] inputBuffer = new byte[65536];
        frequency = new TreeMap<>();
        int CountReBuild=0;
        int CountUpdate=0;
        int CountTreeModifed=0;
        codes = new HashMap<>();

        frequency.clear();
        frequency.put(ESC, 0);
        frequency.put(EOF, 1000000000);
        root = reBuildTree();
        traverseTree(weights.get(weights.size()-1));
        TreePrinter tp = new TreePrinter();
        tp.printTree(getRoot(weights.get(0)));
      //  List<Integer> result = new ArrayList<>();
        int timePutCodeToFile=0;
        int timeTraverseTree=0;
        int timeUpdateTree=0;
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
                //   result.add(codes.get(ESC).getIntCode());
                    putSymbolToFile(bos, c);
                  //  result.add(c);
                    // addNewNode(new TreeNode((int)c, 1));
                    reBuildTree();
                    CountReBuild++;
                 //   tp.printTree(getRoot(weights.get(0)));
                } else {
                    long s=System.currentTimeMillis();
                    putCodeToFile(bos, codes.get(c));
                    long e=System.currentTimeMillis();
                    timePutCodeToFile+=e-s;
                 //   result.add(codes.get(c).getIntCode());
                    s=System.currentTimeMillis();
                    boolean treeModified=updateTree(codes.get(c));
                    e=System.currentTimeMillis();
                    timeTraverseTree+=e-s;
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
        System.out.println("Counter of rebuilds: "+CountReBuild+", counter of updates: "+CountUpdate+", counter of tree modifications: "+CountTreeModifed);
        System.out.println("Total time of putCodeToFile: "+timePutCodeToFile);
        System.out.println("Total time of traverseTree: "+timeTraverseTree);
        System.out.println("Total time of UpdateTree: "+timeUpdateTree);
        //System.out.println(result.toString());
        return null;
    }

    private TreeNode findNode(byte[] newCode, int len) {
        boolean match = false;
        for (TreeNode node : weights) {
            byte[] code = node.getCode();
            if (node.getC() != null) {
                if (len == node.getCodeLen()) {
                    match = true;
                    for (int i = 0; i != node.getCodeLen(); i++) {
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
        TreeNode node;
        weights = new ArrayList<>();
        byte[] bufOut = new byte[16];
        int bufCount = 0;
        byte[] code = new byte[32];
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
            code[codeLen++] = (byte) bis.read();
            node = findNode(code, codeLen);
            if (node != null) {
                if (node.getC() == ESC) {
                    c = getSymbolFromFile(bis);
                    Integer counter = frequency.get(c);
                    frequency.put(c, counter == null ? 1 : counter + 1);
                    reBuildTree();
                    tp.printTree(getRoot(weights.get(0)));
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
                Arrays.fill(code, (byte) -1);
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
        return "";
    }
}