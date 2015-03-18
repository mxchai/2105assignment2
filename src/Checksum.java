/**
 * Created by mx on 30/1/15.
 */

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.CRC32;

public class Checksum {
    public static void main(String[] args){
        try {
            String src = args[0];

            byte[] bytes = Files.readAllBytes(Paths.get(src));
            CRC32 crc = new CRC32();

            crc.update(bytes);
            System.out.println(crc.getValue());
        } catch (Exception e){
            System.out.println("Invalid command");
        }
    }
}
