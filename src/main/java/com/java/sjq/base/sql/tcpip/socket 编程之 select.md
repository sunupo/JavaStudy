> [(125条消息) linux使用epoll进行socket编程\_linux socket epoll\_路过的小熊\~的博客-CSDN博客](https://blog.csdn.net/qq_32348883/article/details/126686979)
>
> [(125条消息) 为什么linux select函数的第一个参数总应该是fdmax + 1 ?------poll和epoll不需要+1\_涛歌依旧的博客-CSDN博客](https://blog.csdn.net/stpeace/article/details/73612532)
>
> 
>
> [(125条消息) Linux epoll 在socket使用（二）\_epoll socket使用\_rjszcb的博客-CSDN博客](https://blog.csdn.net/rjszcb/article/details/122351674?spm=1001.2101.3001.6650.6&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-6-122351674-blog-126686979.pc_relevant_aa2&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-6-122351674-blog-126686979.pc_relevant_aa2&utm_relevant_index=9)
>
> [(125条消息) linux 应用和网络开发\_rjszcb的博客-CSDN博客](https://blog.csdn.net/rjszcb/category_10560608.html)
>
> 
>
> [(125条消息) linux select 多路复用介绍（一）-CSDN博客](https://blog.csdn.net/rjszcb/article/details/122351572)
>
> 

> [(125条消息) socket编程之select-CSDN博客](https://blog.csdn.net/NBA_1/article/details/123049632)

通过下面的代码，让我们对 socket 使用 select 编程有一个了解。

创建socket，bind，listen，accept。accept 返回的 connfd 在 select 注册，在死循环中不停的调用 select。 当FD_ISSET判断为true，就调用recv读数据。

```c++
#include <stdio.h>
#include <sys/time.h>
#include <sys/types.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <string.h>
#include <fcntl.h>
#include <stdlib.h>

int main(int argc, char* argv[]){
    if(argc <= 2)
    {
    printf(“usage: ip address + port numbers\n”);
    return -1;
    }
    const char* ip = argv[1];
    int port = atoi(argv[2]);

        printf("ip: %s\n",ip);
        printf("port: %d\n",port);

    int ret = 0;
    struct sockaddr_in address;
    bzero(&address,sizeof(address));
    address.sin_family = AF_INET;
    inet_pton(AF_INET,ip,&address.sin_addr);
    address.sin_port = htons(port);

    int listenfd = socket(PF_INET,SOCK_STREAM,0);
    if(listenfd < 0)
    {
        printf("Fail to create listen socket!\n");
        return -1;
    }

    ret = bind(listenfd,(struct sockaddr*)&address,sizeof(address));
    if(ret == -1)
    {
        printf("Fail to bind socket!\n");
        return -1;
    }

    ret = listen(listenfd,5); //监听队列最大排队数设置为5
    if(ret == -1)
    {
        printf("Fail to listen socket!\n");
        return -1;
    }

    struct sockaddr_in client_address;  //记录进行连接的客户端的地址
    socklen_t client_addrlength = sizeof(client_address);
    int connfd = accept(listenfd,(struct sockaddr*)&client_address,&client_addrlength);
    if(connfd < 0)
    {
        printf("Fail to accept!\n");
        close(listenfd);
    }

    char buff[1024]; //数据接收缓冲区
    fd_set read_fds;  //读文件操作符
    fd_set exception_fds; //异常文件操作符
    FD_ZERO(&read_fds);
    FD_ZERO(&exception_fds);

    while(1)
    {
        memset(buff,0,sizeof(buff));
        /*每次调用select之前都要重新在read_fds和exception_fds中设置文件描述符connfd，因为事件发生以后，文件描述符集合将被内核修改*/
        FD_SET(connfd,&read_fds);
        FD_SET(connfd,&exception_fds);

        ret = select(connfd+1,&read_fds,NULL,&exception_fds,NULL);
        if(ret < 0)
        {
            printf("Fail to select!\n");
            return -1;
        }
        if(FD_ISSET(connfd, &read_fds))
        {
            ret = recv(connfd,buff,sizeof(buff)-1,0);
            if(ret <= 0)
            {
                break;
            }
            printf("get %d bytes of normal data: %s \n",ret,buff);
        }
        else if(FD_ISSET(connfd,&exception_fds)) //异常事件
        {
            ret = recv(connfd,buff,sizeof(buff)-1,MSG_OOB);
            if(ret <= 0)
            {
                break;
            }
            printf("get %d bytes of exception data: %s \n",ret,buff);
        }
    }
    close(connfd);
    close(listenfd);
	return 0;
}

```
