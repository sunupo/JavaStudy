# ------------------------dijkstra----------------

# [1514. 概率最大的路径 - 力扣（Leetcode）](https://leetcode.cn/problems/path-with-maximum-probability/description/)

## 我的做法

参考【[图解Dijkstra最短路径算法(邻接矩阵)](https://mp.weixin.qq.com/s/K7jo-cYSKqWVvo3ETfPCKA)】的做法。

> 1. 算法的描述：
>
>    -   1 将选定的**初始节点**标记为当前距离为**0**，**其余**节点标记为**无穷大**。
>
>    -   2 将当前**距离最小的未访问节点**设置为当前节点**C**。
>
>    -   3 对于当前节点**C**的每个邻居**N**：将**C**的当前距离与连接**C-N**的边的权值相加。如果小于当前距离 **N**，则设置为新的当前距离 **N**。
>
>    -   4 标记当前节点**C****已访问**。
>
>    -   5 如果存在**未访问**节点，请转**步骤2**。

### 邻接表1

```java
class SO2 {
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
         double v = new SO2().maxProbability(n, edges, succProb, start, end);
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
         Queue<State> q = new PriorityQueue<State>( (a,b)-> Double.compare(b.dis, a.dis));
        q.offer(new State(start, 1));
        double [] disTo = new double[n];
        disTo[start] = 1;

        while(!q.isEmpty()){
            State maxState = q.poll();
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

```

### 邻接表2

邻接表1 用了 priorityqueue ，但是实际上没什么用。这儿直接用一个对象代替。 

```java
package com.java.sjq.dataStructure;

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

```



### 邻接矩阵

```java
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

```



## labuladong 的做法

> [Dijkstra 算法模板及应用 :: labuladong的算法小抄](https://labuladong.gitee.io/algo/di-yi-zhan-da78c/shou-ba-sh-03a72/dijkstra-s-6d0b2/)

```java
double maxProbability(int n, int[][] edges, double[] succProb, int start, int end) {
    List<double[]>[] graph = new LinkedList[n];
    for (int i = 0; i < n; i++) {
        graph[i] = new LinkedList<>();
    }
    // 构造邻接表结构表示图
    for (int i = 0; i < edges.length; i++) {
        int from = edges[i][0];
        int to = edges[i][1];
        double weight = succProb[i];
        // 无向图就是双向图；先把 int 统一转成 double，待会再转回来
        graph[from].add(new double[]{(double)to, weight});
        graph[to].add(new double[]{(double)from, weight});
    }
    
    return dijkstra(start, end, graph);
}

class State {
    // 图节点的 id
    int id;
    // 从 start 节点到达当前节点的概率
    double probFromStart;

    State(int id, double probFromStart) {
        this.id = id;
        this.probFromStart = probFromStart;
    }
}

double dijkstra(int start, int end, List<double[]>[] graph) {
    // 定义：probTo[i] 的值就是节点 start 到达节点 i 的最大概率
    double[] probTo = new double[graph.length];
    // dp table 初始化为一个取不到的最小值
    Arrays.fill(probTo, -1);
    // base case，start 到 start 的概率就是 1
    probTo[start] = 1;

    // 优先级队列，probFromStart 较大的排在前面
    Queue<State> pq = new PriorityQueue<>((a, b) -> {
        return Double.compare(b.probFromStart, a.probFromStart);
    });
    // 从起点 start 开始进行 BFS
    pq.offer(new State(start, 1));

    while (!pq.isEmpty()) {
        State curState = pq.poll();
        int curNodeID = curState.id;
        double curProbFromStart = curState.probFromStart;

        // 遇到终点提前返回
        if (curNodeID == end) {
            return curProbFromStart;
        }
        
        if (curProbFromStart < probTo[curNodeID]) {
            // 已经有一条概率更大的路径到达 curNode 节点了
            continue;
        }
        // 将 curNode 的相邻节点装入队列
        for (double[] neighbor : graph[curNodeID]) {
            int nextNodeID = (int)neighbor[0];
            // 看看从 curNode 达到 nextNode 的概率是否会更大
            double probToNextNode = probTo[curNodeID] * neighbor[1];
            if (probTo[nextNodeID] < probToNextNode) {
                probTo[nextNodeID] = probToNextNode;
                pq.offer(new State(nextNodeID, probToNextNode));
            }
        }
    }
    // 如果到达这里，说明从 start 开始无法到达 end，返回 0
    return 0.0;
}

```



## 区别：

1. 我的做法是依据：当前节点访问结束之后，只能从未访问节点只能怪选取最优（概率最大）的节点。

2. labuladong 的做法是。每次==当前节点==和==邻接点==比较结束之后，都把==邻接点==往 priorityqueue 里面塞，然后一轮循环通过 

   > ```java
   > if (curProbFromStart < probTo[curNodeID]) {
   >             // 已经有一条概率更大的路径到达 curNode 节点了
   >             continue;
   > }
   > ```
   >
   > 剪枝。（就算不剪枝，也不影响结果，只是浪费后面的计算。）因为

# [1631. 最小体力消耗路径 - 力扣（Leetcode）](https://leetcode.cn/problems/path-with-minimum-effort/)

你准备参加一场远足活动。给你一个二维 `rows x columns` 的地图 `heights` ，其中 `heights[row][col]` 表示格子 `(row, col)` 的高度。一开始你在最左上角的格子 `(0, 0)` ，且你希望去最右下角的格子 `(rows-1, columns-1)` （注意下标从 **0** 开始编号）。你每次可以往 **上**，**下**，**左**，**右** 四个方向之一移动，你想要找到耗费 **体力** 最小的一条路径。

一条路径耗费的 **体力值** 是路径上相邻格子之间 **高度差绝对值** 的 **最大值** 决定的。

请你返回从左上角走到右下角的最小 **体力消耗值** 。

**示例 1：**

![](https://assets.leetcode-cn.com/aliyun-lc-upload/uploads/2020/10/25/ex1.png)

```
输入：heights = [[1,2,2],[3,8,2],[5,3,5]]
输出：2
解释：路径 [1,3,5,3,5] 连续格子的差值绝对值最大为 2 。
这条路径比路径 [1,2,2,2,5] 更优，因为另一条路径差值最大值为 3 。
```

```java
class Solution {
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
         System.out.println(new Solution().minimumEffortPath(heights));
     }

    public int minimumEffortPath(int[][] heights) {

        PriorityQueue<State> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(a -> a.dis));
        priorityQueue.offer(new State(0,0,0));

        int[][] disTo = new int[heights.length][heights[0].length];
        for (int i = 0; i < heights.length; i++) {
            Arrays.fill(disTo[i], Integer.MAX_VALUE);
        }
        disTo[0][0] = 0;

        while(!priorityQueue.isEmpty()){
            State state = priorityQueue.poll();
            int x = state.x, y = state.y, dis = state.dis;
            if(dis > disTo[x][y] ){
                continue;
            }
            for (int[] adj: getAdj(x,y,heights)) {
                int adjX = adj[0];
                int adjY = adj[1];
                int newDis = Math.max(disTo[x][y] , Math.abs(heights[x][y]-heights[adjX][adjY]));
                if(disTo[adjX][adjY] > newDis ){
                    disTo[adjX][adjY] = newDis;
                    priorityQueue.offer(new State(adjX, adjY, newDis));
                }

            }

        }
         return disTo[heights.length-1][heights[0].length-1];

    }
    List<int[]> getAdj(int i, int j, int[][] heights){
        List<int[]> list = new ArrayList<>();
        int[][] dirs = new int[][]{{0,-1},{0,1},{-1,0},{1,0}};
        for (int[] dir : dirs) {
            int x = i+dir[0];
            int y = j+dir[1];
            if(x>=0 && x<heights.length &&y>=0 && y<heights[0].length){
                list.add(new int[]{x,y});
            };
        }
        return list;
    }
}
```



#   [图解Dijkstra最短路径算法(邻接矩阵)](https://mp.weixin.qq.com/s/K7jo-cYSKqWVvo3ETfPCKA)

## 1 Dijkstra算法简介

[图](https://mp.weixin.qq.com/s?__biz=MzI4ODgwMjYyNQ==&mid=2247487290&idx=1&sn=2a01489f602ae9f403191a242023beaa&scene=21#wechat_redirect)是元素对之间连接的图形表示。连接称为边，而元素称为节点。

有三种图：

-   **无向图**：可以使用边向任何方向移动。
    
-   **有向图**：可以移动的方向是指定的，并使用箭头显示。
    
-   **加权图**：加权图的边表示一定的度量，如距离、使用边移动所花费的时间等。
    

**Dijkstra算法**利用图中给定的权值计算并找到**节点之间的最短路径**。

Dijkstra算法用于计算**单源路径**，即计算**某一**结点(单源)到图中**所有**其它结点的距离。

Dijkstra算法跟踪源节点到其他节点的当前已知距离，如果找到更短的路径，则**动态更新**这些值。

接下来用一个例子来解释算法。计算节点**C**与图中其他节点之间的最短路径：

![图片](https://mmbiz.qpic.cn/mmbiz_png/8LIHzsJ61ObBlWEMbQ2s1BrGEZHhezUDxzzdNQjyXNiaJ3DeLbYxsWPwRlicEgbAic6VmQTY5mhUaTPeuPf7UWNDQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

在算法执行过程中，标记每个节点到**节点C**(所选的节点)的最小距离。对于结点**C**，这个**距离是0**。对于**其余的节点**，由于不知道最小距离，初始化为**无穷**()：

![图片](https://mmbiz.qpic.cn/mmbiz_png/8LIHzsJ61ObBlWEMbQ2s1BrGEZHhezUDiatnxEJK839AMxS7nKRKYheXRqodgictP4aK6Vh0O429iaduJvtY5w7xA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

还有一个当前节点。最初，将它设置为**C**。在图像中，用**红点**标记**当前节点**。

现在检查当前节点的**邻居**(**A, B和D**)，没有特定的顺序。从B开始。将当前节点的最小距离(在本例中为0)与连接当前节点和B的边的权值(在本例中为**7**)相加，就得到**0 + 7 = 7**。将该值与**B**(无穷大)的最小距离进行比较；最小值是**B**的最小距离(在这种情况下，**7**小于无穷)：

![图片](https://mmbiz.qpic.cn/mmbiz_png/8LIHzsJ61ObBlWEMbQ2s1BrGEZHhezUD2OeDS3kXbkOdx7xb8icxCxUMgliaAnKeGCySKJbfFkia5mWnrTw7VPDiaA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

现在检查一下邻居**A**。将**0**(当前节点**C**的最小距离)与**1**(当前节点与**A**连接的边的权值)相加得到**1**。将**1**与**A**的最小距离(无穷大)进行比较，并留下最小值：

![图片](https://mmbiz.qpic.cn/mmbiz_png/8LIHzsJ61ObBlWEMbQ2s1BrGEZHhezUDtTZVuibMmoicCAta82QONrGsPgP9hMADpoHbQFQhcMudbUlyZLpH9ictg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

对**D**重复同样的步骤：

![图片](https://mmbiz.qpic.cn/mmbiz_png/8LIHzsJ61ObBlWEMbQ2s1BrGEZHhezUDcLZwrupxtCoYOib98gMEbKQsvMpxkBiaIsqnmJ1hFbWKNUtOeMgywa7Q/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

已经检查了**C**的所有邻居，因此将其标记为**已访问**。用**绿色**的复选标记来表示**访问过**的节点：

![图片](https://mmbiz.qpic.cn/mmbiz_png/8LIHzsJ61ObBlWEMbQ2s1BrGEZHhezUDb3GT9Y5OJSOUNlu4hFxSbFWKficEqA3D2CzqhibVZCvKvpPa6XbM1eLA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

现在需要选择一个**新的当前节点**。该节点必须是距离最小的**未访问节点**(因此，具有最小数字且没有检查标记的节点)。这是**A**，用红点标记一下：

![图片](https://mmbiz.qpic.cn/mmbiz_png/8LIHzsJ61ObBlWEMbQ2s1BrGEZHhezUDyeUDXhEpV1xn7icHpjnys5xmvMqLKBn14lUwCudtzpvEXJOrj0bcp4A/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

现在重复这个算法。检查当前节点的**邻居**，**忽略已访问**的节点。这意味着只检查**B**。

对于**B**，将**1**(当前节点**A**的最小距离)与**3**(连接**A**和**B**的边的权值)相加得到**4**。将**4**与**B(7)**的最小距离进行比较，并留下最小值**4**。

![图片](https://mmbiz.qpic.cn/mmbiz_png/8LIHzsJ61ObBlWEMbQ2s1BrGEZHhezUDTm0MbscvngN60C5rBdf3tNufbgSjop0SwlV2xM1MddQFxcHjKDHM5g/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

然后将**A**标记为已访问，并选取一个新的当前节点**D**，即当前距离最小的未访问节点。

![图片](https://mmbiz.qpic.cn/mmbiz_png/8LIHzsJ61ObBlWEMbQ2s1BrGEZHhezUDrErPFc1AovYEz0SMnmicdQMne6uekogc83eYfpPhVIibNHcjD7icahxkQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

再重复一遍算法。这次检查**B**和**E**。

对于**B**，得到**2 + 5 = 7**。将该值与**B**的最小距离(**4**)进行比较，并留下最小值(**4**)。对于**E**，得到**2 + 7 = 9**，将其与**E**的最小距离(无穷大)进行比较，并留下最小距离(**9**)。

将**D**标记为已访问，并将当前节点设置为**B**。

![图片](https://mmbiz.qpic.cn/mmbiz_png/8LIHzsJ61ObBlWEMbQ2s1BrGEZHhezUDeIlLGCuIyc6yOJIPCZPs5NQI5MptynKo3MKLzaicJwPia2On62JHic3Zw/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

只需要检查**E**。**4 + 1 = 5**，它小于**E**的最小距离(**9**)，所以留下**5**。然后，将**B**标记为访问过的节点，并设置**E**为当前节点。

![图片](https://mmbiz.qpic.cn/mmbiz_png/8LIHzsJ61ObBlWEMbQ2s1BrGEZHhezUDgnCC56vjS5Q5icO7QXVTvVklfBGNdWmOInEAgorkN7HzCfH4atAric4A/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

没有未拜访过的邻居，所以不需要检查任何东西。把它标记为已访问。

![图片](https://mmbiz.qpic.cn/mmbiz_png/8LIHzsJ61ObBlWEMbQ2s1BrGEZHhezUDyl2dAvIJaDnahAK55HvuDtrYqheQDCM7lMvnXfxQs4U7D6uul9ib3KA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

没有未访问的节点，就完成了!现在每个节点的最小距离实际上代表了从该节点到节点**C**(选择的初始节点)的最小距离!

下面是算法的描述：

-   1 将选定的**初始节点**标记为当前距离为**0**，**其余**节点标记为**无穷大**。
    
-   2 将当前**距离最小的未访问节点**设置为当前节点**C**。
    
-   3 对于当前节点**C**的每个**邻居****N**：将**C**的当前距离与连接**C-N**的边的权值相加。如果小于当前距离 **N**，则设置为新的当前距离 **N**。
    
-   4 标记当前节点**C****已访问**。
    
-   5 如果存在**未访问**节点，请转**步骤2**。
    

## 2 实现([邻接矩阵](http://mp.weixin.qq.com/s?__biz=MzI4ODgwMjYyNQ==&mid=2247487291&idx=1&sn=4a3321566b07ac9cae057f968e247e0b&chksm=ec399d15db4e1403602e75850c746a894278017ad4eb07a44e48412d90c4e7298f95ed19b59f&scene=21#wechat_redirect))

```python
class Graph:
    def __init__(self, num_nodes):
        self.INF = 10 ** 6
        self.V = num_nodes
        self.dist = [self.INF for _ in range(self.V)]
        self.visit = [False for _ in range(self.V)]
        self.graph = [[0 for _ in range(self.V)]
                      for _ in range(self.V)]

    def dijkstra(self, srcNode):
        self.dist[srcNode] = 0
        for i in range(self.V):
            u = self.minDistance(self.dist, self.visit)
            self.visit[u] = True

            for v in range(self.V):
                if self.graph[u][v] > 0 and self.visit[v] == False and \
                        self.dist[v] > self.dist[u] + self.graph[u][v]:
                    self.dist[v] = self.dist[u] + self.graph[u][v]
                    
        self.printDist(self.dist)

    def minDistance(self, dist, visit):
        minimum = self.INF
        for v in range(self.V):
            if dist[v] < minimum and visit[v] == False:
                minimum = dist[v]
                min_index = v
        return min_index

    def printDist(self, dist):
        print("Node \t Distance from 0")
        for i in range(self.V):
            print(i, "\t", dist[i])
            
ourGraph = Graph(7)
ourGraph.graph = [[0, 2, 6, 0, 0, 0, 0],
        [2, 0, 0, 5, 0, 0, 0],
        [6, 6, 0, 8, 0, 0, 0],
        [0, 0, 8, 0, 10, 15, 0],
        [0, 0, 0, 10, 0, 6, 2],
        [0, 0, 0, 15, 6, 0, 6],
        [0, 0, 0, 0, 2, 6, 0],
        ]

ourGraph.dijkstra(0)
```

结果如下：

```auto
Node   Distance from 0
0   0
1   2
2   6
3   7
4   17
5   22
6   19
```

  

时间复杂度：

  

参考：

\[1\] https://www.section.io/engineering-education/dijkstra-python/

\[2\] https://www.section.io/engineering-education/dijkstra-python/





# -------------------Floyd-----------------





# -------------------------bellman-ford-------------------------------

[Java Bellman-Ford算法原理是什么，怎么实现？-群英](https://www.qycn.com/xzx/article/16575.html)

 [![](https://www.qycn.com/home/img/helpfile/ad_04.jpg)](https://www.qy.cn/qyCloud/buy.html)这篇文章主要给大家介绍“Java Bellman-Ford算法原理是什么，怎么实现？”的相关知识，下文通过实际案例向大家展示操作过程，内容简单清晰，易于学习，有这方面学习需要的朋友可以参考，希望这篇“Java Bellman-Ford算法原理是什么，怎么实现？”文章能对大家有所帮助。

## 一 点睛  

**如果遇到负权边，则在没有负环（回路的权值之和为负）存在时，可以采用 Bellman-Ford 算法求解最短路径**。该算法的优点是变的权值可以是负数、实现简单，缺点是时间复杂度过高。但是该算法可以进行若干种优化，以提高效率。

Bellman-Ford 算法与 Dijkstra 算法类似，都是以松弛操作作为基础。Dijkstra 算法以贪心法选取未被处理的具有最小权值的节点，然后对其进行松弛操作；而 Bellman-Ford 算法对所有边都进行松弛操作，共 n-1 次。因为负环可以无限制地减少最短路径长度，所以吐过发现第 n 次操作仍然可松弛，则一定存在负环。Bellman-Ford 算法最长运行时间为O(nm),其中 n 和 m 分别是节点数和边数。

## 二 算法步骤

1 数据结构

因为需要利用边进行松弛，因此采用边集数组存储。每条边都有三个域：两个端点a和b，以及边权w

2 松弛操作

对所有的边 j(a,b,w),如果 dis\[e\[j\]b\]>dis\[e\[j\].a\]+e\[j\].w,则松弛，另 dis\[e\[j\]b\]=dis\[e\[j\].a\]+e\[j\].w。其中，dis\[v\] 表示从源点到节点 v 的最短路径长度。

3 重复松弛操作 n-1 次

4 负环判断

再执行一次松弛操作，如果仍然可以松弛，则说明右负环。

## 三 算法实现

> Bellman-Ford算法经常会在未达到 n-1 轮松弛前就已经计算出最短路，之前我们已经说过，n-1 其实是最大值。因此可以添加一个一维数组用来备份数组 dis。如果在新-一轮的松弛中数组 dis 没有发生变化，则可以提前跳出循环.
>
> [Bellman-ford（解决负权边）](https://blog.csdn.net/yuewenyao/article/details/81026278?spm=1001.2101.3001.6661.1&utm_medium=distribute.pc_relevant_t0.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-1-81026278-blog-119022511.235%5Ev26%5Epc_relevant_default&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-1-81026278-blog-119022511.235%5Ev26%5Epc_relevant_default&utm_relevant_index=1)

```java
package graph.bellmanford;
 
import java.util.Scanner;
 
public class BellmanFord {
    static node e[] = new node[210];
    static int dis[] = new int[110];
    static int n;
    static int m;
    static int cnt = 0;
 
    static {
        for (int i = 0; i < e.length; i++) {
            e[i] = new node();
        }
    }
 
    static void add(int a, int b, int w) {
        e[cnt].a = a;
        e[cnt].b = b;
        e[cnt++].w = w;
    }
 
    static boolean bellman_ford(int u) { // 求源点 u 到其它顶点的最短路径长度，判负环
        for (int i = 0; i < dis.length; i++) {
            dis[i] = 0x3f;
        }
        dis[u] = 0;
        for (int i = 1; i < n; i++) { // 执行 n-1 次
            boolean flag = false; // //用来标记本轮松弛操作中数组dis是否会发生更新
            for (int j = 0; j < m; j++) // 边数 m 或 cnt
                if (dis[e[j].b] > dis[e[j].a] + e[j].w) {
                    dis[e[j].b] = dis[e[j].a] + e[j].w;
                    flag = true;  //  //数组dis发生更新，改变 flag 的值
                }
            if (!flag) //  //如果dis数组没有更新，提前退出循环结束算法(优化)
                return false;
        }
        for (int j = 0; j < m; j++) // 再执行 1 次，还能松弛说明有环
            if (dis[e[j].b] > dis[e[j].a] + e[j].w)
                return true;
        return false;
    }
 
 
    static void print() { // 输出源点到其它节点的最短距离
        System.out.println("最短距离：");
        for (int i = 1; i <= n; i++)
            System.out.print(dis[i] + " ");
        System.out.println();
    }
 
    public static void main(String[] args) {
        int a, b, w;
        Scanner scanner = new Scanner(System.in);
        n = scanner.nextInt();
        m = scanner.nextInt();
        for (int i = 0; i < m; i++) {
            a = scanner.nextInt();
            b = scanner.nextInt();
            w = scanner.nextInt();
            add(a, b, w);
        }
        if (bellman_ford(1)) // 判断负环
            System.out.println("有负环！");
        else
            print();
    }
}
 
class node {
    int a;
    int b;
    int w;
}
```

## 四 测试

1 没有负环的测试

![](https://www.qycn.com/uploads/allimg/2022/09/1899245605908674872.png)

2 有负环的测试

![](https://www.qycn.com/uploads/allimg/2022/09/9079492986153921556.png)

以上就是关于“Java Bellman-Ford算法原理是什么，怎么实现？”的介绍了，感谢各位的阅读，希望文本对大家有所帮助。如果想要了解更多知识，欢迎关注群英网络，小编每天都会为大家更新不同的知识。

免责声明：本站发布的内容（图片、视频和文字）以原创、转载和分享为主，文章观点不代表本网站立场，如果涉及侵权请联系站长邮箱：mmqy2019@163.com进行举报，并提供相关证据，查实之后，将立刻删除涉嫌侵权内容。



# [1293. 网格中的最短路径 - 力扣（Leetcode）](https://leetcode.cn/problems/shortest-path-in-a-grid-with-obstacles-elimination/solutions/101739/wang-ge-zhong-de-zui-duan-lu-jing-by-leetcode-solu/)

