package main;

import java.io.IOException;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws IOException{
		
		//Define necessary variables
		Scanner inint = new Scanner(System.in);
		Scanner inString = new Scanner(System.in);
		
		// Prompt the user for the number of nodes in the network
		System.out.println("Enter the number of nodes you want on your network");
		int number = inint.nextInt();
		int[] ports = new int[number];
		
		// Add the nodes to the network
		for(int i1 = 0; i1< number; i1++) {
			ports[i1] = 8080 + i1;
			int addport = ports[i1];
			Thread node = new Node(addport);
			node.start();
			System.out.println("Node with port number " + addport + " has been added");
		}
		
		// Set static variables in the Node and the Disseminator
		Node.nodecount = ports.length;
		Disseminator.ports = ports;

		// Prepare the message
		byte[] message = new byte[100];			
		for(int i2 = 0; i2 < 100; i2++) {
			message[i2] = (byte) i2;
		}
		
		Thread th1 = new Disseminator(message, 0);
		th1.start();
		
		// Close the scanners
		inint.close();
		inString.close();
	}
}