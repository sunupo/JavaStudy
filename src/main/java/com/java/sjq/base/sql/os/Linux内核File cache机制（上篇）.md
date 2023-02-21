**一、什么是File cache？**

**1.  File cache概述**

Linux File cache机制，每次动笔想写到该知识点的时候，我心里总会犹豫迟疑，众所周知内存管理是Linux系统的比较难啃的子系统之一，而内核文件缓存机制是内存管理框架中难度较大的知识点。其中包括文件缓存预读取流程、写流程、回收流程等，希望我们这次将其一探究竟。

讨论Linux File cache前，先看下什么是Linux cache机制呢？

我们在使用Linux系统的时候，经常会发现系统的空闲内存（后文以memfree代替）经常处于一个较低的状态，有时8G的手机刚开机memfree就低于2G，而此时可能并无启动多少应用。仔细查看发现，此时系统的cached可能达到3G以上【图1：meminfo@1】，这时很多用户会有疑问：cached是什么？是内存泄露吗？显然不是。cached表示系统的缓存内存大小，当用户需要读取文件中的数据时，操作系统会先分配内存，然后将数据从存储器读入到内存中，最后将内存中的数据分发给用户；当用户需要往文件中写数据时，操作系统会先分配内存接收用户的数据，然后再将数据从内存写到磁盘中。而Linux cache机制就是对这些由操作系统内核分配，并用来存储文件数据的内存进行管理。

那么可能有人会问：Cache机制为什么会缓存这么大？是否会被回收？

如果系统内存充足，缓存在内存中的文件数据是可以在内存中长时间驻留的，如果有其他的进程访问这部分的数据，就不需要访问磁盘，我们知道内存访问速度比磁盘访问速度要快，该机制可以避免用户因为磁盘访问导致的长时间等待。所以在内存充足的情况下，系统的cache大小是会越来越大的；当系统的内存不足，Linux内存回收机制就会把cache的内存进行回收，以缓解内存压力。

在Linux内核中，cache主要主要包括：（对应【图1：meminfo@2】）

-   普通文件数据的页面；
    
-   目录等信息的页面；
    
-   直接从块设备文件读出来的数据页；
    
-   swapcache；
    
-   特殊文件系统的页面，例如shm文件系统；
    

cache中大部分是文件缓存，即本文讨论的File cache，其包含活跃和非活跃的部分，对应如下：Active(file)和Inactive(file)【图1：meminfo@3@4】。

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9kNGhvWUpseE9qUDNYUVlmVzdQczNNMVFoVFNnSEQ3bmlhQlpFWHBSbUJ6TUI5R1JyOGswSDI3b3FpYlA4TU5uWlRBOU9kSnk5cTlkM1pmMGljMW9EMDhqdy82NDA?x-oss-process=image/format,png)

【图1：meminfo】

**2.  File cache机制框架**

（1）系统层面下的File cache机制

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9kNGhvWUpseE9qUDNYUVlmVzdQczNNMVFoVFNnSEQ3bjJCZUMyNzIxWVRpYUZRaWJNcUVYY0UwME1pYzUwVUg2RWVMbHlMc0RxTzY2a3RXbjIxQmdnaWJIckEvNjQw?x-oss-process=image/format,png)

【图2：Linux I/O操作流程图】

当用户发起一个读或者写文件请求时，流程如【图2】，整体的流程如下：

-   系统调用read()会调用到VFS（Virtual file system，后文以VFS简称）相应的函数，传递参数有文件描述符合文件偏移量等；
    
-   接着缓存机制（[Buffer](https://so.csdn.net/so/search?q=Buffer&spm=1001.2101.3001.7020) page Cache）确定请求的数据是否已经在内存缓冲区；如果在缓存区并且数据是最新的，那就不用发起IO操作，返回数据给用户。
    
-   若数据不在内存中，确定如何执行读操作，通过具体的文件系统（Disk System）和通用块设备层（Generic Block layer），确定数据在物理设备中的位置，构建IO请求；
    
-   在通用块设备之下是IO调度层（IO Scheduler Layer），根据内核的调度策略，对等待的IO等待队列进行排序；
    
-   最后，块设备驱动（Block Device Driver）通过向磁盘控制器发送相应的命令，执行真正的数据传输；
    

VFS用于与系统调用read/write等接口进行交互，通过VFS后可以通过DIRECT\_IO直接与具体的文件系统进行交互，如果没有DIRECT\_IO，则会通过cache机制与具体的文件系统交互。具体的文件系统例如ext3/ext4等，通过Generic block layer和IO schedule layer与具体的块设备驱动交互。

所以理论上cached的机制的设计逻辑在于具体的具体文件系统之上，VFS之下，即上图中“Buffer page Cache”部分。

（2）File cache机制内部框架梳理

File cache机制，从内部框架简单分为两部分：File cache的产生和回收。学习文件缓存按照下面的框架进行由浅至深进行分析，更加容易抓住设计的逻辑。

-   File cache产生流程
    
-   读文件：读文件流程以及预读机制，包括read和mmap发起的文件读取流程；
    
-   写文件：写文件流程；
    
-   File cahce回收流程
    

以下的分析基于Linux-4.19，并且基于不讨论DIRECT\_IO模式。

**二、read发起读文件流程分析**

**1\. read函数生命周期**

用户读取的文件，可以有不同的实现方法，但是普遍是通过read系统和mmap接口进行读取，该章节介绍read的读取流程分析：

ssize\_t read(int fd, void \*buf, size\_t count);

用户调用该接口会调用内核的sys\_read接口，并最终会通过VFS调用到具体文件系统的读文件接口，并经过内核“六个阶段”，最终调用块设备驱动程序的接口，通过向磁盘控制器发送相应的命令，执行真正的数据传输；

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X2pwZy9kNGhvWUpseE9qUDNYUVlmVzdQczNNMVFoVFNnSEQ3blhRaWNRV0pESkV2WHFaUFhSNm5tT1ZtbnN5RU1Cc3Axb1J1Tkw1VHhoRUdnVEUxd1dVeTRVVncvNjQw?x-oss-process=image/format,png)

【图3：Linux read函数生命周期图】

从图3，我们可以看出filecache的设计逻辑集中“file cache”方框，这部分在具体文件系统之上，在VFS之下。

从图3我们也可以看出具体的文件系统（EXT2/3/4等）负责在文件cache和存储设备之间交换数据，而VFS负责应用程序和文件cache之间通过read()/write()等接口交换数据。

**2. 预读机制**

从图3中，当文件缓存读取的过程主要是通过page\_cache\_sync\_readhead()和page\_cache\_async\_readahead()两部分，从函数的名字可以看出两个函数的作用分别是“同步预读”和“异步预读”。但是从代码的逻辑看，其实page\_cache\_sync\_readhead()这个名字取得并不准确，因为同步预读的语义应该是进程同步等待直至读取文件内容成功，但是后面分析我们会发现这两个函数仅仅做到事情如下，并非真正的等到文件内容读取完成。

-   将文件读取需求提交给具体文件系统，后者做成bio提交给Generic block layer；
    
-   将分配的文件缓存加入到搜索树和zone lruvec中；
    

说回预读（read ahead）机制，就是在数据真正访问之前，从普通文件或者块设备文件批量的读取更多的连续文件页面到内存中。内核为了提供IO性能，当用户要求读取文件页面时，会通过预读算法计算是否将相邻的文件页面提前从磁盘读入到内存中。

（1）预读机制的优势和风险

预读机制有两个优势：

-   减少磁盘控制器处理的命令数，每次IO请求读取多个扇区，提升系统的IO吞吐量，提高磁盘的性能；  
    
-   提前预判用户即将访问的文件，减少用户读取文件同步等待的时间，提高用户的执行速度；
    

当然预读也是存在风险，特别是随机读的时候，预读对系统是有害的，因为对于随机读取这种场景，预读的文件页面被用户访问的概率偏低。如果被提前预读的文件页面没有被用户访问，该场景会浪费系统的物理内存，并且会造成阶段性的IO负载。

（2）Linux内核预读规则

为了规避上面提到的预读风险，Linux内核对预读的机制秉承着的规则：（这里不讨论用户调用madvise等系统调用指定特定区域即将被访问的场景，读者可以自行分析源码）

-   只对顺序读进行预读，随机读不进行预读。
    
-   如果进程不断发起顺序读操作，那么接下来单次预读的量会不断增加，直至达到设置的单次最大预读值；
    
-   如果文件的所有的页面都已经在缓存在内存中了，那么停止预读；
    

（3）Linux内核预读机制设计模式

内核对预读的设计是通过“窗口”来实现的，有两个窗口：当前窗口和前进窗口。

-   当前窗口：已经读取的文件页面，包括用户要求的页面以及提前预读在缓存中的页面；
    
-   前进窗口：即将为用户提前预读的页面；
    

简单理解窗口表示一些页面的集合，例如图4，当用户要求读取1个文件页面时，其实系统总共预读4个页面，当前窗口表示用户要求读取的页面，前进窗口表示提前预读的3个页面，不管这三个文件页面是否完整读入到内存中了。

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9kNGhvWUpseE9qUDNYUVlmVzdQczNNMVFoVFNnSEQ3bmY3anBaNWV0d01sUzFpYTlNUTE3NWliVnRJTGtpYWliencwT1VuWVJ5bnFLRVNJNGlha2pSVTlTS013LzY0MA?x-oss-process=image/format,png)

【图4：预读窗口解析1】

如果经过一段时间，前进窗口已经成功提前将文件页面读入到内存中，并且用户命中了预读的文件页面，则此时图4中的前进窗口会转变为当前窗口，并重新构建前进窗口，如图5：

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9kNGhvWUpseE9qUDNYUVlmVzdQczNNMVFoVFNnSEQ3bklEamo2aFMyNDczaWN1N01mcHMxdUQyTFJLNHI3QjNlcHJuSHdPRklqNGlhSHNpYXB2cW15eU5pY1EvNjQw?x-oss-process=image/format,png)

【图5：预读窗口解析2】

所以如果说预读不断命中，前进窗口是不断转换为当前窗口的，当然如果预读没有命中，例如随机读场景，那么预读机制就会被关闭，此时就不存在前进窗口了，但是当前窗口总是存在的。最理想的状态，就是当前窗口的页面被用户访问完后，前进窗口的页面也预读完成，并且接下来被用户读取命中，此时前进窗口转为当前窗口，并且重新构建新的前进窗口进行预读。

“窗口”的概念，Linux通过“struct file\_ra\_state”进行抽象，定义在include/linux/fs.h：

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9kNGhvWUpseE9qUDNYUVlmVzdQczNNMVFoVFNnSEQ3bkZGNVhiSTdZbWtjb1dhSXFIWFBPN1B0UXJpYUZNSnhsUXpzZVFuNjRpYlRoSHNIb1ZodWpBbFdnLzY0MA?x-oss-process=image/format,png)

这里需要注意的“窗口”的概念是针对文件个体设定的，即不同文件对应着各自的窗口实体，所以如果连续打开不同的文件，不同文件之间的预读大小是不会互相影响的。系统打开的每个文件，都有一个file\_ra\_state实例：

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9kNGhvWUpseE9qUDNYUVlmVzdQczNNMVFoVFNnSEQ3bmp0U2JKTHNySVIxVE9ucEpaV3FCS0RqVmpxaGhHSDRZMVZsbWFtUzZBV2dtRmJjMDIzWXc0QS82NDA?x-oss-process=image/format,png)

每个文件被打开时会对该文件的file\_ra\_state结构体进行初始化，默认状态file\_ra\_state的成员状态如下：

-   start = size = async = mmap\_miss = 0;
    
-   prev\_pos = -1;
    
-   ra\_pages = 单次最大的预读页面数；
    

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9kNGhvWUpseE9qUDNYUVlmVzdQczNNMVFoVFNnSEQ3bkRtRW5iWDdGaWFpY3p3N1c3Z1VqeXU2eWliSFVGQTNPNEROeDBqazBpYnF0dFJCYVpSaWN5YnJaV0F3LzY0MA?x-oss-process=image/format,png)

**3. generic\_file\_buffered\_read**

明白了总体框架后，那就跟随者那句经典的“read the f\*\*\*ing source code”，来看看代码generic\_file\_buffered\_read流程，该函数定义在mm/filemap.c：

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9kNGhvWUpseE9qTjJWS3k5RGFxMTZpYTdOUG9pYnF6MFZUUUlsUFpXUDl3UXYxaWFEM1FPUnpzTUp3N0lVdVc1UE0yUnV1MDNxdkZ4QldpYlp3dHA0dzVpYUVRLzY0MA?x-oss-process=image/format,png)

@0：函数一开始，首先拿到该文件的窗口实例，如果是第一次读取那么该实体就是初始状态，如果非第一次读取页面，该实体就是上一次预读的状态，本次读取会根据上次的预读状态和本次是否命中调整窗口实例。另外这里有两个局部变量需要关注一下：  

-   index：表示用户要求读取的第一个页面在文件中的页面序号，如果是文件头那么该值为0.
    
-   last\_index：表示用户要求读取的最后一个页面在文件中的页面序号；
    

last\_index减去index表示用户要求读取的页面数目。

该函数会执行同步预读和异步预读两个部分，这里分开分析：

（1）同步预读

@1调用find\_get\_page根据mapping和index查看一个文件页面是否在缓存中了，其实就是在文件缓存树中进行查找。此时有两种结果：在文件缓存树中找到页面或者找不到页面。

-   在文件缓存找不到page的情况：第一次读取肯定会走到这里。
    

@2调用page\_cache\_sync\_readahead()函数进行同步预读，对于该函数需要注意的两点：

-   仅仅是分配好页面，调用具体文件系统的接口封装好bio请求发给Gerneric block layer，也就说该函数并没法保证能把文件数据读取到文件中；
    
-   该函数分配内存gfp\_mask调用的是\_\_GFP\_NORETRY | \_\_GFP\_NOWARN，所以该函数在内存紧张时是很可能分配不到内存的，所以才有@3的第二次判断；
    

@3重新判断是否分配好页面并加入到缓存树中，这里有两个结果：在文件缓存中找到页面或者找不到页面；

-   找的到页面：
    

@5通过PageUptodate()判断页面是否读取到最新数据，如果不是最新的数据没有读取完成，就会调用wait\_on\_page\_locked\_killable()->io\_schedule()进行等待，这就是systrace 中read进程Block IO的原因。这里可能有人会问题PG\_uptodate和PG\_locked是在哪里设置的？

当分配内存并将页面插入到缓存树以及zone lruvec中前会通过add\_to\_page\_cache\_lru()->\_\_SetPageLocked()设置页面的PG\_locked，而PG\_uptodate内存申请时默认不设置。当发起IO请求，并且IO操作完成时会及时将页面的PG\_locked清除，并设置PG\_uptodate。

所以wait\_on\_page\_locked\_killable(page)函数此刻就能起到同步等待的数据读取完成的作用，而并非是在page\_cache\_sync\_readahead()同步等待，该函数命名比较迷惑。

@6表示一个页面已经更新完数据了，此时会做几件事：

1） 将读取的页面发送拷贝给用户；

2）记录当前读取的数据对应的页面序号到在prev\_index中；以便@9更新到窗口中，用于下一次页面读取判断用户是否是顺序读；

3）然后更新index，记录要读取的下一个文件页面序号；

4）通过iov\_iter\_count判断是否已经读取完成，完成则执行@9更新当前窗口的状态，并退出；否则根据index，读取下一个页面；

-   找不到的页面：
    

@3找不到页面的情况，即此时可能是因为不支持预读或者页面分配没有成功等原因，此时就需要改变内存分配的标志，并且等到该文件更新完数据。

@7通过page\_cache\_alloc分配页面，分配标志没有\_\_GFP\_NORETRY | \_\_GFP\_NOWARN，表示内存紧张会进入慢速路径，分配成功后将页面插入到缓存树和zone lruvec中。

@8调用文件系统的readpage进行文件数据读取，并同步等待读取完成；读取完成后就执行@6进行下一个页面读取或者退出本次读取过程；

-   在文件缓存找得到page的情况
    

如果一开始就在缓存树中找到了页面，那么就直接执行@5的流程执行，等待页面的数据读取完成，后续流程跟上面一致。

所以同步预读核心因素是page\_cache\_async\_readahead函数，定义在mm/readahead.c中，该函数仅仅是ondemand\_readahead的封装，该函数在“4.ondemand\_readahead”分析。

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9kNGhvWUpseE9qTjJWS3k5RGFxMTZpYTdOUG9pYnF6MFZUQUMwZ1dVRmVUdUVoUzA1a2JHa2Naa2RTTzNvMGVpYUNRVzNjaWEyMjg4aktaU1hJaWEyM1JvS1lBLzY0MA?x-oss-process=image/format,png)

（2）异步预读

异步预读的处理集中在@4，先通过PageReadahead(page)判断页面的是否设置了PG\_readahead，如果该页面设置该标志，表示本地当前窗口读取的文件页面命中了上一个前进窗口预读的页面，此时就要通过异步预读操作发起一个新预读。

关于PG\_readahead是在预读时标记的，规则如图6，当用户要求读入一个文件页面，系统预读的其后连续的3个文件页面，那么第一个预读的页面就会被标记PG\_readahead；

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9kNGhvWUpseE9qTjJWS3k5RGFxMTZpYTdOUG9pYnF6MFZURVVaYnlMa25jcFBDcEFTV3NxbmNtQk1EcXFHbkswSGZsQmljT2ZBOVFWM0RqNmFEYmlhRjk0N1EvNjQw?x-oss-process=image/format,png)

【图6：PG\_readahead设置规则】

page\_cache\_async\_readahead()函数的参数和同步预读一样，只多一个struct page结构体，作用是将该page的PG\_readahead的标志清空，接着也是调用ondemand\_readahead()函数。我们发现generic\_file\_buffered\_read()发起的同步预读和异步预读最终都是调用ondemand\_readahead()函数，区别是第四个传参hit\_readahead\_marker为true或false。

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9kNGhvWUpseE9qTjJWS3k5RGFxMTZpYTdOUG9pYnF6MFZUQWljangyZ09pYk16UFdYQjZmWEZzOEd4Wmc5ajlpYUJ2MHB3MEJzMkpqVWJDOWxOQno5U1VYNzFRLzY0MA?x-oss-process=image/format,png)

**4. ondemand\_readahead**

ondemand\_readahead()函数定义在mm/readahead.c中，总共6个参数，这里主要关注4个参数：

-   ra：窗口的抽象结构体；
    
-   hit\_readahead\_marker：区分是sync readahead还是async readahead；
    
-   offset：表示读取的第一个页面在文件中的页面序号，个人觉得命名为index比较符合语义；
    
-   req\_size：表示用户请求读取的页面数；
    

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X2pwZy9kNGhvWUpseE9qTjJWS3k5RGFxMTZpYTdOUG9pYnF6MFZURXFibEluSW53UkZXVWg1aWFGS1B3dTA5R0pqaWFITmVwdzBLaWFUQlVIUkRvVzF4aWNDOVAwWDF1dy82NDA?x-oss-process=image/format,png)  

函数一开始先获取该窗口单次预读最大的页面数。该函数分为两种场景：从文件头开始读取或者非文件头读取。

@1：判断当前读取是否从文件头开始读？offset表示读取的第一个页面在文件中的页面序号，为0表示为从文件头读取。

（1）从文件头开始读

从文件头部开始读，代码流程如下：

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9kNGhvWUpseE9qTjJWS3k5RGFxMTZpYTdOUG9pYnF6MFZUeUVRUGhYeVdDVVFwOG9xUG9OU1NpYXlDNDEydlQ4TGdrQzZUclRUbmVWODB2bmVVUkRad2RlZy82NDA?x-oss-process=image/format,png)

@1：如果该页面是文件中的第一页面，即从头开始读，那么就判断为顺序读，开始初始化当前的窗口；

@2：先把读取的第一个页面序号赋值给ra->start；然后调用get\_init\_ra\_size()函数根据用户要求读取的页面数和单次最大允许的预读页面数，得到本次窗口的预读页面数；

如果本次读取的页面数大于用户请求读取的页面数，则将多预读的页面数记录到ra->async\_size，这部分页面表示异步读取；

get\_init\_ra\_size()函数定义在mm/readahead.c 中，该函数的参数：

-   size：表示用户要求读取的页面数目；
    
-   max：表示单次预读最多可以读取的页面数目；
    

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9kNGhvWUpseE9qTjJWS3k5RGFxMTZpYTdOUG9pYnF6MFZUeFREN1d5V0RDUlZHZVpwQUF1cHkxQXNpYjVXOFZDRXlFbEV2em5ialZPWmw4ME5aVDEzUWpWUS82NDA?x-oss-process=image/format,png)

对于内核这种固定数值，又没给出注释的方式的公式，个人觉得不是很“优雅”。

这套计算公式分别用最大预读数128Kbytes和512Kbytes，推导结果如下：（req\_size表示用户要求读取的页面数，new\_size表示实际预读的页面数）。从这个结果可以得出，设置单次最大的预读页面数目，影响不仅仅是最大的预读页面数，对预读的每个环节都有影响。

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9kNGhvWUpseE9qTjJWS3k5RGFxMTZpYTdOUG9pYnF6MFZUckFvUkk4cWZIaWNkV1A5TkhUaWFRdGlhSzJiNXNhNmlhQTFXSFFPNG5xdkllMW9MZkhJWG5ONHVHZy82NDA?x-oss-process=image/format,png)

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9kNGhvWUpseE9qTjJWS3k5RGFxMTZpYTdOUG9pYnF6MFZUdjRnYnFTWDVKVERhTnlQNVppYmJoWWVzbHhuNGNVbWRabGlhbUxxRHVEZWlhbmVLNnQ2bVdEcUNBLzY0MA?x-oss-process=image/format,png)

@3：调用ra\_submit()发起读页面请求，该函数定义在mm/internal.h，是对\_\_do\_page\_cache\_readahead的封装，传入本次预读的起始页面序号，预读页面数，异步预读页面数。

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9kNGhvWUpseE9qTjJWS3k5RGFxMTZpYTdOUG9pYnF6MFZUOFZMR2lhNTllSk0zaWFSOGplNXo0bE1ob0V3N2NIcTRwVGxwdExOaWF5S1cxZHEzeTh6NjBReFJBLzY0MA?x-oss-process=image/format,png)

\_\_do\_page\_cache\_readahead()函数，比较关键的三个参数：

-   offset：表示本地预读的第一个页面在文件中的序号，个人觉得叫做index比较妥当；
    
-   nr\_to\_read：发起的预读大小；
    
-   lookahead\_size：异步预读的大小；
    

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9kNGhvWUpseE9qTjJWS3k5RGFxMTZpYTdOUG9pYnF6MFZUMkM4Q3JMZGY2S2JsMnNTU2F1b3J2Z1hDSU54QXhxenJkSUMyajJaQ1BnRDIzamljYjhLRWoyUS82NDA?x-oss-process=image/format,png)

\_\_do\_page\_cache\_readahead()逻辑是比较简单的，这里不做过多阐述，这里需要注意：

@1：该函数分配的页面是带\_\_GFP\_NORETRY的，也就是内存紧张时不会进入分配慢速路径。

@2：如果一个文件所有数据读取完成，必须停止剩下的预读；

@3：对第一个异步预读的页面标志PG\_readahead，对应【图4：PG\_readahead设置规则】。

@4：调用具体文件系统的readpages接口发起IO流程，并将page加入到缓存树和zone lru（请查阅文件缓存回收流程解析章节）；

（2）非文件头读取

回到ondemand\_readahead函数，如果是非文件头读取文件页面，有几种可能 ：

@4：顺序读情况处理：如果请求的第一个页面序号与上次预读的最后一个页面时相邻的（page(hit2)），或者刚好是上次第一个异步预读的页面(page(hit1))，则表示此时读取是顺序读，增加预读页面数进行预读。假设用户上次要求读取一个页面，加上预读总共读取了4个页面，如果此次我们读取到page(hit1)或者page(hit2)，则表示顺序读，此时直接增加预读数，最后走到@3通过ra\_submit发起预读就完成了。

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9kNGhvWUpseE9qTjJWS3k5RGFxMTZpYTdOUG9pYnF6MFZUZnQ5Rkd6V1JSTWJ3M0xQUll4aWFKTjhxODM3TEZraWM2aWF0Z3F4UjR4WVlyNXdreUZiaWFaQWJaUS82NDA?x-oss-process=image/format,png)

【图7：顺序读】

更新到ra->start到page(hit1)或者page(hit2)的序号，然后通过get\_next\_ra\_size()获取下一次的预读的大小：

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9kNGhvWUpseE9qTjJWS3k5RGFxMTZpYTdOUG9pYnF6MFZUU2pSTDNodnZUendXaExEdnVVVjR1S2NNWTJrR0JpYzZ4S0NRSXdjOXpxNHM0OXdHRVFXWjdmZy82NDA?x-oss-process=image/format,png)

根据上面的规则，大多数情况都是上次预读页面数目的两倍。我们看下最大预读数分配为128Kbytes和512Kbytes的情况下，用户需要命中多少轮才能达到最大的预读页面数。

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9kNGhvWUpseE9qTjJWS3k5RGFxMTZpYTdOUG9pYnF6MFZUM3MzWlVQMjZieFd6d1JjMklxejFyVmxpYWJsbXNuRld5R1Z0RkxKc0NiaFhzZjVKZ1BpYUVMaWNRLzY0MA?x-oss-process=image/format,png)

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9kNGhvWUpseE9qTjJWS3k5RGFxMTZpYTdOUG9pYnF6MFZURnVtaWFzaGZ4MEljS05IMXJPaWFyaWFaaWNVV3ZwVmhMQmVjWHkwdzQ3a3NCSlpZNUg5Qk44a3RpYlEvNjQw?x-oss-process=image/format,png)

@5：异步预读命中处理：如果是page\_cache\_async\_readahead()函数调用进来，hit\_readahead\_marker为true，这种情况已经确认命中PG\_readahead的页面，所以肯定增大预读页面数，再次发起预读。首先查找\[index+1, max\_pages\]这个文件区间内第一个没有在缓存树中的页面，以此页面为新的起点，增加好预读数，并构建好前进窗口，最后跳到@5:ra\_submit发起预读请求；

@6：该场景有两种，其一是此时读取的页面和上一次访问的页面相同；其二是如果用户要求读入多个页面，如果预读来不及处理多个页面，那么就会出现多个页面连续进来预读的情况，如图8读取到page\*。这两种情况需要重新初始化预读状态，并将第一个读取页面序号指向当前读取页面；

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9kNGhvWUpseE9qTjJWS3k5RGFxMTZpYTdOUG9pYnF6MFZUZDNlNG5VM05Ya3lWanhtbDJ3VFNZMXUwQm5XakFwelRvU1NSODlhZGRrRjBKeWZhb0FBZ3JBLzY0MA?x-oss-process=image/format,png)

【图8】

@7的场景是通过预读历史判断是否继续预读；

@8随机读场景：表示系统判断此时页面读取是随机读，这种场景会关闭预读，\_\_do\_page\_cache\_readahead()的nr\_to\_read参数传入req\_size，表示只读取用户要求的文件页面。

**三、File cache写流程分析**

**1\. write函数生命周期**

用户写文件没有像读文件类似的预读模式，所以整个过程是比较简单的，以下不考虑Direct\_IO的模式：

ssize\_t write(int fd, void \*buf, size\_t count);

内核调用的流程如下，其中ext4\_write\_begin会判断需要写的页面是否在内存中，如果不在会分配内存并通过add\_to\_page\_cache\_lru()将页面插入到缓存树和zone lru中;ext4\_write\_end会发起IO操作。由于篇幅的原因，本文就不再贴出具体的分析过程，如果有兴趣可以跟着源码细读。

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X2pwZy9kNGhvWUpseE9qTjJWS3k5RGFxMTZpYTdOUG9pYnF6MFZUSUdjemxQMzdOeVdxT0NMQ0c0MnUxR2JSdzZIZDFDYk5uTlFEUHhWdnJ2cXhNaWF5cFIwUGRBdy82NDA?x-oss-process=image/format,png)

【图9：Linux write函数生命周期图】

**四、mmap发起读文件流程分析**

Linux mmap读文件流程涉及缺页中断和部分虚拟内存管理，此部分内容将放置在《Linux内核File cache机制（中篇）》中，后续发布，有兴趣的可以关注。

**五、File cache回收流程分析**

Linux File cache的回收涉及的知识点很多，包括内存管理LRU机制、workingset机制、内存回收shrink机制和脏页管理机制等，此部分内容将放置在《Linux内核File cache机制（下篇）》中，后续发布，有兴趣的可以关注。

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X2dpZi9kNGhvWUpseE9qTTlXV0JzVnNVcGlhR21BUGlhVEFKSXNNOFlNVEVySnJReTl2aWNoT3p1aEIyQk5TZEtyS3lRMGVPQzBsWVJWclBNTFVKT3ZFT0d0bzRNZy82NDA?x-oss-process=image/format,png)

**长按关注**

**内核工匠微信**

Linux 内核黑科技 | 技术文章 | 精选教程