package com.java.sjq.dataStructure;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;

class SO {
     class State{
         int i;
         double dis;
         State(int i, double dis){
             this.i = i;
             this.dis = dis;
         }
     }
     public static void main(String[] args){
       //
         int n = 3, start = 0, end = 2;
         int[][] edges = {{0,1},{1,2},{0,2}};
         double[] succProb = {0.5,0.5,0.3};
         double v = new SO().maxProbability(n, edges, succProb, start, end);
         System.out.println(v);
     }
    public double maxProbability(int n, int[][] edges, double[] succProb, int start, int end) {
        double[][] matrix = new double[n][n];
        for(int i=0;i<edges.length;i++ ){
            matrix[edges[i][0]][edges[i][1]] = succProb[i];
            matrix[edges[i][1]][edges[i][0]] = succProb[i];
        }
        boolean[] visited = new boolean[n];
        Arrays.fill(visited, false);
         Queue<State> q = new PriorityQueue<State>( (a,b)-> {
             if(b.dis -a.dis>0){
                 return 1;
             }else if(b.dis -a.dis<0){
             return -1;
             }
             return 0;
         });
        q.offer(new State(start, 1));
        double [] disTo = new double[n];
        disTo[start] = 0;

        while(!q.isEmpty()){
            State maxState = q.poll();
            int maxStateI = maxState.i;
            double maxStateDis = maxState.dis;
            System.out.printf("%d, %s\n",maxStateI, maxStateDis);


            visited[maxStateI] = true;
            // for(int adjNode: getAdj(matrix, maxStateI)){
            for(int adjNode=0; adjNode<n; adjNode++ ){
                if(visited[adjNode]) continue;
                System.out.println("disTo[adjNode]<matrix[maxStateI][adjNode] * maxStateDis"+ disTo[adjNode]+":"+matrix[maxStateI][adjNode] * maxStateDis);
                if(disTo[adjNode]<matrix[maxStateI][adjNode] * maxStateDis){
                    disTo[adjNode] = matrix[maxStateI][adjNode] * maxStateDis;
                }
            }
            int maxProbNode = getMaxProbNode(disTo, visited);
            if(maxProbNode != -1){
                q.offer(new State(maxProbNode, disTo[maxProbNode]));
            }

        }
        return disTo[end];
    }
    int getMaxProbNode(double[] disTo, boolean[] visited){
        double max = 0;
        int adj = -1;
        for(int i=0;i<disTo.length;i++){
            if(!visited[i] && disTo[i]>max){
                adj = i;
                max = disTo[i];
            }
        }
        return adj;
    }
}
