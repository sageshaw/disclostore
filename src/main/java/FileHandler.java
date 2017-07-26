import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.util.Base64;


public class FileHandler {

    private String fileName;
    private File file;
    public FileHandler(String name, String path) {
        file = new File(path);
        fileName = name;
    }

    public void processFile() throws IOException {
        System.out.println("Encoding File...");
        byte[] encodedFile = Base64.getUrlEncoder().encode(Files.readAllBytes(file.toPath()));
//        for(byte i : encodedFile){
//            System.out.print(i + " ");
//        }


    }





}
