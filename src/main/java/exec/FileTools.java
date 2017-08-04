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

    //Outputs into two dimensional array of encoded file separated into 32 byte chunks.
    //This is not how files are now stored, but included for use or regression to old storage method.

    public static byte[][] encodeFile(File file) throws IOException {
        System.out.println("Encoding and processing file '" + file.getName() + "'...");
        //Read all data into a byte array
        byte[] encodedFile = Base64.getMimeEncoder().encode(Files.readAllBytes(file.toPath()));

        //create output array with size calculating based on the length of the encoded file. This is how we'll separate the
        //data into 32 byte chunks
        byte[][] segmentedFile = new byte[encodedFile.length / 32 + 1][32];

        int btSeg = 0;
        int btNum = -1;

        //Fill out array
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
        //move data from 32 byte chunks in two-demensional array to one
        byte[] encodedFile = new byte[segmentedFile.length * 32];
        int btIndex = 0;

        for (int btSeg = 0; btSeg < segmentedFile.length; btSeg++) {
            for (int btNum = 0; btNum < segmentedFile[btSeg].length; btNum++) {
                encodedFile[btIndex] = segmentedFile[btSeg][btNum];
                btIndex++;
            }
        }

        //Decode Base64 scheme
        byte[] decodedFile = Base64.getMimeDecoder().decode(encodedFile);

        //Create new file in user-specified directory with filename and extension
        File file = new File(name);

        //Write decoded data to file
        FileOutputStream writer = new FileOutputStream(file);
        writer.write(decodedFile);
        writer.close();

        //Return for any future use
        return file;

    }


}
