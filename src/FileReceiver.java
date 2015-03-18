// Author: Chai Ming Xuan

import java.io.*;
import java.net.*;
import java.net.DatagramSocket;

class FileReceiver {

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
        String fileName = getFileNameFromPacket(serverSocket);
        FileOutputStream fos = new FileOutputStream(fileName, false);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        while (true){
            DatagramPacket receivedPacket = receivePacket(rcvBuffer, serverSocket);
            checkForTermination(receivedPacket);
            writePacketToOutputStream(receivedPacket, bos);
        }
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
