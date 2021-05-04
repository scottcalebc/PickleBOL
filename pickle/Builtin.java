package pickle;

import java.util.ArrayList;

public interface Builtin {
    public ResultValue execute(ArrayList<Result> param) throws PickleException;
}
