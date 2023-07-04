>  [select()，fd\_set()，fd\_isset() - 额是无名小卒儿 - 博客园](https://www.cnblogs.com/gjmhome/p/13985714.html)

> select的结果会对fd_set造成影响。例如，对于一个监听的socket，通过select筛选处于就绪状态的fd。
>
> ```c
> #include <WinSock2.h>
> #include <stdio.h>
> #pragma comment(lib,"WS2_32.lib")   
> int main()
> {  
>     FD_SET   ReadSet;  
>     FD_ZERO(&ReadSet); 
>     WSADATA   wsaData; 
>     WSAStartup(MAKEWORD(2, 2), &wsaData);         //初始化
>     SOCKET  ListenSocket = WSASocket(AF_INET, SOCK_STREAM, IPPROTO_TCP, NULL, 0, WSA_FLAG_OVERLAPPED);  //定义一个监听套接字    
>     //bind等操作这里省略....   //.....    
>     FD_SET(ListenSocket, &ReadSet);      //将套接字加入ReadSet集合中    
>     int isset = FD_ISSET(ListenSocket, &ReadSet);         //这里并没有通过select对fd_set进行筛选   
>     printf("Before select, isset = %d\n", isset);         //所以这里打印结果为1 
>     struct timeval tTime;  
>     tTime.tv_sec = 10; 
>     tTime.tv_usec = 0; 
>     select(0, &ReadSet, NULL, NULL, &tTime);       //通过select筛选处于就绪状态的fd                                                  
>     //这时，刚才的ListenSocket并不在就绪状态(没有连接连入)，那么就从ReadSet中去除它    
>     isset = FD_ISSET(ListenSocket, &ReadSet);  
>     printf("After select, isset = %d\n", isset);     //所以这里打印的结果为0 
>     system("pause");   
>     return 0;
> }
> ```
>
> 