package com.java.sjq.dataStructure.tree;

import java.util.*;

/**
 * 层次遍历二叉树（https://leetcode-cn.com/problems/binary-tree-level-order-traversal/）：
 */
public class LevelOrder {
    public static void main(String[] args){
        PriorityQueue p = new PriorityQueue<>();
        int c=97;//41,42 01000001 01000010
        char[] d = "".toCharArray();
        System.out.println((char)c+""+('a'^'b')+d.length);
        int a = 1998, b = 2023;
        a ^= b;
        b ^= a;
        a ^= b;
        System.out.println(a+""+b);
// 现在 a = 2, b = 1


    }

    public List<List<Integer>> levelOrder1(TreeNode root) {
        List<List<Integer>> res= new ArrayList<>();
        if(root == null){
            return res;
        }
        Queue<TreeNode> queue = new ArrayDeque();
        queue.offer(root);
        TreeNode firstItem;
        while(queue.size()>0){
            int curLevelSize = queue.size();
            List<Integer> levelList = new ArrayList<>();
            res.add(levelList);
            while(curLevelSize>0){
                curLevelSize--;
                firstItem = queue.poll();
                levelList.add(firstItem.val);
                if(firstItem.left != null) {
                    queue.offer(firstItem.left);
                }
                if(firstItem.right != null) {
                    queue.offer(firstItem.right);
                }
                // if(firstItem != null) {
                //     levelList.add(firstItem.val);
                //     queue.offer(firstItem.left);  // queue 不允许添加 null
                //     queue.offer(firstItem.right);
                // }
            }
        }
        return res;
    }

    public List<List<Integer>> levelOrder2(TreeNode root) {
        List<List<Integer>> res= new ArrayList<>();
        if(root == null){
            return res;
        }
         travel(root, res, 0);
        return res;

    }

    public void travel(TreeNode node, List<List<Integer>> res,int level){
        if(node==null){
            return;
        }
        if(res.size()<=level){
            List<Integer> levelList = new ArrayList<>();
            res.add(levelList);
        }
        res.get(level).add(node.val);
        travel(node.left, res, level+1);
        travel(node.right, res, level+1);

    }
}
