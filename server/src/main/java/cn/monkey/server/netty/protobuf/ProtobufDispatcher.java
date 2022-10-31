package cn.monkey.server.netty.protobuf;

import cn.monkey.proto.Command;
import cn.monkey.server.Dispatcher;

public interface ProtobufDispatcher extends Dispatcher<Command.Package> {
}
