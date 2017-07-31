package exec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;


public class FileTools {


    public static byte[][] encodeFile(File file) throws IOException {
        System.out.println("Encoding and processing file...");
        byte[] encodedFile = Base64.getUrlEncoder().encode(Files.readAllBytes(file.toPath()));

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

    public static byte[] encodeFileRaw(File file) throws IOException {
        return Base64.getUrlEncoder().encode(Files.readAllBytes(file.toPath()));
    }

    public static File decodeFile(String name, byte[][] segmentedFile) throws IOException {
        System.out.println("Reassembling file...");
        byte[] encodedFile = new byte[segmentedFile.length * 32];
        int btIndex = 0;

        for (int btSeg = 0; btSeg < segmentedFile.length; btSeg++) {
            for (int btNum = 0; btNum < segmentedFile.length; btNum++) {
                encodedFile[btIndex] = segmentedFile[btSeg][btNum];
                btIndex++;
            }
        }

        byte[] decodedFile = Base64.getUrlDecoder().decode(encodedFile);
        File file = new File("C:/Users/seiji/devstuff/" + name);


        FileOutputStream writer = new FileOutputStream(file);
        writer.write(decodedFile);
        writer.close();


        return file;

    }

    public static File decodeFileRaw(String name, byte[] data) throws IOException {
        System.out.println("Reassembling raw file...");
        byte[] decodedFile = Base64.getUrlDecoder().decode(data);

        File file = new File("C:/Users/seiji/devstuff/" + name);


        FileOutputStream writer = new FileOutputStream(file);
        writer.write(decodedFile);
        writer.close();


        return file;

    }


}
