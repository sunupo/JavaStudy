## [12.0 Zookeeper 数据同步流程 | 菜鸟教程](https://www.runoob.com/w3cnote/zookeeper-data-sync.html)

在 Zookeeper 中，主要依赖 ZAB 协议来实现分布式数据一致性。

ZAB 协议分为两部分：

-   消息广播
-   崩溃恢复

### 消息广播

Zookeeper 使用单一的主进程 Leader 来接收和处理客户端所有事务请求，并采用 ZAB 协议的原子广播协议，将事务请求以 Proposal 提议广播到所有 Follower 节点，当集群中有过半的Follower 服务器进行正确的 ACK 反馈，那么Leader就会再次向所有的 Follower 服务器发送commit 消息，将此次提案进行提交。这个过程可以简称为 2pc 事务提交，整个流程可以参考下图，注意 Observer 节点只负责同步 Leader 数据，不参与 2PC 数据同步过程。

![](https://www.runoob.com/wp-content/uploads/2020/09/zk-data-stream-async.png)

### 崩溃恢复

在正常情况消息广播情况下能运行良好，但是一旦 Leader 服务器出现崩溃，或者由于网络原理导致 Leader 服务器失去了与过半 Follower 的通信，那么就会进入崩溃恢复模式，需要选举出一个新的 Leader 服务器。在这个过程中可能会出现两种数据不一致性的隐患，需要 ZAB 协议的特性进行避免。

-   1、Leader 服务器将消息 commit 发出后，立即崩溃
-   2、Leader 服务器刚提出 proposal 后，立即崩溃

ZAB 协议的恢复模式使用了以下策略：

-   1、选举 zxid 最大的节点作为新的 leader
-   2、新 leader 将事务日志中尚未提交的消息进行处理

下个章节详细讲解 leader 选举过程。



## ✅Zookeeper集群中的角色有哪些？有什么区别？

ZK中主要有以下角色：

领导者（leader）：负责进行投票的发起和决议，更新系统状态。为客户端提供读和写服务。

跟随者（follower）：用于接受客户端请求并想客户端返回结果，在选主过程中参与投票。为客户端提供读服务。

观察者（observer）：可以接受客户端连接，将写请求转发给leader，但observer不参加投票过程，只同步leader的状态，observer的目的是为了扩展系统，提高读取速度。

客户端（client）：请求发起方

