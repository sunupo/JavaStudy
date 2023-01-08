[【Java】释放锁和不会释放锁情况总结](https://blog.csdn.net/qq_40839718/article/details/124393748#:~:text=2.%E4%B8%8B%E9%9D%A2%E6%93%8D%E4%BD%9C%E4%B8%8D%E4%BC%9A%E9%87%8A%E6%94%BE%E9%94%81%EF%BC%9A%20%281%29%20%E7%BA%BF%E7%A8%8B%E6%89%A7%E8%A1%8C%E5%90%8C%E6%AD%A5%E4%BB%A3%E7%A0%81%E5%9D%97%E6%88%96%E5%90%8C%E6%AD%A5%E6%96%B9%E6%B3%95%E6%97%B6%EF%BC%8C%E7%A8%8B%E5%BA%8F%E8%B0%83%E7%94%A8Thread.sleep%20%28%29%E3%80%81Thread.yield,%28%29%E6%96%B9%E6%B3%95%E6%9A%82%E5%81%9C%E5%BD%93%E5%89%8D%E7%BA%BF%E7%A8%8B%E7%9A%84%E6%89%A7%E8%A1%8C%EF%BC%8C%E4%B8%8D%E4%BC%9A%E9%87%8A%E6%94%BE%E9%94%81%EF%BC%9B%20%282%29%20%E7%BA%BF%E7%A8%8B%E6%89%A7%E8%A1%8C%E5%90%8C%E6%AD%A5%E4%BB%A3%E7%A0%81%E5%9D%97%E6%97%B6%EF%BC%8C%E5%85%B6%E4%BB%96%E7%BA%BF%E7%A8%8B%E8%B0%83%E7%94%A8%E4%BA%86%E8%AF%A5%E7%BA%BF%E7%A8%8B%E7%9A%84suspend%20%28%29%E6%96%B9%E6%B3%95%E5%B0%86%E8%AF%A5%E7%BA%BF%E7%A8%8B%E6%8C%82%E8%B5%B7%EF%BC%8C%E8%AF%A5%E7%BA%BF%E7%A8%8B%E4%B8%8D%E4%BC%9A%E9%87%8A%E6%94%BE%E9%94%81%E3%80%82)

# 释放锁
## 1.下面操作会释放锁：
1) 当前线程的同步方法、同步代码块执行结束；
2) 当前线程在同步代码块、同步方法中遇到break、return；
3) 当前线程在同步代码块、同步方法中出现未处理的Error或Exception，导致异常结束；
4) 当前线程在同步代码块、同步方法中执行了线程对象的`wait`( )方法，当前线程暂停并释放锁；

## 2.下面操作不会释放锁：
1) 线程执行同步代码块或同步方法时，程序调用Thread.`sleep`( )、Thread.`yield`( )方法暂停当前线程的执行，不会释放锁；
2) 线程执行同步代码块时，其他线程调用了该线程的`suspend`( )方法将该线程挂起，该线程不会释放锁。
