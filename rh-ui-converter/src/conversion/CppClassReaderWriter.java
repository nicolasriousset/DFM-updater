package conversion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class CppClassReaderWriter {
    public CppClass read(File headerFile, File cppFile) throws IOException, CppClassReaderWriterException {
        return new CppClass(readFile(headerFile.getAbsolutePath()), readFile(cppFile.getAbsolutePath()));
    }

    public void write(CppClass cppClass, File aCppHeader, File aCppBody) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(aCppHeader.getAbsolutePath()), StandardCharsets.ISO_8859_1, StandardOpenOption.CREATE)) {
            writer.write(cppClass.getCppHeader());
        } catch (IOException e) {
            e.printStackTrace();
        }        

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(aCppBody.getAbsolutePath()), StandardCharsets.ISO_8859_1, StandardOpenOption.CREATE)) {
            writer.write(cppClass.getCppBody());
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }

    private String readFile(String file) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(file));
        return new String(encoded, "Cp1252");
    }
}
