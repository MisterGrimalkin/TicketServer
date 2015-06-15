#!/usr/bin/env bash
java \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.port=8855 \
-Dcom.sun.management.jmxremote.local.only=false \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=false \
-Djava.library.path=../LightBoard/native/libpi4j.so \
-cp "../LightBoard/lib/*:lib/*:target/classes" \
net.amarantha.ticketserver.Main $*
