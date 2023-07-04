# prim 算法

>  [prim算法（普里姆算法）详解](http://c.biancheng.net/algorithm/prim.html)

## 详细说明

了解了什么是[最小生成树](http://c.biancheng.net/algorithm/minimum-spanning-tree.html)后，本节为您讲解如何用普里姆（prim）算法查找连通网（带权的连通图）中的最小生成树。  

普里姆算法查找最小生成树的过程，采用了贪心算法的思想。对于包含 N 个顶点的连通网，普里姆算法每次从连通网中找出一个权值最小的边，这样的操作**重复 N-1 次**，由 N-1 条权值最小的边组成的生成树就是最小生成树。  

那么，如何找出 N-1 条权值最小的边呢？普里姆算法的实现思路是：

1.  将连通网中的所有顶点分为两类（假设为 A 类和 B 类）。初始状态下，所有顶点位于 B 类；
2.  选择任意一个顶点，将其从 B 类移动到 A 类；
3.  从 B 类的所有顶点出发，找出一条连接着 A 类中的某个顶点且权值最小的边，将此边连接着的 A 类中的顶点移动到 B 类；
4.  重复执行第 3  步，直至 B 类中的所有顶点全部移动到 A 类，恰好可以找到 N-1 条边。


举个例子，下图是一个连通网，使用普里姆算法查找最小生成树，需经历以下几个过程：  


![](http://c.biancheng.net/uploads/allimg/210820/1454315049-0.gif)  
图 1 连通网


1) 将图中的所有顶点分为 A 类和 B 类，初始状态下，A = {}，B = {A, B, C, D, S, T}。  
  
2) 从 B 类中任选一个顶点，假设选择 S 顶点，将其从 B 类移到 A 类，A = {S}，B = {A, B, C, D, T}。从 A 类的 S 顶点出发，到达 B 类中顶点的边有 2 个，分别是 S-A 和 S-C，其中 S-A 边的权值最小，所以选择 S-A 边组成最小生成树，将 A 顶点从 B 类移到 A 类，A = {S, A}，B = {B, C, D, T}。  


![](http://c.biancheng.net/uploads/allimg/210820/1454311B6-1.gif)  
图 2 S-A 边组成最小生成树


3) 从 A 类中的 S、A 顶点出发，到达 B 类中顶点的边有 3 个，分别是 S-C、A-C、A-B，其中 A-C 的权值最小，所以选择 A-C 组成最小生成树，将顶点 C 从 B 类移到 A 类，A = {S, A, C}，B = {B, D, T}。  

![](http://c.biancheng.net/uploads/allimg/210820/145431BB-2.gif)  
图 3 A-C 边组成最小生成树


4) 从 A 类中的 S、A、C 顶点出发，到达 B 类顶点的边有 S-C、A-B、C-B、C-D，其中 C-D 边的权值最小，所以选择 C-D 组成最小生成树，将顶点 D 从 B 类移到 A 类，A = {S, A, C, D}，B = {B, T}。  


![](http://c.biancheng.net/uploads/allimg/210820/1454314421-3.gif)  
图 4 C-D 边组成最小生成树


5) 从 A 类中的 S、A、C、D 顶点出发，到达 B 类顶点的边有 A-B、C-B、D-B、D-T，其中 D-B 和 D-T 的权值最小，任选其中的一个，例如选择 D-B 组成最小生成树，将顶点 B 从 B 类移到 A 类，A = {S, A, C, D, B}，B = {T}。  


![](http://c.biancheng.net/uploads/allimg/210820/1454315601-4.gif)  
图 5 D-B 边组成最小生成树


6) 从 A 类中的 S、A、C、D、B 顶点出发，到达 B 类顶点的边有 B-T、D-T，其中 D-T 的权值最小，选择 D-T 组成最小生成树，将顶点 T 从 B 类移到 A 类，A = {S, A, C, D, B, T}，B = {}。  


![](http://c.biancheng.net/uploads/allimg/210820/145431L19-5.gif)  
图 6 D-T 边组成最小生成树


7) 由于 B 类中的顶点全部移到了 A 类，因此 S-A、A-C、C-D、D-B、D-T 组成的是一个生成树，而且是一个最小生成树，它的总权值为 17。  


![](http://c.biancheng.net/uploads/allimg/210820/1454313005-6.gif)  
图 7 最小生成树

## 

# kruskal 算法

> [Union-Find 并查集算法详解](https://mp.weixin.qq.com/s/gUwLfi25TYamq8AJVIopfA)
>
> [Union-Find 算法怎么应用？](https://mp.weixin.qq.com/s?__biz=MzAxODQxMDM0Mw==&mid=2247484759&idx=1&sn=a88337164c741b9740e50523b41b7659&scene=21&key=2cb354f123e7dbbb1268dccfbd84332c58b2070b47c04df2b93e00b6b9935191383ad1a5adbf64b6593015310d905cb22d114a71358979648426b04fd2b8ab6504f5446a4e6df2a117eabb7d1b0ebf8f873f7971af9e5ba84a70bac83f03a8ef0de93802442bac0fe415471471a2077715f291f89b5b54417359fad797fd78fc&ascene=14&uin=MTExMDAxODcwMQ%3D%3D&devicetype=Windows+10+x64&version=6309001c&lang=zh_CN&countrycode=CN&exportkey=n_ChQIAhIQxoeaxWKWr%2Fr%2BmM12KJw56BLbAQIE97dBBAEAAAAAACfJNzwTgRcAAAAOpnltbLcz9gKNyK89dVj0YN58dt31iroxDqt%2FsnpFCAH9DlSZkodSRQdlBOAQn05enxz1UONItFTryTpK9lPBe%2BD%2FMnh%2BhOBTnGUvHmuADtHtCRfgSgyw00n5xHJTX%2F%2BVQcEDD2cWVsvG8aHeA1tFqzzvpREmQIzj%2Bs1slQaIJJ67lBzBcWBdq%2BrtenXR4cqkpYbpowVBG4yY15EQJ7a1lTdSmBM9hnIgzbhuKL40gt5K1Qg3Fu6APiCs6aI%2FRiKHANffEQ%3D%3D&acctmode=0&pass_ticket=8cps3TRfW0mfCPRdg6Cpn%2B8okhtwJYzvreyNEI3YDdzeN4Lngk6o%2BU4wTisruo0M4UU5rxX3PEQ0S0Pb93PZhQ%3D%3D&wx_header=1&fontgear=2)
>
> [东哥带你刷图论第五期：Kruskal 最小生成树算法](https://mp.weixin.qq.com/s?__biz=MzAxODQxMDM0Mw==&mid=2247492575&idx=1&sn=bf63eb391351a0dfed0d03e1ac5992e7&chksm=9bd41dd7aca394c12c168a207cd0da8bdb50580a277d6ca8be12bfa8d2a165bcc6adb42f64a1&mpshare=1&scene=24&srcid=0319lNf2lieZNSyvLz7l82zm&sharer_sharetime=1679210050187&sharer_shareid=d709f244d68b5bb950581bdad38392d4&key=01f3420b03ce3211907bf72d04d0ef64f2ef1313eddf4d798743c476130d0a398162f70e9a397a5337ec8d9309f330e78da8553ad7f8b71fc883f2f661ec349d62ab51e8e0a0c65e8ee65846eb7f8ea092c4551bc6f6bc58d1a69435dfc442b2f67ff7b920488f48066efd1a54bf1e71fd637e4ad274e9ba80aa7905b2cdb685&ascene=14&uin=MTExMDAxODcwMQ%3D%3D&devicetype=Windows+10+x64&version=6309001c&lang=zh_CN&countrycode=CN&exportkey=n_ChQIAhIQfHz95aZj5clZS%2Fmy7lFFxhLbAQIE97dBBAEAAAAAALyCDPow%2FSoAAAAOpnltbLcz9gKNyK89dVj0FZrOWh5yjAURlpRWcJEM264Lg7QQCMp0%2FiDSk9%2BFgGxAOq4u%2FHSc3Rs7an3YcbsdO4m9c4kX0jELiIiffXBB6DWQDcYCRuyEOZ4DLzXoInfMPcR%2FzCfAEQ4zto5IedCeagcXKsyrhKbM6ql511lRlD6XOMbES9iKsTe1wUG62g%2F%2FFPnrviIvGDn7%2BCvFP3bXd3d8zQf%2FephtLUIlmXsBpsatY%2F0GrIMGP9NuA%2FHmLO8XKYewTg%3D%3D&acctmode=0&pass_ticket=8cps3TRfW0mfCPRdg6Cpn%2B8okhtwJYzvreyNEI3YDdy72vZuMbw%2FJ%2FNMr5%2F%2BAKQswHfoERzGYsGggNfnuhRUzg%3D%3D&wx_header=1&fontgear=2)
>
> [并查集（Union-Find）算法 :: labuladong的算法小抄](https://labuladong.github.io/algo/di-yi-zhan-da78c/shou-ba-sh-03a72/bing-cha-j-323f3/)
>
> 
>
> [算法 - 并查集 | CS-Notes](http://www.cyc2018.xyz/%E7%AE%97%E6%B3%95/%E5%9F%BA%E7%A1%80/%E7%AE%97%E6%B3%95%20-%20%E5%B9%B6%E6%9F%A5%E9%9B%86.html#quick-find)

## 详细说明

在连通网中查找[最小生成树](http://c.biancheng.net/algorithm/minimum-spanning-tree.html)的常用方法有两个，分别称为[普里姆算法](http://c.biancheng.net/algorithm/prim.html)和克鲁斯卡尔算法。本节，我们给您讲解克鲁斯卡尔算法。  

克鲁斯卡尔算法查找最小生成树的方法是：将连通网中所有的边按照权值大小做升序排序，从权值最小的边开始选择，只要此边不和已选择的边一起构成环路，就可以选择它组成最小生成树。对于 N 个顶点的连通网，挑选出 N-1 条符合条件的边，这些边组成的生成树就是最小生成树。  

举个例子，图 1 是一个连通网，克鲁斯卡尔算法查找图 1 对应的最小生成树，需要经历以下几个步骤：  


![](http://c.biancheng.net/uploads/allimg/210820/145G46133-0.gif)  
图 1 连通网


1) 将连通网中的所有边按照权值大小做升序排序：  


![](http://c.biancheng.net/uploads/allimg/210820/145G44143-1.gif)


2) 从 B-D 边开始挑选，由于尚未选择任何边组成最小生成树，且 B-D 自身不会构成环路，所以 B-D 边可以组成最小生成树。  


![](http://c.biancheng.net/uploads/allimg/210820/145G41F4-2.gif)  
图 2 B-D 边组成最小生成树


3) D-T 边不会和已选 B-D 边构成环路，可以组成最小生成树：  


![](http://c.biancheng.net/uploads/allimg/210820/145G4D41-3.gif)  
图 3 D-T 边组成最小生成树


4) A-C 边不会和已选 B-D、D-T 边构成环路，可以组成最小生成树：  


![](http://c.biancheng.net/uploads/allimg/210820/145G44336-4.gif)  
图 4 A-C 边组成最小生成树


5) C-D 边不会和已选 A-C、B-D、D-T 边构成环路，可以组成最小生成树：  


![](http://c.biancheng.net/uploads/allimg/210820/145G4CN-5.gif)  
图 5 C-D 边组成最小生成树


6) C-B 边会和已选 C-D、B-D 边构成环路，因此不能组成最小生成树：  


![](http://c.biancheng.net/uploads/allimg/210820/145G44357-6.gif)  
图 6 C-B 边不能组成最小生成树


7) B-T 、A-B、S-A 三条边都会和已选 A-C、C-D、B-D、D-T 构成环路，都不能组成最小生成树。而 S-A 不会和已选边构成环路，可以组成最小生成树。  


![](http://c.biancheng.net/uploads/allimg/210820/145G45057-7.gif)  
图 7 S-A 边组成最小生成树


8) 如图 7 所示，对于一个包含 6 个顶点的连通网，我们已经选择了 5 条边，这些边组成的生成树就是最小生成树。  


![](http://c.biancheng.net/uploads/allimg/210820/145G4M94-8.gif)  
图 8 最小生成树

## 克鲁斯卡尔算法的具体实现

实现克鲁斯卡尔算法的难点在于“**如何判断一个新边是否会和已选择的边构成环路**”，这里教大家一种判断的方法：初始状态下，为连通网中的各个顶点配置不同的标记。对于一个新边，如果它两端顶点的标记不同，就不会构成环路，可以组成最小生成树。一旦新边被选择，需要将它的两个顶点以及和它直接相连的所有已选边两端的顶点改为相同的标记；反之，如果新边两端顶点的标记相同，就表示会构成环路。  

举个例子，在上面的图 4 中，已选择的边为 A-C、B-D、D-T，接下来要判断 C-D 边是否可以组成最小生成树。对于已经选择的边，B-D 和 D-T 直接相邻，所以 B、D、T 的标记相同（假设为 1），A-C 边两端顶点的标记也相同（假设为 2）。判断 C-D 边是否可以组成最小生成树，由于 C、D 的标记不同（1 ≠ 2），所以可以组成最小生成树。C-D 作为新选择的边，与它相连的已选边有 A-C、B-D、D-T，所以要将 A、C、D、B、T 改为相同的标记。  

再比如说，在图 5 的基础上判断 C-B 是否可以组成最小生成树。由上面例子的分析结果得知，C、B 两个顶点的标记相同，因此 C-B 边会和其它已选边构成环路，不能组成最小生成树（如图 6 所示）。

### 例子：

#### [1584. 连接所有点的最小费用 - 力扣（Leetcode）](https://leetcode.cn/problems/min-cost-to-connect-all-points/description/)

给你一个`points` 数组，表示 2D 平面上的一些点，其中 `points[i] = [x<sub>i</sub>, y<sub>i</sub>]` 。

连接点 `[x<sub>i</sub>, y<sub>i</sub>]` 和点 `[x<sub>j</sub>, y<sub>j</sub>]` 的费用为它们之间的 **曼哈顿距离** ：`|x<sub>i</sub> - x<sub>j</sub>| + |y<sub>i</sub> - y<sub>j</sub>|` ，其中 `|val|` 表示 `val` 的绝对值。

请你返回将所有点连接的最小总费用。只有任意两点之间 **有且仅有** 一条简单路径时，才认为所有点都已连接。

**示例 1：**

![](https://assets.leetcode.com/uploads/2020/08/26/d.png)

```
输入：points = [[0,0],[2,2],[3,10],[5,2],[7,0]]
输出：20
解释：

我们可以按照上图所示连接所有点得到最小总费用，总费用为 20 。
注意到任意两个点之间只有唯一一条路径互相到达。
```



```java
class Solution {
    class Line{
        int i,j;
        long dis;
        Line(int i, int j,long dis){
            this.i = i;
            this.j = j;
            this. dis = dis;
        }

    }
    public int minCostConnectPoints(int[][] points) {
        Queue<Line> queue = new PriorityQueue<Line>((a,b)->{
            if(a.dis>b.dis){
                return 1; 
            }else if(a.dis<b.dis){
                return -1;
            }else{
                return 0;
            }
        });
        for(int i=0; i<points.length; i++){
            for(int j=i+1; j<points.length;j++){
                long dis = Math.abs(points[j][0] - points[i][0]) + Math.abs(points[j][1] - points[i][1]); 
                queue.offer(new Line(i,j,dis));
            }
        }
        System.out.println(queue.size());
        int[] marks = new int[points.length];
        for(int i=0; i<points.length;i++){
            marks[i] = i;
        }

        long res = 0L;
        int num=0;
        for(int t=0; t< points.length*(points.length-1)*0.5; t++){
            Line line = queue.poll();

            int startFlag = marks[line.i];

            int flag = marks[line.i];
            if(marks[line.i]!=marks[line.j]){
                for(int k=0; k< marks.length;k++){
                    if(marks[k] == flag ){
                        marks[k] = marks[line.j] ;
                    }
                }
                res += line.dis;
                num++;
            }
            
            if (num == points.length - 1) {
                break;
            }
        }

        return (int)res;
    }
}
```

