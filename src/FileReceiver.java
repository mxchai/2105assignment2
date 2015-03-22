// Author: Chai Ming Xuan

import java.io.*;
import java.net.*;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

class FileReceiver {
    private final static int PAYLOAD_SIZE = 888;
    private FileOutputStream fos;
    private BufferedOutputStream bos;

    public static void main(String[] args) throws Exception {

        // check if the number of command line argument is 1
        if (args.length != 1) {
            System.out.println("Usage: java FileReceiver port");
            System.exit(1);
        }

        // if valid input
        int portNumber = Integer.parseInt(args[0]);
        new FileReceiver(portNumber);
    }

    public FileReceiver(int portNumber) throws Exception {
        printWelcomeMessage(portNumber);

        // Declarations
        byte[] rcvBuffer = new byte[1000];
        DatagramSocket serverSocket = new DatagramSocket(portNumber);

        DatagramSocket sendingSocket = new DatagramSocket();


//        String fileName = getFileNameFromPacket(serverSocket);
//        FileOutputStream fos = new FileOutputStream(fileName, false);
//        BufferedOutputStream bos = new BufferedOutputStream(fos);


        // ================================================================
        // Main Loop
        // ================================================================
        boolean setToZero = false;
        int sequenceNumber;
        while (true){
            // Alternate the sequence number
            if (setToZero) {
                sequenceNumber = 0;
            } else {
                sequenceNumber = 1;
            }

            DatagramPacket receivedPacket = receivePacket(rcvBuffer, serverSocket);

            // Extract information from the packet
            byte[] packetData = receivedPacket.getData();

            ByteBuffer dataWrapper = ByteBuffer.wrap(packetData);

            int receivedSequenceNumber = dataWrapper.getInt();
            byte[] fileNameBytes = new byte[100];
            dataWrapper.get(fileNameBytes);
            String fileName = new String(fileNameBytes);

            int fileNameLength = dataWrapper.getInt();

            byte[] payload = new byte[PAYLOAD_SIZE];

            long checksum = dataWrapper.getLong();

            // Checksum
            CRC32 crc = new CRC32();
            crc.update(payload);
            long calcChecksum = crc.getValue();

            // Verification
            // If valid
            if (checksum == calcChecksum) {
                // Check if file exist
                // If doesn't exist, create file and write to it
                // If exists, write to it

                bos = getBufferedOutputStream(fileName);
                writePacketToOutputStream(payload, bos);
            } else {
                // If invalid
                // Send ACK of the currentSequence number
                InetAddress senderAddress = receivedPacket.getAddress();
                int senderPort = receivedPacket.getPort();

                DatagramPacket ack = createAck(sequenceNumber, senderAddress, senderPort);
                serverSocket.send(ack);
            }

            // If valid, check for termination, then write
            // If not valid, send ACK for previous packet

            checkForTermination(receivedPacket);
        }
    }

    private DatagramPacket createAck(int sequenceNumber, InetAddress senderAddress, int senderPort) {
        // Init the ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(sequenceNumber);

        byte[] ackBuffer = buffer.array();
        DatagramPacket ack = new DatagramPacket(ackBuffer, ackBuffer.length, senderAddress, senderPort);
        
        return ack;
    }


    // Note: this method has the side-effect of setting bos!
    private BufferedOutputStream getBufferedOutputStream(String fileName) {
        // If bos is set, return bos
        if (bos != null) {
            return bos;
        } else {
            // If bos is not set, create fos and bos and set them
            try {
                fos = new FileOutputStream(fileName, false);
                bos = new BufferedOutputStream(fos);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bos;
    }


    private DatagramPacket receivePacket(byte[] rcvBuffer, DatagramSocket serverSocket){
        DatagramPacket receivedPacket = new DatagramPacket(rcvBuffer, rcvBuffer.length);
        try {
            serverSocket.receive(receivedPacket);
        } catch (IOException e){
            System.out.println("Packet cannot be received.");
        }

        return receivedPacket;
    }

    // Writing from a DatagramPacket
    private void writePacketToOutputStream(DatagramPacket receivedPacket, BufferedOutputStream bos){
        try {
            bos.write(receivedPacket.getData(), 0, receivedPacket.getLength());
            // DEBUG
            //System.out.println("packet length: " + receivedPacket.getLength());
            bos.flush();
        } catch (Exception e){
            System.out.println("File cannot be written to output stream.");
        }

    }

    // Writing from a byte[]
    private void writePacketToOutputStream(byte[] byteArray, BufferedOutputStream bos){
        try {
            bos.write(byteArray, 0, byteArray.length);
            // DEBUG
            //System.out.println("packet length: " + receivedPacket.getLength());
            bos.flush();
        } catch (Exception e){
            System.out.println("File cannot be written to output stream.");
        }

    }

    private void checkForTermination(DatagramPacket receivedPacket){
        if (isEmptyPacket(receivedPacket)){
            System.out.println("File received");
            System.exit(1);
        }
    }

    private void printWelcomeMessage(int portNumber){
        System.out.println("Running on port: " + portNumber);
    }

    private String getFileNameFromPacket(DatagramSocket serverSocket){
        byte[] buffer = new byte[1000];
        DatagramPacket fileNamePkt = new DatagramPacket(buffer, buffer.length);
        try {
            serverSocket.receive(fileNamePkt);
            return new String(fileNamePkt.getData(), 0, fileNamePkt.getLength()); //note the constructor difference
        } catch (Exception e){
            System.out.println("File name is not received");
            return null;
        }
    }

    private boolean isEmptyPacket(DatagramPacket pkt){
        if (pkt.getLength() == 0){
            return true;
        }
        return false;
    }

}
