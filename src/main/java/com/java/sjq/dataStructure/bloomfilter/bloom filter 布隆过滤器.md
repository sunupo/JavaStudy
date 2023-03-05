[(122条消息) 布隆过滤器Bloom Filter简介\_多啦@不懂a梦的博客-CSDN博客](https://blog.csdn.net/qq_55624813/article/details/121316520)
# 参数的选择：
假设E表示错误率，n表示要插入的元素个数，m表示bit数组的长度，k表示hash函数的个数。

（1）当hash函数个数 k = (ln2) \* (m/n)时，错误率E最小（此时bit数组中有一半的值为0）

（2）在错误率不大于E的情况下，bit数组的长度m需要满足的条件为：m ≥ n \* lg(1/E)。

（3）结合上面两个公式，在hash函数个数k取到最优时，要求错误率不大于E，这时我们对bit数组长度m的要求是：m>=nlg(1/E) \* lg(e) ，也就是 m ≥ 1.44n\*lg(1/E)（lg表示以2为底的对数）
[(122条消息) Bloom Filter原理与实现\_guoziqing506的博客-CSDN博客](https://blog.csdn.net/guoziqing506/article/details/52852515)


[Redis 之布隆过滤器，增强版，布谷鸟过滤器 - CharyGao - 博客园](https://www.cnblogs.com/Chary/p/15682276.html)

[布隆，牛逼！布谷鸟，牛逼！ - 掘金](https://juejin.cn/post/6924636027948630029#heading-1)
过滤器的数组中为 1 的位置越来越多，带来的结果就是误判率的提升。从而必须得进行重建。

除了删除这个问题之外，布隆过滤器还有一个问题：查询性能不高。

因为真实场景中过滤器中的数组长度是非常长的，经过多个不同 Hash 函数后，得到的数组下标在内存中的跨度可能会非常的大。跨度大，就是不连续。不连续，就会导致 CPU 缓存行命中率低。

- 误判率推导： http://pages.cs.wisc.edu/~cao/papers/summary-cache/node8.html
    - 当hash函数个数 k = (ln2) * (m/n)时，错误率E最小（此时bit数组中有一半的值为0）
    - 

如果你想玩一下布隆过滤器，可以访问一下这个网站：
https://www.jasondavies.com/bloomfilter/