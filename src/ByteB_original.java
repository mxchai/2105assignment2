import java.nio.ByteBuffer;


public class ByteB_original {

    public static void main(String[] args) {
        
        // Sending
        ByteBuffer forPkt = ByteBuffer.allocate(1000);
        
        byte[] data = "Hello Ming Xuan!".getBytes();
        forPkt.put(data);
        forPkt.position(32);
        
        forPkt.putInt(123);
        
        byte[] buffer = forPkt.array();
        
        
        
        // Receiving
        ByteBuffer wrapper = ByteBuffer.wrap(buffer);
        
        byte[] receivedData = new byte[32];
        wrapper.get(receivedData);
        String sentence = new String(receivedData);
        System.out.println(sentence);
        
        System.out.println("Current wrapper position: " + wrapper.position());
        int receivedInt = wrapper.getInt();
        System.out.println(receivedInt);

    }

}
