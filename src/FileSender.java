// Author: Chai Ming Xuan

import java.net.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.CRC32;

/**
 *
 * Modified to transfer over unreliable channel.
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

        // Creating the payload buffer
        byte[] buffer = new byte[988];

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
            // Creating the byte[] array
            byte[] sendBuffer = createPacket(buffer, 0);

            // Form the packet to be sent out
            DatagramPacket sendPkt = new DatagramPacket(sendBuffer, len, serverAddress, portNumber);

            clientSocket.send(sendPkt);
            Thread.sleep(5);

            // DEBUG
            // System.out.println("packet sent " + counter + ", packet length: " + len);
            //counter++;
        }
        bis.close(); // VERY important to close, or else the received file will have extra bytes

        // Send empty packet, signify file transfer end
        DatagramPacket emptyPkt = new DatagramPacket(buffer, 0, serverAddress, portNumber);
        clientSocket.send(emptyPkt);

        System.out.println("File transfer completed");
    }

    public byte[] createPacket(byte[] payload, int sequenceNumber) {
        // Creates the ByteBuffer
        ByteBuffer output = ByteBuffer.allocate(1000);

        // Calculates checksum of payload
        CRC32 crc = new CRC32();
        crc.update(payload);
        long checksum = crc.getValue();

        // Puts the checksum, followed by sequenceNumber, then payload
        output.putLong(checksum);
        output.putInt(sequenceNumber);
        output.put(payload);

        return output.array();
    }
}















