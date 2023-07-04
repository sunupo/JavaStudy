[【RocketMQ】msgId与offsetMsgId - 知乎](https://zhuanlan.zhihu.com/p/410367864)
## 一. 概念

## 1\. msgId(uniqId)

> 由 **producer客户端** 生成，调用方法MessageClientIDSetter.createUniqID()生成全局唯一的Id

```
private static final int LEN;
    private static final String FIX_STRING;
    private static final AtomicInteger COUNTER;
    private static long startTime;
    private static long nextStartTime;

    public static String createUniqID() {

        StringBuilder sb = new StringBuilder(LEN * 2);
        sb.append(FIX_STRING);   // 固定值，程序启动时生成
        sb.append(UtilAll.bytes2string(createUniqIDBuffer())); // 变化值 根据时间差+自增长生成
        return sb.toString();
    }
```

1.  **FIX\_STRING** 固定前缀
2.  生成规则：客户端的IP、进程ID、MessageClientIDSetter类加载器的hashcode

```
static {
        byte[] ip;
        try {
            ip = UtilAll.getIP();
        } catch (Exception e) {
            ip = createFakeIP();
        }
        LEN = ip.length + 2 + 4 + 4 + 2;
        ByteBuffer tempBuffer = ByteBuffer.allocate(ip.length + 2 + 4);
        tempBuffer.put(ip);                                 // 客户端IP
        tempBuffer.putShort((short) UtilAll.getPid());      // 进程ID
        tempBuffer.putInt(MessageClientIDSetter.class.getClassLoader().hashCode()); // 类加载器hashCode
        FIX_STRING = UtilAll.bytes2string(tempBuffer.array());
        setStartTime(System.currentTimeMillis());
        COUNTER = new AtomicInteger(0);
    }


     private synchronized static void setStartTime(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        startTime = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, 1);
        nextStartTime = cal.getTimeInMillis();
    }

    // 获取进程ID
    public static int getPid() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName(); // format: "pid@hostname"
        try {
            return Integer.parseInt(name.substring(0, name.indexOf('@')));
        } catch (Exception e) {
            return -1;
        }
    }
```

1.  **createUniqIDBuffer** 变化值
2.  生成规则：当前时间与系统启动时间的差值，以及自增序号

```
private static byte[] createUniqIDBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(4 + 2);
        long current = System.currentTimeMillis();

        // 每月1号重新计算 startTime，避免时间戳差值无限增加
        if (current >= nextStartTime) {
            setStartTime(current);
        }

        // 时间差值（当前时间戳-系统启动时时间戳）
        buffer.putInt((int) (System.currentTimeMillis() - startTime));

         // 1. short：毫秒内自增长，几万够了 
         // 2. COUNTER达到最大值后，会从最大的负数开始递增，如-2147483648 -2147483647

        buffer.putShort((short) COUNTER.getAndIncrement());
        return buffer.array();
    }
```

1.  Broker 服务端将消息追加到内存后会返回其物理偏移量，即在 commitlog 文件中的偏移量，然后会生成一个id，代码中虽然也叫 msgId，其实是 offsetMsgId
2.  组成规则：Broker 服务器的 IP 与端口号、消息的物理偏移量

-   ByteBuffer input： 用来存放 offsetMsgId 的字节缓存区
-   ByteBuffer addr： Broker 服务器的 IP 地址与端口号，即通过解析 offsetMsgId 从而得到消息服务器的地址信息
-   long offset：消息的物理偏移量

```
类：org.apache.rocketmq.common.message.MessageDecoder

    public static String createMessageId(final ByteBuffer input, final ByteBuffer addr, final long offset) {
        input.flip();
        int msgIDLength = addr.limit() == 8 ? 16 : 28;
        input.limit(msgIDLength);

        input.put(addr);
        input.putLong(offset);

        return UtilAll.bytes2string(input.array());
    }
```

## 二. 消息发送

> 其中包含了msgId、offsetMsgId

![](https://pic1.zhimg.com/v2-531450ca43566ea998f88b0fef70f91c_b.png)

## 三. 消息消费

![](https://pic4.zhimg.com/v2-9cd6ff6c68606cf25bee146d898c140f_b.jpg)

1.  消费客户端返回的对象是 MessageClientExt ，继承自 MessageExt ，MessageExt继承自 Message
2.  **如果消息消费失败需要重试，RocketMQ 的做法是将消息重新发送到 Broker 服务器，此时 msgId 是不会发生变化的，但该消息的 offsetMsgId 会发生变化，因为其存储在 Broker 服务器中的位置发生了变化**
3.  在调用 MessageClientExt 中的 getMsgId() 方法时，先返回消息属性中的MsgId，不存在则返回消息的 offsetMsgId

```
public String getMsgId() {
    String uniqID = MessageClientIDSetter.getUniqID(this);
    if (uniqID == null) {
        return this.getOffsetMsgId();
    } else {
        return uniqID;
    }
}
```

1.  MessageExt 的toString 方法

```
public String toString() {
        return "MessageExt [brokerName=" + brokerName + ", queueId=" + queueId + ", storeSize=" + storeSize + ", queueOffset=" + queueOffset
            + ", sysFlag=" + sysFlag + ", bornTimestamp=" + bornTimestamp + ", bornHost=" + bornHost
            + ", storeTimestamp=" + storeTimestamp + ", storeHost=" + storeHost + ", msgId=" + msgId
            + ", commitLogOffset=" + commitLogOffset + ", bodyCRC=" + bodyCRC + ", reconsumeTimes="
            + reconsumeTimes + ", preparedTransactionOffset=" + preparedTransactionOffset
            + ", toString()=" + super.toString() + "]";
    }
```

1.  MessageClientExt类( 本类没有重写toString()方法，调用的是父类MessageExt的方法)

```
public class MessageClientExt extends MessageExt {

    public String getOffsetMsgId() {
        return super.getMsgId();
    }

    public void setOffsetMsgId(String offsetMsgId) {
        super.setMsgId(offsetMsgId);
    }

    @Override
    public String getMsgId() {
        String uniqID = MessageClientIDSetter.getUniqID(this);
        if (uniqID == null) {
            return this.getOffsetMsgId();
        } else {
            return uniqID;
        }
    }

    public void setMsgId(String msgId) {
        //DO NOTHING
        //MessageClientIDSetter.setUniqID(this);
    }
}
```

## 四. Dashboard根据ID查询

1.  界面显示的是 msgId，即uniqId
2.  RocketMQ会先调用queryMsgById方法，先通过msgId查询，查询无结果再通过 offsetMsgId 查询
3.  界面新增显示 offsetMsgId 字段

```
MessageServiceImpl.viewMessage

    String offsetMsgId = ((MessageClientExt) messageExt).getOffsetMsgId();
    messageView.setOffsetMsgId(offsetMsgId);
```

## 唯一性：
[RocketMQ msgId生成算法](https://www.shuzhiduo.com/A/KE5Q4L1PJL/)

### msgId
首先应用不重启的情况下msgId是保证唯一性的，应用重启了只要系统的时钟不变msgId也是唯一的。所以只要系统的时钟不回拨我们就可以保证msgId的全局唯一。

有人也许会说应用运行了一个月再进行重启msgId就会重复了。从生成算法上来说是的！但是MQ的message是有时效性的，有效期是72小时也就是3天。每天的凌晨4点rocketMQ会把过期的message清除掉。所以msgId也是保证全局唯一的。


### offsetMsgId
如果消息消费失败需要重试，RocketMQ 的做法是将消息重新发送到 Broker 服务器，此时全局 msgId 是不会发送变化的，但该消息的 offsetMsgId 会发送变化，因为其存储在服务器中的位置发生了变化。