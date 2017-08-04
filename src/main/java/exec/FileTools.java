package exec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

//Handles file encoding and decoding.
//Files are read into a byte array and encoding with Base64 encoding. This is then
//Outputted to the blockchain.
public class FileTools {

    //Legacy file conversion method. Outputs into two dimensional array of encoded file separated into 32 byte chunks.
    //This is not how files are now stored, but included for use or regression to old storage method.

    public static byte[][] encodeFile(File file) throws IOException {
        System.out.println("Encoding and processing file '" + file.getName() + "'...");
        byte[] encodedFile = Base64.getMimeEncoder().encode(Files.readAllBytes(file.toPath()));

        byte[][] segmentedFile = new byte[encodedFile.length / 32 + 1][32];

        int btSeg = 0;
        int btNum = -1;

        for (int i = 0; i < encodedFile.length; i++) {
            btNum++;
            segmentedFile[btSeg][btNum] = encodedFile[i];

            if (btNum >= 31) {
                btSeg++;
                btNum = -1;
            }
        }

        return segmentedFile;
    }



    //Legacy decoding. See encodeFile for reasoning.

    public static File decodeFile(String name, byte[][] segmentedFile) throws IOException {
        System.out.println("Reassembling file...");
        byte[] encodedFile = new byte[segmentedFile.length * 32];
        int btIndex = 0;

        for (int btSeg = 0; btSeg < segmentedFile.length; btSeg++) {
            for (int btNum = 0; btNum < segmentedFile[btSeg].length; btNum++) {
                encodedFile[btIndex] = segmentedFile[btSeg][btNum];
                btIndex++;
            }
        }

        byte[] decodedFile = Base64.getMimeDecoder().decode(encodedFile);
        File file = new File(name);


        FileOutputStream writer = new FileOutputStream(file);
        writer.write(decodedFile);
        writer.close();


        return file;

    }

    //Takes byte array, decodes Base64, and constructs new file and writes data to it. Returns file object for other use.
    public static File decodeFileRaw(String name, byte[] data) throws IOException {
        System.out.println("Reassembling raw file...");
        byte[] decodedFile = Base64.getUrlDecoder().decode(data);

        File file = new File(name);


        FileOutputStream writer = new FileOutputStream(file);
        writer.write(decodedFile);
        writer.close();


        return file;

    }


}
