/**
 *  Copyright (C) 2012  Stefan Radomski (stefan.radomski@cs.tu-darmstadt.de)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the FreeBSD license as published by the FreeBSD
 *  project.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  You should have received a copy of the FreeBSD license along with this
 *  program. If not, see <http://www.opensource.org/licenses/bsd-license>.
 */

#include "umundo/config.h"
#include "umundo/core.h"
#include <iostream>
#include <string.h>

using namespace umundo;

int main(int argc, char** argv) {
	printf("umundo-ping version " UMUNDO_VERSION " (" CMAKE_BUILD_TYPE " build)\n");
	Publisher pubFoo("ping");
	
	Discovery disc(Discovery::MDNS);
	Node node;
	disc.add(node);
	node.addPublisher(pubFoo);
	
	while(1) {
		Thread::sleepMs(1000);
		Message* msg = new Message();
		msg->setData("ping", 4);
		std::cout << "o" << std::flush;
		pubFoo.send(msg);
		delete(msg);
	}
}
