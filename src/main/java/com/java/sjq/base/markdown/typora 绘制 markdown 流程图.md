# Typora 支持, [sequence](https://bramp.github.io/js-sequence-diagrams/), [flowchart](http://flowchart.js.org/) and [mermaid](https://knsv.github.io/mermaid/#mermaid), 使用前要先从偏好设置面板启用该功能。

# [sequence](https://bramp.github.io/js-sequence-diagrams/)

```sequence

Andrew->China: Says Hello
Note right of China: China thinks\nabout it
China-->Andrew: How are you?
Andrew->>China: I am good thanks!
```

# [Flow](http://flowchart.js.org/)

```flow
st=>start: Start:>http://www.google.com[blank]
e=>end:>http://www.google.com
op1=>operation: My Operation
sub1=>subroutine: My Subroutine
cond=>condition: Yes
or No?:>http://www.google.com
io=>inputoutput: catch something...
para=>parallel: parallel tasks

st->op1->cond
cond(yes)->io->e
cond(no)->para
para(path1, bottom)->sub1(right)->op1
para(path2, top)->op1


```



```flow
st=>start: Start|past:>http://www.google.com[blank]
e=>end: End|future:>http://www.google.com
op1=>operation: My Operation|past
op2=>operation: Stuff|current
sub1=>subroutine: My Subroutine|invalid
cond=>condition: Yes
or No?|approved:>http://www.google.com
c2=>condition: Good idea|rejected
io=>inputoutput: catch something...|future

st->op1(right)->cond
cond(yes, right)->c2
cond(no)->sub1(left)->op1
c2(yes)->io->e
c2(no)->op2->e
```



# [Mermaid](https://blog.csdn.net/lis_12/article/details/80693975)



```mermaid
graph LR
  A(1.检查类是否已经加载)
  B(2.为新生对象分配内存)
  C(3.把分配的内存区域初始化零值)
  D(4.设置对象头)
  E(调用构造函数初始化对象资源)
  A-->B-->C-->D-->E

```



```mermaid
graph LR;
  A-->B
  B-->C
  C-->D
  D-->A
```



```mermaid
graph TB
  A
  B[bname]
  C(cname)
  D((dname))
  E>ename]
  F{fname}

```



```mermaid
graph TB
  A1-->B1
  A2---B2
  A3--text---B3
  A4--text-->B4
  A5-.-B5
  A6-.->B6
  A7-.text.-B7
  A8-.text.->B8
  A9===B9
  A10==>B10
  A11==text===B11
  A12==text==>B12
```

