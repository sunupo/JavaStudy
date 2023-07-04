
### [mongoDB数据更新与操作符 - 老天的背包 - 博客园](https://www.cnblogs.com/zhang-yong/p/7390393.html)

### save
已经废弃


### replaceOne 与 updateOne
replacement 不能替换主键，否则会失败。
```mongodb
db.collection.replaceOne(
   <filter>,
   <replacement>,
   {
     upsert: <boolean>,
     writeConcern: <document>,
     collation: <document>,
     hint: <document|string>                   // Available starting in 4.2.1
   }
)
```
```spring-mongodb-json
db.collection.update(
   <query>,
   <update>,  // update的对象和一些更新的操作符（如$,$inc...）   
   {
     upsert: <boolean>, //  可选，这个参数的意思是，如果不存在update的记录，是否插入。true为插入，默认是false，不插入。
     multi: <boolean>,  // 可选，mongodb 默认是false,只更新找到的第一条记录，如果这个参数为true,就把按条件查出来多条记录全部更新。
     writeConcern: <document>
   }
)
```

- 使用`replaceOne（）`只能替换整个文档，而`updateOne（）`则允许更新字段。由于`replaceOne（）`替换了整个文档-旧文档中不包含在新文档中的字段将丢失。使用`updateOne（）`可以添加新字段，而不会丢失旧文档中的字段

### find
#### 根据数组中的某一个元素作为查询条件`{ages: {$elemMatch:{$eq: 20}}}`
```spring-mongodb-json
mdb> db.cap100coll.find()
[
  { _id: ObjectId("6407d49b953b6a053e6ac754"), name: 'jgg', age: 37 },
  { _id: ObjectId("6407d49f953b6a053e6ac755"), name: 'jgg', age: 38 },
  {
    _id: ObjectId("6407e820953b6a053e6ac75a"),
    names: [ 'zs', 'ls', 'ww' ],
    ages: [ 10, 20, 30 ],
    address: 'beijing'
  }
]
mdb> db.cap100coll.find({address: {$eq: null}, $or:[{age:{$lt:38}}, {ages: {$elemMatch:{$eq: 20}}}]})
[ { _id: ObjectId("6407d49b953b6a053e6ac754"), name: 'jgg', age: 37 } ]
mdb>
```
#### 根据数组长度查找，根据元素是否存在查找
```spring-mongodb-json
mdb> db.cap100coll.find()
[
  { _id: ObjectId("6407d49b953b6a053e6ac754"), name: 'jgg', age: 37 },
  { _id: ObjectId("6407d49f953b6a053e6ac755"), name: 'jgg', age: 38 },
  {
    _id: ObjectId("6407e820953b6a053e6ac75a"),
    names: [ 'zs', 'ls', 'ww' ],
    ages: [ 10, 20, 30 ],
    address: 'beijing'
  }
]
mdb> db.cap100coll.find({'ages': {$exists: true}, $where:'this.ages.length>2'})
[
  {
    _id: ObjectId("6407e820953b6a053e6ac75a"),
    names: [ 'zs', 'ls', 'ww' ],
    ages: [ 10, 20, 30 ],
    address: 'beijing'
  }
]
mdb> db.cap100coll.find({'ages': {$exists: false}})
[
  { _id: ObjectId("6407d49b953b6a053e6ac754"), name: 'jgg', age: 37 },
  { _id: ObjectId("6407d49f953b6a053e6ac755"), name: 'jgg', age: 38 }
]
mdb>
```


### $type
- 匹配元素类型。
- 如果元素是数组类型的话，也可以匹配数组的元素类型。
下面的例子， `{$type:2}字符串` 与 `{$type:4}数组` 都能匹配到
```spring-mongodb-json
mdb> db.cap100coll.find()
[
  { _id: ObjectId("6407d49b953b6a053e6ac754"), name: 'jgg', age: 37 },
  { _id: ObjectId("6407d49f953b6a053e6ac755"), name: 'jgg', age: 38 },
  {
    _id: ObjectId("6407e820953b6a053e6ac75a"),
    names: [ 'zs', 'ls', 'ww' ],
    ages: [ 10, 20, 30 ],
    address: 'beijing'
  }
]
mdb> db.cap100coll.find({names:{$type: 2}})
[
  {
    _id: ObjectId("6407e820953b6a053e6ac75a"),
    names: [ 'zs', 'ls', 'ww' ],
    ages: [ 10, 20, 30 ],
    address: 'beijing'
  }
]
mdb> db.cap100coll.find({names:{$type: 4}})
[
  {
    _id: ObjectId("6407e820953b6a053e6ac75a"),
    names: [ 'zs', 'ls', 'ww' ],
    ages: [ 10, 20, 30 ],
    address: 'beijing'
  }
]
mdb> db.cap100coll.find({ages:{$type: 4}})
[
  {
    _id: ObjectId("6407e820953b6a053e6ac75a"),
    names: [ 'zs', 'ls', 'ww' ],
    ages: [ 10, 20, 30 ],
    address: 'beijing'
  }
]

```

ages 数组的数字默认是32为int，$type=16
```spring-mongodb-json
mdb> db.cap100coll.find()
[
  { _id: ObjectId("6407d49b953b6a053e6ac754"), name: 'jgg', age: 37 },
  { _id: ObjectId("6407d49f953b6a053e6ac755"), name: 'jgg', age: 38 },
  {
    _id: ObjectId("6407e820953b6a053e6ac75a"),
    names: [ 'zs', 'ls', 'ww' ],
    ages: [ 10, 20, 30 ],
    address: 'beijing'
  }
]
mdb> db.cap100coll.find({ages:{$type: 4}})
[
  {
    _id: ObjectId("6407e820953b6a053e6ac75a"),
    names: [ 'zs', 'ls', 'ww' ],
    ages: [ 10, 20, 30 ],
    address: 'beijing'
  }
]
mdb> db.cap100coll.find({ages:{$type: 2}})

mdb> db.cap100coll.find({ages:{$type: 1}})

mdb> db.cap100coll.find({ages:{$type: 16}})
[
  {
    _id: ObjectId("6407e820953b6a053e6ac75a"),
    names: [ 'zs', 'ls', 'ww' ],
    ages: [ 10, 20, 30 ],
    address: 'beijing'
  }
]
mdb> db.cap100coll.find({ages:{$type: 18}})

mdb>
```

### skip sort  limit 顺序

三者同时出现，不管带啊的顺序，都是按照 sort，skip，limit的顺序
```spring-mongodb-json
mdb> db.ml.find()
[
  { _id: ObjectId("6407fad8953b6a053e6ac75b"), name: 'a', age: 1 },
  { _id: ObjectId("6407fadb953b6a053e6ac75c"), name: 'a', age: 2 },
  { _id: ObjectId("6407fadd953b6a053e6ac75d"), name: 'a', age: 3 },
  { _id: ObjectId("6407fadf953b6a053e6ac75e"), name: 'a', age: 4 },
  { _id: ObjectId("6407fae1953b6a053e6ac75f"), name: 'a', age: 5 },
  { _id: ObjectId("6407fae4953b6a053e6ac760"), name: 'a', age: 6 },
  { _id: ObjectId("6407faec953b6a053e6ac761"), name: 'a', age: 7 },
  { _id: ObjectId("6407faef953b6a053e6ac762"), name: 'a', age: 8 },
  { _id: ObjectId("6407faff953b6a053e6ac763"), name: 'a', age: 9 },
  { _id: ObjectId("6407fb02953b6a053e6ac764"), name: 'a', age: 10 }
]
mdb> db.ml.find().sort({age: -1}).skip(5).limit(2)
[
  { _id: ObjectId("6407fae1953b6a053e6ac75f"), name: 'a', age: 5 },
  { _id: ObjectId("6407fadf953b6a053e6ac75e"), name: 'a', age: 4 }
]
mdb> db.ml.find().skip(5).limit(2).sort({age: -1})
[
  { _id: ObjectId("6407fae1953b6a053e6ac75f"), name: 'a', age: 5 },
  { _id: ObjectId("6407fadf953b6a053e6ac75e"), name: 'a', age: 4 }
]
mdb>
```

指定顺序：
```spring-mongodb-json
mdb> db.getCollection('ml').aggregate([{$skip:5}, {$limit:2}, {$sort:{age:-1}}])
[
  { _id: ObjectId("6407faec953b6a053e6ac761"), name: 'a', age: 7 },
  { _id: ObjectId("6407fae4953b6a053e6ac760"), name: 'a', age: 6 }
]
```



### 数组中的 field.$，代表它自己
> [mongoDB数据更新与操作符 - 老天的背包 - 博客园](https://www.cnblogs.com/zhang-yong/p/7390393.html)
- $是他自己的意思，代表按条件找出的**数组里面某项**他自己。
- 需要注意的是，$只会应用找到的第一条数组项(数组两个相同元素只会修改第一个)，后面的就不管了。还是看例子：
```spring-mongodb-json
mdb> db.createCollection("t1")
{ ok: 1 }
mdb> db.t1.insert({x:[1,2,3,4]})
{
  acknowledged: true,
  insertedIds: { '0': ObjectId("64083927953b6a053e6ac765") }
}
mdb> db.t1.find()
[ { _id: ObjectId("64083927953b6a053e6ac765"), x: [ 1, 2, 3, 4 ] } ]
mdb> db.t1.update({x:2}, {$inc: {'x.$': 1}})
{
  acknowledged: true,
  insertedId: null,
  matchedCount: 1,
  modifiedCount: 1,
  upsertedCount: 0
}
mdb> db.t1.find()
[ { _id: ObjectId("64083927953b6a053e6ac765"), x: [ 1, 3, 3, 4 ] } ]
mdb>
```

### 还有注意的是$配合$unset使用的时候，
还有注意的是$配合$unset使用的时候，会留下一个null的数组项，不过可以用{$pull:{x:null}}删除全部是null的数组项。例：


### mongodb 投影 project 取字段值的时候需要这样用
```spring-mongodb-json
字段名加单引号 '$field'，因为是作为 value 而不是 key
```
```spring-mongodb-json
// 通过管道 aggregate 的方式：

mdb> db.ml.find()
[
  { _id: ObjectId("6407fad8953b6a053e6ac75b"), name: 'a', age: 11 },
  { _id: ObjectId("6407fadb953b6a053e6ac75c"), name: 'a', age: 12 },
]
mdb> db.getCollection('ml').aggregate([{$limit:2},{$project: {newFIledName:{$concat:[`$name`,'--',toString(`$age`)]}}}])
[
  {
    _id: ObjectId("6407fad8953b6a053e6ac75b"),
    newFIledName: 'a--[object Undefined]'
  },
  {
    _id: ObjectId("6407fadb953b6a053e6ac75c"),
    newFIledName: 'a--[object Undefined]'
  }
]
```

```spring-mongodb-json
// 通过 find 函数 的第二个参数
mdb> db.ml.find().limit(2)
[
  { _id: ObjectId("6407fad8953b6a053e6ac75b"), name: 'a', age: 11 },
  { _id: ObjectId("6407fadb953b6a053e6ac75c"), name: 'a', age: 12 }
]
mdb> db.ml.find({},{newFieldName: {$concat:[`$name`, `$name`]}}).limit(2)
[
  { _id: ObjectId("6407fad8953b6a053e6ac75b"), newFieldName: 'aa' },
  { _id: ObjectId("6407fadb953b6a053e6ac75c"), newFieldName: 'aa' }
]
mdb> db.ml.find({},{newFieldName: `$name`}).limit(2).pretty()
[
{ _id: ObjectId("6407fad8953b6a053e6ac75b"), newFieldName: 'a' },
{ _id: ObjectId("6407fadb953b6a053e6ac75c"), newFieldName: 'a' }
]
mdb>
```

### update 然后更新字段的值的时候字段这样用
直接用`field`字段名， 不加单引号，不加`$`符号
```spring-mongodb-json
mdb> db.ml.find()
[
  { _id: ObjectId("6407fad8953b6a053e6ac75b"), name: 'a', age: 1 },
  { _id: ObjectId("6407fadb953b6a053e6ac75c"), name: 'a', age: 2 },
  { _id: ObjectId("6407fadd953b6a053e6ac75d"), name: 'a', age: 3 },
  { _id: ObjectId("6407fadf953b6a053e6ac75e"), name: 'a', age: 4 },
  { _id: ObjectId("6407fae1953b6a053e6ac75f"), name: 'a', age: 5 },
  { _id: ObjectId("6407fae4953b6a053e6ac760"), name: 'a', age: 6 },
  { _id: ObjectId("6407faec953b6a053e6ac761"), name: 'a', age: 7 },
  { _id: ObjectId("6407faef953b6a053e6ac762"), name: 'a', age: 8 },
  { _id: ObjectId("6407faff953b6a053e6ac763"), name: 'a', age: 9 },
  { _id: ObjectId("6407fb02953b6a053e6ac764"), name: 'a', age: 10 }
]
mdb> db.ml.update({age: {$lt: 5}}, {$inc: {age: 10}}, {multi: true})
{
  acknowledged: true,
  insertedId: null,
  matchedCount: 4,
  modifiedCount: 4,
  upsertedCount: 0
}
mdb> db.ml.find()
[
  { _id: ObjectId("6407fad8953b6a053e6ac75b"), name: 'a', age: 11 },
  { _id: ObjectId("6407fadb953b6a053e6ac75c"), name: 'a', age: 12 },
  { _id: ObjectId("6407fadd953b6a053e6ac75d"), name: 'a', age: 13 },
  { _id: ObjectId("6407fadf953b6a053e6ac75e"), name: 'a', age: 14 },
  { _id: ObjectId("6407fae1953b6a053e6ac75f"), name: 'a', age: 5 },
  { _id: ObjectId("6407fae4953b6a053e6ac760"), name: 'a', age: 6 },
  { _id: ObjectId("6407faec953b6a053e6ac761"), name: 'a', age: 7 },
  { _id: ObjectId("6407faef953b6a053e6ac762"), name: 'a', age: 8 },
  { _id: ObjectId("6407faff953b6a053e6ac763"), name: 'a', age: 9 },
  { _id: ObjectId("6407fb02953b6a053e6ac764"), name: 'a', age: 10 }
]
mdb>
```

### update 的 upsert 是这个意思
- upsert 为 false，没找到就不修改
- upsert 为 true，没找到就插入
```spring-mongodb-json
mdb> db.ml.find()
[
  { _id: ObjectId("6407fad8953b6a053e6ac75b"), name: 'a', age: 11 },
  { _id: ObjectId("6407fadb953b6a053e6ac75c"), name: 'a', age: 12 },
  { _id: ObjectId("6407fadd953b6a053e6ac75d"), name: 'a', age: 13 },
  { _id: ObjectId("6407fadf953b6a053e6ac75e"), name: 'a', age: 14 },
  { _id: ObjectId("6407fae1953b6a053e6ac75f"), name: 'a', age: 5 },
  { _id: ObjectId("6407fae4953b6a053e6ac760"), name: 'a', age: 6 },
  { _id: ObjectId("6407faec953b6a053e6ac761"), name: 'a', age: 7 },
  { _id: ObjectId("6407faef953b6a053e6ac762"), name: 'a', age: 8 },
  { _id: ObjectId("6407faff953b6a053e6ac763"), name: 'a', age: 9 },
  { _id: ObjectId("6407fb02953b6a053e6ac764"), name: 'a', age: 10 }
]
mdb> db.ml.update({age: {$lt: -100}}, {$inc: {age: 10}}, {multi: true})
{
acknowledged: true,
insertedId: null,
matchedCount: 0,
modifiedCount: 0,
upsertedCount: 0
}
mdb> db.ml.update({age: {$lt: -100}}, {$inc: {age: 10}}, {upsert: true, multi: true})
{
acknowledged: true,
insertedId: ObjectId("640840342f6b214d41bd8f49"),
matchedCount: 0,
modifiedCount: 0,
upsertedCount: 1
}
mdb> db.ml.find()
[
{ _id: ObjectId("6407fad8953b6a053e6ac75b"), name: 'a', age: 11 },
{ _id: ObjectId("6407fadb953b6a053e6ac75c"), name: 'a', age: 12 },
{ _id: ObjectId("6407fadd953b6a053e6ac75d"), name: 'a', age: 13 },
{ _id: ObjectId("6407fadf953b6a053e6ac75e"), name: 'a', age: 14 },
{ _id: ObjectId("6407fae1953b6a053e6ac75f"), name: 'a', age: 5 },
{ _id: ObjectId("6407fae4953b6a053e6ac760"), name: 'a', age: 6 },
{ _id: ObjectId("6407faec953b6a053e6ac761"), name: 'a', age: 7 },
{ _id: ObjectId("6407faef953b6a053e6ac762"), name: 'a', age: 8 },
{ _id: ObjectId("6407faff953b6a053e6ac763"), name: 'a', age: 9 },
{ _id: ObjectId("6407fb02953b6a053e6ac764"), name: 'a', age: 10 },
{ _id: ObjectId("640840342f6b214d41bd8f49"), age: 10 }
]
mdb>
```

### 类型转换
```spring-mongodb-json
// string -> double
parseInt(x)

// string -> int
NumberInt(x)

// string -> date
new ISODate(x)

// date -> string
new toISOString(x)

```