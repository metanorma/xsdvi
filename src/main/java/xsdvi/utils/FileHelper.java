package xsdvi.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Scanner;

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
    
    public void deleteFolder(Path path) {
        if (Files.exists(path)) {
            try {
                Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                            .forEach(File::delete);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
