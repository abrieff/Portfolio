class Parser {
 
  int num_cols;
  int num_rows;
  String[] lines;
  
  Parser(String file_name) {
    lines = loadStrings(file_name);
    num_rows = lines.length;
    num_cols = splitTokens(lines[0], ",").length;
  }

  String[] parse_row(int row_index) {
    String temp[] = splitTokens(lines[0], ",");
    String data[] = new String[num_rows];
    temp = splitTokens(lines[row_index], ",");
    for (int j = 0; j < temp.length; j++) {
      data[j] = trim(temp[j]);  
    }
    return data;
  } 
}
