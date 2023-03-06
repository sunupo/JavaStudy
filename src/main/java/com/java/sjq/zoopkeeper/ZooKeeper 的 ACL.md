[(122条消息) 【zookeeper】ACL\_zk的acl\_绝世好阿狸的博客-CSDN博客](https://blog.csdn.net/u010900754/article/details/78504292/)

ACL全称是access control list，是zk中控制访问权限的一种策略。

大致的思路是任何人都可以登录zk，但是登录以后，zk的节点树的每一个节点都有自己的访问控制，所以用户登录以后需要先添加自己此次登录的权限，然后才能访问相应的节点。如果没有某一个节点的权限，那么zk会报错。

传统的访问控制最经典的是linux系统的基于user，group和other三种类型的权限控制。这种控制权限方案是粗粒度的，因为用户1创建了f文件，用户1属于g1和g2两个组，那么如果想让g1和g2拥有对f不同的权限是不行的，因为g1和g2都属于f的g权限，肯定是相同的，所以这就是linux这种权限控制的弊端。

zk的acl是一个三元组：<schema，id，permission>

`schema` 是acl的类型，zk的acl权限有以下四种常用类型：

1.world：这是zk创建节点以后的默认acl类型，其实相当于不设置权限，任何人都可以操作和访问；

2.digest：这个应该是最常用的类型，相当于用户名和密码这种；

3.super：超管模式，需要在启动zk服务器时添加，主要用于管理忘记acl的节点；

4.ip：通过ip来限制访问。

id指的是实体，实体又是什么？这个是和schema相关的，每一种schema的实体的类型是不同的。说白了就是这个schema指定的acl类型的内容。

![](https://img-blog.csdn.net/20171111005718835?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDkwMDc1NA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

这是schema和id的对应关系：

ip的schema，id就是允许的ip地址。当然zk允许以网段的形式来配置，比如“ip:192.168.1.\*” 和 “ip:192.168.1.10/16”。

world的schema，id只能是固定的“anyone”。

super和digest一样，但是需要在启动脚本配置。

digest：id就是用户名和密码，但是密码并不是明文，而是一个哈希值，这也是一种比较好的服务端存储客户端密码的方式，存密码的哈希而不是明文的好处有：一旦服务器的密码被泄露，窃听者也不会得到密码，安全系数更高。那么格式就是上表所示。我们指定了用户名和密码以后，如何得到哈希值呢？可以使用zk提供的工具。

```
String m = DigestAuthenticationProvider.generateDigest("ly:123");
```

使用如上函数即可得到。

permission:

总共有五种权限

c：在当前节点创建子节点；

d：在当前节点删除子节点；

r：获取数据；

w：写入数据；

a：设置acl权限；

需要注意的是，zk的权限是不继承的。

下面看一下使用zk客户端脚本的例子：

可以使用getAcl 和setAcl命令来获取和设置节点acl。

创建一个新的节点/Tacl，查看默认acl：

![](https://img-blog.csdn.net/20171111011539316?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDkwMDc1NA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

可以看到默认的acl'是world：anyone，权限是dcrwa。

设置一个digest的acl，ly：123.权限是ra。

哈希值是：

```
ly:4y+/eVxq19jjdUAGDetnozMNvls=
```

![](https://img-blog.csdn.net/20171111012235513?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDkwMDc1NA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

查看当前的acl：

![](https://img-blog.csdn.net/20171111012312742?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDkwMDc1NA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

此时我们只有ra权限，如果write值会报错：

![](https://img-blog.csdn.net/20171111012401258?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDkwMDc1NA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

重新登录下，然后使用addauth命令添加权限：

![](https://img-blog.csdn.net/20171111013328350?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDkwMDc1NA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

修改为ip的schema：

![](https://img-blog.csdn.net/20171111013542772?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDkwMDc1NA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

![](https://img-blog.csdn.net/20171111013655707?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDkwMDc1NA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

下面是使用java的api的例子：

使用addAuthInfo函数来添加权限，也就是在使用ZooKeep实例登录zk以后。

```
public static void main(String[] args) {try {ZooKeeper zk = new ZooKeeper("127.0.0.1:2181", 5000, null);zk.addAuthInfo("digest", "ly:123".getBytes());System.out.println(zk.getData("/Tacl", false, null));ZooKeeper zk1 = new ZooKeeper("127.0.0.1:2181", 5000, null);System.out.println(zk1.getData("/Tacl", false, null));} catch (IOException e) {e.printStackTrace();} catch (KeeperException e) {e.printStackTrace();} catch (InterruptedException e) {e.printStackTrace();}}
```

这里的/Tacl节点是digest类型的acl，有rwa权限。然后创建了两个zk客户端，第一个加了auth，第二个没加，最后只有第一个可以获取data：

![](https://img-blog.csdn.net/20171111014634141?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDkwMDc1NA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

接下来，我们在上面的基础上使用api修改权限。

```
public static void main(String[] args) {try {ZooKeeper zk = new ZooKeeper("127.0.0.1:2181", 5000, null);zk.addAuthInfo("digest", "ly:123".getBytes());List<ACL> acls = new ArrayList<ACL>();Id id1 = new Id("digest", DigestAuthenticationProvider.generateDigest("ly:999"));ACL acl1 = new ACL(Perms.READ | Perms.WRITE | Perms.ADMIN, id1);acls.add(acl1);Id id2 = new Id("world", "anyone");ACL acl2 = new ACL(Perms.READ, id2);acls.add(acl2);zk.setACL("/Tacl", acls, -1);} catch (IOException e) {e.printStackTrace();} catch (KeeperException e) {e.printStackTrace();} catch (InterruptedException e) {e.printStackTrace();} catch (NoSuchAlgorithmException e) {e.printStackTrace();}}
```

ID类封装了acl的id信息。ACL类封装了一个acl的信息，需要有id和perm，perm是枚举类型，也就是权限，可以使用或操作连接多个。每一次设置acl的时候，我们都可以设置多个acl，这就是为什么setAcl函数需要一个ACL的List类型。

在上面的例子，我们为/Tacl节点添加了两种acl，一个是digest的，有rwa，另一个是world，只有r。

运行以后，再登陆客户端检查权限：

![](https://img-blog.csdn.net/20171111020253891?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDkwMDc1NA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

设置成功。