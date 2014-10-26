import java.util.ArrayList;

public class Faces {

    private ArrayList<ArrayList<Integer>> interior;
    private ArrayList<Integer> external;
    private int size;

    public Faces(ArrayList<ArrayList<Integer>> interior, ArrayList<Integer> external) {
        if(interior != null && external != null) {
            this.interior = (ArrayList<ArrayList<Integer>>) interior.clone();
            this.external = (ArrayList<Integer>) external.clone();
            size = interior.size() + external.size();
        } else {
            size = 0;
        }
    }

    public ArrayList<ArrayList<Integer>> getInterior() {
        return (ArrayList<ArrayList<Integer>>) interior.clone();
    }

    public ArrayList<Integer> getExternal() {
        return (ArrayList<Integer>) external.clone();
    }

    @Override
    public String toString() {
        String result = "Faces size = " + size + "\nExternal face:\n" + external + "\nInterior faces:\n";
        for(ArrayList<Integer> f : interior) {
            result += f + "\n";
        }
        return result;
    }
}
