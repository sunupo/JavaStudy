顺 序：where → group by → having → order by → limit

英语意思：

where：哪里
group by：分组（圈子）
having：有
order by：排序（顺序）
limit：限制



[图解 SQL 执行顺序，通俗易懂！](https://mp.weixin.qq.com/s?__biz=MjM5NzEyMzg4MA==&mid=2649468111&idx=6&sn=a87a37d675039f92dfd1df76a65c8a5f&chksm=bec1ce8889b6479ef314f7ea1aa4204eb9b1d4037847bf3ef8bdffd7a5f2f96ad34095bc4996&scene=27)

其中你需要记住SELECT查询时的两个顺序：

1.关键字的顺序是不能颠倒的：

```
SELECT ... FROM ... WHERE ... GROUP BY ... HAVING ... ORDER BY ...
```

2.SELECT语句的执行顺序（在MySQL和Oracle中，SELECT执行顺序基本相同）：

```
FROM > WHERE > GROUP BY > HAVING > SELECT的字段 > DISTINCT > ORDER BY > LIMIT
```





[sql语句执行顺序 - 信智微客](https://www.xinzhiweike.com/wenda/1669391857690947)

**SQL语句执行顺序：FROM、ON 、JOIN、WHERE、GROUP BY、AGG\_FUNC、WITH、HAVING、SELECT、UNION、DISTINCT 、ORDER BY、LIMIT。**

在实际执行过程中，每个步骤都会为下一个步骤生成一个虚拟表，这个虚拟表将作为下一个执行步骤的数据。

**1、FROM**：选择FROM后面跟的表，产生虚拟表1。

**2、ON**：ON是JOIN的连接条件，符合连接条件的行会被记录在虚拟表2中。

**3、JOIN**：如果指定了LEFT JOIN，那么保留表中未匹配的行就会作为外部行添加到虚拟表2中，产生虚拟表3。如果有多个JOIN链接，会重复执行步骤1~3，直到处理完所有表。

**4、WHERE**：对虚拟表3进行WHERE条件过滤，符合条件的记录会被插入到虚拟表4中。

**5、GROUP BY**：根据GROUP BY子句中的列，对虚拟表4中的记录进行分组操作，产生虚拟表5。

**6、AGG\_FUNC**：常用的 Aggregate 函数包涵以下几种：（AVG：返回平均值）、（COUNT：返回行数）、（FIRST：返回第一个记录的值）、（LAST：返回最后一个记录的值）、（MAX： 返回最大值）、（MIN：返回最小值）、（SUM： 返回总和）。

**7、WITH** 对虚拟表5应用ROLLUP或CUBE选项，生成虚拟表 6。

**8、HAVING**：对虚拟表6进行HAVING过滤，符合条件的记录会被插入到虚拟表7中。

**9、SELECT**：SELECT到一步才执行，选择指定的列，插入到虚拟表8中。

**10、UNION**：UNION连接的两个SELECT查询语句，会重复执行步骤1~9，产生两个虚拟表9，UNION会将这些记录合并到虚拟表10中。

**11、DISTINCT** 将重复的行从虚拟表10中移除，产生虚拟表 11。DISTINCT用来删除重复行，只保留唯一的。

**12、ORDER BY**: 将虚拟表11中的记录进行排序，虚拟表12。

**13、LIMIT**：取出指定行的记录，返回结果集。