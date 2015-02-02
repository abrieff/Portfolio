int indexofEdge(ArrayList<Edge> edges, Edge e) {
  
  for( int i=0; i<edges.size(); i++ ) {
    if( edges.get(i).containsNode(e.get_id1()) & edges.get(i).containsNode(e.get_id2()) ) {
      return i;
    }
  }
  
  return -1;
}

ArrayList<Integer> indicesofEdgesWithNodeId(ArrayList<Edge> edges, int id) {
  ArrayList<Integer> idx = new ArrayList<Integer>();
  
  for( int i=0; i<edges.size(); i++ ) {
    if( edges.get(i).containsNode(id) ) {
      idx.add(i);
    }
  }
  
  return idx;
}

class Edge {
  int id1, id2;
  float l,w;
  
  Edge(int id1_, int id2_, float l_, float w_) {
    id1 = id1_; id2 = id2_; l = l_; w = w_;
  }
  
  void setLength(float l_) {l = l_;}
  float getLength() { return l; }
  int get_id1() { return id1; }
  int get_id2() { return id2; }
  void setWeight(float w_) {w = w_;}
  float getWeight() { return w; }
  void addWeight(float w_) {w += w_;}
  
  boolean containsNode(int id) {
    
    if( id1 == id || id2 == id ) {
      return true;
    } else {
      return false;
    }
    
  }
  int getConnector(int id_)
  {
    if (id1 == id_) return id2;
    else if (id2 == id_) return id1;
    else return -1;
  }

  
}
