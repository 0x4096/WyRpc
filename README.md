
## 项目说明
使用 Sring Boot,Netty,Zookeeper,Protostuff 实现简单的 RPC 框架。
学习 dubbo 的方式,使用注解提供,发现服务。

## 存在问题
目前还存在两个严重问题
1. 服务端服务进行服务调用类转换异常(spring 注入问题,目前无法发起请求到服务提供者...这个问题好多天过去了,也没解决...)
2. Netty 第一次发送后开始出现异常(@ChannelHandler.Sharable可以解决)

## 优化
1. Netty 长连接

## 心得
1. 不会 Netty 的程序员,真的很渣
2. 看看 dubbo 源码,应该会更好玩(目前只是使用阶段...)
3. 反射,还可以更深的了解

## 参考学习
1. https://github.com/taoxun/simple_rpc
