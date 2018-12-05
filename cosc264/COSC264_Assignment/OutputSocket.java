
 

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import java.nio.channels.SocketChannel;


 
/**
 * @author Cameron Auld and Patrick Laffey
 *
 */
 
public class OutputSocket {
	
		InetSocketAddress socketOutRemoteAddress = null;
		InetSocketAddress socketOutLocalAddress = null;
		SocketChannel socketOut = null;
		
		int portRemote;
		int portLocal;
		
		public OutputSocket(int port1, int port2) {
	
			portLocal = port1;
			portRemote = port2;
			socketOutRemoteAddress = new InetSocketAddress("localhost", portRemote);
			socketOutLocalAddress = new InetSocketAddress(portLocal);
			
			try {
				
				socketOut = SocketChannel.open(socketOutRemoteAddress);
				socketOut.bind(socketOutLocalAddress);
				socketOut.configureBlocking(false);
				while(!socketOut.isConnected()) {
					socketOut.connect(socketOutRemoteAddress);
				}
			
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			
		}
		public void sendPacket(Packet packet) {
			byte[] bytes = null;
			try {
				bytes = convertToBytes(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			
			try {
				socketOut.write(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			buffer.clear();
			
		}
		private byte[] convertToBytes(Object object) throws IOException {
		    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
		         ObjectOutput out = new ObjectOutputStream(bos)) {
		        out.writeObject(object);
		        return bos.toByteArray();
		    } 
		}
		
		public void closeSocket() {
			
		try {
			socketOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
	