package conversion;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

public class Utils {
    static String add(String sVal, int increment) {
        int val = 0;
        if (sVal != null && !sVal.isEmpty())
            val = Integer.parseInt(sVal);
        val += increment;
        return String.valueOf(val); 
    }
    
    public static File replaceExtension(File inputfile, String newExtension) {
        if (!newExtension.startsWith("."))
            newExtension = "." + newExtension;  
        return new File(FilenameUtils.removeExtension(inputfile.getAbsolutePath()) + newExtension);
    }
}