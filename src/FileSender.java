// Author: Chai Ming Xuan

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.CRC32;

/**
 *
 * Modified to transfer over unreliable channel.
 *
 */

class FileSender {

    private final static int MAX_BUFFER_SIZE = 1000;
    private final static int PAYLOAD_SIZE = 888;
    private final static int DATA_SIZE = 992; // Seq num + name + name.length + payload size
    private final static long TIMEOUT = 1000;
    private final static long RE_TIMEOUT = 1;
    private final static String LOCALHOST = "localhost";

    public DatagramSocket socket;
    public DatagramPacket pkt;

    public static void main(String[] args) throws Exception {

        // check if the number of command line argument is 3
        if (args.length != 3) {
            System.out.println("Usage: java FileSender <path/filename> "
                    + "<unreliNetPort> <rcvFileName>");
            System.exit(1);
        }

        new FileSender(args[0], LOCALHOST, args[1], args[2]);
    }

    public FileSender(String fileToOpen, String host, String port, String rcvFileName) throws Exception {
        // Declarations
        final DatagramSocket clientSocket = new DatagramSocket();
        InetAddress serverAddress = InetAddress.getByName(host);
        int portNumber = Integer.parseInt(port);

        // Creating the payload buffer
        byte[] buffer = new byte[PAYLOAD_SIZE];

        // File input
        FileInputStream fis = new FileInputStream(fileToOpen);
        BufferedInputStream bis = new BufferedInputStream(fis);

        // Send filename packet
//        byte[] fileName = rcvFileName.getBytes();
//        DatagramPacket fileNamePkt = new DatagramPacket(fileName, fileName.length, serverAddress, portNumber);
//        clientSocket.send(fileNamePkt);

        // ================================================================
        // Main Loop
        // ================================================================
        int counter = 0;
        int len;
        boolean setToZero = true;
        int sequenceNumber;
        while ((len = bis.read(buffer)) > 0) {
            // Alternate the sequence number
            if (setToZero) {
                sequenceNumber = 0;
            } else {
                sequenceNumber = 1;
            }

            // Creating the byte[] array
            byte[] sendBuffer = createByteArray(buffer, sequenceNumber, fileToOpen);

            // Form the packet to be sent out
            final DatagramPacket sendPkt = new DatagramPacket(sendBuffer, len, serverAddress, portNumber);

            // DEBUG
//            extractDatagramData(sendPkt);

            // Send out the packet
            clientSocket.send(sendPkt);

            // Start timer
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        clientSocket.send(sendPkt);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            },TIMEOUT, RE_TIMEOUT);

            // Wait for ACK
            byte[] ackBuffer = new byte[4];
            DatagramPacket ack = receivePacket(ackBuffer, clientSocket);
            clientSocket.receive(ack);

//            DatagramPacket ack = new DatagramPacket(ackBuffer, ackBuffer.length, serverAddress, portNumber);


            // Verify ACK
            byte[] ackByte = ack.getData();
            ByteBuffer ackWrapper = ByteBuffer.wrap(ackByte);

            int receivedSequenceNumber = ackWrapper.getInt();
            if (receivedSequenceNumber == sequenceNumber) {
                timer.cancel();
            } else {
                clientSocket.send(sendPkt);
            }

            // Flip the boolean value
            setToZero ^= true;

            // DEBUG
            System.out.println("packet sent " + counter + ", payload length: " + len);
            counter++;
        }
        bis.close(); // VERY important to close, or else the received file will have extra bytes

        // Send empty packet, signify file transfer end
        DatagramPacket emptyPkt = new DatagramPacket(buffer, 0, serverAddress, portNumber);
        for (int i = 0; i < 10; i++) { // spam send
            clientSocket.send(emptyPkt);
        }

        System.out.println("File transfer completed");
        System.exit(1);
    }

    public byte[] createByteArray(byte[] payload, int sequenceNumber, String fileName) {
        // Creates the ByteBuffer
        ByteBuffer output = ByteBuffer.allocate(MAX_BUFFER_SIZE);

        // Puts the sequenceNumber, fileName, fileName length, payload
        output.putInt(sequenceNumber);
        output.put(fileName.getBytes());
        output.putInt(fileName.length());
        output.put(payload);

        // Calculates checksum of payload and append to back
        byte[] data = new byte[DATA_SIZE];
        output.position(0); // move to start
        output.get(data);

        CRC32 crc = new CRC32();
        crc.update(data);
        long checksum = crc.getValue();
        output.position(DATA_SIZE);
        output.putLong(checksum);

        // DEBUG
        System.out.println("Sequence number: " + sequenceNumber);
        System.out.println("File name: " + fileName);
        System.out.println("File name length: " + fileName.length());
        System.out.println("Checksum: " + checksum);

        return output.array();
    }

    public void extractDatagramData(DatagramPacket dp) {
        // Declarations
        ByteBuffer wrapper = ByteBuffer.allocate(MAX_BUFFER_SIZE);

        // Wrap the byte array with ByteBuffer
        byte[] dpData = dp.getData();
        wrapper.wrap(dpData);

        // Extracting the information
        int receivedSequenceNumber = wrapper.getInt();
        byte[] fileNameBytes = new byte[100];
        wrapper.get(fileNameBytes);
        String fileName = new String(fileNameBytes);

        int fileNameLength = wrapper.getInt();

        byte[] payload = new byte[PAYLOAD_SIZE];

        long checksum = wrapper.getLong();

        System.out.println("Sequence number: " + receivedSequenceNumber);
        System.out.println("File name: " + fileName);
        System.out.println("File name length: " + fileName.length());
        System.out.println("Checksum: " + checksum);
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
}















