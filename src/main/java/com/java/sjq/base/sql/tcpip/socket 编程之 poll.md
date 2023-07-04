### poll

```c
int poll(struct pollfd *fds, unsigned int nfds, int timeout);

```

- 先来说**nfds**，这个是指当前需要关心的文件描述符的个数；

- **timeout**同样是设置超时时间，只是和select的timeout是一个结构体不一样，这里只是一个整型类型，且含义是**毫秒**；

- 而**fds**是一个结构体指针，定义为一个数组，数组的长度可作为 nfds 的值

### pollfd

poll 中的描述符是 pollfd 类型的数组，pollfd 的定义如下：

```c
struct pollfd {
               int   fd;         /* file descriptor */
               short events;     /* requested events */
               short revents;    /* returned events */
           };
```



**fd**表示所要关心的文件描述符；

**events**表示该文件描述符所关心的事件，这是一个**输入型参数**，要告诉操作系统这个文件描述符对应的事件所关心的操作事件是什么，比如读或写；

**revents**是一个**输出型参数**，表示当poll返回时告诉用户什么操作事件是就绪的，比如如果POLLIN是就绪的，那么返回时revent的值就是POLLIN，告诉用户fd事件的POLLIN是就绪的；

### demo

```c
// The structure for two events
struct pollfd fds[2];

// Monitor sock1 for input
fds[0].fd = sock1;
fds[0].events = POLLIN;

// Monitor sock2 for output
fds[1].fd = sock2;
fds[1].events = POLLOUT;

// Wait 10 seconds
int ret = poll( &fds, 2, 10000 );
// Check if poll actually succeed
if ( ret == -1 )
    // report error and abort
else if ( ret == 0 )
    // timeout; no event detected
else
{
    // If we detect the event, zero it out so we can reuse the structure
    if ( fds[0].revents & POLLIN )
        fds[0].revents = 0;
        // input event on sock1

    if ( fds[1].revents & POLLOUT )
        fds[1].revents = 0;
        // output event on sock2
}
```

