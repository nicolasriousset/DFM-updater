package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.common.collect.Iterables;
import com.google.common.io.PatternFilenameFilter;

import conversion.AConversionRule;
import conversion.ChangePropertyValue;
import conversion.RemoveProperty;
import conversion.ReplaceCode;
import cpp.CppClass;
import cpp.CppClass.CppFile;
import cpp.CppClassReaderWriter;
import cpp.CppClassReaderWriterException;
import cpp.Utils;
import dfm.DfmObject;
import dfm.DfmReaderWriter;
import dfm.DfmReaderWriterException;

public class RhUiModernizer {
	static Logger log = Logger.getLogger(RhUiModernizer.class.getName());

	public RhUiModernizer() {
	}

	private String fileMaskToRegEx(String fileMask) {
		StringBuilder builder = new StringBuilder();
		builder.append("(?i)");
		for (char ch : fileMask.toCharArray()) {
			switch (ch) {
			case '*':
				builder.append(".*");
				break;
			case '?':
				builder.append(".");
				break;
			default:
				if (!Character.isLetterOrDigit(ch))
					builder.append("\\");
				builder.append(ch);
			}
		}
		builder.append("(?-i)");
		String regex = builder.toString();
		return regex;
	}

	public void run(File fileMask) {
		try {

			File inputDirectory;
			String fileNameFilter;
			if (fileMask.isDirectory()) {
				inputDirectory = fileMask;
				fileNameFilter = fileMaskToRegEx("*.dfm");
			} else {
				inputDirectory = new File(fileMask.getParent());
				fileNameFilter = fileMaskToRegEx(fileMask.getName());
			}
			PatternFilenameFilter dfmFileFilter = new PatternFilenameFilter(fileNameFilter);
			File[] inputFiles = inputDirectory.listFiles(dfmFileFilter);
			if (inputFiles == null) {
				throw new IOException("Could not find input files : " + fileMask.getAbsolutePath());
			}
			log.info("Found " + inputFiles.length + " input files");
			for (File dfmFile : inputFiles) {
				ProcessDfm(dfmFile);
			}
		} catch (IOException e) {
			log.severe(e.getMessage());
		}
	}

	private boolean updateDfmObjects(DfmObject dfmObject, CppClass cppClass) {
		ArrayList<AConversionRule> rules = new ArrayList<AConversionRule>();
		rules.add(new RemoveProperty("TColoredGroupBox", "BorderColor"));
		// rules.add(new RenameProperty("TfpSpread", "On_Click", "OnClick"));
		
		// rules.add(new CompositeRule(new ChangeObjectType("TComboBoxEx",
		// "TColoredComboBox"), new AddInclude(CppFile.HEADER,
		// "ColoredComboBox.h")));
		/*
		 * rules.add(new RestyleSpeedButton("Fermer", "IMG_FERMER",
		 * RhConst.BOUTON_FERMER_GLYPH)); rules.add(new
		 * RestyleSpeedButton("Valider", "IMG_OK",
		 * RhConst.BOUTON_VALIDER_GLYPH)); rules.add(new
		 * RestyleSpeedButton("Annuler", "IMG_ANNULER",
		 * RhConst.BOUTON_ANNULER_GLYPH)); rules.add(new RestyleSpeedButton());
		 * rules.add(new RestyleForm());
		 * 
		 * rules.add(new TvaToTfpSpread()); rules.add(new CompositeRule(new
		 * ChangeObjectType("TfpText", "TColoredEdit"), new
		 * AddInclude(CppFile.HEADER, "ColoredEdit.h"), new
		 * RemoveProperty("ControlData"), new RemoveProperty("DataBindings")));
		 * rules.add(new CompositeRule(new ChangeObjectType("TEdit",
		 * "TColoredEdit"), new AddInclude(CppFile.HEADER, "ColoredEdit.h")));
		 * rules.add(new CompositeRule(new ChangeObjectType("TGroupBox",
		 * "TColoredGroupBox"), new AddInclude(CppFile.HEADER,
		 * "ColoredGroupBox.h"))); rules.add(new CompositeRule(new
		 * ChangeObjectType("TMaskEdit", "TColoredMaskEdit"), new
		 * AddInclude(CppFile.HEADER, "ColoredMaskEdit.h"))); rules.add(new
		 * CompositeRule(new ChangeObjectType("(TComboBox)|(TComboBox98)",
		 * "TColoredComboBox"), new AddInclude(CppFile.HEADER,
		 * "ColoredComboBox.h")));
		 * 
		 * rules.add(new ChangePropertyValue(
		 * "(TComboBoxEx)|(TColoredEdit)|(TColoredMaskEdit)|(TfpDateTime)|(TfpText)|(TfpMask)|(TfpDoubleSingle)|(TfpLongInteger)",
		 * "Height", "24")); rules.add(new
		 * ChangePropertyValue("(TColoredEdit)|(TColoredMaskEdit)", "AutoSize",
		 * "false"));
		 * 
		 * rules.add(new UseParentFont()); rules.add(new
		 * ChangeFont("(TComboBoxEx)|(ColoredEdit)|(TColoredMaskEdit)"));
		 * 
		 * rules.add(new ChangePropertyValue("TImage", "Transparent", "True"));
		 * rules.add(new ChangePropertyValue("TPanel", "ParentColor", "True",
		 * new PropertyValueIsNullOrEquals("Color", "clBtnFace")));
		 * rules.add(new RestyleSpreadPanel()); rules.add(new RestyleBevel());
		 */

		boolean updated = false;
		for (AConversionRule rule : rules) {
			updated = rule.apply(dfmObject, cppClass) || updated;
		}

		// Recursively apply to children
		// the list of children may be modified by some rules
		// To avoid ConcurrentModificationException, we convert it to an array
		// before iterating over it
		DfmObject[] children = Iterables.toArray(dfmObject, DfmObject.class);
		for (DfmObject childObject : children) {
			updated = updateDfmObjects(childObject, cppClass) || updated;
		}

		return updated;
	}

	boolean updateCppCode(DfmObject dfmObject, CppClass cppClass) {

		ArrayList<AConversionRule> rules = new ArrayList<AConversionRule>();
		// rules.add(new AddInclude(CppFile.BODY, "<memory>", new IsContainingCode("auto_ptr<")));
		
		rules.add(new ReplaceCode(CppFile.BOTH, "TVariant .CursorPos", "VARIANT *CursorPos"));
		rules.add(new ReplaceCode(CppFile.BOTH, "TVariant .Cancel", "VARIANT *Cancel"));
		rules.add(new ReplaceCode(CppFile.BOTH, "TOLEBOOL", "VARIANT_BOOL"));
		// rules.add(new FixSpreadSetSetParams());
		
		/*
		 * rules.add(new CompositeRule(new ChangeBaseClass("TFormExtented", new
		 * BaseClassTypeCheck("TForm")), new AddInclude(CppFile.HEADER,
		 * "def_tform.h"))); rules.add(new RemoveLineOfCode(CppFile.BODY,
		 * ".*\\QOn empeche la fenetre de se d�placer\\E.*")); rules.add(new
		 * RemoveLineOfCode(CppFile.BODY, ".*\\QGetSystemMenu(Handle\\E.*"));
		 * rules.add(new RemoveLineOfCode(CppFile.BODY,
		 * ".*\\QRemoveMenu\\E.*")); rules.add(new
		 * RemoveLineOfCode(CppFile.BODY, ".*\\QCenter_Win\\E.*"));
		 * rules.add(new RemoveLineOfCode(CppFile.BODY,
		 * ".*\\Qcenter_win\\E.*")); rules.add(new
		 * RemoveLineOfCode(CppFile.BODY, ".*\\QwPrinc->Width/2\\E.*"));
		 * rules.add(new RemoveLineOfCode(CppFile.BODY,
		 * ".*\\QwPrinc->Height/2\\E.*"));
		 */

		boolean updated = false;
		for (AConversionRule rule : rules) {
			updated = rule.apply(dfmObject, cppClass) || updated;
		}

		return updated;
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

			boolean updated = updateDfmObjects(dfmRoot, cppClass);
			updated = updateCppCode(dfmRoot, cppClass) || updated;

			dfmReaderWriter.write(dfmFile, dfmRoot);
			cppReaderWriter.write(cppClass, headerFile, cppFile);
			if (updated) {
				cppReaderWriter.reformat(headerFile);
				cppReaderWriter.reformat(cppFile);
			}

			log.info(dfmFile.getAbsolutePath() + " processed");
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
