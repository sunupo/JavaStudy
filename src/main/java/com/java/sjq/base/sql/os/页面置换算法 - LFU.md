# [数据结构基本功：设计最大频率栈](https://mp.weixin.qq.com/s/FM7pi3VH4jVlb_BNcGR8ow)

****后台回复******进群******一起刷力扣😏****

## 

****点击卡片可搜索关键词👇****

## 

**读完本文，可以去力扣解决如下题目：**

895.最大频率栈（**Hard**）

![图片](https://mmbiz.qpic.cn/mmbiz_png/zG6oSx6T0qQFxmfYcbKJwRF0w1oBzgrBeMNt9eHuannDuxkIXVicAKiavNlLleVwXu8icv6mI9Q9Ih4KichBjj8F4w/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

我个人很喜欢设计特殊数据结构的问题，毕竟在工作中会经常用到基本数据结构，而设计类的问题就非常考验对基本数据结构的理解和运用。

力扣第 895 题要求我们实现一个特殊的数据结构「最大频率栈」，比较有意思，让我们实现下面这两个 API：

```auto
class FreqStack {    // 在栈中加入一个元素 val    public void push(int val) {}    // 从栈中删除并返回出现频率最高的元素    // 如果频率最高的元素不止一个，    // 则返回最近添加的那个元素    public int pop() {}}
```

比如下面这个例子：

```auto
FreqStack stk = new FreqStack();// 向最大频率栈中添加元素
stk.push(2); stk.push(7); stk.push(2);
stk.push(7); stk.push(2); stk.push(4);

// 栈中元素：[2,7,2,7,2,4]

stk.pop() // 返回 2
// 因为 2 出现了三次

// 栈中元素：[2,7,2,7,4]

stk.pop() // 返回 7
// 2 和 7 都出现了两次，但 7 是最近添加的

// 栈中元素：[2,7,2,4]

stk.pop() // 返回 2

// 栈中元素：[2,7,4]

stk.pop() // 返回 4

// 栈中元素：[2,7]
```

**这种设计数据结构的问题，主要是要搞清楚问题的难点在哪里，然后结合各种基本数据结构的特性，高效实现题目要求的 API**。

那么，我们仔细思考一下 `push` 和 `pop` 方法，难点如下：

1、每次 `pop` 时，必须要知道频率最高的元素是什么。

2、如果频率最高的元素有多个，还得知道哪个是最近 `push` 进来的元素是哪个。

为了实现上述难点，我们要做到以下几点：

1、肯定要有一个变量 `maxFreq` 记录当前栈中最高的频率是多少。

2、我们得知道一个频率 `freq` 对应的元素有哪些，且这些元素要有时间顺序。

3、随着 `pop` 的调用，每个 `val` 对应的频率会变化，所以还得维持一个映射记录每个 `val` 对应的 `freq`。

综上，我们可以先实现 `FreqStack` 所需的数据结构：

```auto
class FreqStack {
    // 记录 FreqStack 中元素的最大频率
    int maxFreq = 0;
    // 记录 FreqStack 中每个 val 对应的出现频率，后文就称为 VF 表
    HashMap<Integer, Integer> valToFreq = new HashMap<>();
    // 记录频率 freq 对应的 val 列表，后文就称为 FV 表
    HashMap<Integer, Stack<Integer>> freqToVals = new HashMap<>();
}
```

其实这有点类似前文 手把手实现 LFU 算法，注意 `freqToVals` 中 `val` 列表用一个栈实现，如果一个 `freq` 对应的元素有多个，根据栈的特点，可以首先取出最近添加的元素。

要记住在 `push` 和 `pop` 方法中同时修改 `maxFreq`、`VF` 表、`FV` 表，否则容易出现 bug。

现在，我们可以来实现 `push` 方法了：

```auto
public void push(int val) {
    // 修改 VF 表：val 对应的 freq 加一
    int freq = valToFreq.getOrDefault(val, 0) + 1;
    valToFreq.put(val, freq);
    // 修改 FV 表：在 freq 对应的列表加上 val
    freqToVals.putIfAbsent(freq, new Stack<>());
    freqToVals.get(freq).push(val);
    // 更新 maxFreq
    maxFreq = Math.max(maxFreq, freq);
}
```

`pop` 方法的实现也非常简单：

```auto
public int pop() {
    // 修改 FV 表：pop 出一个 maxFreq 对应的元素 v
    Stack<Integer> vals = freqToVals.get(maxFreq);
    int v = vals.pop();
    // 修改 VF 表：v 对应的 freq 减一
    int freq = valToFreq.get(v) - 1;
    valToFreq.put(v, freq);
    // 更新 maxFreq
    if (vals.isEmpty()) {
        // 如果 maxFreq 对应的元素空了
        maxFreq--;
    }
    return v;
}
```

这样，两个 API 都实现了，算法执行过程如下：

![图片](data:image/svg+xml,%3C%3Fxml version='1.0' encoding='UTF-8'%3F%3E%3Csvg width='1px' height='1px' viewBox='0 0 1 1' version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'%3E%3Ctitle%3E%3C/title%3E%3Cg stroke='none' stroke-width='1' fill='none' fill-rule='evenodd' fill-opacity='0'%3E%3Cg transform='translate(-249.000000, -126.000000)' fill='%23FFFFFF'%3E%3Crect x='249' y='126' width='1' height='1'%3E%3C/rect%3E%3C/g%3E%3C/g%3E%3C/svg%3E)

嗯，这道题就解决了，Hard 难度的题目也不过如此嘛~

以往的数据结构设计文章：

-   [**手把手实现 LFU 算法**](http://mp.weixin.qq.com/s?__biz=MzAxODQxMDM0Mw==&mid=2247486545&idx=1&sn=315ebfafa82c0dd3bcd9197eb270a7b6&chksm=9bd7f259aca07b4f063778509b3803993bc0d6cdaff32c076a102547b0afb82a5eea6119ed1a&scene=21#wechat_redirect)

-   [**手把手实现 LRU 算法**](http://mp.weixin.qq.com/s?__biz=MzAxODQxMDM0Mw==&mid=2247486428&idx=1&sn=3611a14535669ba3372c73e24121247c&chksm=9bd7f5d4aca07cc28c02c3411d0633fc12c94c2555c08cbfaa2ccd50cc2d25160fb23bccce7f&scene=21#wechat_redirect)

-   [**设计朋友圈时间线功能**](http://mp.weixin.qq.com/s?__biz=MzAxODQxMDM0Mw==&mid=2247484499&idx=1&sn=64f75d4bdbb4c5777ba199aae804d138&chksm=9bd7fa5baca0734dc51f588af913140560b994e3811dac6a7fa8ccfc2a31aca327f1faf964c2&scene=21#wechat_redirect)

-   [**单调栈结构解决三道算法题**](http://mp.weixin.qq.com/s?__biz=MzAxODQxMDM0Mw==&mid=2247487704&idx=1&sn=eb9ac24c644aa0950638c9b20384e982&chksm=9bd7eed0aca067c6b4424c40b7f234c815f83edfbb5efc9f51581335f110e9577114a528f3ec&scene=21#wechat_redirect)

-   [**单调队列结构解决滑动窗口问题**](http://mp.weixin.qq.com/s?__biz=MzAxODQxMDM0Mw==&mid=2247488087&idx=1&sn=673aa4e8deb942b951948650928c336e&chksm=9bd7ec5faca06549ba6176540fef04f93c1c9f55b303106688b894a2029e00b8cce1a9ba57a4&scene=21#wechat_redirect)

-   [**一道数组去重的算法题把我整不会了**](http://mp.weixin.qq.com/s?__biz=MzAxODQxMDM0Mw==&mid=2247486946&idx=1&sn=94804eb15be33428582544a1cd90da4d&chksm=9bd7f3eaaca07afc6fdfa94d05fa3007d9ecc54914a238e6deabeafd5032a299155505b40f2d&scene=21#wechat_redirect)




**[精华文章目录点这里](http://mp.weixin.qq.com/s?__biz=MzAxODQxMDM0Mw==&mid=2247490273&idx=1&sn=f4e68b429830da44872f531c06204ea3&chksm=9bd7e4e9aca06dff6cbe3839e8ad427594d011674a228e346861684e7424325ab4bb5ccc0767&scene=21#wechat_redirect)** **🔗**

＿＿＿＿＿＿＿＿＿＿＿＿＿

**学好算法靠套路，认准 labuladong，****知乎、B站账号同名。****公众号后台****回复****「进群」****可加我好友，拉你进算法刷题群。**

**扫码关注我的微信视频号，不定期发视频、搞直播：**



![图片](data:image/svg+xml,%3C%3Fxml version='1.0' encoding='UTF-8'%3F%3E%3Csvg width='1px' height='1px' viewBox='0 0 1 1' version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'%3E%3Ctitle%3E%3C/title%3E%3Cg stroke='none' stroke-width='1' fill='none' fill-rule='evenodd' fill-opacity='0'%3E%3Cg transform='translate(-249.000000, -126.000000)' fill='%23FFFFFF'%3E%3Crect x='249' y='126' width='1' height='1'%3E%3C/rect%3E%3C/g%3E%3C/g%3E%3C/svg%3E)

 # [一文带你读懂LFU算法](https://baijiahao.baidu.com/s?id=1729525047952230592&wfr=spider&for=pc)

### LFU算法介绍

上篇文章一文带你读懂LRU算法中讲解了常用的内存淘汰算法——LRU算法的原理及代码实现，这篇文章我们再来了解另一种经常使用的内存淘汰算法——LFU算法。

为什么要引入LFU算法呢？因为LRU算法淘汰数据的立足点是根据使用时间间隔淘汰数据，将当前内存中最长时间没有使用过的数据清理掉。这样就存在一个问题：如果一个不会经常使用的数据偶然被使用了一次，那这个并不被经常使用的数据就会一直待在内存中，浪费内存空间。如果我们想淘汰内存中不经常使用的数据，保留经常使用的热点数据，就要使用到LFU算法了。

LFU的全称为Least Frequently Used，意思就是最不频繁使用，所以，LFU算法会淘汰掉使用频率最低的数据。如果存在相同使用频率的数据，则再根据使用时间间隔，将最久未使用的数据淘汰。

### LFU算法原理

上篇文章 LRU算法详解 中，LRU算法的实现使用了一个hash表+一个双向链表，hash表中存储了key值对应的具体node节点，node节点之间组成了一个双向链表，这样既提高了查询效率，又提高了操作效率。同样作为内存淘汰算法，LFU也需要使用类似的数据结构实现，不过LFU算法淘汰数据是基于使用频率的，所以，我们需要快速找到同一频率的所有节点，然后按照需要淘汰掉最久没被使用过的数据。所以，首先我们要有一个hash表来存储每个频次对应的所有节点信息，同时为了保证操作效率，节点与节点之间同样要组成一个双向链表，得到如下结构：

![](https://pics7.baidu.com/feed/b8389b504fc2d562e68fe4261bfeb6e574c66c6b.jpeg@f_auto?token=71dca382267fc81d1ae6194e8cf53252)

hash表中的key表示访问次数，value就是一个双向链表，链表中所有节点都是被访问过相同次数的数据节点。可以看到，相比较于LRU算法中的节点信息，LFU算法中节点的要素中除了包含具体的key和value之外，还包含了一个freq要素，这个要素就是访问次数，同hash表中的key值一致。这样做的好处是当根据key查找得到一个节点时，我们可以同时得到该节点被访问的次数，从而得到当前访问次数的所有节点。

有了LFU算法的主体结构之后，我们发现还缺少一个重要功能，就是如何根据key值获取value值。所以，参考LRU算法的数据结构，我们还需要有一个hash表来存储key值与节点之间的对应关系。最终，我们就可以得到LFU算法完整的数据结构：

![](https://pics2.baidu.com/feed/bd3eb13533fa828bd945a14effef673e960a5a36.jpeg@f_auto?token=2ad8da79205f489a81d9bfbb104c6ad5)

### LFU算法实现

基于上面的数据结构，我们就可以实现完整的LRU算法功能。首先看下查询数据的get操作的具体逻辑：

1.  如果key不存在返回null；

2.  如果key存在，则返回对应节点的value值。同时，需要将节点的访问次数+1，然后将该节点从原双向链表中移除，添加到新的访问次数对应的双向链表中。注意：如果原来访问次数对应的双向链表在移除该节点之后，只剩下了head节点和tail节点，说明没有真实的业务数据节点存在，则需要从访问次数hash表中移除这个链表。


为了方便理解，我从网上找了两个动态流程图可以参考：

（1）https://pic.leetcode-cn.com/00ec8b79c1ada23bb3910f81d688468cd0cc5179f85f9c266a5c76e827c3cdd6-4.gif

（2）https://pic.leetcode-cn.com/d652bc2345cf6b0ad980c8d7dae2c905b926a23e85fcd1c7270751786a353019-5.gif

1.  如果key已经存在，则更新对应节点的value值，然后将该节点的访问次数+1，然后将该节点从原双向链表中移除，添加到新的访问次数对应的双向链表中。注意：如果原来访问次数对应的双向链表在移除该节点之后，只剩下了head节点和tail节点，说明没有真实的业务数据节点存在，则需要从访问次数hash表中移除这个链表。

2.  如果key不存在，则执行新增数据的动作：


-   如果还未达到最大容量，则插入新的数据节点。节点的访问次数为1，如果hash表中不存在对应的双向链表则需要先创建链表；

-   如果超过了最大容量，则需要先删除访问次数最低的节点，再插入新节点。节点的访问次数为1，如果hash表中不存在对应的双向链表则需要先创建链表。


为了方便理解，我也找了一个put操作的动态流程图作为参考：  
https://pic.leetcode-cn.com/f9cbf292271ab715f5dab1f08bb0bab834fae7d24d26cc675ee0cc4fdb2f18c7-6.gif

清楚了具体实现逻辑，接下来就可以通过代码实现LFU算法了：

![](https://pics7.baidu.com/feed/b2de9c82d158ccbfdd67507a1b289a34b3354195.png@f_auto?token=a4ac135ef31c8c3b1582cd28f73112ea)

![](https://pics0.baidu.com/feed/4610b912c8fcc3ce4dcce6806eaaf082d53f206a.jpeg@f_auto?token=45f7980056db2240d1ca40bbc8e2e2f3)

### LRU和LFU对比

LRU算法淘汰数据的注重点是时间间隔，只淘汰最久未使用的数据；LFU算法淘汰数据的注重点是使用频率，只淘汰最不经常使用的数据。

LRU算法实现简单，只需要一个hash表+一个双向链表即可实现；LFU算法实现就复杂很多，需要两个hash表+多个双向链表才能实现。

具体使用时，选择哪种算法作为内存淘汰策略要看具体场景，如果对于热点数据的查询要求比较高，则最好采用LFU算法作为内存淘汰策略。如果没有那么高的热点数据要求，则可以选择实现更为简单的LRU算法。