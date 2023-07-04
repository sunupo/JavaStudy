
[(122条消息) 索引合并机制详解\_斗者\_2013的博客-CSDN博客](https://blog.csdn.net/w1014074794/article/details/124141550)

## 前言

一般情况下，针对单表的一个简单where查询只会使用一个索引，但是这样的话，针对单表中多个字段建立的普通索引就没有了意义。  
那么，一个简单查询中如何使用多个索引呢？  
这就要提到Mysql中的`索引合并机制`了。

___

## 一、什么是索引合并机制？

MySQL5.0之前，一个表一次只能使用一个索引，无法同时使用多个索引分别进行条件扫描。但是从5.1开始，引入了 `index merge` 优化技术，对同一个表可以使用多个索引分别进行条件扫描，然后将结果进行合并处理，然后在进行[回表查询](https://so.csdn.net/so/search?q=%E5%9B%9E%E8%A1%A8%E6%9F%A5%E8%AF%A2&spm=1001.2101.3001.7020)。

官网链接：[https://dev.mysql.com/doc/refman/8.0/en/index-merge-optimization.html](https://dev.mysql.com/doc/refman/8.0/en/index-merge-optimization.html)

**索引合并的流程大致如下：**

```
SELECT * FROM tbl_name WHERE key1 = 10 and key2 = 20;
```

1、同时根据index1和index2的索引进行查询。  
2、根据查询返回的主键id取交集。  
3、根据主键id列表执行回表查询，返回结果。

这样做的好处是，可以同时根据index1、index2两个索引去过滤id值，只对共有的id值执行回表操作，节省了很多回表操作带来的开销。  
![在这里插入图片描述](https://img-blog.csdnimg.cn/8281e657ad1a4775b37ddef04f836df9.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5paX6ICFXzIwMTM=,size_14,color_FFFFFF,t_70,g_se,x_16)

**普通索引回表查询说明：**  
每次从二级索引中读取到一条记录后，就会根据该记录的主键值执行回表操作。  
而在某个扫描区间中的二级索引记录的主键值是无序的，也就是说这些二级索引记录对应的聚簇索引记录所在的页面的页号是无序的。  
每次执行回表操作时都相当于要随机读取一个聚簇索引页面，而这些随机I/O 带来的性能开销比较大。  
所以Mysql中通过`MRR 多范围读取`对回表查询进行了优化，先读取一部分二级索引记录，将他们的主键值排好序之后，再统一执行回表操作。

**注意⚠️：**  
这里要把索引合并机制和多字段的组合索引区分开，优先还是考虑建立组合索引，一般来说索引合并会有性能消耗，相比而言组合索引查询效率会更高。

## 二、索引合并机的类型

index merge: 同一个表的多个索引的范围扫描可以对结果进行合并，合并方式分为三种：  
`intersection，union , Sort-Union`。

**测试表初始化：**

```
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(20) NOT NULL DEFAULT '' COMMENT '用户名',
  `age` int(3) DEFAULT NULL COMMENT '年龄',
  `score` int(3) DEFAULT NULL COMMENT '分数',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`),
  KEY `ix_age` (`age`),
  KEY `ix_score` (`score`)
) ENGINE=InnoDB AUTO_INCREMENT=336 DEFAULT CHARSET=utf8 COMMENT='用户表';

-- ----------------------------
-- Records of t_user
-- ----------------------------
BEGIN;
INSERT INTO `t_user` VALUES (280, '娜娜', 24, 100);
INSERT INTO `t_user` VALUES (281, '老王', 35, 70);
INSERT INTO `t_user` VALUES (282, '阿龙', 26, 80);
INSERT INTO `t_user` VALUES (283, '花花', 15, 88);
INSERT INTO `t_user` VALUES (284, '天天', 18, 75);
INSERT INTO `t_user` VALUES (285, '小李', 20, 68);
INSERT INTO `t_user` VALUES (286, '阿雷', 18, 50);
INSERT INTO `t_user` VALUES (287, '小月', 20, 65);
INSERT INTO `t_user` VALUES (288, '娜娜', 24, 100);
INSERT INTO `t_user` VALUES (289, '老王', 35, 70);
INSERT INTO `t_user` VALUES (290, '阿龙', 26, 80);
INSERT INTO `t_user` VALUES (291, '花花', 15, 88);
INSERT INTO `t_user` VALUES (292, '天天', 18, 75);
INSERT INTO `t_user` VALUES (293, '小李', 20, 68);
INSERT INTO `t_user` VALUES (294, '阿雷', 18, 50);
INSERT INTO `t_user` VALUES (295, '小月', 20, 65);
INSERT INTO `t_user` VALUES (296, '娜娜', 24, 100);
INSERT INTO `t_user` VALUES (297, '老王', 35, 70);
INSERT INTO `t_user` VALUES (298, '阿龙', 26, 80);
INSERT INTO `t_user` VALUES (299, '花花', 15, 88);
INSERT INTO `t_user` VALUES (300, '天天', 18, 75);
INSERT INTO `t_user` VALUES (301, '小李', 20, 68);
INSERT INTO `t_user` VALUES (302, '阿雷', 18, 50);
INSERT INTO `t_user` VALUES (303, '小月', 20, 65);
INSERT INTO `t_user` VALUES (304, '娜娜', 24, 100);
INSERT INTO `t_user` VALUES (305, '老王', 35, 70);
INSERT INTO `t_user` VALUES (306, '阿龙', 26, 80);
INSERT INTO `t_user` VALUES (307, '花花', 15, 88);
INSERT INTO `t_user` VALUES (308, '天天', 18, 75);
INSERT INTO `t_user` VALUES (309, '小李', 20, 68);
INSERT INTO `t_user` VALUES (310, '阿雷', 18, 50);
INSERT INTO `t_user` VALUES (311, '小月', 20, 65);
INSERT INTO `t_user` VALUES (312, '娜娜', 24, 100);
INSERT INTO `t_user` VALUES (313, '老王', 35, 70);
INSERT INTO `t_user` VALUES (314, '阿龙', 26, 80);
INSERT INTO `t_user` VALUES (315, '花花', 15, 88);
INSERT INTO `t_user` VALUES (316, '天天', 18, 75);
INSERT INTO `t_user` VALUES (317, '小李', 20, 68);
INSERT INTO `t_user` VALUES (318, '阿雷', 18, 50);
INSERT INTO `t_user` VALUES (319, '小月', 20, 65);
INSERT INTO `t_user` VALUES (320, '娜娜', 24, 100);
INSERT INTO `t_user` VALUES (321, '老王', 35, 70);
INSERT INTO `t_user` VALUES (322, '阿龙', 26, 80);
INSERT INTO `t_user` VALUES (323, '花花', 15, 88);
INSERT INTO `t_user` VALUES (324, '天天', 18, 75);
INSERT INTO `t_user` VALUES (325, '小李', 20, 68);
INSERT INTO `t_user` VALUES (326, '阿雷', 18, 50);
INSERT INTO `t_user` VALUES (327, '小月', 20, 65);
INSERT INTO `t_user` VALUES (328, '娜娜', 24, 100);
INSERT INTO `t_user` VALUES (329, '老王', 35, 70);
INSERT INTO `t_user` VALUES (330, '阿龙', 26, 80);
INSERT INTO `t_user` VALUES (331, '花花', 15, 88);
INSERT INTO `t_user` VALUES (332, '天天', 18, 75);
INSERT INTO `t_user` VALUES (333, '小李', 20, 68);
INSERT INTO `t_user` VALUES (334, '阿雷', 18, 50);
INSERT INTO `t_user` VALUES (335, '小月', 20, 65);
COMMIT;

```

## 1.Index Merge Intersection 索引合并-取交集

采用多索引AND等值查询。

```
EXPLAIN SELECT * from t_user t where t.name = '阿龙' and t.age = 26;
```

**执行结果：**  
![在这里插入图片描述](https://img-blog.csdnimg.cn/85b18523c7d949b994ad54bbb00ff914.png)  
查询的type为：index\_merge，说明使用了索引合并  
Extra中为：Using intersect(idx\_name,ix\_age); Using where，说明索引合并后取的交集。

## 2.Index Merge Union 索引合并-取并集

采用多索引OR等值查询。

```
EXPLAIN SELECT * from t_user t where t.name = '阿龙' or t.age = 26;
```

**执行结果：**  
![在这里插入图片描述](https://img-blog.csdnimg.cn/42577abd50fe499a86d8c2f3cd664ff2.png)

查询的type为：index\_merge，说明使用了索引合并  
Extra中为：Using union(idx\_name,ix\_age); Using where，说明索引合并后取的并集。

![在这里插入图片描述](https://img-blog.csdnimg.cn/4f61bc6ca8eb42a6b39a0867b2c82378.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5paX6ICFXzIwMTM=,size_14,color_FFFFFF,t_70,g_se,x_16)

## 3.Index Merge Sort-Union 索引合并-取有序并集

当 WHERE 子句转换为 `OR 组合的多个范围条件`时，可以采用排序联合算法`Sort-Union`。但 Index Merge 联合算法不适用。

排序联合算法`Sort-Union`和联合算法`Union`的区别在于，排序联合算法必须首先获取所有行的行 id，并在返回任何行之前对它们进行排序。

**示例：**

```
EXPLAIN SELECT * from t_user t where   t.age > 30 or t.score <60;
```

但是执行结果中没有进行索引合并，可能和表数据以及查询优化器的分析有关，认为在在当前数据下，OR 组合的多个范围条件采用全表扫描更合适。  
![在这里插入图片描述](https://img-blog.csdnimg.cn/b4e2a13e9057484e848535542aaa2328.png)

___

## 三、索引合并机制的开关

**1、索引合并机制有4个开关选项：**

-   `index_merge` 索引合并机制的总开关
-   `index_merge_intersection` 索引合并-取交集
-   `index_merge_union` 索引合并-取并集
-   `index_merge_sort_union` 索引合并-排序并集

默认情况下，这些优化开关的值都是on，即打开状态。

**2、查看优化开关配置：**

```
mysql> SELECT @@optimizer_switch;
*************************** 1. row ***************************
@@optimizer_switch: index_merge=on,index_merge_union=on,
                    index_merge_sort_union=on,index_merge_intersection=on,
                    engine_condition_pushdown=on,index_condition_pushdown=on,
                    mrr=on,mrr_cost_based=on,block_nested_loop=on,
                    batched_key_access=off,materialization=on,semijoin=on,
                    loosescan=on,firstmatch=on,duplicateweedout=on,
                    subquery_materialization_cost_based=on,
                    use_index_extensions=on,condition_fanout_filter=on,
                    derived_merge=on,use_invisible_indexes=off,skip_scan=on,
                    hash_join=on,subquery_to_derived=off,
                    prefer_ordering_index=on,hypergraph_optimizer=off,
                    derived_condition_pushdown=on
1 row in set (0.00 sec)
```

**注意⚠️：**  
@@optimizer\_switch 表示全局优化开关；  
@optimizer\_switch 表示Session级别优化开关；

**3、设置优化开关的值**

```
SET [GLOBAL|SESSION] optimizer_switch='command[,command]...';
```

示例：关闭索引合并优化

```
SET  @@optimizer_switch='index_merge=off';
```

**4、重新打开**

```
SET  GLOBAL optimizer_switch='index_merge=on';
-- 发现采用@@optimizer_switch设置的是SESSION级别的，需要采用下面的语句才能重新打开
SET  SESSION optimizer_switch='index_merge=on';
-- 是否生效
SELECT  @@optimizer_switch;
```

## 总结

本文主要介绍了Mysql中的索引合并机制`index merge` 。  
1、通过索引合并机制，可以实现`针对单表的一次查询中利用多个索引`，好处是`减少了回表查询的消耗`。  
2、索引合并有三种算法：`交集intersection，并集union , 有序并集Sort-Union`。  
3、索引合并优化开关的配置。