package pickle;

public class FileNotFoundException extends PickleException{

    /**
     *
     * Used to indicate that the pickle source file could not be found
     * <p>
     *
     * @param sourceFileNm      Source file name
     *
     */
    public FileNotFoundException(String sourceFileNm) {
        this.errMessageStr =  "Invalid source file: \'" + sourceFileNm + "\'" ;
    }

}
