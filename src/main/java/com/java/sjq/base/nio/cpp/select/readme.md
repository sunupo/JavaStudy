
这是一个使用Winsock2库实现的TCP服务器程序。要运行此程序，请按照以下步骤操作：

1. 在Windows操作系统上打开命令提示符或PowerShell。
2. 使用cd命令导航到程序所在的目录。
3. 输入以下命令来编译程序：
 ```shell
 g++ server-select.cpp -o server-select -lwsock32
 ```
4. 这些命令将编译并运行TCP服务器程序
```shell
./server-select.exe
```

在运行程序后，它将等待客户端连接。要继续与服务器交互，请打开另一个命令提示符或PowerShell窗口，并使用telnet命令连接到服务器的IP地址和端口号。例如，如果服务器运行在本地计算机上的端口12345上，则可以使用以下命令连接到服务器：
```shell
telnet localhost 12345
```

To install the Telnet Client feature, follow these steps:

    1. Open the Start menu and search for "Control Panel".
    2. Open the Control Panel and select "Programs and Features".
    3. Click "Turn Windows features on or off".
    4. Scroll down and locate "Telnet Client".
    5. Check the box next to "Telnet Client" and click "OK".
    6. Wait for the feature to install, then try running the "telnet" command again.



