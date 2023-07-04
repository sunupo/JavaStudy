#include <stdio.h>
#include <string.h>
#include <stdlib.h>

int main(void){
    char  *s = (char *)"abc def";
//    char  *s = "abcd def";
//    char w[] = "abcd def";
    char *p = NULL;
//    char *h = NULL;

//    h = strtok(w, " ");
//    printf("h = %s\n",h);
        printf("before p ");
    p = strtok(s, " ");
    printf("s = %s\n",s);
    printf("p = %s\n",p);
            printf("after p ");
}
// g++ server-select.cpp -o server-select -lwsock32
//   char *__cdecl strtok(char * __restrict__ _Str,const char * __restrict__ _Delim) __MINGW_ATTRIB_DEPRECATED_SEC_WARN;
//strtok 函数第一个参数 接受 char * 类型