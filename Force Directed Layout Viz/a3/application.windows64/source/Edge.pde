class Edge {
  int id1, id2;
  float l;
  
  Edge(int id1_, int id2_, float l_) {
    id1 = id1_; id2 = id2_; l = l_;
  }
  
  void setLength(float l_) {l = l_;}
  float getLength() { return l; }
  int get_id1() { return id1; }
  int get_id2() { return id2; }
  
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
