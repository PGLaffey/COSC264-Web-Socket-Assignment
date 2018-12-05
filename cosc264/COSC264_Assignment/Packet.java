import java.io.Serializable;  
import java.util.zip.Adler32;

public class Packet implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3372401161917693082L;
	private int type;
	private String magicnoStr;
	private int magicnoInt;
	private int seqno;
	private String data;
	private int dataLen;
	private int checkSum;
	
	public Packet(int packetType,int seqNO){
		type = packetType; //0 = dataPacket and 1 = acknowledgementPacket
		magicnoStr = "0x497E";
		magicnoInt = Integer.parseInt(magicnoStr.substring(2, 6), 16);
		
		dataLen = 0;
		seqno = seqNO;
		checkSum = 0;
		data = "";
	}
	@Override
	public String toString() {
		return "Packet [type=" + type + ", magicnoStr=" + magicnoStr + ", magicnoInt=" + magicnoInt + ", seqno=" + seqno
				+ ", data=" + data + ", dataLen=" + dataLen + "]";
	}
	
	public int calculateChecksum() {
		String header = getHeader();
		Adler32 checksum = new Adler32();
		checksum.update(header.getBytes());
		return (int) checksum.getValue();
	}
	public void setChecksum() {
		int checksum = calculateChecksum();
		checkSum = checksum;
	}
	public int getChecksum() {
		return checkSum;
	}
	public boolean verifyCheckSum() {
		boolean isValid = false;
		if (getChecksum() == (calculateChecksum())) {
			isValid = true;
		}
		return isValid;
		
	}
	
	public String getHeader() {
		return "" + type + magicnoStr + seqno + dataLen + "";
	}
	
	public void setData(String dataIn){
		int totalLen = dataIn.length();
		if (totalLen > 512){
			System.out.println("Error Packet Too Large: Max packet size is 512 bytes,"
					+ " you tried to create packet of size " + totalLen
					+ " bytes.");
		}
		else {
			data = dataIn;
			dataLen = totalLen;
		}
	}
	
	public String getData(){
		return data;
	}
	
	public String getMagicnoStr(){
		return magicnoStr;
	}
	
	public int getMagicnoInt(){
		return magicnoInt;
	}
	
	public int getLength(){
		return dataLen;
	}
	
	public int getType() {
		return type;
	}
	public int getSeqNo() {
		return seqno;
	}
	public int getCheckSum() {
		return checkSum;
	}

	public void setLength(int length) {
		dataLen = length;
    }

}
