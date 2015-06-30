package main;
public class Main {
    public static void main(String[] args) {
        String inputDirectoryName = ".";
        if (args.length > 0)
            inputDirectoryName = args[0];

        String dfmFilesPattern = ".*\\.dfm";
        if (args.length > 1)
            dfmFilesPattern = args[1];

        RhUiModernizer modernizer = new RhUiModernizer();
        modernizer.run(inputDirectoryName, dfmFilesPattern);
    }

    /*
     * (non-Java-doc)
     * 
     * @see java.lang.Object#Object()
     */
    public Main() {
        super();
    }

}