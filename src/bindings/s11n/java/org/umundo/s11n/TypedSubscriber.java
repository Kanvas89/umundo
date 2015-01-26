/**
 *  Copyright (C) 2012  Daniel Schreiber
 *  Copyright (C) 2012  Stefan Radomski
 *  Copyright (C) 2013  Dirk Schnelle-Walka
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

package org.umundo.s11n;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.umundo.core.Message;
import org.umundo.core.Receiver;
import org.umundo.core.Subscriber;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.DynamicMessage.Builder;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;

public class TypedSubscriber extends Subscriber {
	private Map<String, Method> deserializerMethods = new HashMap<String, Method>();
	private DeserializingReceiverDecorator decoratedReceiver;

	private boolean autoRegisterTypes = false;
	private Map<String, Object> autoDeserLoadFailed = new HashMap<String, Object>();
	private ExtensionRegistry extensionRegistry;

	class DeserializingReceiverDecorator extends Receiver {
		ITypedReceiver r;

		DeserializingReceiverDecorator(ITypedReceiver r) {
			this.r = r;
		}

		public void receive(Message msg) {
			// just receive the message if there is no serialized object inside
			if (!msg.getMeta().containsKey("um.s11n.type")) {
				r.receiveObject(null, msg);
				return;
			}

			String type = msg.getMeta("um.s11n.type");
			byte[] data = msg.getData();
						
			// try to auto-register the serializer
			if (TypedSubscriber.this.autoRegisterTypes && !TypedSubscriber.this.autoDeserLoadFailed.containsKey(type)
					&& !TypedSubscriber.this.deserializerMethods.containsKey(type)) {
				try {
					Class<?> c = Class.forName(type);
					if (GeneratedMessage.class.isAssignableFrom(c)) {
						TypedSubscriber.this.registerType((Class<? extends GeneratedMessage>) Class.forName(type));
					}
				} catch (ClassNotFoundException e) {
					TypedSubscriber.this.autoDeserLoadFailed.put(type, null);
					TypedSubscriber.this.deserializerMethods.remove(type);
				} catch (SecurityException e) {
					TypedSubscriber.this.autoDeserLoadFailed.put(type, null);
					TypedSubscriber.this.deserializerMethods.remove(type);
				}
			}

			// do we have a suitable deserializer?
			if (TypedSubscriber.this.deserializerMethods.containsKey(type)) {
				Object o = null;
				Method m = TypedSubscriber.this.deserializerMethods.get(type);
				if (m == null) {
					r.receiveObject(null, msg);
					return;
				}
				try {
					o = m.invoke(null, data, extensionRegistry);
				} catch (IllegalArgumentException e) {
					r.receiveObject(null, msg);
					return;
				} catch (IllegalAccessException e) {
					r.receiveObject(null, msg);
					return;
				} catch (InvocationTargetException e) {
					System.out.println("InvocationTargetException");
					e.getCause().printStackTrace();
					r.receiveObject(null, msg);
					return;
				}
				r.receiveObject(o, msg);
			} else if (TypedPublisher.protoDescForMessage(type) != null) {
				try {
					DynamicMessage _protoMsg = DynamicMessage.getDefaultInstance(TypedPublisher.protoDescForMessage(type));
					Builder builder = _protoMsg.toBuilder(); 
					builder.mergeFrom(data, extensionRegistry);
					r.receiveObject(builder.build(), msg);
				} catch (InvalidProtocolBufferException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				System.err.println("Received type " + type + " but no deserializer is known");
				r.receiveObject(null, msg);
			}
		}
	}

	/**
	 * Experimental.
	 * 
	 * @param auto
	 */
	public void setAutoRegisterTypesByReflection(boolean auto) {
		this.autoRegisterTypes = auto;
	}

	public void setExtensionRegistry(ExtensionRegistry registry) {
		extensionRegistry = registry;
	}

	public void registerType(Class<? extends GeneratedMessage> type) throws SecurityException {
		String n = type.getSimpleName();
		try {
			Method m = type.getMethod("parseFrom", byte[].class, ExtensionRegistryLite.class);
			deserializerMethods.put(n, m);
		} catch (NoSuchMethodException e) {
			System.err.println("GeneratedMessage in protobuf no longer has a parseFrom method?");
			e.printStackTrace();
		}
	}

	public TypedSubscriber(String channel, ITypedReceiver receiver) {
		this(channel, receiver, false);
	}

	public TypedSubscriber(String channel, ITypedReceiver receiver, boolean autoRegisterTypes) {
		super(channel);
		decoratedReceiver = new DeserializingReceiverDecorator(receiver);
		setReceiver(decoratedReceiver);
		this.autoRegisterTypes = autoRegisterTypes;
	}
}
