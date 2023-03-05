[(122条消息) Thrift使用教程（Java版本）-CSDN博客](https://blog.csdn.net/wuxiaopengnihao1/article/details/125955885)

## [Thrift](https://so.csdn.net/so/search?q=Thrift&spm=1001.2101.3001.7020)简介

> Thrift是一个跨语言的服务部署框架，最初由Facebook于2007年开发，2008年进入Apache[开源项目](https://so.csdn.net/so/search?q=%E5%BC%80%E6%BA%90%E9%A1%B9%E7%9B%AE&spm=1001.2101.3001.7020)。Thrift通过一个中间语言(IDL, 接口定义语言)来定义RPC的接口和数据类型，然后通过一个编译器生成不同语言的代码（目前支持C++,Java, Python, PHP, Ruby, Erlang, Perl, Haskell, C#, Cocoa, Smalltalk和OCaml）,**并由生成的代码负责RPC协议层和传输层的实现**。

## Thrift协议栈

Thrift 客户端、服务端API架构如下图所示：

图片来自https://en.wikipedia.org/wiki/Apache\_Thrift

Thrift的网络栈如下所示：

### Transport

> Transport层提供了一个简单的网络**读写抽象层**。这使得thrift底层的transport从系统其它部分（如：序列化/反序列化）解耦。以下是一些Transport接口提供的方法：

```
openclosereadwriteflush
```

更加详细方法如，

Thrift支持如下几种Transport：

> 在之前的一篇博文【一步一步完成thrift Java示例】中，给出了一个使用thrift完成rpc的示例。

在本篇博文，我们会给出一个使用Thrift的基本教程~

更多教程请访问[码农之家](http://www.manongzj.com/ "码农之家")   

## Thrift简介

> Thrift是一个跨语言的服务部署框架，最初由Facebook于2007年开发，2008年进入Apache开源项目。Thrift通过一个中间语言([IDL](https://so.csdn.net/so/search?q=IDL&spm=1001.2101.3001.7020), 接口定义语言)来定义RPC的接口和数据类型，然后通过一个编译器生成不同语言的代码（目前支持C++,Java, Python, PHP, Ruby, Erlang, Perl, Haskell, C#, Cocoa, Smalltalk和OCaml）,**并由生成的代码负责RPC协议层和传输层的实现**。

## Thrift协议栈

Thrift 客户端、服务端API架构如下图所示：

图片来自https://en.wikipedia.org/wiki/Apache\_Thrift

Thrift的网络栈如下所示：

### Transport

> Transport层提供了一个简单的网络**读写抽象层**。这使得thrift底层的transport从系统其它部分（如：序列化/反序列化）解耦。以下是一些Transport接口提供的方法：

```
openclosereadwriteflush
```

更加详细方法如，

Thrift支持如下几种Transport：

-   TIOStreamTransport和TSocket这两个类的结构对应着阻塞同步IO, TSocket封装了Socket接口
-   TNonblockingTrasnsort，TNonblockingSocket这两个类对应着非阻塞IO
-   TMemoryInputTransport封装了一个字节数组byte\[\]来做输入流的封装
-   TMemoryBuffer使用字节数组输出流ByteArrayOutputStream做输出流的封装
-   TFramedTransport则封装了TMemoryInputTransport做输入流，封装了TByteArryOutPutStream做输出流，作为内存读写缓冲区的一个封装。TFramedTransport的flush方法时，会先写4个字节的输出流的长度作为消息头，然后写消息体。和FrameBuffer的读消息对应起来。FrameBuffer对消息时，先读4个字节的长度，再读消息体
-   TFastFramedTransport是内存利用率更高的一个内存读写缓存区，它使用自动增长的byte\[\](不够长度才new)，而不是每次都new一个byte\[\]，提高了内存的使用率。其他和TFramedTransport一样，flush时也会写4个字节的消息头表示消息长度。

### Protocol

> Protocol抽象层定义了一种将内存中数据结构映射成可传输格式的机制。换句话说，Protocol定义了datatype怎样使用底层的Transport对自己进行编解码。因此，Protocol的实现要给出编码机制并负责对数据进行序列化。

Thrift支持如下几种protocols：

-   TBinaryProtocol : 二进制格式.
-   TCompactProtocol : 压缩格式
-   TJSONProtocol : JSON格式
-   TSimpleJSONProtocol : 提供JSON只写协议, 生成的文件很容易通过脚本语言解析
-   等等

主要的方法有：

```
writeMessageBegin(name, type, seq)writeMessageEnd()writeStructBegin(name)writeStructEnd()writeFieldBegin(name, type, id)writeFieldEnd()writeFieldStop()writeMapBegin(ktype, vtype, size)writeMapEnd()writeListBegin(etype, size)writeListEnd()writeSetBegin(etype, size)writeSetEnd()writeBool(bool)writeByte(byte)writeI16(i16)writeI32(i32)writeI64(i64)writeDouble(double)writeString(string)
```

读操作~

```
, type, seq = readMessageBegin()            readMessageEnd()= readStructBegin() readStructEnd(), type, id = readFieldBegin()           readFieldEnd(), v, size = readMapBegin()       readMapEnd(), size = readListBegin()        readListEnd(), size = readSetBegin()        readSetEnd()= readBool()= readByte()= readI16()= readI32()= readI64() = readDouble() = readString()
```

### Processor

> **Processor封装了从输入数据流中读数据和向数据数据流中写数据的操作**。读写数据流用Protocol对象表示。

Processor的结构体非常简单:

```
/*e org.apache.thrift; org.apache.thrift.protocol.TProtocol;/** interface TProcessor {ic boolean process(TProtocol in, TProtocol out)rows TException;}
```

与服务相关的processor实现由编译器产生。

> Processor主要工作流程如下： 从连接中读取数据（使用输入protocol），将处理授权给handler（由用户实现），最后将结果写到连接上（使用输出protocol）。

### Server

Server将以上所有特性集成在一起，Server实现的几个步骤如下~

> （1） 创建一个transport对象 （2） 为transport对象创建输入输出protocol （3） 基于输入输出protocol创建processor （4） 等待连接请求并将之交给processor处理

示例：

```
e com.xxx.tutorial.thrift.server; java.util.logging.Logger; org.apache.thrift.TProcessor; org.apache.thrift.protocol.TBinaryProtocol; org.apache.thrift.server.TServer; org.apache.thrift.server.TSimpleServer; org.apache.thrift.transport.TServerSocket; com.xxx.tutorial.thrift.service.UserService; com.xxx.tutorial.thrift.service.impl.UserServiceImpl;/** class TSimpleServerExample {private static final Logger logger = Logger.getLogger(TSimpleServerExample.class.getName());private static final int SERVER_PORT = 9123;public static void main(String[] args) {try {/*** 1. 创建Transport*/TServerSocket serverTransport = new TServerSocket(SERVER_PORT);TServer.Args tArgs = new TServer.Args(serverTransport);/*** 2. 为Transport创建Protocol*/tArgs.protocolFactory(new TBinaryProtocol.Factory());// tArgs.protocolFactory(new TCompactProtocol.Factory());// tArgs.protocolFactory(new TJSONProtocol.Factory());/*** 3. 为Protocol创建Processor*/TProcessor tprocessor = new UserService.Processor<UserService.Iface>(new UserServiceImpl());tArgs.processor(tprocessor);/*** 4. 创建Server并启动** org.apache.thrift.server.TSimpleServer - 简单的单线程服务模型，一般用于测试*/TServer server = new TSimpleServer(tArgs);logger.info("UserService TSimpleServer start ....");server.serve();} catch (Exception e) {logger.severe("Server start error!!!" + e.getLocalizedMessage());e.printStackTrace();}}}
```

## Thrift类型系统

> Thrift类型系统包括预定义的基本类型（如bool , byte, double, string）、特殊类型(如binary)、用户自定义结构体（看上去像C 语言的结构体）、容器类型（如list，set，map）以及异常和服务定义~

### 基本类型（Base Type）

```
  ：布尔类型(true or value)，占一个字节/i8 ：有符号字节  :  16位有符号整型  :  32位有符号整型  :  64位有符号整型  ：64位浮点数  ：未知编码或者二进制的字符串
```

> 注意， thrift不支持无符号整型，因为很多目标语言不存在无符号整型（如java）。

### 特殊类型（Special type）

```
binary   ：未经过编码的字节流
```

Thrift基本类型、特殊类型和Java类型的对应关系如下表所示：

### 容器（container）

Thrift容器与类型密切相关，它与当前流行编程语言提供的容器类型相对应，Thrift提供了3种容器类型：

```
<t1>   ：一系列t1类型的元素组成的有序表，元素可以重复<t1>    ：一系列t1类型的元素组成的无序表，元素唯一<t1,t2> ：key/value对（key的类型是t1且key唯一，value类型是t2）
```

> 注意： 容器中的元素类型可以是**除了service之外**的任何合法thrift类型（包括结构体和异常）。

Thrift容器类型和Java类型的对应关系如下表所示：

### 结构（struct）

> Thrift结构体在概念上同C语言结构体类型—-一种将相关属性聚集（封装）在一起的方式。在面向对象语言中，thrift结构体被转换成类，在Java语言中，这等价于JavaBean的概念~

如,

```
  User {  :i32 userId,:string name}
```

### 异常（Exception）

> 异常在语法和功能上类似于结构体，只不过异常使用关键字exception而不是struct关键字声明。 但它在语义上不同于结构体—当定义一个RPC服务时，开发者可能需要声明一个远程方法抛出一个异常。

如，

```
ion MyException {1: string code;2: string message;}
```

### 服务（Service）

> 一个服务包含一系列命名函数，每个函数包含一系列的参数以及一个返回类型。 **在语法上，服务等价于定义一个接口或者纯虚抽象类~**

格式如下，

```
e <name> {<returntype> <name> (<arguments>)[throws (<exceptions>)]...}
```

如，

```
e  UserService {ng sayHello(1:string name);}
```

## 其它语法参考

### Typedefs

Thrift支持C/C++风格的typedef, 如

```
typedef i32 MyInteger
```

> 说明： a. 末尾没有逗号 b. struct可以使用typedef

```
f i32 MyInteger  User {  :MyInteger userId,:string name}
```

### 枚举Enums

可以像C/C++那样定义枚举类型，如：

```
enum Gender {MALE,FEMALE,UNKONWN}
```

Thrfit支持shell注释风格，C/C++语言中单行或者多行注释风格

```
# This is a valid comment./** This is a multi-line comment.* Just like in C.*/// C++/Java style single-line comments work just as well.
```

### 命名空间Namespace

> Thrift中的命名空间同C++中的namespace和java中的**package**类似，它们均提供了一种组织（隔离）代码的方式。因为每种语言均有自己的命名空间定义方式（如python中有module），thrift允许开发者针对特定语言定义namespace：

```
ace cpp com.example.project  // aace java com.example.project // b
```

### Includes

Thrift允许thrift文件包含，用户需要**使用thrift文件名作为前缀访问被包含的对象**，如：

```
e "user.thrift"ace java com.xxx.tutorial.thrift.servicee  UserService {ng sayHello(1:string name), saveUser(1:user.User user)}
```

> 说明： a． thrift文件名要用双引号包含，末尾没有逗号或者分号 b． 注意user前缀

### 常量Constants

Thrift允许用户定义常量，复杂的类型和结构体可使用JSON形式表示。

```
i32 INT_CONST = 1234;    // amap<string,string> MAP_CONST = {"hello": "world", "goodnight": "moon"}
```

## 编写一个Thrift文件

有了上述Thrift IDL的语法参考之外，我们就可以来根据这些语法信息，编写thrift文件，并完成生成java代码，结合示例来体验一把~

### 基本类型和特殊类型

定义一个**types.thrift**文件，内容如下:

```
 Types {1: bool boolValue;  2: i8   byteValue;  3: i16  shortValue;  4: i32  intValue;  5: i64  longValue;  6: double doubleValue;  7: string stringValue;  8: binary binaryValue;}
```

根据types.thrift生成的Types.java文件，类型相关的部分代码如下：

```
/**@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.10.0)", date = "2017-06-08") class Types implements org.apache.thrift.TBase<Types, Types._Fields>, java.io.Serializable, Cloneable, Comparable<Types> {ate static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("Types");ate static final org.apache.thrift.protocol.TField BOOL_VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("boolValue", org.apache.thrift.protocol.TType.BOOL, (short)1);ate static final org.apache.thrift.protocol.TField BYTE_VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("byteValue", org.apache.thrift.protocol.TType.BYTE, (short)2);ate static final org.apache.thrift.protocol.TField SHORT_VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("shortValue", org.apache.thrift.protocol.TType.I16, (short)3);ate static final org.apache.thrift.protocol.TField INT_VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("intValue", org.apache.thrift.protocol.TType.I32, (short)4);ate static final org.apache.thrift.protocol.TField LONG_VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("longValue", org.apache.thrift.protocol.TType.I64, (short)5);ate static final org.apache.thrift.protocol.TField DOUBLE_VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("doubleValue", org.apache.thrift.protocol.TType.DOUBLE, (short)6);ate static final org.apache.thrift.protocol.TField STRING_VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("stringValue", org.apache.thrift.protocol.TType.STRING, (short)7);ate static final org.apache.thrift.protocol.TField BINARY_VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("binaryValue", org.apache.thrift.protocol.TType.STRING, (short)8);ate static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new TypesStandardSchemeFactory();ate static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new TypesTupleSchemeFactory();ic boolean boolValue; // requiredic byte byteValue; // requiredic short shortValue; // requiredic int intValue; // requiredic long longValue; // requiredic double doubleValue; // requiredic java.lang.String stringValue; // requiredic java.nio.ByteBuffer binaryValue; // required/** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */ic enum _Fields implements org.apache.thrift.TFieldIdEnum {OL_VALUE((short)1, "boolValue"),TE_VALUE((short)2, "byteValue"),ORT_VALUE((short)3, "shortValue"),T_VALUE((short)4, "intValue"),NG_VALUE((short)5, "longValue"),UBLE_VALUE((short)6, "doubleValue"),RING_VALUE((short)7, "stringValue"),NARY_VALUE((short)8, "binaryValue");ivate static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();atic {for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {  byName.put(field.getFieldName(), field);}}/**blic static _Fields findByThriftId(int fieldId) {switch(fieldId) {  case 1: // BOOL_VALUE    return BOOL_VALUE;  case 2: // BYTE_VALUE    return BYTE_VALUE;  case 3: // SHORT_VALUE    return SHORT_VALUE;  case 4: // INT_VALUE    return INT_VALUE;  case 5: // LONG_VALUE    return LONG_VALUE;  case 6: // DOUBLE_VALUE    return DOUBLE_VALUE;  case 7: // STRING_VALUE    return STRING_VALUE;  case 8: // BINARY_VALUE    return BINARY_VALUE;  default:    return null;}}/**blic static _Fields findByThriftIdOrThrow(int fieldId) {_Fields fields = findByThriftId(fieldId);if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");return fields;}/**blic static _Fields findByName(java.lang.String name) {return byName.get(name);}ivate final short _thriftId;ivate final java.lang.String _fieldName;_Fields(short thriftId, java.lang.String fieldName) {_thriftId = thriftId;_fieldName = fieldName;}blic short getThriftFieldId() {return _thriftId;}blic java.lang.String getFieldName() {return _fieldName;}}// isset id assignmentsate static final int __BOOLVALUE_ISSET_ID = 0;ate static final int __BYTEVALUE_ISSET_ID = 1;ate static final int __SHORTVALUE_ISSET_ID = 2;ate static final int __INTVALUE_ISSET_ID = 3;ate static final int __LONGVALUE_ISSET_ID = 4;ate static final int __DOUBLEVALUE_ISSET_ID = 5;ate byte __isset_bitfield = 0;ic static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;ic {va.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);pMap.put(_Fields.BOOL_VALUE, new org.apache.thrift.meta_data.FieldMetaData("boolValue", org.apache.thrift.TFieldRequirementType.DEFAULT,   new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));pMap.put(_Fields.BYTE_VALUE, new org.apache.thrift.meta_data.FieldMetaData("byteValue", org.apache.thrift.TFieldRequirementType.DEFAULT,   new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BYTE)));pMap.put(_Fields.SHORT_VALUE, new org.apache.thrift.meta_data.FieldMetaData("shortValue", org.apache.thrift.TFieldRequirementType.DEFAULT,   new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I16)));pMap.put(_Fields.INT_VALUE, new org.apache.thrift.meta_data.FieldMetaData("intValue", org.apache.thrift.TFieldRequirementType.DEFAULT,   new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));pMap.put(_Fields.LONG_VALUE, new org.apache.thrift.meta_data.FieldMetaData("longValue", org.apache.thrift.TFieldRequirementType.DEFAULT,   new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));pMap.put(_Fields.DOUBLE_VALUE, new org.apache.thrift.meta_data.FieldMetaData("doubleValue", org.apache.thrift.TFieldRequirementType.DEFAULT,   new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));pMap.put(_Fields.STRING_VALUE, new org.apache.thrift.meta_data.FieldMetaData("stringValue", org.apache.thrift.TFieldRequirementType.DEFAULT,   new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));pMap.put(_Fields.BINARY_VALUE, new org.apache.thrift.meta_data.FieldMetaData("binaryValue", org.apache.thrift.TFieldRequirementType.DEFAULT,   new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));taDataMap = java.util.Collections.unmodifiableMap(tmpMap);g.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Types.class, metaDataMap);}ic Types() {}ic Types(olean boolValue,te byteValue,ort shortValue,t intValue,ng longValue,uble doubleValue,va.lang.String stringValue,va.nio.ByteBuffer binaryValue){is();is.boolValue = boolValue;tBoolValueIsSet(true);is.byteValue = byteValue;tByteValueIsSet(true);is.shortValue = shortValue;tShortValueIsSet(true);is.intValue = intValue;tIntValueIsSet(true);is.longValue = longValue;tLongValueIsSet(true);is.doubleValue = doubleValue;tDoubleValueIsSet(true);is.stringValue = stringValue;is.binaryValue = org.apache.thrift.TBaseHelper.copyBinary(binaryValue);}/**ic Types(Types other) {__isset_bitfield = other.__isset_bitfield;is.boolValue = other.boolValue;is.byteValue = other.byteValue;is.shortValue = other.shortValue;is.intValue = other.intValue;is.longValue = other.longValue;is.doubleValue = other.doubleValue; (other.isSetStringValue()) {this.stringValue = other.stringValue;} (other.isSetBinaryValue()) {this.binaryValue = org.apache.thrift.TBaseHelper.copyBinary(other.binaryValue);}}ic Types deepCopy() {turn new Types(this);}@Overrideic void clear() {tBoolValueIsSet(false);is.boolValue = false;tByteValueIsSet(false);is.byteValue = 0;tShortValueIsSet(false);is.shortValue = 0;tIntValueIsSet(false);is.intValue = 0;tLongValueIsSet(false);is.longValue = 0;tDoubleValueIsSet(false);is.doubleValue = 0.0;is.stringValue = null;is.binaryValue = null;}ic boolean isBoolValue() {turn this.boolValue;}ic Types setBoolValue(boolean boolValue) {is.boolValue = boolValue;tBoolValueIsSet(true);turn this;}... ...}
```

可以看出来，thrift文件中定义的类型，转换成java代码的类型，和下面表格展示的是一致的~

### 枚举类

定义一个枚举 gender.thrift

```
enum Gender {MALE,FEMALE,UNKONWN}
```

根据gender.thrift生成的java代码如下：

```
/** java.util.Map; java.util.HashMap; org.apache.thrift.TEnum; enum Gender implements org.apache.thrift.TEnum {(0),LE(1),NWN(2);ate final int value;ate Gender(int value) {is.value = value;}/**ic int getValue() {turn value;}/**ic static Gender findByValue(int value) { itch (value) {case 0:  return MALE;case 1:  return FEMALE;case 2:  return UNKONWN;default:  return null;}}}
```

### 异常exception

创建一个exception.thrift文件，并写入如下几个属性~

```
ion MyException {1: string code;2: string message;}
```

根据exception.thrift生成java代码

```
/**@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.10.0)", date = "2017-06-08") class MyException extends org.apache.thrift.TException implements org.apache.thrift.TBase<MyException, MyException._Fields>, java.io.Serializable, Cloneable, Comparable<MyException> {ate static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MyException");ate static final org.apache.thrift.protocol.TField CODE_FIELD_DESC = new org.apache.thrift.protocol.TField("code", org.apache.thrift.protocol.TType.STRING, (short)1);ate static final org.apache.thrift.protocol.TField MESSAGE_FIELD_DESC = new org.apache.thrift.protocol.TField("message", org.apache.thrift.protocol.TType.STRING, (short)2);ate static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new MyExceptionStandardSchemeFactory();ate static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new MyExceptionTupleSchemeFactory();ic java.lang.String code; // requiredic java.lang.String message; // required/** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */ic enum _Fields implements org.apache.thrift.TFieldIdEnum {DE((short)1, "code"),SSAGE((short)2, "message");ivate static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();atic {for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {  byName.put(field.getFieldName(), field);}}/**blic static _Fields findByThriftId(int fieldId) {switch(fieldId) {  case 1: // CODE    return CODE;  case 2: // MESSAGE    return MESSAGE;  default:    return null;}}/**blic static _Fields findByThriftIdOrThrow(int fieldId) {_Fields fields = findByThriftId(fieldId);if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");return fields;}/**blic static _Fields findByName(java.lang.String name) {return byName.get(name);}ivate final short _thriftId;ivate final java.lang.String _fieldName;_Fields(short thriftId, java.lang.String fieldName) {_thriftId = thriftId;_fieldName = fieldName;}blic short getThriftFieldId() {return _thriftId;}blic java.lang.String getFieldName() {return _fieldName;}}// isset id assignmentsic static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;ic {va.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);pMap.put(_Fields.CODE, new org.apache.thrift.meta_data.FieldMetaData("code", org.apache.thrift.TFieldRequirementType.DEFAULT,   new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));pMap.put(_Fields.MESSAGE, new org.apache.thrift.meta_data.FieldMetaData("message", org.apache.thrift.TFieldRequirementType.DEFAULT,   new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));taDataMap = java.util.Collections.unmodifiableMap(tmpMap);g.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MyException.class, metaDataMap);}ic MyException() {}ic MyException(va.lang.String code,va.lang.String message){is();is.code = code;is.message = message;}/**ic MyException(MyException other) { (other.isSetCode()) {this.code = other.code;} (other.isSetMessage()) {this.message = other.message;}}ic MyException deepCopy() {turn new MyException(this);}@Overrideic void clear() {is.code = null;is.message = null;}ic java.lang.String getCode() {turn this.code;}ic MyException setCode(java.lang.String code) {is.code = code;turn this;}ic void unsetCode() {is.code = null;}/** Returns true if field code is set (has been assigned a value) and false otherwise */ic boolean isSetCode() {turn this.code != null;}ic void setCodeIsSet(boolean value) { (!value) {this.code = null;}}ic java.lang.String getMessage() {turn this.message;}ic MyException setMessage(java.lang.String message) {is.message = message;turn this;}ic void unsetMessage() {is.message = null;}/** Returns true if field message is set (has been assigned a value) and false otherwise */ic boolean isSetMessage() {turn this.message != null;}ic void setMessageIsSet(boolean value) { (!value) {this.message = null;}}ic void setFieldValue(_Fields field, java.lang.Object value) {itch (field) {se CODE:if (value == null) {  unsetCode();} else {  setCode((java.lang.String)value);}break;se MESSAGE:if (value == null) {  unsetMessage();} else {  setMessage((java.lang.String)value);}break;}}ic java.lang.Object getFieldValue(_Fields field) {itch (field) {se CODE:return getCode();se MESSAGE:return getMessage();}row new java.lang.IllegalStateException();}/** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */ic boolean isSet(_Fields field) { (field == null) {throw new java.lang.IllegalArgumentException();}itch (field) {se CODE:return isSetCode();se MESSAGE:return isSetMessage();}row new java.lang.IllegalStateException();}@Overrideic boolean equals(java.lang.Object that) { (that == null)return false; (that instanceof MyException)return this.equals((MyException)that);turn false;}ic boolean equals(MyException that) { (that == null)return false; (this == that)return true;olean this_present_code = true && this.isSetCode();olean that_present_code = true && that.isSetCode(); (this_present_code || that_present_code) {if (!(this_present_code && that_present_code))  return false;if (!this.code.equals(that.code))  return false;}olean this_present_message = true && this.isSetMessage();olean that_present_message = true && that.isSetMessage(); (this_present_message || that_present_message) {if (!(this_present_message && that_present_message))  return false;if (!this.message.equals(that.message))  return false;}turn true;}@Overrideic int hashCode() {t hashCode = 1;shCode = hashCode * 8191 + ((isSetCode()) ? 131071 : 524287); (isSetCode())hashCode = hashCode * 8191 + code.hashCode();shCode = hashCode * 8191 + ((isSetMessage()) ? 131071 : 524287); (isSetMessage())hashCode = hashCode * 8191 + message.hashCode();turn hashCode;}@Overrideic int compareTo(MyException other) { (!getClass().equals(other.getClass())) {return getClass().getName().compareTo(other.getClass().getName());}t lastComparison = 0;stComparison = java.lang.Boolean.valueOf(isSetCode()).compareTo(other.isSetCode()); (lastComparison != 0) {return lastComparison;} (isSetCode()) {lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.code, other.code);if (lastComparison != 0) {  return lastComparison;}}stComparison = java.lang.Boolean.valueOf(isSetMessage()).compareTo(other.isSetMessage()); (lastComparison != 0) {return lastComparison;} (isSetMessage()) {lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.message, other.message);if (lastComparison != 0) {  return lastComparison;}}turn 0;}ic _Fields fieldForId(int fieldId) {turn _Fields.findByThriftId(fieldId);}ic void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {heme(iprot).read(iprot, this);}ic void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {heme(oprot).write(oprot, this);}@Overrideic java.lang.String toString() {va.lang.StringBuilder sb = new java.lang.StringBuilder("MyException(");olean first = true;.append("code:"); (this.code == null) {sb.append("null");} else {sb.append(this.code);}rst = false; (!first) sb.append(", ");.append("message:"); (this.message == null) {sb.append("null");} else {sb.append(this.message);}rst = false;.append(")");turn sb.toString();}ic void validate() throws org.apache.thrift.TException {// check for required fields// check for sub-struct validity}ate void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {y {write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));} catch (org.apache.thrift.TException te) {throw new java.io.IOException(te);}}ate void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {y {read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));} catch (org.apache.thrift.TException te) {throw new java.io.IOException(te);}}ate static class MyExceptionStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {blic MyExceptionStandardScheme getScheme() {return new MyExceptionStandardScheme();}}ate static class MyExceptionStandardScheme extends org.apache.thrift.scheme.StandardScheme<MyException> {blic void read(org.apache.thrift.protocol.TProtocol iprot, MyException struct) throws org.apache.thrift.TException {org.apache.thrift.protocol.TField schemeField;iprot.readStructBegin();while (true){  schemeField = iprot.readFieldBegin();  if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {     break;  }  switch (schemeField.id) {    case 1: // CODE      if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {        struct.code = iprot.readString();        struct.setCodeIsSet(true);      } else {         org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);      }      break;    case 2: // MESSAGE      if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {        struct.message = iprot.readString();        struct.setMessageIsSet(true);      } else {         org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);      }      break;    default:      org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);  }  iprot.readFieldEnd();}iprot.readStructEnd();// check for required fields of primitive type, which can't be checked in the validate method      struct.validate();    }    public void write(org.apache.thrift.protocol.TProtocol oprot, MyException struct) throws org.apache.thrift.TException {      struct.validate();      oprot.writeStructBegin(STRUCT_DESC);      if (struct.code != null) {        oprot.writeFieldBegin(CODE_FIELD_DESC);        oprot.writeString(struct.code);        oprot.writeFieldEnd();      }      if (struct.message != null) {        oprot.writeFieldBegin(MESSAGE_FIELD_DESC);        oprot.writeString(struct.message);        oprot.writeFieldEnd();      }      oprot.writeFieldStop();      oprot.writeStructEnd();    }  }  private static class MyExceptionTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {    public MyExceptionTupleScheme getScheme() {      return new MyExceptionTupleScheme();    }  }  private static class MyExceptionTupleScheme extends org.apache.thrift.scheme.TupleScheme<MyException> {    @Override    public void write(org.apache.thrift.protocol.TProtocol prot, MyException struct) throws org.apache.thrift.TException {      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;      java.util.BitSet optionals = new java.util.BitSet();      if (struct.isSetCode()) {        optionals.set(0);      }      if (struct.isSetMessage()) {        optionals.set(1);      }      oprot.writeBitSet(optionals, 2);      if (struct.isSetCode()) {        oprot.writeString(struct.code);      }      if (struct.isSetMessage()) {        oprot.writeString(struct.message);      }    }    @Override    public void read(org.apache.thrift.protocol.TProtocol prot, MyException struct) throws org.apache.thrift.TException {      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;      java.util.BitSet incoming = iprot.readBitSet(2);      if (incoming.get(0)) {        struct.code = iprot.readString();        struct.setCodeIsSet(true);      }      if (incoming.get(1)) {        struct.message = iprot.readString();        struct.setMessageIsSet(true);      }    }  }  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();  }}
```

### 容器

编写user.thrift，用于定义一个User类, 会在集合类中使用~

```
ace java com.xxx.tutorial.thrift.entity  /**  User {  :i32 userId,:string name}  
```

创建containerTypes.thrift，用于使用容器类型，包括list、map和set~

```
e "user.thrift"ace java com.xxx.tutorial.rpc.entity ContainerTypes {1: list<string> stringValueList;2: set<string> stringValueSet;3: map<string,string> stringValueMap;4: list<user.User> userList;}
```

根据thrift文件生成java代码~

```
/**e com.xxx.tutorial.rpc.entity;@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.10.0)", date = "2017-06-08") class ContainerTypes implements org.apache.thrift.TBase<ContainerTypes, ContainerTypes._Fields>, java.io.Serializable, Cloneable, Comparable<ContainerTypes> {ate static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ContainerTypes");ate static final org.apache.thrift.protocol.TField STRING_VALUE_LIST_FIELD_DESC = new org.apache.thrift.protocol.TField("stringValueList", org.apache.thrift.protocol.TType.LIST, (short)1);ate static final org.apache.thrift.protocol.TField STRING_VALUE_SET_FIELD_DESC = new org.apache.thrift.protocol.TField("stringValueSet", org.apache.thrift.protocol.TType.SET, (short)2);ate static final org.apache.thrift.protocol.TField STRING_VALUE_MAP_FIELD_DESC = new org.apache.thrift.protocol.TField("stringValueMap", org.apache.thrift.protocol.TType.MAP, (short)3);ate static final org.apache.thrift.protocol.TField USER_LIST_FIELD_DESC = new org.apache.thrift.protocol.TField("userList", org.apache.thrift.protocol.TType.LIST, (short)4);ate static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new ContainerTypesStandardSchemeFactory();ate static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new ContainerTypesTupleSchemeFactory();ic java.util.List<java.lang.String> stringValueList; // requiredic java.util.Set<java.lang.String> stringValueSet; // requiredic java.util.Map<java.lang.String,java.lang.String> stringValueMap; // requiredic java.util.List<com.xxx.tutorial.thrift.entity.User> userList; // required/** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */ic enum _Fields implements org.apache.thrift.TFieldIdEnum {RING_VALUE_LIST((short)1, "stringValueList"),RING_VALUE_SET((short)2, "stringValueSet"),RING_VALUE_MAP((short)3, "stringValueMap"),ER_LIST((short)4, "userList");ivate static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();atic {for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {  byName.put(field.getFieldName(), field);}}/**blic static _Fields findByThriftId(int fieldId) {switch(fieldId) {  case 1: // STRING_VALUE_LIST    return STRING_VALUE_LIST;  case 2: // STRING_VALUE_SET    return STRING_VALUE_SET;  case 3: // STRING_VALUE_MAP    return STRING_VALUE_MAP;  case 4: // USER_LIST    return USER_LIST;  default:    return null;}}... ... }
```

从上述生成的代码可以看出，thrift文件中定义的容器类型转换成Java类型之后，与下图展示的内容一致~

### 服务Service

编写一个exception.thrift, 用于自定义异常类~

```
ace java com.xxx.tutorial.rpc.exceptionion UserNotFoundException {1: string code;2: string message;}
```

编写userService.thrift， 用于服务接口定义~

```
e "user.thrift"e "exception.thrift"ace java com.xxx.tutorial.thrift.service  /**e  UserService {   /**保存用户*/  save(1:user.User user),/**根据name获取用户列表*/ <user.User> findUsersByName(1:string name),/**删除用户*/  deleteByUserId(1:i32 userId) throws (1: exception.UserNotFoundException e)}  
```

生成的UserService代码

```
e com.xxx.tutorial.thrift.service;@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.10.0)", date = "2017-06-08") class UserService {/**ic interface Iface {/**blic boolean save(com.xxx.tutorial.thrift.entity.User user) throws org.apache.thrift.TException;/**blic java.util.List<com.xxx.tutorial.thrift.entity.User> findUsersByName(java.lang.String name) throws org.apache.thrift.TException;/**blic void deleteByUserId(int userId) throws com.xxx.tutorial.rpc.exception.UserNotFoundException, org.apache.thrift.TException;}ic interface AsyncIface {blic void save(com.xxx.tutorial.thrift.entity.User user, org.apache.thrift.async.AsyncMethodCallback<java.lang.Boolean> resultHandler) throws org.apache.thrift.TException;blic void findUsersByName(java.lang.String name, org.apache.thrift.async.AsyncMethodCallback<java.util.List<com.xxx.tutorial.thrift.entity.User>> resultHandler) throws org.apache.thrift.TException;blic void deleteByUserId(int userId, org.apache.thrift.async.AsyncMethodCallback<Void> resultHandler) throws org.apache.thrift.TException;}ic static class Client extends org.apache.thrift.TServiceClient implements Iface {blic static class Factory implements org.apache.thrift.TServiceClientFactory<Client> {public Factory() {}public Client getClient(org.apache.thrift.protocol.TProtocol prot) {  return new Client(prot);}public Client getClient(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot) {  return new Client(iprot, oprot);}}blic Client(org.apache.thrift.protocol.TProtocol prot){super(prot, prot);}blic Client(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot) {super(iprot, oprot);}blic boolean save(com.xxx.tutorial.thrift.entity.User user) throws org.apache.thrift.TException{send_save(user);return recv_save();}blic void send_save(com.xxx.tutorial.thrift.entity.User user) throws org.apache.thrift.TException{save_args args = new save_args();args.setUser(user);sendBase("save", args);}blic boolean recv_save() throws org.apache.thrift.TException{save_result result = new save_result();receiveBase(result, "save");if (result.isSetSuccess()) {  return result.success;}throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "save failed: unknown result");}blic java.util.List<com.xxx.tutorial.thrift.entity.User> findUsersByName(java.lang.String name) throws org.apache.thrift.TException{send_findUsersByName(name);return recv_findUsersByName();}blic void send_findUsersByName(java.lang.String name) throws org.apache.thrift.TException{findUsersByName_args args = new findUsersByName_args();args.setName(name);sendBase("findUsersByName", args);}blic java.util.List<com.xxx.tutorial.thrift.entity.User> recv_findUsersByName() throws org.apache.thrift.TException{findUsersByName_result result = new findUsersByName_result();receiveBase(result, "findUsersByName");if (result.isSetSuccess()) {  return result.success;}throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "findUsersByName failed: unknown result");}blic void deleteByUserId(int userId) throws com.xxx.tutorial.rpc.exception.UserNotFoundException, org.apache.thrift.TException{send_deleteByUserId(userId);recv_deleteByUserId();}blic void send_deleteByUserId(int userId) throws org.apache.thrift.TException{deleteByUserId_args args = new deleteByUserId_args();args.setUserId(userId);sendBase("deleteByUserId", args);}blic void recv_deleteByUserId() throws com.xxx.tutorial.rpc.exception.UserNotFoundException, org.apache.thrift.TException{deleteByUserId_result result = new deleteByUserId_result();receiveBase(result, "deleteByUserId");if (result.e != null) {  throw result.e;}return;}}... ... }
```

## 示例

### 说明

在这个示例中，我们主要在用户接口中定义三个接口：保存用户，根据name获取用户列表以及删除用户，如：

```
/**blic boolean save(com.xxx.tutorial.thrift.entity.User user) throws org.apache.thrift.TException;/**blic java.util.List<com.xxx.tutorial.thrift.entity.User> findUsersByName(java.lang.String name) throws org.apache.thrift.TException;/**blic void deleteByUserId(int userId) throws com.xxx.tutorial.thrift.exception.UserNotFoundException, org.apache.thrift.TException;
```

然后使用多种Server创建方法，Thrift支持的Serer有多种，如TSimpleServer、TThreadPoolServer等~

### **产生代码**

根据thrift文件生成Java代码，这里就不再描述，请参考以前的博文【[一步步完成thrift rpc示例](http://mp.weixin.qq.com/s?__biz=MzAxMTY0Nzg1Mg==&mid=2648941714&idx=1&sn=ae4bb5bdac4773c2cbf8c070d4d4e199&chksm=83aaea91b4dd638707bda31e5277d5b702844efa48162eef9df200bd10e052b584148d4bf861&scene=21#wechat_redirect "一步步完成thrift rpc示例")】

### **接口代码**

将生成的Java代码放入thrift-demo-interface模块~ 如，

### **实现代码**

在thrift-demo-service模块增加**UserService**的实现类~

**UserServiceImpl.java**的内容如下：

```
/**e com.xxx.tutorial.thrift.service.impl; java.util.Arrays; java.util.List; java.util.logging.Logger; org.apache.thrift.TException; com.xxx.tutorial.thrift.entity.User; com.xxx.tutorial.thrift.exception.UserNotFoundException; com.xxx.tutorial.thrift.service.UserService;/** class UserServiceImpl implements UserService.Iface {ate static final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());ic boolean save(User user) throws TException {gger.info("方法save的参数user的内容==>" + user.toString());turn true;}ic List<User> findUsersByName(String name) throws TException {gger.info("方法findUsersByName的参数name的内容==>" + name);turn Arrays.asList(new User(1, "Wang"), new User(2, "Mengjun"));}ic void deleteByUserId(int userId) throws UserNotFoundException, TException {/**gger.info("方法deleteByUserId的参数userId的内容==>" + userId);row new UserNotFoundException("1001", String.format("userId=%d的用户不存在", userId));}}
```

有了实现之后，就可以编写Server端的代码和Client端调用的代码~

### TSimpleServer（阻塞IO）

在thrift-demo-server模块编写服务端代码~

四个步骤创建Server，如：

```
e com.xxx.tutorial.thrift.server; java.util.logging.Logger; org.apache.thrift.TProcessor; org.apache.thrift.protocol.TBinaryProtocol; org.apache.thrift.server.TServer; org.apache.thrift.server.TSimpleServer; org.apache.thrift.transport.TServerSocket; com.xxx.tutorial.thrift.service.UserService; com.xxx.tutorial.thrift.service.impl.UserServiceImpl;/** class TSimpleServerExample {ate static final Logger logger = Logger.getLogger(TSimpleServerExample.class.getName());ate static final int SERVER_PORT = 9123;ic static void main(String[] args) {y {/** * 1. 创建Transport */TServerSocket serverTransport = new TServerSocket(SERVER_PORT);TServer.Args tArgs = new TServer.Args(serverTransport);/** * 2. 为Transport创建Protocol */tArgs.protocolFactory(new TBinaryProtocol.Factory());// tArgs.protocolFactory(new TCompactProtocol.Factory());// tArgs.protocolFactory(new TJSONProtocol.Factory());/** * 3. 为Protocol创建Processor */TProcessor tprocessor = new UserService.Processor<UserService.Iface>(new UserServiceImpl());tArgs.processor(tprocessor);/** * 4. 创建Server并启动 *  * org.apache.thrift.server.TSimpleServer - 简单的单线程服务模型，一般用于测试 */TServer server = new TSimpleServer(tArgs);logger.info("UserService TSimpleServer start ....");server.serve();} catch (Exception e) {logger.severe("Server start error!!!" + e.getLocalizedMessage());e.printStackTrace();}}}
```

启动Server，

```
: Failed to load class "org.slf4j.impl.StaticLoggerBinder".: Defaulting to no-operation (NOP) logger implementation: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.六月 08, 2017 7:03:46 下午 com.xxx.tutorial.thrift.server.TSimpleServerExample main信息: UserService TSimpleServer start ....
```

在thrift-demo-client模块编写客户端代码~

如：

```
e com.xxx.tutorial.thrift.client; java.util.List; java.util.logging.Logger; org.apache.thrift.TException; org.apache.thrift.protocol.TBinaryProtocol; org.apache.thrift.protocol.TProtocol; org.apache.thrift.transport.TSocket; org.apache.thrift.transport.TTransport; org.apache.thrift.transport.TTransportException; com.xxx.tutorial.thrift.entity.User; com.xxx.tutorial.thrift.exception.UserNotFoundException; com.xxx.tutorial.thrift.service.UserService; class UserClient {ate static final Logger logger = Logger.getLogger(UserClient.class.getName());ic static void main(String[] args) {y {TTransport transport = new TSocket("127.0.0.1", 9123);TProtocol protocol = new TBinaryProtocol(transport);UserService.Client client = new UserService.Client(protocol);transport.open();/** * 查询User列表 */List<User> users = client.findUsersByName("wang");logger.info("client.findUsersByName()方法結果 == >" + users);/** * 保存User */boolean isUserSaved = client.save(new User(101, "WMJ"));logger.info("user saved result == > " + isUserSaved);/** * 删除用户 */client.deleteByUserId(1002);transport.close();} catch (TTransportException e) {logger.severe("TTransportException==>" + e.getLocalizedMessage());} catch (UserNotFoundException e) {logger.severe("UserNotFoundException==>" + e.getLocalizedMessage());} catch (TException e) {logger.severe("TException==>" + e.getLocalizedMessage());}}}
```

三个方法的结果都有了~

```
: Failed to load class "org.slf4j.impl.StaticLoggerBinder".: Defaulting to no-operation (NOP) logger implementation: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.ed 1六月 08, 2017 7:06:21 下午 com.xxx.tutorial.thrift.client.UserClient main信息: client.findUsersByName()方法結果 == >[User(userId:1, name:Wang), User(userId:2, name:Mengjun)]ed 2六月 08, 2017 7:06:21 下午 com.xxx.tutorial.thrift.client.UserClient main信息: user saved result == > trueed 3六月 08, 2017 7:06:21 下午 com.xxx.tutorial.thrift.client.UserClient main严重: UserNotFoundException==>userId=1002的用户不存在
```

就这样，阻塞IO的示例就完成了~

### **TThreadPoolServer（多线程阻塞IO）**

服务端代码示例：

```
e com.xxx.tutorial.thrift.server; java.util.logging.Logger; org.apache.thrift.TProcessor; org.apache.thrift.protocol.TBinaryProtocol; org.apache.thrift.server.TServer; org.apache.thrift.server.TThreadPoolServer; org.apache.thrift.transport.TServerSocket; com.xxx.tutorial.thrift.service.UserService; com.xxx.tutorial.thrift.service.impl.UserServiceImpl;/** class TThreadPoolServerExample {ate static final Logger logger = Logger.getLogger(TThreadPoolServerExample.class.getName());ate static final int SERVER_PORT = 9125;ic static void main(String[] args) {y {/** * 1. 创建Transport */TServerSocket serverTransport = new TServerSocket(SERVER_PORT);TThreadPoolServer.Args tArgs = new TThreadPoolServer.Args(serverTransport);/** * 2. 为Transport创建Protocol */tArgs.protocolFactory(new TBinaryProtocol.Factory());// tArgs.protocolFactory(new TCompactProtocol.Factory());// tArgs.protocolFactory(new TJSONProtocol.Factory());/** * 3. 为Protocol创建Processor */TProcessor tprocessor = new UserService.Processor<UserService.Iface>(new UserServiceImpl());tArgs.processor(tprocessor);/** * 4. 创建Server并启动 *  * org.apache.thrift.server.TThreadPoolServer - 简单的单线程服务模型，一般用于测试 */TServer server = new TThreadPoolServer(tArgs);logger.info("UserService TSimpleServer start ....");server.serve();} catch (Exception e) {logger.severe("Server start error!!!" + e.getLocalizedMessage());e.printStackTrace();}}}
```

同样客户端的代码还可以是：

```
e com.xxx.tutorial.thrift.client; java.util.List; java.util.logging.Logger; org.apache.thrift.TException; org.apache.thrift.protocol.TBinaryProtocol; org.apache.thrift.protocol.TProtocol; org.apache.thrift.transport.TSocket; org.apache.thrift.transport.TTransport; org.apache.thrift.transport.TTransportException; com.xxx.tutorial.thrift.entity.User; com.xxx.tutorial.thrift.exception.UserNotFoundException; com.xxx.tutorial.thrift.service.UserService; class UserClient {private static final Logger logger = Logger.getLogger(UserClient.class.getName());public static void main(String[] args) {try {TTransport transport = new TSocket("127.0.0.1", 9123);TProtocol protocol = new TBinaryProtocol(transport);UserService.Client client = new UserService.Client(protocol);transport.open();/*** 查询User列表*/List<User> users = client.findUsersByName("wang");logger.info("client.findUsersByName()方法結果 == >" + users);/*** 保存User*/boolean isUserSaved = client.save(new User(101, "WMJ"));logger.info("user saved result == > " + isUserSaved);/*** 删除用户*/client.deleteByUserId(1002);transport.close();} catch (TTransportException e) {logger.severe("TTransportException==>" + e.getLocalizedMessage());} catch (UserNotFoundException e) {logger.severe("UserNotFoundException==>" + e.getLocalizedMessage());} catch (TException e) {logger.severe("TException==>" + e.getLocalizedMessage());}}}
```

同样调用成功

服务端也打印了方法调用的信息：

```
: Failed to load class "org.slf4j.impl.StaticLoggerBinder".: Defaulting to no-operation (NOP) logger implementation: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.六月 08, 2017 7:43:35 下午 com.xxx.tutorial.thrift.server.TThreadPoolServerExample main信息: UserService TSimpleServer start ....六月 08, 2017 7:43:39 下午 com.xxx.tutorial.thrift.service.impl.UserServiceImpl findUsersByName信息: 方法findUsersByName的参数name的内容==>wang六月 08, 2017 7:43:39 下午 com.xxx.tutorial.thrift.service.impl.UserServiceImpl save信息: 方法save的参数user的内容==>User(userId:101, name:WMJ)六月 08, 2017 7:43:39 下午 com.xxx.tutorial.thrift.service.impl.UserServiceImpl deleteByUserId信息: 方法deleteByUserId的参数userId的内容==>1002
```

### **THsHaServer（多线程 NIO）**

THsHaServer的描述如下：

```
public class THsHaServer extends TNonblockingServer {... ...}
```

> THsHaServer使用了Java NIO channel~ 在这种Server类型下，**一定要使用TFrameTransport**~

服务端代码示例如下：

```
e com.xxx.tutorial.thrift.server; java.util.logging.Logger; org.apache.thrift.TProcessor; org.apache.thrift.protocol.TBinaryProtocol; org.apache.thrift.server.THsHaServer; org.apache.thrift.server.TServer; org.apache.thrift.transport.TFramedTransport; org.apache.thrift.transport.TNonblockingServerSocket; com.xxx.tutorial.thrift.service.UserService; com.xxx.tutorial.thrift.service.impl.UserServiceImpl;/** class THsHaServerExample {private static final Logger logger = Logger.getLogger(THsHaServerExample.class.getName());private static final int SERVER_PORT = 9123;public static void main(String[] args) {try {/*** 1. 创建Transport*///TServerSocket serverTransport = new TServerSocket(SERVER_PORT);TNonblockingServerSocket serverTransport = new TNonblockingServerSocket(SERVER_PORT);THsHaServer.Args tArgs = new THsHaServer.Args(serverTransport);/*** 2. 为Transport创建Protocol*/tArgs.transportFactory(new TFramedTransport.Factory());tArgs.protocolFactory(new TBinaryProtocol.Factory());// tArgs.protocolFactory(new TCompactProtocol.Factory());// tArgs.protocolFactory(new TJSONProtocol.Factory());/*** 3. 为Protocol创建Processor*/TProcessor tprocessor = new UserService.Processor<UserService.Iface>(new UserServiceImpl());tArgs.processor(tprocessor);/*** 4. 创建Server并启动* * org.apache.thrift.server.TSimpleServer - 简单的单线程服务模型，一般用于测试*///TServer server = new TSimpleServer(tArgs);//半同步半异步的服务模型TServer server = new THsHaServer(tArgs);logger.info("UserService TSimpleServer start ....");server.serve();} catch (Exception e) {logger.severe("Server start error!!!" + e.getLocalizedMessage());e.printStackTrace();}}}
```

客户端代码如下：

```
e com.xxx.tutorial.thrift.client; java.util.List; java.util.logging.Logger; org.apache.thrift.TException; org.apache.thrift.protocol.TBinaryProtocol; org.apache.thrift.protocol.TProtocol; org.apache.thrift.transport.TFramedTransport; org.apache.thrift.transport.TSocket; org.apache.thrift.transport.TTransport; org.apache.thrift.transport.TTransportException; com.xxx.tutorial.thrift.entity.User; com.xxx.tutorial.thrift.exception.UserNotFoundException; com.xxx.tutorial.thrift.service.UserService; class UserClient2 {private static final Logger logger = Logger.getLogger(UserClient.class.getName());public static void main(String[] args) {try {TTransport transport = new TFramedTransport(new TSocket("127.0.0.1", 9123, 3000));TProtocol protocol = new TBinaryProtocol(transport);UserService.Client client = new UserService.Client(protocol);transport.open();/*** 查询User列表*/List<User> users = client.findUsersByName("wang");logger.info("client.findUsersByName()方法結果 == >" + users);/*** 保存User*/boolean isUserSaved = client.save(new User(101, "WMJ"));logger.info("user saved result == > " + isUserSaved);/*** 删除用户*/client.deleteByUserId(1002);transport.close();} catch (TTransportException e) {logger.severe("TTransportException==>" + e.getLocalizedMessage());} catch (UserNotFoundException e) {logger.severe("UserNotFoundException==>" + e.getLocalizedMessage());} catch (TException e) {logger.severe("TException==>" + e.getLocalizedMessage());}}}
```

同样，执行结果成功~

```
: Failed to load class "org.slf4j.impl.StaticLoggerBinder".: Defaulting to no-operation (NOP) logger implementation: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.ed 1六月 08, 2017 7:51:12 下午 com.xxx.tutorial.thrift.client.UserClient2 main信息: client.findUsersByName()方法結果 == >[User(userId:1, name:Wang), User(userId:2, name:Mengjun)]ed 2六月 08, 2017 7:51:12 下午 com.xxx.tutorial.thrift.client.UserClient2 main信息: user saved result == > trueed 3六月 08, 2017 7:51:12 下午 com.xxx.tutorial.thrift.client.UserClient2 main严重: UserNotFoundException==>userId=1002的用户不存在
```

## 小结

本教程主要帮助开发人员熟悉Thrift的IDL语法，并给出Java对应的示例，并给出几种不同的Server和Client端调用实现~

**限于篇幅，AsyncIface和AsyncClient等会在后续的博文中补充上去~**

另外，FaceBook也开源了**Nifty**。

> Nifty是facebook公司开源的，基于netty的thrift服务端和客户端实现。 详细资料可以参考Nifty官网【https://github.com/facebook/nifty/】

后续，也可以给出Nifty相关的示例~

## 代码下载

【https://pan.baidu.com/s/1c900r0】

## 参考文献

【1】https://media.readthedocs.org/pdf/thrift-tutorial/latest/thrift-tutorial.pdf

【2】https://diwakergupta.github.io/thrift-missing-guide/thrift.pdf

【3】http://dongxicheng.org/search-engine/thrift-guide/

【4】http://www.micmiu.com/soa/rpc/thrift-sample/

【5】http://blog.csdn.net/ITer\_ZC/article/details/39695187