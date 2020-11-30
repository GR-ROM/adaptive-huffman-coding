package com.company;

public class TreeNode implements Comparable<TreeNode> {
    private Integer c;
    private int code;
    private int codeLen;
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

    public TreeNode(Integer c, int weight) {
        this.c = c;
        this.code = 0;
        this.codeLen = 0;
        this.weight = weight;
    }

    public TreeNode(TreeNode left, TreeNode right) {
        this.c = null;
        this.code = 0;
        this.codeLen = 0;
        this.weight = left.weight + right.weight;
        this.left = left;
        this.right = right;
    }

    public Integer getC() {
        return c;
    }

    public void setC(Integer c) {
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

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getIntCode() {
        return this.code;
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
                "code:" + this.code + ",";
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
