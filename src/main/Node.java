package main;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.arni.ncutils.CodedPacket;
import ch.epfl.arni.ncutils.PacketDecoder;
import ch.epfl.arni.ncutils.UncodedPacket;

public class Node extends Thread{
	
	ArrayList<byte[]> messages = new ArrayList<byte[]>();
    int port;
    DatagramSocket server;
    static int nodecount;
    
    /** The constructor to set the server at the specified port and on localhost
	 * 
	 * @param port
	 * 		The port to set the server on
	 */
	Node(int port) throws IOException{
		this.port = port;
		this.server = new DatagramSocket(this.port);
	}
	
	 @Override
	public void run() {
	    try {
	    	int count = 0;
	    		while(true) {
	    			
	    			  // Set the necessary variables for decoding.
	    			  PacketDecoder decoder = new PacketDecoder(Disseminator.ff, Disseminator.generationSize, Disseminator.payloadLen);
	    			  byte[] buf1 = new byte[Disseminator.bufferSize];
	    			  byte[] buf2 = {1};
	    			  byte[] message = new byte[Disseminator.payloadLen*Disseminator.generationSize];
	    			  DatagramPacket packet = new DatagramPacket(buf1, buf1.length);
	    			  
	        		  // Receive the packets individually
	      	          while(decoder.getSubspaceSize() != 10){
	      	        	  buf1 = new byte[Disseminator.bufferSize];
	      	        	  packet = new DatagramPacket(buf1, buf1.length);
	      	        	  this.server.receive(packet);
	      	         	  ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buf1));
	      	              List<UncodedPacket> packets = decoder.addPacket((CodedPacket) ois.readObject());
	      	              
	      	              // Decipher the message
	      	              for(UncodedPacket p : packets) {
	      	            	  byte[] temp = p.getPayload();
	      	            	  for(int i1 = 0; i1 < temp.length; i1++) {
	      	            		  message[(10*p.getId()) + i1] = temp[i1];
	      	            	  }
	      	              }
	      	          }         	      	         
	      	          
	      	          this.messages.add(message);
	      	          this.server.send(new DatagramPacket(buf2, buf2.length, packet.getAddress(), packet.getPort()));
	      	          
	      	          // Send the verification wave for Byzantine Fault Tolerance
	                  if(count%Disseminator.ports.length==0) {
	                       Thread th1 = new Disseminator(message, this.port);
	                       th1.start();
	                  }
	                  count++;
	    		}     
	      } catch (Exception e) {
				e.printStackTrace();	
		  }
	 }
}