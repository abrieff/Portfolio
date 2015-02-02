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


Row r;
int zoomLevel;
ArrayList<Row> rows;

void readFile(String fname){

  // Open the file from the createWriter() example
  reader = createReader(fname);
  String line = "";
  String csvSplitBy = ",";
  int num_edges;
  int num_nodes = Integer.parseInt(getLine(reader));
  String [] data;
  float x,y,m,r,x1,y1;
  x1 = rand(width);
  int id;
  for (int i = 0; i < num_nodes ; i++)
  {
    line = getLine(reader);
     data = line.split(csvSplitBy);
     x = rand(width);
     y = rand(height);
     mass = data[1];
     id = data[0];
     radius = data[1] * RADIUS_CONSTANT;
    Node tmp = new Node(x, y, mass, radius, id);
    nodes.add(tmp);
  }
  num_edges = Integer.parseInt(getLine(reader));
  for (int i = 0; < num_nodes; i++)
  {
    line = getLine(reader);
    data = line.split(csvSplitBy);
    int id1 = data[0];
    int id2 = data[1];
    float dist = data[2];
    Edge tmp = new Edge(id1, id2, dist);
    edges.add(tmp);
  }
}
  
  
    
   }
