package main;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

import ch.epfl.arni.ncutils.CodedPacket;
import ch.epfl.arni.ncutils.FiniteField;
import ch.epfl.arni.ncutils.UncodedPacket;

public class Disseminator extends Thread{
	
	static int[] ports;
	byte[] message;
	int noBroadCast;
	static FiniteField ff = FiniteField.getDefaultFiniteField();
	static int generationSize = 10, payloadLen = 10, bufferSize = 5371;
	CodedPacket[] codewords;
	
	/** The constructor to set the message parameter to be distributed among all nodes except the noBroadCase
	 * 
	 * @param message
	 * 		The message to be send to all nodes except noBroadCast
	 * @param noBroadCast
	 * 		The only port number that will not receive the message.
	 */
	Disseminator(byte[] message, int noBroadCast){
		this.message = message;
		this.noBroadCast = noBroadCast;
		this.codewords = new CodedPacket[Disseminator.generationSize];
	}
	
	@Override
	public void run(){
		
		try {
			
			//Encode the message to packets
			for ( int i1 = 0 ; i1 < Disseminator.generationSize ; i1++) {
	        	byte[] payload = Arrays.copyOfRange(message,i1*payloadLen,(i1+1)*payloadLen);
	            codewords[i1] = new CodedPacket( new UncodedPacket(i1, payload), generationSize, ff);
	        }
			
		    DatagramSocket socket = new DatagramSocket();
		
		    // Iterate through all node on the local network
			for(int i2 = 0;i2<ports.length;i2++) {
				
				if(ports[i2] == this.noBroadCast) {
					continue;
				}	
				
				byte[] buf1 = {0};
				while(buf1[0]!=1) {
				
				    // Send the elements of the array individually
					for(int i3 = 0; i3 < Disseminator.generationSize; i3++) {
						ByteArrayOutputStream baos = new ByteArrayOutputStream(this.codewords[i3].toByteArray().length);
						ObjectOutputStream oos = new ObjectOutputStream(baos);
						oos.writeObject(this.codewords[i3]);
						oos.flush();
						socket.send(new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, InetAddress.getByName("localhost"), Disseminator.ports[i2]));
						oos.close();
					}
					
					// Receive confirmation in 1 second
					socket.setSoTimeout(1000);
			    	try {
			    		socket.receive(new DatagramPacket(buf1, buf1.length));
			    	}catch(Exception e) {
			    		System.out.println("Confirmation failed");
			    	}
				}
			} 
			socket.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}