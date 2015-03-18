// Author: Chai Ming Xuan

import java.net.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

/**
 *
 * You may assume that underlying transmission channel is perfect and all data will be received in good order.
 *
 */

class FileSender {

    public DatagramSocket socket;
    public DatagramPacket pkt;

    public static void main(String[] args) throws Exception {

        // check if the number of command line argument is 4
        if (args.length != 4) {
            System.out.println("Usage: java FileSender <path/filename> "
                    + "<rcvHostName> <rcvPort> <rcvFileName>");
            System.exit(1);
        }

        new FileSender(args[0], args[1], args[2], args[3]);
    }

    public FileSender(String fileToOpen, String host, String port, String rcvFileName) throws Exception {
        // Declarations
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress serverAddress = InetAddress.getByName(host);
        int portNumber = Integer.parseInt(port);

        // Opening the file
        byte[] buffer = new byte[1000];

        // File input
        FileInputStream fis = new FileInputStream(fileToOpen);
        BufferedInputStream bis = new BufferedInputStream(fis);

        // Send filename packet
        byte[] fileName = rcvFileName.getBytes();
        DatagramPacket fileNamePkt = new DatagramPacket(fileName, fileName.length, serverAddress, portNumber);
        clientSocket.send(fileNamePkt);

        // Send file data
        int counter = 0;
        int len;
        while ((len = bis.read(buffer)) > 0) {
            DatagramPacket sendPkt = new DatagramPacket(buffer, len, serverAddress, portNumber);
            clientSocket.send(sendPkt);
            Thread.sleep(5);

            // DEBUG
            // System.out.println("packet sent " + counter + ", packet length: " + len);
            //counter++;
        }
        bis.close();

        // Send empty packet, signify file transfer end
        DatagramPacket emptyPkt = new DatagramPacket(buffer, 0, serverAddress, portNumber);
        clientSocket.send(emptyPkt);

        System.out.println("File transfer completed");
    }
}
