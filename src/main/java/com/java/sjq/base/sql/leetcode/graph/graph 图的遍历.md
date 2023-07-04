> [拓扑排序，YYDS！](https://mp.weixin.qq.com/s/7nP92FhCTpTKIAplj_xWpA)
>
> [单链表的六大解题套路，你都见过么？](https://mp.weixin.qq.com/s?__biz=MzAxODQxMDM0Mw==&mid=2247492022&idx=1&sn=35f6cb8ab60794f8f52338fab3e5cda5&scene=21#wechat_redirect)
>
> [微软面试题解析：丑数系列算法](https://mp.weixin.qq.com/s/XXsWwDml_zHiTEFPZtbe3g)
>
> [如何用算法高效寻找素数？](https://mp.weixin.qq.com/s?__biz=MzAxODQxMDM0Mw==&mid=2247484472&idx=1&sn=ab8e97d0211de37bf6770a63caacc630&scene=21#wechat_redirect)
>
> [动态规划之 KMP 算法详解](https://mp.weixin.qq.com/s?__biz=MzAxODQxMDM0Mw==&mid=2247484475&idx=1&sn=8e9518d67ae8f4c16f14fb0c4d584c79&source=41#wechat_redirect)
>
> [求图的连通子图的个数并保存每个子图的节点python - 懒惰的星期六 - 博客园](https://www.cnblogs.com/sunupo/p/13510172.html)
>
> [数字字符串变换 - 懒惰的星期六 - 博客园](https://www.cnblogs.com/sunupo/p/13561911.html)
>
> [剑指 Offer II 105. 岛屿的最大面积 - 力扣（Leetcode）](https://leetcode.cn/problems/ZL6zAn/)
>
> [重庆大学第八届编程大赛初赛1、2题目 - 懒惰的星期六 - 博客园](https://www.cnblogs.com/sunupo/p/12993870.html)
>
> [剑指 Offer II 105. 岛屿的最大面积 - 力扣（Leetcode）](https://leetcode.cn/problems/ZL6zAn/solutions/1412188/dao-yu-de-zui-da-mian-ji-by-leetcode-sol-c9ni/)



### [剑指 Offer II 105. 岛屿的最大面积 - 力扣（Leetcode）](https://leetcode.cn/problems/ZL6zAn/solutions/1412188/dao-yu-de-zui-da-mian-ji-by-leetcode-sol-c9ni/)

```java
class Solution {
    public int maxAreaOfIsland(int[][] grid) {
        if(grid.length==0){
            return 0;
        }

        int res=0;
        for(int i=0; i< grid.length; i++){
            for(int j=0; j< grid[0].length; j++){
                if(grid[i][j]==1){
                    res = Math.max(res, dfs(i,j, grid));
                }
            }
        }
       
        return res;

    }
    int dfs(int i, int j, int[][] grid){
        if(i>=0 && j>=0 && i<grid.length && j<grid[0].length && grid[i][j]==1){
            grid[i][j]=0;
            int res = 1;
            res+=dfs(i,j+1,grid);
            res+=dfs(i,j-1,grid);
            res+=dfs(i+1,j,grid);
            res+=dfs(i-1,j,grid);
            return res;
        }
        return 0;
    }
}
```

### [求图的连通子图的个数并保存每个子图的节点python - 懒惰的星期六 - 博客园](https://www.cnblogs.com/sunupo/p/13510172.html)

1.

```
输入：

第一行：第一个数代表有5个节点，第二个数代表下面还有多少行数据

输出：

连通子图的个数

每个连通子图的节点（输出顺序为每个连通子图节点编号最小的先输出）
```

输入： 

```
    # 测试数据一
    # 5 5
    # 1 2
    # 2 2
    # 3 1
    # 4 2
    # 5 4

    # 测试数据二
    # 5 5
    # 1 2
    # 2 3
    # 3 2
    # 4 5
    # 5 5
```

 输出：

```
1
1 2 3 4 5
```

#### 代码一：　

```python
def dfs(matrix,visited,i):
    for j in range(len(matrix)):
        if matrix[i][j]==1 and visited[j]==0:
            visited[j]=1
            id[j]=cnt
            dfs(matrix,visited,j)


if __name__ == '__main__':
    n, m = list(map(int, input().split()))
    data = []
    for _ in range(m):
        data.append(list(map(int,input().split())))
        pass
    matrix=[[0]*n  for _ in range(n)]
    visited=[0]*n  # 记录每个节点是否被访问过
    for i in range(n):
        matrix[i-1][i-1]=1
    for i,j in data:
        matrix[i-1][j-1]=1
        matrix[j - 1][i - 1] = 1
    print(matrix)
    cnt=0
    result=[]  # 记录每个连通区域节点的二维列表，其实可以直接根据id列表的信息，每判断个节点属于哪个联通分区
    ready=set()  # 已经放进了result列表的节点会被加入到ready
    for i in range(len(matrix)):
        if visited[i]==0:
            dfs(matrix,visited,i)
            cnt+=1
        tmp = []
        for idx, ele in enumerate(visited):
            if ele == 1 and idx + 1 not in ready:  # 
                tmp.append(idx + 1)  # 没有放进ready的节点会被加入到tmp，随后添加到result
                ready.add(idx + 1)  #被添加到result的节点 记录到ready中
        if len(tmp)!=0: # 循环为执行len(matrix)次，但是连通分区数目小于等于最大节点数，所以需要判断，避免添加空列表到result
            result.append(tmp)
    print(cnt,result,id)
```



用一个id列表，长度等于节点数目，简化代码。因为只通过id记录了每个节点对应的联通分区数目。

```python
def dfs(matrix,visited,i,cnt,id):
    for j in range(len(matrix)):
        if matrix[i][j]==1 and visited[j]==0:
            visited[j]=1
            id[j]=cnt
            dfs(matrix,visited,j,cnt,id)
if __name__ == '__main__':
    n, m = list(map(int, input().split()))
    data = []
    for _ in range(m):
        data.append(list(map(int,input().split())))
        pass
    matrix=[[0]*n  for _ in range(n)]
    visited=[0]*n  # 记录每个节点是否被访问过
    id=[-1]*n # 记录每个节点对应的联通分区编号
    for i in range(n):
        matrix[i-1][i-1]=1
    for i,j in data:
        matrix[i-1][j-1]=1
        matrix[j - 1][i - 1] = 1
    print(matrix)
    cnt=0
    for i in range(len(matrix)):
        if visited[i]==0:
            dfs(matrix,visited,i,cnt,id)
            cnt+=1
    print(cnt,id)
```

```
输出：
[[1, 1, 0, 0, 0], [1, 1, 1, 0, 0], [0, 1, 1, 0, 0], [0, 0, 0, 1, 1], [0, 0, 0, 1, 1]]
2 [0, 0, 0, 1, 1]
```

![复制代码](assets/copycode.gif)



