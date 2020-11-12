package com.company;

public class TreePrinter {

    private int indent = 5;
    private TreeNode root;

    public void printTree(TreeNode root) {
        this.root = root;
        preorder(root, true, 0, false, 0);
    }

    public void preorder(TreeNode currentNode, boolean lastChild, int previousIndentation, boolean hasSubItems, int deep) {

        if (currentNode == this.root) {
            System.out.println(String.format("%" + this.indent + "s", "") + "└── " + printNode(currentNode));
        } else if (lastChild) {
            System.out.println(String.format("%" + this.indent + "s", "") + "└── " + printNode(currentNode));

        } else {
            if (hasSubItems) {
                String s = "     ";
                for (int i = 0; i != deep; i++) {
                    s = s + "|        ";
                }
                System.out.println(s + "├── " + printNode(currentNode));
            } else {
                System.out.println(String.format("%" + this.indent + "s", "") + "├── " + printNode(currentNode));
            }
        }


        this.indent += 8;
        if (currentNode.getLeft() != null) {
            //   if (currentNode.getLeft().getLeft()!=null || currentNode.getLeft().getRight()!=null) preorder(currentNode.getLeft(), false, this.indent - 8, true, deep+1); else

            preorder(currentNode.getLeft(), false, this.indent - 8, false, deep + 1);
        }
        if (currentNode.getLeft() != null) {
            preorder(currentNode.getRight(), true, this.indent - 8, false, deep + 1);
        }

        this.indent -= 8;
    }

    private String printNode(TreeNode node) {
        return node.getWeight() + "|" + node.getCodeLen() + "|" + node.getC() + "|" + String.format("0x%08X", node.getCode());
    }
}