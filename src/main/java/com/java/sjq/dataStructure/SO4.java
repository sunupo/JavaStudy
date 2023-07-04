package com.java.sjq.dataStructure;

import java.util.*;

class SO4 {
     class State{
         int x,y;
         int dis;
         State(int x, int y, int dis){
             this.x = x;
             this.y = y;
             this.dis = dis;
         }
     }
     public static void main(String[] args){
       //
         int[][] heights= new int[][]{{1,2,1,1,1},{1,2,1,2,1},{1,2,1,2,1},{1,2,1,2,1},{1,1,1,2,1}};
         System.out.println(new SO4().minimumEffortPath(heights));
     }

    public int minimumEffortPath(int[][] heights) {

        PriorityQueue<State> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(a -> a.dis));
        priorityQueue.offer(new State(0,0,0));

        int[][] disTo = new int[heights.length][heights[0].length];
        disTo[0][0] = 0;

        while(!priorityQueue.isEmpty()){
            State state = priorityQueue.poll();
            int x = state.x, y = state.y, dis = state.dis;
            if(dis > disTo[x][y] ){
                continue;
            }
            for (Integer[] adj: getAdj(x,y,heights)) {
                int adjX = adj[0];
                int adjY = adj[1];
                int newDis = Math.min(disTo[x][y] , Math.abs(heights[x][y]-heights[adjX][adjY]));
                if(disTo[adjX][adjY] > newDis ){
                    disTo[adjX][adjY] = newDis;
                    priorityQueue.offer(new State(adjX, adjY, newDis));
                }

            }

        }
         return disTo[heights.length-1][heights[0].length-1];

    }
    List<Integer[]> getAdj(int i, int j, int[][] heights){
        List<Integer[]> list = new ArrayList<>();
        int[][] dirs = new int[][]{{0,-1},{0,1},{-1,0},{1,0}};
        for (int[] dir : dirs) {
            int x = i+dir[0];
            int y = j+dir[1];
            if(x>=0 && x<heights.length &&y>=0 && y<heights[0].length){
                list.add(new Integer[]{x,y});
            };
        }
        return list;
    }

}
