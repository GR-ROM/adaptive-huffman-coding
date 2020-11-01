package com.company;

import sun.reflect.generics.tree.Tree;

import java.util.*;

public class Huffman {

    private final char ESC=0xFF;

    private TreeNode root;
    private Map<Character, Integer> frequency;
    private Map<Character, TreeNode> codes;
    private List<TreeNode> weights;
    private Map<Integer, Character> rcodes;

    private void addNewNode(TreeNode newNode){
        TreeNode escNode=weights.get(0);
        TreeNode parent=escNode.getParent();
        TreeNode middle=new TreeNode(null, escNode.getWeight()+newNode.getWeight());
        if (parent!=null) {
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

    private void traverseTree(TreeNode node, int code){
        node.setCode(code);
        if (node.getLeft()!=null) traverseTree(node.getLeft(), (code<<1) | 1);
        if (node.getRight()!=null) traverseTree(node.getRight(), (code<<1) | 0);
        if (node.getC()!=null){
            codes.put(node.getC(), node);
            rcodes.put(code, node.getC());
        }
    }

    private TreeNode getRoot(TreeNode node){
        while (node.getParent()!=null) node=node.getParent();
        return node;
    }

    private void updateTree(TreeNode node){
        if (node.getParent()==null) return;
        TreeNode inode=node;
        node.setWeight(node.getWeight()+1);
        while (node.getParent()!=null) {
            inode=node.getParent();
            inode.setWeight(inode.getWeight()+1);
            for (int i=weights.indexOf(node)+1;i!=weights.size();i++) {
            TreeNode cnode=weights.get(i);
                if (node.getWeight() > cnode.getWeight()) {
                    TreeNode.SwapNodes(node, cnode);
                    Collections.sort(weights);
                }
            }
            inode.setWeight(inode.getWeight()-1);
            node = node.getParent();
            node.setWeight(node.getLeft().getWeight() + node.getRight().getWeight());
        }
    }

    private TreeNode reBuildTree(){
        List<TreeNode> f=new ArrayList<>();
        weights.clear();
        for (Character c: frequency.keySet()) {
            TreeNode node=new TreeNode(c, frequency.get(c));
            f.add(node);
            weights.add(node);
        }
        TreeNode left;
        TreeNode right;

        while (f.size()>1){
            Collections.sort(f);
            left=f.get(0);
            f.remove(0);
            right=f.get(0);
            f.remove(0);
            TreeNode node=new TreeNode(left, right);
            f.add(node);
            weights.add(node);
        }
        codes.clear();
        rcodes.clear();
        traverseTree(f.get(0), 1);
        Collections.sort(weights);
        System.out.println(weights.toString());
        return f.get(0);
    }

    public List<Integer> encode(String str){
        List<Integer> result=new ArrayList<>();
        codes=new HashMap<>();
        rcodes=new HashMap<>();
        weights=new ArrayList<>();
        Character c;
        frequency=new TreeMap<>();
        frequency.clear();
        frequency.put(ESC, 0);
        root=reBuildTree();
        TreePrinter tp=new TreePrinter();
        for (int i=0;i!=str.length();){
            c=str.charAt(i++);
            Integer counter=frequency.get(c);
            frequency.put(c, counter==null ? 1 : counter+1);
            if (!codes.containsKey(c)){
                result.add(codes.get(ESC).getCode());
                result.add((int)c);
                addNewNode(new TreeNode(c, 1));
              //  root=getRoot(root);
               // traverseTree(root, 1);
                reBuildTree();
            } else {
                result.add(codes.get(c).getCode());
                updateTree(codes.get(c));
                traverseTree(getRoot(weights.get(0)), 1);
                tp.printTree(getRoot(weights.get(0)));
            }//reBuildTree();
        }


        System.out.println(result.toString());
        return result;
    }
}