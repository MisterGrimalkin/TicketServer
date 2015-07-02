#!/usr/bin/env bash
java \
-Djava.library.path=../LightBoard/native/libpi4j.so \
-cp "/home/pi/lightboard/lib/*:/home/pi/ticketserver/lib/*:/home/pi/ticketserver/target/classes" \
net.amarantha.ticketserver.Main $*
