package com.java.sjq.dataStructure.tree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class LevelOrder {
    public static void main(String[] args){

    }

    public List<List<Integer>> levelOrder1(TreeNode root) {
        List<List<Integer>> res= new ArrayList<>();
        // travel(root, res, 0);
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
                firstItem = queue.poll();
                // if(firstItem != null) {
                //     levelList.add(firstItem.val);
                //     queue.offer(firstItem.left);  // queue 不允许添加 null
                //     queue.offer(firstItem.right);
                // }
                levelList.add(firstItem.val);
                if(firstItem.left != null) {
                    queue.offer(firstItem.left);
                }
                if(firstItem.right != null) {
                    queue.offer(firstItem.right);
                }
                curLevelSize--;

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


class TreeNode {
     int val;
     TreeNode left;
     TreeNode right;
     TreeNode() {}
     TreeNode(int val) { this.val = val; }
     TreeNode(int val, TreeNode left, TreeNode right) {
         this.val = val;
         this.left = left;
         this.right = right;
     }
 }
