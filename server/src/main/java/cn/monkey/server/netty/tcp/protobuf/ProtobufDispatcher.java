package cn.monkey.server.netty.tcp.protobuf;

import cn.monkey.proto.Command;
import cn.monkey.server.Dispatcher;

public interface ProtobufDispatcher extends Dispatcher<Command.Package> {
}
