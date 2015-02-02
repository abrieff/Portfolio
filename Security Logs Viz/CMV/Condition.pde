class Condition {
    String col = null;
    String operator = null;
    String value = null; 

    // create a new Condition object that specifies some data column
    // should have some relationship to some value
    //   col: column name of data the relationship applies to
    //   op: operator (e.g. "<=")
    //   value: value to compare to
    Condition(String col, String op, String value) {
        this.col = col;
        this.operator = op;
        this.value = value;
    }
    
    public String toString() {
        return col + " " + operator + " " + value + " ";
    }
    
    boolean equals(Condition cond){
        return operator.equals(cond.operator) && 
        value.equals(cond.value) && 
        col.equals(cond.col);
    }
}


boolean checkConditions(Condition[] conds, TableRow row) {
    if(conds == null || row == null){
        return false;
    }
    boolean and = true;
    for (int i = 0; i < conds.length; i++) {
        and = and && checkCondition(conds[i], row);
    }
    return and;
}

boolean checkCondition(Condition cond, TableRow row) {
    if (cond.operator.equals("=")) { // no need to know col
        String cur = row.getString(cond.col);
        return cur.equals(cond.value);
    }
    return false;
}
  /*  if (cond.operator.equals("<=")) {
        float cur = row.getFloat(cond.col);
        return cur - cond.value <= 0.0001;
    }

    if (cond.operator.equals(">=")) {
        float cur = row.getFloat(cond.col);
        return cur - cond.value >= -0.0001;
    }
    
    */
