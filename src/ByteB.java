import java.nio.ByteBuffer;
import java.util.zip.CRC32;


public class ByteB {

    public static void main(String[] args) {
        
        // Sending
        ByteBuffer forPkt = ByteBuffer.allocate(1000);
        
        byte[] data = "Hello Ming Xuan!".getBytes();
        forPkt.put(data);
        forPkt.position(32);
        
        forPkt.putInt(123);


        int payload = 2139019;
        int sequenceNumber = 1;

        // Creates the ByteBuffer
        ByteBuffer output = ByteBuffer.allocate(1000);

        // Calculates checksum of payload
        CRC32 crc = new CRC32();
        crc.update(payload);
        long checksum = crc.getValue();

        output.putLong(checksum);
        output.putInt(sequenceNumber);
        
//        byte[] buffer = forPkt.array();
        byte[] buffer = output.array();
        
        
        
        // Receiving
        ByteBuffer wrapper = ByteBuffer.wrap(buffer);

        byte[] receivedData = new byte[32];
//        wrapper.get(receivedData);

        long foo = wrapper.getLong();
        System.out.println(foo);

        int bar = wrapper.getInt();
        System.out.println(bar);

//
//        String sentence = new String(receivedData);
//        System.out.println(sentence);
        
//        System.out.println("Current wrapper position: " + wrapper.position());
//        int receivedInt = wrapper.getInt();
//        System.out.println(receivedInt);


    }

}
