package cn.monkey.server.netty.tcp.protobuf;

import cn.monkey.proto.Command;
import cn.monkey.server.Filter;

public interface ProtobufFilter extends Filter<Command.Package> {
}
