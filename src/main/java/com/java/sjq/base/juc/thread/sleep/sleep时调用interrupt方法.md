[java在sleep时调用interrupt方法](https://blog.csdn.net/tomorrow_fine/article/details/73834532)
* 情况1：先睡眠后打断，则直接打断睡眠，并且清除停止状态值，使之变成false：
* 情况2：先打断后睡眠，则直接不睡眠：