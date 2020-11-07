package com.company;

import java.util.Arrays;

public class TreeNode implements Comparable<TreeNode> {
    private Character c;
    private byte[] code;
    private Integer codeLen;
    private int weight;
    private TreeNode parent;
    private TreeNode left;
    private TreeNode right;

    public TreeNode(TreeNode node) {
        this.code = node.code;
        this.weight = node.weight;
        this.codeLen = node.codeLen;
        this.c = node.c;
        this.parent = node.parent;
        this.left = node.left;
        this.right = node.right;
    }

    public TreeNode(Character c, int weight) {
        this.c = c;
        this.weight = weight;
        this.codeLen = null;
    }

    public TreeNode(TreeNode left, TreeNode right) {
        this.c = null;
        this.codeLen = null;
        this.weight = left.weight + right.weight;
        left.setParent(this);
        right.setParent(this);
        this.left = left;
        this.right = right;
    }

    public static void SwapNodes(TreeNode node1, TreeNode node2) {
        TreeNode tParent = node1.parent;
        if (node1.parent != node2.parent) {
            TreeNode tNode = node1;
            if (node1.parent.left == node1) tNode.parent.left = node2;
            else tNode.parent.right = node2;
            if (node2.parent.left == node2) node2.parent.left = node1;
            else node2.parent.right = node1;
            node1.parent = node2.parent;
            node2.parent = tParent;
        } else {
            TreeNode t = tParent.left;
            tParent.left = tParent.right;
            tParent.right = t;
        }
    }

    public Character getC() {
        return c;
    }

    public void setC(Character c) {
        this.c = c;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public TreeNode getLeft() {
        return left;
    }

    public void setLeft(TreeNode left) {
        this.left = left;
    }

    public TreeNode getRight() {
        return right;
    }

    public void setRight(TreeNode right) {
        this.right = right;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public byte[] getCode() {
        return this.code;
    }

    public void setCode(byte[] code) {
        if (this.code == null) this.code = new byte[32];
        System.arraycopy(code, 0, this.code, 0, 32);
    }

    public int getIntCode() {
        int t = 0;
        for (int i = 0; i != 32; i++) {
            t |= (this.code[i] != 0) ? (1 << i) : 0;
        }
        return t;
    }

    public byte getCode(int index) {
        return this.code[index];
    }

    public Integer getCodeLen() {
        return codeLen;
    }

    public void setCodeLen(int codeLen) {
        this.codeLen = codeLen;
    }

    @Override
    public int compareTo(TreeNode o) {
        return this.weight - o.weight;
    }

    @Override
    public String toString() {
        //if (this.c==null) return "";
        String r = "{c:\"" + this.c + "\"," +
                "weight:" + this.weight + "," +
                "code:" + Arrays.toString(this.code) + ",";
        //if (this.left==null) r += "left:\"null\",";
        //else {
        //    r += "left:\"" + this.left.toString() + "\",";
        //}
        //if (this.right==null) r += "right:\"null\"";
        //else r+="right:\"" + this.left.toString() + "\"";
        return r += '}';
    }

    public boolean equals(TreeNode o) {
        return o.weight == this.weight &&
                o.c == this.c &&
                o.parent == this.parent &&
                o.left == this.left &&
                o.right == this.right &&
                o.code == this.code;
    }
}
