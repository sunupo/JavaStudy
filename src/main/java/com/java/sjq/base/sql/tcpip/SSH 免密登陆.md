[(124条消息) 实现SSH免密登录的方法\_ssh免密登陆\_慕城南风的博客-CSDN博客](https://blog.csdn.net/lovedingd/article/details/126487899)

**serverB实现免密登录serverA有两种方向：**

> 1. serverB生成秘钥对，将serverB密钥对中的公钥给serverA
> 2. serverA生成秘钥对，将serverA密钥对中的私钥给serverB
>
> 就是 B登录 ——>A，B保存私钥，A保存公钥