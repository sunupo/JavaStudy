#include <iostream>
#include <winsock2.h>
#include <windows.h>

#pragma comment(lib, "ws2_32.lib")

using namespace std;

int main() {
    WSADATA wsaData;
    WSAStartup(MAKEWORD(2, 2), &wsaData);

    SOCKET serverSocket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);

    sockaddr_in serverAddr;
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons(12345);
    serverAddr.sin_addr.s_addr = htonl(INADDR_ANY);

    bind(serverSocket, (SOCKADDR*)&serverAddr, sizeof(serverAddr));

    listen(serverSocket, SOMAXCONN);

    fd_set readSet;
    FD_ZERO(&readSet);
    FD_SET(serverSocket, &readSet);

    while (true) {
        cout << "hello select" << endl;
        fd_set tmpSet = readSet;
        int ret = select(0, &tmpSet, nullptr, nullptr, nullptr);
        if (ret < 0) {
            cout << "select error" << endl;
            break;
        }
        for (int i = 0; i < tmpSet.fd_count; i++) {
            if (tmpSet.fd_array[i] == serverSocket) {
                sockaddr_in clientAddr;
                int clientAddrLen = sizeof(clientAddr);
                SOCKET clientSocket = accept(serverSocket, (SOCKADDR*)&clientAddr, &clientAddrLen);
                FD_SET(clientSocket, &readSet);
                cout << "new client connected" << endl;
            }
            else {
                char recvBuf[1024];
                int recvLen = recv(tmpSet.fd_array[i], recvBuf, sizeof(recvBuf), 0);
                if (recvLen <= 0) {
                    closesocket(tmpSet.fd_array[i]);
                    FD_CLR(tmpSet.fd_array[i], &readSet);
                    cout << "client disconnected" << endl;
                }
                else {
                    cout << "received message: " << recvBuf << endl;
                }
            }
        }
    }

    closesocket(serverSocket);
    WSACleanup();

    return 0;
}
