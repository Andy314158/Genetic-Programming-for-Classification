package main;

public class Patient {


    private int[] att; //attributes
    private int id;
    private int cond; //condition


    public Patient(String[] attributes) {
        assert attributes.length == 11 : "Must have 11 attributes";

        this.att = new int[9];
        for(int i=1; i < 10; i++){
            this.att[i-1] = getInt(attributes[i]);
        }

        id = getInt(attributes[0]);
        cond = getInt(attributes[10]);
    }


    /**
     * Turns String <code>s</code> into an integer. If s == "?" it turns it into 1
     * @param s
     * @return
     */
    private static int getInt(String s) {
        int i;
        try {
            i = Integer.valueOf(s); // is a valid number
        } catch (NumberFormatException e) {
            i = -1;
        }
        return i;
    }


    public int[] getAttributes() {
        return att;
    }

    public int getCondition() {
        return cond;
    }
}
