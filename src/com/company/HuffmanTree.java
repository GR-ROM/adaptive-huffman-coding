package com.company;

import java.util.*;

public class HuffmanTree {
    private TreeNode root;
    private boolean treeModified;
    private Map<Integer, Integer> frequency;
    private Map<Integer, TreeNode> codes;
    private List<TreeNode> weights;
    TreePrinter tp;

    public HuffmanTree() {
        codes = new HashMap<>();
        weights = new ArrayList<>();

        frequency = new TreeMap<>();
        tp = new TreePrinter();

        this.reset();
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public Map<Integer, Integer> getFrequency() {
        return frequency;
    }

    public void setFrequency(Map<Integer, Integer> frequency) {
        this.frequency = frequency;
    }

    public Map<Integer, TreeNode> getCodes() {
        return codes;
    }

    public void setCodes(Map<Integer, TreeNode> codes) {
        this.codes = codes;
    }

    public List<TreeNode> getWeights() {
        return weights;
    }

    public void setWeights(List<TreeNode> weights) {
        this.weights = weights;
    }

    public TreeNode getCodeNode(int code) {
        return this.codes.get(code);
    }

    public void reset(){
        weights.clear();
        frequency.clear();
        frequency.put(Huffman.ESC, 0);
        frequency.put(Huffman.EOB, 0);
        root = reBuildTree();
        traverseTree(weights.get(weights.size() - 1)); /* generate lookaside buffer */
    }

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

    public void SwapNodes(TreeNode node1, TreeNode node2) {
        TreeNode tNode1Parent=node1.getParent();
        TreeNode tNode2Parent=node2.getParent();
        if (node1.getParent() != node2.getParent()) {
            if (tNode1Parent.getLeft() == node1) tNode1Parent.setLeft(node2); else tNode1Parent.setRight(node2);
            if (tNode2Parent.getLeft() == node2) tNode2Parent.setLeft(node1); else tNode2Parent.setRight(node1);
            node1.setParent(tNode2Parent);
            node2.setParent(tNode1Parent);
        } else {
            TreeNode t = tNode1Parent.getLeft();
            tNode1Parent.setLeft(tNode1Parent.getRight());
            tNode1Parent.setRight(t);
        }
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

    public void printTree(){
        tp.printTree(weights.get(weights.size()-1));
    }

    public boolean updateTree(int code) {
        return updateTree(this.getCodeNode(code));
    }

    public boolean updateTree(TreeNode node) {
        treeModified = false;
        TreeNode parentNode = node;
        node.setWeight(node.getWeight() + 1);
        while (node.getParent() != null) {
            parentNode = node.getParent();
            parentNode.setWeight(parentNode.getWeight() + 1);
            for (int i = weights.indexOf(node) + 1; i != weights.size(); i++) {
                TreeNode cNode = weights.get(i);
                if (node.getWeight() > cNode.getWeight()) {
                    this.SwapNodes(node, cNode);
                    weights.set(i, node);
                    weights.set(weights.indexOf(node), cNode);
                    treeModified = true;
                } else if (treeModified) break;
            }
            if (parentNode != node.getParent())
                parentNode.setWeight(parentNode.getWeight() - 1);
            else {
                node = node.getParent();
                node.setWeight(node.getLeft().getWeight() + node.getRight().getWeight());
            }
        }
        if (treeModified) {
            codes.clear();
            traverseTree(weights.get(weights.size() - 1));
        }
        return treeModified;
    }

    public TreeNode reBuildTree() {
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
            weights.add(node);
        }
        Collections.sort(weights);
        codes.clear();
        traverseTree(weights.get(weights.size() - 1));
        return f.get(0);
    }

    public TreeNode findNode(int newCode, int len) {
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
}
