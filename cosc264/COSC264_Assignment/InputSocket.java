
 
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;


 
/**
 * @author Cameron Auld and Patrick Laffey
 *
 */
 
public class InputSocket {
	

		InetSocketAddress socketInLocalAddress = null;
		ServerSocketChannel socketIn = null;
		SocketChannel socketChannelIn = null;
		int portLocal;
		
		public InputSocket(int port1) {
	
			portLocal = port1;
			
			try {
				socketIn = ServerSocketChannel.open();
				socketInLocalAddress = new InetSocketAddress("localhost", portLocal);
				socketIn.bind(socketInLocalAddress);
				socketIn.configureBlocking(true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				socketChannelIn = socketIn.accept();
				socketChannelIn.configureBlocking(true);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			



			
		}
		public Packet receivePacket() throws IOException {
			
			Packet packet = null;

			ByteBuffer socketChannelInBuffer = ByteBuffer.allocate(65535);
			try {
				socketChannelIn.read(socketChannelInBuffer);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			socketChannelInBuffer.flip();
			byte[] bytes = new byte[socketChannelInBuffer.remaining()];
			socketChannelInBuffer.get(bytes, 0, bytes.length);

			
			try {
			packet = (Packet) convertFromBytes(bytes);

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return packet;
		}

		private Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
		    try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		         ObjectInput in = new ObjectInputStream(bis)) {
		        return in.readObject();
		    } 
		    
		}
		public void closeSocket() {
			
		try {
			socketIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
	