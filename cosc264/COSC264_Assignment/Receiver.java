
 

import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;



 
/**
 * @author Cameron Auld and Patrick Laffey
 *
 */
public class Receiver {
	
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
		
	private static void writeOutput(File output, String data) {
		try {
			FileWriter writer = new FileWriter(output, true);
			writer.write(data);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void createOutput(File output, String data) {
		File outputFile = output;
		if (!output.getAbsolutePath().endsWith(".txt")) {
			outputFile = new File(output.getAbsolutePath() + ".txt");
		}
		try {
			outputFile.createNewFile();
			writeOutput(outputFile, data);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	@SuppressWarnings("unused")
	private static void newOutput(String data, String location) {
		Scanner talker = new Scanner(System.in);
		while (!outputLocationCorrect(location, talker)) {
			System.out.println("Enter new location for output file: ");
			location = talker.nextLine();
		}
		talker.close();
		File output = new File(location);
		if (output.isFile()) {
			writeOutput(output, data);
		}
		else {
			createOutput(output, data);
		}
			
	}
	
	private static boolean outputLocationCorrect(String location, Scanner talker) {
		String answer = new String();
		boolean done = false;
		File temp = new File(location);
		if (temp.exists()) {
			if (temp.isDirectory()) {
				while (!done) {
					System.out.println("Location " + location + " is a dirrectory. Would you like to create a text file by the same name?");
					answer = talker.next();
					if (answer.toLowerCase().equals("yes")) {
						done = true;
						return true;
					}
					else if (answer.toLowerCase().equals("no")) {
						done = true;
						return false;
					}
					else {
						System.out.println("Please answer 'Yes' or 'No'.");
					}
				}
			}
			else if (temp.isFile()) {
				while (!done) {
					System.out.println("File at location " + location + " already exisits. Would you like to write to this file?");					
					answer = talker.next();
					if (answer.toLowerCase().equals("yes")) {
						done = true;
						return true;
					}
					else if (answer.toLowerCase().equals("no")) {
						done = true;
						return false;
					}
					else {
						System.out.println("Please answer 'Yes' or 'No'.");
					}
				}
			}
			else {
				System.out.println("Unknown item at location " + location + " already exisits and cannot be used.");
				return false;
			}
		}
		return true;
	}

	
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException, InterruptedException {
		
		
		int magicNo = 18814;
		int expected = 0;
		int port1 = Integer.parseInt(args[0]);
		int port2 = Integer.parseInt(args[1]);
		int port3 = Integer.parseInt(args[2]);
		String outputFileName = args[3];
		File outputFile = new File(outputFileName);
		
		List<Integer> ports = new ArrayList<>();
		ports.add(port1);
		ports.add(port2);
		ports.add(port3);
		if (!validAndUniquePorts(ports)) {
			System.exit(1);
		}
		
		
		
		
		InputSocket inputSocket = new InputSocket(port3);
		OutputSocket outputSocket = new OutputSocket(port1,port2);
		//newOutput("", outputFileName);
		createOutput(outputFile, "");
		
		
		boolean exitFlag = false;
		
		// main loop
		while (!exitFlag) {
				Packet packet = null;
				packet = inputSocket.receivePacket();
				
				//System.out.print(packet);
				//System.out.println("");
				
				if ((packet.getMagicnoInt() == magicNo) && (packet.getType() == 0) && (packet.getSeqNo() == expected) && (packet.verifyCheckSum())){
					
					Packet acknowledgementPacket = new Packet(1, packet.getSeqNo());
					acknowledgementPacket.setChecksum();
					if(outputSocket.socketOut == null) {
						outputSocket = new OutputSocket(port1, port2);
					}
					outputSocket.sendPacket(acknowledgementPacket);
					expected = 1 - expected;
					//System.out.println("sent acknowledgement");
					if (packet.getLength() > 0) {
						//newOutput(packet.getData(), outputFileName);
						writeOutput(outputFile, packet.getData());
					} else {	//transmission complete close file, sockets exit
						exitFlag = true;
					}	
					
				} else if ((packet.getMagicnoInt() == magicNo) && (packet.getType() == 0) && (packet.verifyCheckSum())) {
					// acknowledgement packet seqNo wrong
					Packet acknowledgementPacket = new Packet(1, packet.getSeqNo());
					acknowledgementPacket.setChecksum();
					if(outputSocket.socketOut == null) {
						outputSocket = new OutputSocket(port1, port2);
					}
					//System.out.println("sent acknowledgement");
					outputSocket.sendPacket(acknowledgementPacket);
				}  
				
				
		}
		//close the output file and all sockets
		// exit, all finished
		}
		
	}


