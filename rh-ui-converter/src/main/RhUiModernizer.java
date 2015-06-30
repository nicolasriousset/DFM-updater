package main;

import java.io.File;
import conversion.*;
import conversion.CppClass.CppFile;
import conversion.rules.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.common.io.Files;
import com.google.common.io.PatternFilenameFilter;

public class RhUiModernizer {
    static Logger log = Logger.getLogger(RhUiModernizer.class.getName());

    public RhUiModernizer() {
    }

    public void run(String folder, String dfmFiles) {
        try {
            Path outputFileName = Paths.get(folder, "processedDfms.txt");
            File inputDirectory = new File(folder);
            PatternFilenameFilter dfmFileFilter = new PatternFilenameFilter(".*\\.dfm");
            File[] inputFiles = inputDirectory.listFiles(dfmFileFilter);
            if (inputFiles == null) {
                throw new IOException("Could not find input *.pos files in " + folder);
            }
            log.info("Found " + inputFiles.length + " input files");
            File outputFile = new File(outputFileName.toString());
            Files.createParentDirs(outputFile);
            for (File dfmFile : inputFiles) {
                ProcessDfm(dfmFile);
            }
        } catch (IOException e) {
            log.severe(e.getMessage());
        }
    }

    private void updateDfm(DfmObject dfmObject, CppClass cppClass) {
        ArrayList<AConversionRule> rules = new ArrayList<AConversionRule>();
        rules.add(new RestyleBoutonFermerRule());
        rules.add(new RestyleFormRule());
        rules.add(new CompositeRule().addRule(new RenameBaseClassRule("TFormExtented")).addRule(new AddIncludeRule(CppFile.HEADER, "def_tform.h")));

        for (AConversionRule rule : rules) {
            rule.apply(dfmObject, cppClass);
        }

        // Recursively apply to children
        for (DfmObject childObject : dfmObject) {
            updateDfm(childObject, cppClass);
        }
    }

    private void ProcessDfm(File dfmFile) {
        try {
            log.info("Processing " + dfmFile.getAbsolutePath());
            DfmReaderWriter dfmReaderWriter = new DfmReaderWriter();
            CppClassReaderWriter cppReaderWriter = new CppClassReaderWriter();
            DfmObject dfmRoot = dfmReaderWriter.read(dfmFile);
            File headerFile = Utils.replaceExtension(dfmFile, "h");
            File cppFile = Utils.replaceExtension(dfmFile, "cpp");
            CppClass cppClass = cppReaderWriter.read(headerFile, cppFile);            
            
            updateDfm(dfmRoot, cppClass);
            
            dfmReaderWriter.write(dfmFile, dfmRoot);
            cppReaderWriter.write(cppClass, headerFile, cppFile);
        } catch (IOException e) {
            log.severe(e.getMessage());
        } catch (InterruptedException e) {
            log.severe(e.getMessage());
        } catch (DfmReaderWriterException e) {
            log.severe(e.getMessage());
        } catch (CppClassReaderWriterException e) {
            log.severe(e.getMessage());
        }
    }
}
