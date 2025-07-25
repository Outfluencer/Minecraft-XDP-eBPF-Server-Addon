package dev.outfluencer.xdpBungeecord;

import dev.outfluencer.aya.mappings.Mappings;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.TimeoutException;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.protocol.channel.BungeeChannelInitializer;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.logging.Level;

public final class XdpBungeecord extends Plugin {

    @Override
    public void onEnable() {
        if (!LibLoader.load()) {
            System.err.println("Could not load jni lib");
            return;
        }

        BungeeChannelInitializer wrap = ProxyServer.getInstance().unsafe().getFrontendChannelInitializer();
        ProxyServer.getInstance().unsafe().setFrontendChannelInitializer(BungeeChannelInitializer.create(channel -> {
            if (!wrap.getChannelAcceptor().accept(channel)) {
                return false;
            }
            channel.pipeline().addBefore("inbound-boss", "exception-handler", new ExceptionHandler());
            return true;
        }));
    }

    public static class ExceptionHandler extends ChannelInboundHandlerAdapter {

        boolean blocked = false;

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            if (blocked || cause instanceof TimeoutException) return;
            SocketAddress address = ctx.channel().remoteAddress();
            blocked = true;
            if (address instanceof InetSocketAddress) {
                InetSocketAddress inets = (InetSocketAddress) address;
                InetAddress inet = inets.getAddress();
                if (inet instanceof Inet4Address) {
                    blockIp((Inet4Address) inet);
                }
            }
        }
    }

    public static void blockIp(Inet4Address address) {
        ProxyServer.getInstance().getLogger().log(Level.WARNING, "Blocked ip address {0}", address);
        Mappings.blockIp(ByteBuffer.wrap(address.getAddress()).getInt());
    }

}
