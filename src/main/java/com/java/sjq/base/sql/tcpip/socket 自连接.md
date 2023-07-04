[(130条消息) socket自连接\_socket自己连接自己\_有梦想的码农001的博客-CSDN博客](https://blog.csdn.net/qq_43062920/article/details/120680747)

## 同时打开

两个应用程序同时彼此执行主动打开的情况是可能的，但是发生的可能性极小。每一方必须发送一个SYN，且这些SYN必须传递给对方。这需要每一方使用一个对方熟知的端口作为本地端口。这又称为同时打开。两端必须几乎在同时启动，以便收到彼此的SYN。只要两端有较长的往返时间就能保证这一点。  
TCP是特意设计为了可以处理同时打开，对于同时打开它仅建立一条连接而不是两条连接（其他的协议族，最突出的是OSI运输层，在这种情况下将建立两条连接而不是一条连接）出现同时打开的情况时，两端几乎在同时发送SYN，并进入 SYN\_SENT状态。当每一端收到SYN时，状态变为SYN\_RCVD，同时它们都再发SYN并对收到的SYN进行确认。当双方都收到SYN及相应的ACK时，状态都变迁为ESTABLISHED。  
在同时打开的情况下，即使客户端没有开启监听依然会建立连接。  
![在这里插入图片描述](https://img-blog.csdnimg.cn/48ea0474f8fd437a98e7d9c2e90aa074.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA5pyJ5qKm5oOz55qE56CB5YacMDAx,size_16,color_FFFFFF,t_70,g_se,x_16)

## 自连接原因

看完同时打开应该明白了为什么会出现子连接，当客户端调用connect连接自身ip时，此时内核随机分配一个端口给客户端（这里并不是真正的随机而是通过计数器来实现的）（其范围可通过sys -A|grep range来查看），若此时分配的端口号等于目的端口号，则会出现自连接。  
客户端发送syn后等待服务器返回ack，而此时客户端发的syn报文从网卡绕了一圈又回到了内核被自己收到，而此时客户端只会认为这个报文是另一台机器发过来的，触发‘同时打开’，发送ack报文，ack报文发送回来后 ，客户端认为是自己发送的第一条请求发回来了，就成功建立起连接，而以后的传输报文都被客户端看成是与服务器同时发的情况，所以ack报文还是能正常收发。

## 解决办法

1、监听端口设置在客户端ip\_local\_port范围外  
2、建立连接后判断是否是自连接（getsockname、getpeername)

## 测试代码

```c
#include<stdio.h>
#include<stdlib.h>
#include<sys/socket.h>
#include<unistd.h>
#include<errno.h>
#include<sys/types.h>
#include<netdb.h>
#include<string.h>
int main(int args,char*argv[])
{
        if(args<3){printf("using ip and port\n");return -1;}
        struct hostent* h;
        struct sockaddr_in clientaddr,servaddr;
        if((h = gethostbyname(argv[1]))==NULL)
        {
        printf("gethostbyname failed:%s\n",strerror(errno));
        return -1;
        }
        servaddr.sin_family = AF_INET;
        servaddr.sin_port = htons(atoi(argv[2]));
        memcpy(&servaddr.sin_addr,h->h_addr,h->h_length);
        int socklen = sizeof(struct sockaddr_in);
        int sockfd;
        int n = 65536;
        int min,max;
        min = max = 50000;
        while(n--)
        {
        memset(&clientaddr,0x00,sizeof(clientaddr));
        sockfd = socket(AF_INET,SOCK_STREAM,6);
        if(sockfd<0){printf("socket failed\n");continue;}
        if(connect(sockfd,(struct sockaddr*)&servaddr,sizeof(clientaddr))!=0)
        {
        if(getsockname(sockfd,(struct sockaddr*)&clientaddr,(socklen_t*)&socklen)!=0)
        {printf("%s\n",strerror(errno)); close(sockfd);return -1;}
        min = min<htons(clientaddr.sin_port)?min:htons(clientaddr.sin_port);
        max = max>htons(clientaddr.sin_port)?max:htons(clientaddr.sin_port);
        printf("connect failed(%d)\n",ntohs(clientaddr.sin_port));
        close(sockfd);
        continue;
        }
        if(getsockname(sockfd,(struct sockaddr*)&clientaddr,(socklen_t*)&socklen)!=0)
        {printf("%s\n",strerror(errno)); close(sockfd);return -1;}
        printf("connect success(%d)\n",ntohs(clientaddr.sin_port));
        break;
        }
        printf("min=%d,max=%d\n",min,max);
        if(n<0)return -1;
        char buffer[10];
        for(int i =0;i<10;++i)
        {buffer[i] = "0123456789ABCDEF"[i%16];}
        int iret;
        for(int i = 0;i<2;++i)
        {
                iret = send(sockfd,buffer,sizeof(buffer),0);
                if(iret<0){printf("send failed:%s\n",strerror(errno));close(sockfd);return -1;}
                if(iret==0){printf("close connect\n");close(sockfd);}
                printf("发送：%s\n",buffer);
                iret = recv(sockfd,buffer,sizeof(buffer),0);
                if(iret<0){printf("recv failed:%s\n",strerror(errno));close(sockfd);return -1;}
                if(iret==0){printf("close connect\n");close(sockfd);}
                printf("接收：%s\n",buffer);
        }
}
```