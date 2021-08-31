import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.lang.Integer;

/*
* COMP 4320 Project 1:
* Implementation of a Simple Web Service over the UDP Transport Service
* @authors Noah Henry, Jordaan Rambus, Hannah Smith
*
 */

public class UDPClient {
    public static void main(String args[]) throws Exception {

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        DatagramSocket clientSocket = new DatagramSocket();
        //put your ip address here
        InetAddress IPAddress = InetAddress.getByName("");
        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];
        int last = 1;
        boolean lastPacket = false;
        
        String f = "";

        Double prob = null;
        System.out.println("Enter Gremlin Probability: ");
        System.out.println("Enter Lost Probability: ");
        prob = Double.parseDouble(inFromUser.readLine());

        System.out.println("Enter HTTP request: ");
        String sentence = inFromUser.readLine();
        String[] s = sentence.split(" ");
        if (s.length != 3) {
            System.out.println("Not a valid HTTP request.");
        } 
        else {
            System.out.println("Sending HTTP request.....");
            sendData = sentence.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 10007);
            clientSocket.send(sendPacket);
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            String modifiedSentence = new String(receivePacket.getData());
            System.out.println("Packets arriving...");

            while (lastPacket == false) {
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(packet);
                byte[] b = packet.getData();
                String a = new String(b, StandardCharsets.US_ASCII);
                DatagramPacket prePacket = packet;
                packet = gremlin(packet, prob);
                errorDetection(packet, prePacket);
                //System.out.println(a);
                f = f + a;
                System.out.println("Packet number " + last + " received");
                last++;
                if (packet.getData() == null) {
                    System.out.println("Last packed has arrived");
                    lastPacket = true;

                }
            }
            clientSocket.close();

            //GET TestFile.html HTTP/1.0
        }
    }

    static DatagramPacket gremlin(DatagramPacket packet, double probability) {
        double rand = Math.random();
        byte[] origArray = packet.getData();
        byte[] tempArray = new byte[origArray.length];
        int countTest = 0;
        if (rand <= probability) {
            for (int i = 0; i < origArray.length; i++) {
                double rand2 = Math.random();
                if (rand2 <= 0.5) {
                    origArray[i]++;
                    countTest++;
                }
                tempArray[i] = origArray[i];
            }
            System.out.println("Packet altered " + countTest + " bytes");
        }
        DatagramPacket newPacket = new DatagramPacket(tempArray, tempArray.length);
        return newPacket;

    } 

    static int errorDetection(DatagramPacket postGrem, DatagramPacket preGrem) {
        byte[] preGremArray = preGrem.getData();
        byte[] postGremArray = postGrem.getData();
        byte firstChecksum = preGremArray[preGremArray.length - 1];
        Integer secondChecksum = 0;
        //System.out.println(postGremArray.length);
        for (int i = 0; i < postGremArray.length; i++) {
            secondChecksum += postGremArray[i];
        }
        byte secondChecksum2 = secondChecksum.byteValue();
        //altered -> 1, non-altered -> 2
        if (firstChecksum != secondChecksum2){
            //possible change later if checksum needs to be edited
            System.out.println("Package was changed by gremlin.");
            return 1;

        }
        else {
            System.out.println("Package was not changed by gremlin.");
            return 2;
        }

    }
    
}
