package conversion;

import java.util.ArrayList;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;

import cpp.CppClass;
import cpp.CppClass.CppFile;
import cpp.CppClass.Visibility;
import cpp.CppClassReaderWriterException;
import cpp.Utils;
import dfm.DfmObject;
import dfm.DfmObject.Direction;

public class RestyleSpreadPanel extends AConversionRule {

    DfmObject findTitlePanel(DfmObject panel) {
        // The title panel must be a child of the main panel, with a toolbar inside
        for (DfmObject child : panel) {
            if (child.isInstanceOf("TPanel")) {
                for (DfmObject grandChild : child) {
                    if (grandChild.isInstanceOf("TToolBar")) {
                        return child;
                    }
                }
            }
        }

        return null;
    }

    DfmObject findSpreadPanel(DfmObject panel) {
        // The spread panel must be a child of the main panel, with a spread inside
        for (DfmObject child : panel) {
            if (child.isInstanceOf("TPanel")) {
                for (DfmObject grandChild : child) {
                    if (grandChild.isInstanceOf("TfpSpread")) {
                        return child;
                    }
                }
            }
        }

        return null;
    }
    
    DfmObject findSpreadToolBar(DfmObject panel) {
        // The toolbar must be a child of the title panel
        DfmObject titlePanel = findTitlePanel(panel);
        if (titlePanel == null)
            return null;

        for (DfmObject grandChild : titlePanel) {
            if (grandChild.isInstanceOf("TToolBar")) {
                return grandChild;
            }
        }

        return null;
    }

    DfmObject findSpread(DfmObject panel) {
        // The spread must be a grand child of the panel
        for (DfmObject child : panel) {
            if (child.isInstanceOf("TPanel")) {
                for (DfmObject grandChild : child) {
                    if (grandChild.isInstanceOf("TfpSpread")) {
                        return grandChild;
                    }
                }
            }
        }

        return null;
    }

    DfmObject findTitleImage(DfmObject panel) {
        // The image must be a child of the title panel
        DfmObject titlePanel = findTitlePanel(panel);
        if (titlePanel == null)
            return null;

        for (DfmObject grandChild : titlePanel) {
            if (grandChild.isInstanceOf("TImage")) {
                return grandChild;
            }
        }

        return null;
    }

    @Override
    public boolean isApplicable(DfmObject dfmObject, CppClass cppClass) {
        if (!dfmObject.isInstanceOf("TPanel"))
            return false;

        if (findSpreadToolBar(dfmObject) == null)
            return false;

        if (findSpread(dfmObject) == null)
            return false;

        if (findTitleImage(dfmObject) == null)
            return false;

        return true;
    }

    @Override
    protected boolean doApply(DfmObject dfmObject, CppClass cppClass) {
        boolean result = true;
        DfmObject mainPanel = dfmObject;
        
        // Updating title panel properties
        DfmObject titlePanel = findTitlePanel(mainPanel);
        titlePanel.properties().put("Height", "27");
        titlePanel.properties().put("Caption", "''");
        titlePanel.properties().put("ParentColor", "True");

        // Updating toolbar properties
        DfmObject toolbar = findSpreadToolBar(mainPanel);
        toolbar.properties().put("Images", "wPrinc.SpreadToolBarImages");
        toolbar.properties().put("Width", "180");
        toolbar.properties().put("ParentColor", "True");
        toolbar.properties().put("Transparent", "True");
        // Removing separators
        for (int i = toolbar.getChildrenCount() -1; i >= 0; i--) {
            DfmObject button = toolbar.getChild(i);
            String style = button.properties().get("Style");
            if (button.isInstanceOf("TToolButton") && style != null && style.compareTo("tbsSeparator") == 0) {
                toolbar.removeChild(i);
                String regex = String.format(".*\\W%s\\W.*", Pattern.quote(button.getName())); 
                cppClass.removeLineOfCode(CppFile.HEADER, regex);
                cppClass.removeLineOfCode(CppFile.BODY, regex);
            }
        }
        try {            
            cppClass.appendToApplyStyleMethod(String.format("    Apparence::ApplySpreadTitleBarStyle(%s, %s, useLegacyUI);", titlePanel.getName(), toolbar.getName()));
        } catch (CppClassReaderWriterException e) {
            e.printStackTrace();
            result = false;
        }
        cppClass.addIncludeHeader(CppFile.BODY, "ImageManager.h");
       
        
        // Updating title image properties
        DfmObject image = findTitleImage(mainPanel);
        image.properties().put("Align", "alRight");
        image.properties().put("Transparent", "True");
        image.properties().put("Width", Utils.add(titlePanel.properties().get("Width"), -Integer.parseInt(toolbar.properties().get("Width"))));
        
        // Updating main panel properties
        ArrayList<String> anchorsList = new ArrayList<String>();
        if (!mainPanel.hasNeighbour(Direction.UP, "TPanel"))
            anchorsList.add("akTop");
        if (!mainPanel.hasNeighbour(Direction.DOWN, "TPanel") || !mainPanel.hasNeighbour(Direction.UP, "TPanel"))
            anchorsList.add("akBottom");
        if (!mainPanel.hasNeighbour(Direction.LEFT, "TPanel"))
            anchorsList.add("akLeft");
        if (!mainPanel.hasNeighbour(Direction.RIGHT, "TPanel"))
            anchorsList.add("akRight");
        String anchors = "[" + Joiner.on(",").join(anchorsList) + "]";
        
        mainPanel.properties().put("Anchors", anchors);
        mainPanel.properties().put("ParentColor", "True");

        // Updating spread panel properties
        DfmObject spreadPanel = findSpreadPanel(mainPanel);
        spreadPanel.properties().put("ParentColor", "True");
        
        // Adding on resize event to resize spread columns
        DfmObject mainForm = mainPanel.getRoot();
        String onResizeMethodName = mainForm.properties().get("OnResize");
        if (onResizeMethodName == null || onResizeMethodName.trim().isEmpty())
            onResizeMethodName = "FormResize";
        DfmObject spread = findSpread(mainPanel);        
        String rawCode = "    // on m�morise dans une constante statique la largeur totale des colonnes d�finie dans l'IDE\r\n" +
                        "    static const double MIN_%1$S_COLUMNS_WIDTH = ComputeVisibleColumnsTotalWidth(%1$s);\r\n" +
                        "    FitColumnsWidthToSpreadWidth(%1$s, MIN_%1$S_COLUMNS_WIDTH);\r\n";
        String onResizeMethodCode = String.format(rawCode, spread.getName());
        try {
            cppClass.createMethodOrAppendTo(Visibility.PUBLISHED, "__fastcall", onResizeMethodName, "TObject *Sender", onResizeMethodCode, "");
            cppClass.addIncludeHeader(CppFile.BODY, "Apparence.h");
            mainForm.properties().put("OnResize", onResizeMethodName);
        } catch (CppClassReaderWriterException e) {
            e.printStackTrace();
            result = false;
        }                
        
        return result;
    }

}
