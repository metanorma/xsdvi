package xsdvi.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alexander Dyuzhev
 */
public class FileHelper {
    
    /**
     * 
     */
    public FileHelper() {
            //
    }
    
    public String readStringFromResourceFile(String resourceFile) {
        InputStream inputStream = null;
        try {
            inputStream = getClass().getClassLoader().getResourceAsStream(resourceFile);
            return readStringFromInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }
    
    private String readStringFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        String line;
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            resultStringBuilder.append(line);
            if (scanner.hasNextLine()) {
                resultStringBuilder.append("\n");
            }
        }

        /*try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            
            while ((line = bufferedReader.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }*/
        return resultStringBuilder.toString();
    }
    
}
