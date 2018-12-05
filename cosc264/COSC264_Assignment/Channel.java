
 
import java.io.ByteArrayInputStream;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

 
/**
 * @author Cameron Auld and Patrick Laffey
 *
 */
 
public class Channel {
	

	private static Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
	    try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
	         ObjectInput in = new ObjectInputStream(bis)) {
	        return in.readObject();
	    } 
	    
	}
	
	
	private static boolean validAndUniquePorts(List<Integer> ports) {
		
		boolean validAndUnique = true;
		for (int i = 0; i < ports.size(); i++) {
			if ((ports.get(i) < 1024) || (ports.get(i) > 64000)) {
				validAndUnique = false;
				System.out.println("ERROR: All ports must satisfy: 1024 <= x <= 64000");
				
			} else {
				for (int j = i+1; j < ports.size(); j++) {
					if (ports.get(i).equals(ports.get(j))) {
						validAndUnique = false;
						System.out.println("All ports must be unique.");
						break;
						
					}
				}
			}
			if (!validAndUnique) {
				break;
			}
		}
		return validAndUnique;
	}

	
	 private static boolean dropPacketError(double dropChance) {
		 double chance = Math.random();
		 if (chance < dropChance) {
			 return true;
		 }
		 else {
		     return false;
		 }
	}
		 
	private static void dataLenError(Packet packet) {
		double chance = Math.random();
		if (chance < 0.1) {
			int increaseby = Double.valueOf(Math.random() * 10).intValue(); 
			packet.setLength(packet.getLength() + increaseby);
		}
	}




	
	
 
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException, InterruptedException {
		
		String socket_id1 = "sender";
		String socket_id2 = "receiver";
		int magicNo = 18814;
		// input arguments
		
		int port1 = Integer.parseInt(args[0]);
		int port2 = Integer.parseInt(args[1]);
		int port3 = Integer.parseInt(args[2]);
		int port4 = Integer.parseInt(args[3]);
		int port5 = Integer.parseInt(args[4]);
		int port6 = Integer.parseInt(args[5]);
		double P = Double.parseDouble(args[6]);

		List<Integer> ports = new ArrayList<>();
		ports.add(port1);
		ports.add(port2);
		ports.add(port3);
		ports.add(port4);
		ports.add(port5);
		ports.add(port6);
		if (!validAndUniquePorts(ports)) {
			System.exit(1);
		}
		
		
		int totalPacketsReceivedFromSender = 0;
		int totalPacketsReceivedFromReceiver = 0;
		int totalPacketsSentToSender = 0;
		int totalPacketsSentToReceiver = 0;
		
		
		// Selector for incoming sockets
		Selector selector = Selector.open(); 
		
		// Incoming socket from sender
		ServerSocketChannel socketInFromSender = ServerSocketChannel.open();
		InetSocketAddress socketInFromSenderAddress = new InetSocketAddress("localhost", port1);
		socketInFromSender.bind(socketInFromSenderAddress);
		socketInFromSender.configureBlocking(false);
		
		// Incoming socket from receiver
		ServerSocketChannel socketInFromReceiver = ServerSocketChannel.open();
		InetSocketAddress socketInFromReceiverAddress = new InetSocketAddress("localhost", port2);
		socketInFromReceiver.bind(socketInFromReceiverAddress);
		socketInFromReceiver.configureBlocking(false);
		
		// Outgoing sockets
		OutputSocket outputSocketToReceiver = new OutputSocket(port5, port6);
		OutputSocket outputSocketToSender = new OutputSocket(port3, port4);
		

		// Selection key for socket from sender
		int ops1 = socketInFromSender.validOps();
		SelectionKey selectKy1 = socketInFromSender.register(selector, ops1, socket_id1);
		
		// Selection key for socket from receiver
		int ops2 = socketInFromReceiver.validOps();
		SelectionKey selectKy2 = socketInFromReceiver.register(selector, ops2, socket_id2);
		
		
		

		// Infinite loop..
		// Keep server running
		while (true) {
 

			// Selects a set of keys whose corresponding channels are ready for I/O operations
			
			int num = selector.select();
			// If you don't have any activity, loop around and wait
			// again.
			if (num == 0) {
			    continue;
			}
			// token representing the registration of a SelectableChannel with a Selector
			Set<SelectionKey> selectionKey = selector.selectedKeys();
			Iterator<SelectionKey> selectionIterator = selectionKey.iterator();
 
			while (selectionIterator.hasNext()) {
				SelectionKey myKey = selectionIterator.next();
				String socket_id = (String) myKey.attachment();
				
				// Tests whether this key's channel is ready to accept a new socket connection
				if (myKey.isAcceptable()) {
					if (socket_id.equals("sender")) {
						
					
						SocketChannel socketChannelInFromSender = socketInFromSender.accept();
					
						// Adjusts this channel's blocking mode to false
						socketChannelInFromSender.configureBlocking(false);
 
						// Operation-set bit for read operations
						socketChannelInFromSender.register(selector, SelectionKey.OP_READ, socket_id1);
						log("Connection Accepted: " + socketChannelInFromSender.getLocalAddress() + "\n");
					
					} else if (socket_id.equals("receiver")) {
						
						SocketChannel socketChannelInFromReceiver = socketInFromReceiver.accept();
						
						// Adjusts this channel's blocking mode to false
						socketChannelInFromReceiver.configureBlocking(false);
	 
						// Operation-set bit for read operations
						socketChannelInFromReceiver.register(selector, SelectionKey.OP_READ, socket_id2);
						log("Connection Accepted: " + socketChannelInFromReceiver.getLocalAddress() + "\n");		
					}
					
					
					
					
					// Tests whether this key's channel is ready for reading
				} else if (myKey.isReadable()) {
					if (socket_id.equals("sender")) {
						
						Packet packet = null;
						SocketChannel socketChannelInFromSender = (SocketChannel) myKey.channel();
						ByteBuffer socketChannelInFromSenderBuffer = ByteBuffer.allocate(65535);
						socketChannelInFromSender.read(socketChannelInFromSenderBuffer);
						socketChannelInFromSenderBuffer.flip();
						byte[] bytes = new byte[socketChannelInFromSenderBuffer.remaining()];
						socketChannelInFromSenderBuffer.get(bytes, 0, bytes.length);
						totalPacketsReceivedFromSender += 1;
						//
						
						
						try {
						packet = (Packet) convertFromBytes(bytes);
				
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
						if(outputSocketToReceiver.socketOut == null) {
							outputSocketToReceiver = new OutputSocket(port5, port6);
						}
						
						//System.out.println("got packet from sender");
						// Forward packet to receiver
						
						if ((!dropPacketError(P)) && (packet.getMagicnoInt() == magicNo )); {
							dataLenError(packet);
							outputSocketToReceiver.sendPacket(packet);
							//System.out.println("sent packet to receiver");
							totalPacketsSentToReceiver += 1;
						}
					
						
						
							
						
					} else if (socket_id.equals("receiver")) {
					
						Packet packet = null;
						SocketChannel socketChannelInFromReceiver = (SocketChannel) myKey.channel();
						ByteBuffer socketChannelInFromReceiverBuffer = ByteBuffer.allocate(65535);
						socketChannelInFromReceiver.read(socketChannelInFromReceiverBuffer);
						socketChannelInFromReceiverBuffer.flip();
						byte[] bytes = new byte[socketChannelInFromReceiverBuffer.remaining()];
						socketChannelInFromReceiverBuffer.get(bytes, 0, bytes.length);
						totalPacketsReceivedFromReceiver += 1;
						
						
						try {
						packet = (Packet) convertFromBytes(bytes);
		
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						//System.out.println("got packet from receiver");
						// Forward packet to receiver
						if(outputSocketToSender.socketOut == null) {
							outputSocketToSender = new OutputSocket(port3, port4);
						}
						if ((!dropPacketError(P)) && (packet.getMagicnoInt() == magicNo )); {
							dataLenError(packet);
							outputSocketToSender.sendPacket(packet);
							totalPacketsSentToSender += 1;
							//System.out.println("sent packet to sender");
						}
					}
			
				}	
				selectionIterator.remove();
				}
		}
		}
			
				

		
		
		
	
 
	private static void log(String str) {
		System.out.println(str);
	}
}
