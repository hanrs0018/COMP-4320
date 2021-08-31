import java.io.*;
import java.nio.charset.StandardCharsets;
import java.net.*;
import java.util.Scanner;
import java.util.Arrays;

/*
 * COMP 4320 Project 1:
 * Implementation of a Simple Web Service over the UDP Transport Service
 * @authors Noah Henry, Jordaan Rambus, Hannah Smith
 *
 */
public class UDPServer {

    public static void main(String args[]) throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(10007);
        System.out.println("Connected to UDP Server!");
        byte[] receiveData = new byte[1024];
        byte[] sendData  = new byte[1024];
        Scanner scanner;
        String file = "";

        while(true)         {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            System.out.println("Received an HTTP request!");

            String sentence = new String(receivePacket.getData());
            scanner  = new Scanner(sentence);
            scanner.next();
            sentence = scanner.next();
            scanner.close();

            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            scanner = new Scanner(new File(sentence));

            while (scanner.hasNext()) {
                file += scanner.nextLine();
            }
            System.out.println("File retrieved and opened!");
            long length = file.length();
            //String editFile = file.replace("   ", "\n");

            scanner.close();


            byte[] segmented = file.getBytes(StandardCharsets.UTF_8);
            int begin = 0;
            int end = 128;
            int num = 1;
            for (int i = 0; i < segmented.length/128; i++) {
                sendData = Arrays.copyOfRange(segmented, begin, end);
                if (num == 1) {
                    String s = new String(sendData, StandardCharsets.US_ASCII);
                    String withHeader = ("HTTP/1.0 200 Document Follows\r\n" + "Content-Type: text/plain\r\n" +
                            "Content-Length: " + length + "\r\n" + "\r\n" + s);
                    //System.out.println(withHeader);
                    sendData = withHeader.getBytes(StandardCharsets.UTF_8);
                    byte checkSum = calculateChecksum(sendData);
                    //sendData = Arrays.copyOf(sendData, sendData.length + 1);
                    sendData[sendData.length - 1] = checkSum;
                }
                else {
                    String s = new String(sendData, StandardCharsets.US_ASCII);
                    String header = ("Sequence number is " + num + "\n");
                    //System.out.println(header);
                    sendData = header.getBytes(StandardCharsets.UTF_8);
                    byte checkSum = calculateChecksum(sendData);
                    //sendData = Arrays.copyOf(sendData, sendData.length + 1);
                    sendData[sendData.length - 1] = checkSum;
                }

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                System.out.println("Sending packet number " + num);
                serverSocket.send(sendPacket);
                begin = end;
                end += 128;
                num++;
            }
            sendData [0] = 0;
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            serverSocket.send(sendPacket);
            System.out.println("Finished sending packets!");


        }

    }

    static byte calculateChecksum(byte[] header) {
        int sum = 0;
        for (int i = 0; i < header.length; i++) {
            sum += header[i];
        }
        byte sum2 = (byte)sum;
        return sum2;
    }


}
