BufferedReader reader;
//helper function to clean up reading lines
String getLine(BufferedReader reader){
  String tmpline;
  try {
       tmpline = reader.readLine();
  } catch (IOException e) {
       tmpline = null;
  }
  return tmpline;

}
void readFile(String fname){

  // Open the file from the createWriter() example
  reader = createReader(fname);
  String line = "";
  String csvSplitBy = ",";
  int num_edges;
  NUM_NODES = Integer.parseInt(getLine(reader));
  String [] data;
  float x,y,m,r;
  int id;
  for (int i = 0; i < NUM_NODES ; i++)
  {
    line = getLine(reader);
     data = line.split(csvSplitBy);
     x = random(BORDER_SIZE, width - BORDER_SIZE);
     y = random(BORDER_SIZE, height - BORDER_SIZE);
     m = Float.parseFloat(data[1]);
     id = Integer.parseInt(data[0]);
     r = m * RADIUS_CONSTANT;
    Node tmp = new Node(x, y, m, r, id);
    nodes.add(tmp);
  }
  num_edges = Integer.parseInt(getLine(reader));
  for (int i = 0; i < num_edges; i++)
  {
    line = getLine(reader);
    data = line.split(csvSplitBy);
    int id1 = Integer.parseInt(data[0]);
    int id2 = Integer.parseInt(data[1]);
    float dist = Float.parseFloat(data[2]);
    Edge tmp = new Edge(id1, id2, dist);
    edges.add(tmp);
  } 
}
