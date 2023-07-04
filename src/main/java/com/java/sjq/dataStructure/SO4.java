package com.java.sjq.dataStructure;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.*;

class SO3 {
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
         double v = new SO3().maxProbability(n, edges, succProb, start, end);
         System.out.println(v);
     }

    List<Double[]>[] getAdjTable(int n, int[][] edges, double[] succProb){
         List<Double[]>[] adjTable = new ArrayList[n];
        for(int i = 0; i < adjTable.length; i++) {
            adjTable[i] = new ArrayList<>();
        }
         for(int i = 0; i < edges.length; i++) {
           adjTable[edges[i][0]].add(new Double[]{edges[i][1]*1.0, succProb[i]});
           adjTable[edges[i][1]].add(new Double[]{edges[i][0]*1.0, succProb[i]});
         }
         return adjTable;
    }
    public double maxProbability(int n, int[][] edges, double[] succProb, int start, int end) {
        List<Double[]>[] adjTable = getAdjTable(n,edges,succProb);
        boolean[] visited = new boolean[n];
        Arrays.fill(visited, false);
        State nextState = new State(start, 1);
        double [] disTo = new double[n];
        disTo[start] = 1;

        while(nextState!=null){
            State maxState = nextState;
            int maxStateI = maxState.i;
            double maxStateDis = maxState.dis;


            visited[maxStateI] = true;
            for (Double[] d : adjTable[maxStateI]) {
                int adjNode =  d[0].intValue();
                double prob = d[1];
                System.out.println("adj\t"+adjNode+"\t"+prob);

                if(visited[adjNode]) {
                    continue;
                }
                if(disTo[adjNode] < prob * maxStateDis){
                    disTo[adjNode] = prob * maxStateDis;
                }
            }
            int maxProbNode = getMaxProbNode(disTo, visited);
            if(maxProbNode != -1){
                nextState = new State(maxProbNode, disTo[maxProbNode]);
            }else{
                nextState=null;
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
