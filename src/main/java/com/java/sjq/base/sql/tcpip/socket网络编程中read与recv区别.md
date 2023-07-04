[(125条消息) socket网络编程中read与recv区别\_socket read\_高晓伟\_Steven的博客-CSDN博客](https://blog.csdn.net/superbfly/article/details/72782264)

使用read接收文件，由于传过来的文件大小错误，结果导致程序卡死在read处，之后改用[recv](https://so.csdn.net/so/search?q=recv&spm=1001.2101.3001.7020)接收。  
recv使用MSG\_DONTWAIT，在发现多次接收长度小于等于0时，中断接收返回。  
下面是摘抄的一段read和recv区别的介绍。

1、read 与 recv 区别

read 原则：

```
    数据在不超过指定的长度的时候有多少读多少，没有数据则会一直等待。所以一般情况下：我们读取数据都需要采用循环读的方式读取数据，因为一次read 完毕不能保证读到我们需要长度的数据，read 完一次需要判断读到的数据长度再决定是否还需要再次读取。
```

recv 原则：  
recv 中有一个MSG\_WAITALL 的参数:  
recv(sockfd, buff, buff\_size, MSG\_WAITALL),  
正常情况下recv 是会等待直到读取到buff\_size 长度的数据，但是这里的WAITALL 也只是尽量读全，在有中断的情况下recv 还是可能会被打断，造成没有读完指定的buff\_size的长度。所以即使是采用recv + WAITALL 参数还是要考虑是否需要循环读取的问题，在实验中对于多数情况下recv (使用了MSG\_WAITALL)还是可以读完buff\_size，  
所以相应的性能会比直接read 进行循环读要好一些。

2、read 与 recv函数调用

```
    read(sockfd, buff, buff_size);       
    write(sockfd, buff, buff_size);
    recv(sockfd, buff, buff_size,MSG_WAITALL); //阻塞模式接收        
    send(scokfd, buff, buff_size,MSG_WAITALL); //阻塞模式发送
    recv(sockfd, buff, buff_size,MSG_DONTWAIT); //非阻塞模式接收        
    send(scokfd, buff, buff_size,MSG_DONTWAIT); //非阻塞模式发送
    recv(sockfd, buff, buff_size,0);        
    send(scokfd, buff, buff_size,0);
```

3、[socket](https://so.csdn.net/so/search?q=socket&spm=1001.2101.3001.7020)编程经验

```
    1）尽量使用recv(,,MSG_WAITALL),read必须配合while使用，否则数据量大(240*384)时数据读不完
    2）编程时写入的数据必须尽快读出，否则后面的数据将无法继续写入
    3）最佳搭配如下：
            nbytes = recv(sockfd, buff, buff_size,MSG_WAITALL);
            nbytes = send(scokfd, buff, buff_size,MSG_WAITALL);
```