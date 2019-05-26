package com.wy.rpc.client.netty;

import com.wy.rpc.client.connent.ConnectManage;
import com.wy.rpc.client.handler.RpcClientHandler;
import com.wy.rpc.common.code.decode.RpcDecoder;
import com.wy.rpc.common.code.encode.RpcEncoder;
import com.wy.rpc.common.request.RpcRequest;
import com.wy.rpc.common.response.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.net.SocketAddress;
import java.util.concurrent.SynchronousQueue;

/**
 * @Author: peng.zhup
 * @Project: wy-rpc
 * @DateTime: 2019/5/16 22:50
 * @Description:
 */
@Component
public class NettyClient  {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClient.class);

    private EventLoopGroup group = new NioEventLoopGroup(1);
    private Bootstrap bootstrap = new Bootstrap();

    @Autowired
    private RpcClientHandler rpcClientHandler;

    @Autowired
    private ConnectManage connectManage;


    public NettyClient(){
        bootstrap.group(group).
                channel(NioSocketChannel.class).
                option(ChannelOption.TCP_NODELAY, true).
                option(ChannelOption.SO_KEEPALIVE,true).
                handler(new ChannelInitializer<SocketChannel>() {
                    //创建NIOSocketChannel成功后，在进行初始化时，将它的ChannelHandler设置到ChannelPipeline中，用于处理网络IO事件
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, 0, 30));
                        pipeline.addLast(new RpcEncoder(RpcRequest.class));
                        pipeline.addLast(new RpcDecoder(RpcRequest.class));
                        pipeline.addLast("handler", rpcClientHandler);
                    }
                });
        LOGGER.info("init netty-client");
    }

    @PreDestroy
    public void destroy(){
        LOGGER.info("RPC客户端退出,释放资源!");
        group.shutdownGracefully();
    }

    public Object send(RpcRequest request) throws InterruptedException{
        Channel channel = connectManage.chooseChannel();
        if (channel!=null && channel.isActive()) {
            SynchronousQueue<Object> queue = rpcClientHandler.sendRequest(request,channel);
            Object result = queue.take();
            return result;
        }else{
            RpcResponse res = new RpcResponse();
            return null;
        }
    }


    public Channel doConnect(SocketAddress address) throws InterruptedException {
        ChannelFuture future = bootstrap.connect(address);
        Channel channel = future.sync().channel();
        return channel;
    }

}
