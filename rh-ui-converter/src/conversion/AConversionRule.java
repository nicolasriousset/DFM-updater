package conversion;

import java.util.logging.Logger;

import main.DfmObject;

public abstract class AConversionRule {
    static Logger log = Logger.getLogger(AConversionRule.class.getName());
    
    public void apply(DfmObject dfmObject, CppClass cppClass) {
        if (!isApplicable(dfmObject, cppClass))
            return;

        log.info("Applying " + this.getClass().getName());
        doApply(dfmObject, cppClass);
    }

    abstract public boolean isApplicable(DfmObject dfmObject, CppClass cppClass);

    abstract protected void doApply(DfmObject dfmObject, CppClass cppClass);

}
