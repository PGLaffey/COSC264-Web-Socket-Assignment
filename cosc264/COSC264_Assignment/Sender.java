
 
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

 
/**
 * @author Cameron Auld 
 * @author Patrick Laffey
 *
 */
 
@SuppressWarnings("unused")
public class Sender {
	
	private static int packetSize = 512;	
	
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
	
	private static void log(String str) {
		System.out.println(str);
	}
	
	private static Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
	    try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
	         ObjectInput in = new ObjectInputStream(bis)) {
	        return in.readObject();
	    } 
	    
	}
	
	public void setPacketSize(int size){
		if (size <= 0){
			System.out.println("Packet payload size must be at least 1 byte.");
		}
		else if (size > 512){
			System.out.println("Packet payload size must be less than 512 bytes.");
		}
		else {
			packetSize = size;
			System.out.println("Packet payload size set to " + size + " byte(s).");
		}
		
	}

	
	private static String readData(FileReader reader){
		char[] packetData = new char[packetSize];
		try {
			if (reader.read(packetData, 0, packetSize) == -1) {
				return null;
			}
			return String.valueOf(packetData);
		} 
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Packet makePacket(String packetData, int seqNo){
		Packet data = new Packet(0, seqNo);
		data.setData(packetData);
		data.setChecksum();
		return data;
	}
	
	private static ArrayList<Packet> readFile(String dir){
		String packetData = new String();
		ArrayList<Packet> packets = new ArrayList<Packet>();
		int seqNo = 0;
		try {
			FileReader reader = new FileReader(dir);
			packetData = readData(reader);
			while (packetData != null) {
				packets.add(makePacket(packetData, seqNo));
				seqNo = 1 - seqNo;
				packetData = readData(reader);
			}
			return packets;
		} 
		catch (FileNotFoundException e) {
			System.out.println("File " + dir + " not found./n" + e);
			return null;
		}
	}
	
	
//	public void readFile(String dir){
//	try {
//		BufferedReader reader = new BufferedReader(new FileReader(dir));
//		int numPackets = getNumPackets(dir);
//		int remaining = new Integer(numPackets);
//		Packet[] packets = new Packet[numPackets];
//		while (remaining > 1){
//			char[] data = new char[packetSize];
//			reader.read(data, 0, packetSize);
//			packets[remaining - 1] = new Packet(0);
//			packets[remaining - 1].setData(data.toString());
//			remaining -= 1;				
//		}
//		ArrayList<Character> finalPacketData = new ArrayList<Character>();
//		int charact = reader.read();
//		while (charact != -1){
//			finalPacketData.add(((char) charact));
//			charact = reader.read();
//		}
//		packets[0] = new Packet(0);
//		packets[0].setData(dataIn);;
//		System.out.println(finalPacketData.size());
//		int count = 0;
//		while (count < numPackets){
//			System.out.println(packets[count].getData().length());
//			count += 1;
//		}
//		reader.close();
//	} catch (FileNotFoundException e) {
//		System.out.println("File Error: File not found.");
//		e.printStackTrace();
//	} catch (IOException e) {
//		System.out.println("IO Error: Error reading the input file.");
//		e.printStackTrace();
//	}
//}

	
//	private int getNumPackets(String dir){
//	Scanner findLen;
//	try {
//		findLen = new Scanner(new FileReader(dir));
//		String line = "";
//		int totalLen = 0;
//		while (findLen.hasNextLine()){
//			line = findLen.nextLine();
//			totalLen = totalLen + line.length() + 2;
//		}
//		findLen.close();
//		return ((totalLen - 2) / packetSize) + 1;
//	} catch (FileNotFoundException e) {
//		System.out.println("File Error: File not found at dirrectory " + dir);
//		e.printStackTrace();
//		return 0;
//	}
//}

	
	
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException, InterruptedException {
		

		// input arguments
		
		int port1 = Integer.parseInt(args[0]);
		int port2 = Integer.parseInt(args[1]);
		int port3 = Integer.parseInt(args[2]);
		List<Integer> ports = new ArrayList<>();
		ports.add(port1);
		ports.add(port2);
		ports.add(port3);
		if (!validAndUniquePorts(ports)) {
			System.exit(1);
		}
		
		String filename = args[3];
		int magicNo = 18814;
		int totalPacketsSent = 0;
		ArrayList<Packet> packets = null;
		
		// check port numbers are valid and unique
		
		//
		//
		packets = readFile(filename);
		
		
		//
		//
		
		// Selector for incoming sockets
		Selector selector = Selector.open(); 
		
		// Incoming socket from sender
		ServerSocketChannel socketIn = ServerSocketChannel.open();
		InetSocketAddress socketInAddress = new InetSocketAddress("localhost", port3);
		socketIn.bind(socketInAddress);
		socketIn.configureBlocking(false);
		
		// Outgoing sockets
		OutputSocket outputSocket = new OutputSocket(port1, port2);

		// Selection key for socket from sender
		int ops1 = socketIn.validOps();
		SelectionKey selectKy1 = socketIn.register(selector, ops1);
		
 
		boolean exitFlag = false;
		boolean receivedAcknowledgement = false;
		int expected = 0;
		int totalPacketsReceived = 0;

		// outer loop stops when end of file reached 
		while (!exitFlag) {
 
			Packet packet = null;
			
			
			//TESTING
			for (int i = 0; i <= packets.size(); i++) {
				receivedAcknowledgement = false;
				
				if (i == packets.size()) {
					//done exit!
					packet = new Packet(1, expected);
					exitFlag = true;
					receivedAcknowledgement = true;
				} else {
					packet = packets.get(i);
				}
			
				
			while(!receivedAcknowledgement)	{
			
				// Forward packet to receiver
				// Forward packet to receiver
				if(outputSocket.socketOut == null) {
					outputSocket = new OutputSocket(port1, port2);
				}
				outputSocket.sendPacket(packet);
				totalPacketsSent += 1;
				// Selects a set of keys whose corresponding channels are ready for I/O operations
				
				
				int num = selector.select(1000);
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
					
						
					
						SocketChannel socketChannelIn = socketIn.accept();
					
						// Adjusts this channel's blocking mode to false
						socketChannelIn.configureBlocking(false);
 
						// Operation-set bit for read operations
						socketChannelIn.register(selector, SelectionKey.OP_READ);
						log("Connection Accepted: " + socketChannelIn.getLocalAddress() + "\n");
					
					 
					
					
					
					
					// Tests whether this key's channel is ready for reading
					} else if (myKey.isReadable()) {
						
						Packet packetReceived = null;
						SocketChannel socketChannelIn = (SocketChannel) myKey.channel();
						ByteBuffer socketChannelInBuffer = ByteBuffer.allocate(65535);
						socketChannelIn.read(socketChannelInBuffer);
						socketChannelInBuffer.flip();
						byte[] bytes = new byte[socketChannelInBuffer.remaining()];
						socketChannelInBuffer.get(bytes, 0, bytes.length);
						totalPacketsReceived += 1;
						//socketChannelInBuffer.flip();
						
						try {
							packetReceived = (Packet) convertFromBytes(bytes);
							
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						//System.out.println("received something");
						if ((packetReceived.getMagicnoInt() == magicNo) && (packetReceived.getType() == 1) && (packetReceived.getSeqNo() == expected)
								&& (packetReceived.getLength() == 0) && (packetReceived.verifyCheckSum())){
							//System.out.println("received acknowledgement");
							expected = 1 - expected;
							receivedAcknowledgement = true;
						}
						
						
					}
				selectionIterator.remove();
					
				}
			
				
			}
			}//TEST
		}
		// close everything
		System.out.println(totalPacketsSent);
		System.out.println(totalPacketsReceived);
		}
}
