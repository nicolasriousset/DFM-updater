package conversion;

import main.DfmObject;

public class FormStyleRule extends AConversionRule {

    @Override
    public boolean isApplicable(DfmObject dfmObject, CppClass cppClass) {
        if (dfmObject.getParent() != null)
            return false;

        return true;
    }

    @Override
    protected void doApply(DfmObject dfmObject, CppClass cppClass) {
        dfmObject.getProperties().put("Color", "clWhite");
        dfmObject.getProperties().put("Font.Charset", "ANSI_CHARSET");
        dfmObject.getProperties().put("Font.Color", "clGray");
        dfmObject.getProperties().put("Font.Height", "-13");
        dfmObject.getProperties().put("Font.Name", "'Arial'");
        dfmObject.getProperties().put("TextHeight", "16");
        dfmObject.getProperties().put("Font.Style", "[]");
        dfmObject.getProperties().put("Constraints.MinHeight", Utils.add(dfmObject.getProperties().get("ClientHeight"), 39));
        dfmObject.getProperties().put("Constraints.MinWidth", Utils.add(dfmObject.getProperties().get("ClientWidth"), 16));
    }

}