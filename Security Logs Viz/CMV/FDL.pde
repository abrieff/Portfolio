final float timestep = 1.0/frameRate*0.1;
final float constantSpeedLimit = 1e3;

class FDL {
  ArrayList<Node> nodes = null;
  ArrayList<Edge> edges = null;

  final color colfNode = color(0, 0, 255);
  final color colsNode = color(0, 0, 0);
  final color colfHiNode = color(100, 255, 100); //color(0, 191, 255);
  final color colsHiNode = color(0, 0, 0);
  final color colsEdge = color(0, 0, 0);
  final color colsHiEdge = color(100, 255, 100); //color(0, 191, 255);
  final color colfHiText = color(0, 0, 0);
  final float constantCoulomb = 5e8;
  final float constantPotential = 1e0;
  final float constantDrag = 1e-2;
  final float SPRING_CONSTANT = 1e3;
  final float constantDampening = (1-5e-1);
  final float TOTAL_ENERGY_THRESHOLD = 25e3;
  final float edgeWidthMax = 8, edgeWidthMin = 1; // in pixels
  final float strwNode = 1.5;
  final float textHeightAboveNode = 5;
  final float textWidthMult = 1.5, textHeightMult = 1.2;

  float BORDER_SIZE = 20.0;
  float edgeWeightMax = 0, edgeWeightMin = 0;
  float total_energy = 1e5;
  boolean bSimRunning = true;
  float tlx, tly, wid, hei;

  FDL(ArrayList<Node> nodes_, ArrayList<Edge> edges_, float tlx_, float tly_, float wid_, float hei_, float defb) {
    nodes = nodes_; 
    edges = edges_;
    tlx = tlx_; 
    tly = tly_; 
    wid = wid_; 
    hei = hei_;
    BORDER_SIZE = defb;

    edgeWeightMax = edges.get(0).getWeight();
    edgeWeightMin = edges.get(0).getWeight();
    for ( int i=1; i<edges.size (); i++ ) {
      float ew = edges.get(i).getWeight();
      if ( ew>edgeWeightMax ) {
        edgeWeightMax = ew;
      }
      if ( ew<edgeWeightMin ) {
        edgeWeightMin = ew;
      }
    }
  }

  void setPosition(float tlx_, float tly_) {
    if ( tlx_ != tlx | tly_ != tly ) {
      setSimRunning(true);
    }
    tlx = tlx_; 
    tly = tly_;
  }

  void setSize(float wid_, float hei_) {
    if ( wid_ != wid | hei_ != hei ) {
      setSimRunning(true);
    }
    wid = wid_; 
    hei = hei_;
  }

  boolean isect(int node_id) {
    Node node = nodes.get(node_id);
    if (abs(node.get_posX() - mouseX) > node.get_radius()/2.0 || 
      abs(node.get_posY() - mouseY) > node.get_radius()/2.0) {
      return false;
    } else {
      return true;
    }
  }
  
  ArrayList<Integer> getNodeIdsWithinRect(Rectangle r) {
    ArrayList<Integer> n = new ArrayList<Integer>();
    
    for( int i=0; i<nodes.size(); i++ ) {
      float nx = nodes.get(i).get_posX(); float ny = nodes.get(i).get_posY();
      if(nx>=r.p1.x & nx<=r.p2.x & ny>=r.p1.y & ny<=r.p2.y) {
        n.add(i);
      }
    }
    
    return n;
  }

  void resetForces() { // do this first
    int n = nodes.size();

    for ( int i=0; i<n; i++ ) {
      nodes.get(i).set_force(0.0, 0.0);
    }
  }

  void updateForcesPotential() { // do this after resetForces() and before updateForcesDampening()
    int n = nodes.size();
    float pxcenter = tlx + wid/2, pycenter = tly + hei/2;
    float gxcenter = 0.0, gycenter = 0.0;

    for ( int i=0; i<n; i++ ) {
      Node c = nodes.get(i);
      gxcenter += pxcenter - c.get_posX();
      gycenter += pycenter - c.get_posY();
    }

    float magf = constantPotential*(sq(gxcenter) + sq(gycenter));
    float th = atan2(gycenter, gxcenter);

    for ( int i=0; i<n; i++ ) {
      Node c = nodes.get(i);
      c.add_force(magf*cos(th), magf*sin(th));
    }
  }

  void updateForcesCoulomb() { // do this after resetForces() and before updateForcesDampening()
    int n = nodes.size();

    for ( int i=0; i<n; i++ ) {
      float fx_on_i = 0.0, fy_on_i = 0.0;
      for ( int j=0; j<n; j++ ) {
        if ( i != j ) {
          // want force that node j exerts on node i
          Node ni = nodes.get(i), nj = nodes.get(j);
          float dx = ni.get_posX()-nj.get_posX(); // ni.x - nj.x
          float dy = ni.get_posY()-nj.get_posY(); // ni.y - nj.y
          float magf = constantCoulomb/(sq(dx)+sq(dy)); // magnitude of force
          float th = atan2(dy, dx);
          float fx_on_i_by_j = magf*cos(th), fy_on_i_by_j = magf*sin(th);
          fx_on_i += fx_on_i_by_j; 
          fy_on_i += fy_on_i_by_j;
        }
      }
      nodes.get(i).add_force(fx_on_i, fy_on_i);
    }
  }

  float sign(float x) {
    if (x == 0.0) {
      return 0.0;
    } else if ( x > 0.0 ) {
      return 1.0;
    } else {
      return -1.0;
    }
  }

  void updateForcesDampening() { // do this after coulomb, spring, and potential forces have been calculated
    int n = nodes.size();

    for ( int i=0; i<n; i++ ) {
      Node c = nodes.get(i);
      float fx = c.get_forceX(), fy = c.get_forceY();
      nodes.get(i).set_force(fx*constantDampening, fy*constantDampening);
    }
  }  

  void updateForcesDrag() { // do this after coulomb, spring, and potential forces have been calculated
    int n = nodes.size();

    for ( int i=0; i<n; i++ ) {
      Node c = nodes.get(i);
      float vx = c.get_velX(), vy = c.get_velY();
      float vms = sq(vx) + sq(vy);
      nodes.get(i).add_force(-sign(vx)*vms*constantDrag, -sign(vy)*vms*constantDrag);
    }
  }

  void updateForcesHookes()
  {
    int n = nodes.size();
    int e = edges.size();
    int b_id = -1;
    Node a, b;
    for (int i = 0; i<n; i++)
    {
      a = nodes.get(i);
      float fx_on_i = 0.0, fy_on_i = 0.0;
      for ( int j=0; j<e; j++ ) {

        b_id = edges.get(j).getConnector(a.get_id());
        if (b_id != -1)
        {
          b = nodes.get(getNodeIndex(b_id));
          float dx = a.get_posX() - b.get_posX();
          float dy = a.get_posY() - b.get_posY();
          float deltax = edges.get(j).getLength() - dist(b.get_posX(), b.get_posY(), a.get_posX(), a.get_posY());
          float magf = SPRING_CONSTANT * deltax;
          float th = atan2(dy, dx);
          float fx_on_a_by_b = magf*cos(th), fy_on_a_by_b = magf*sin(th);
          fx_on_i += fx_on_a_by_b; 
          fy_on_i += fy_on_a_by_b;
        }
      }
      nodes.get(i).add_force(fx_on_i, fy_on_i);
    }
  }

  void updateBoundary() {
    float left_boundary = tlx, right_boundary = tlx + wid, top_boundary = tly, bottom_boundary = tly + hei;
    float boundary_padding = BORDER_SIZE;
    int n = nodes.size();

    for ( int i=0; i<n; i++ ) {
      Node c = nodes.get(i);
      if ( c.get_posX() < boundary_padding & c.get_velX() < 0 ) { // left going left
        c.set_pos(boundary_padding, c.get_posY());
        c.set_vel(0.0, c.get_velY());
      }
      if ( c.get_posX() > right_boundary - boundary_padding & c.get_velX() > 0 ) { // right going right
        c.set_pos(right_boundary - boundary_padding, c.get_posY());
        c.set_vel(0.0, c.get_velY());
      }    
      if ( c.get_posY() < boundary_padding & c.get_velY() < 0 ) { // up going up
        c.set_pos(c.get_posX(), boundary_padding);
        c.set_vel(c.get_velX(), 0.0);
      }
      if ( c.get_posY() > bottom_boundary - boundary_padding & c.get_velY() > 0 ) { // down going down
        c.set_pos(c.get_posX(), bottom_boundary - boundary_padding);
        c.set_vel(c.get_velX(), 0.0);
      }
    }
  }

  int getNodeIndex(int id_)
  {
    int n = nodes.size();
    for (int i = 0; i < nodes.size (); i++)
    {
      if (nodes.get(i).get_id() == id_)
      {
        return i;
      }
    }
    return -1;
  }

  void update_total_energy()
  {
    Node n = nodes.get(0);
    float vel = sqrt(n.get_velX() * n.get_velX() + n.get_velY() * n.get_velY());
    total_energy = 0.5 * n.get_mass() * vel * vel;

    for (int i = 1; i < nodes.size (); i++) {
      n = nodes.get(i);
      vel = sqrt(n.get_velX() * n.get_velX() + n.get_velY() * n.get_velY());
      total_energy += 0.5 * n.get_mass() * vel * vel;
    }
  }

  void updatePositions() {
    for (int i = 0; i < nodes.size (); i++) {
      nodes.get(i).update_pos();
    }
  }

  void draw_edges(ArrayList<Integer> ids)
  {
    int e = edges.size();
    Node a, b;
    int a1, b1;

    
    for (int i = 0; i < e; i++)
    {
      a1 = edges.get(i).get_id1(); 
      b1 = edges.get(i).get_id2();
      a = nodes.get(getNodeIndex(a1)); 
      b = nodes.get(getNodeIndex(b1));
      if(ids.contains(i)) {
        stroke(colsHiEdge);
        strokeWeight(map(edges.get(i).getWeight(), edgeWeightMin, edgeWeightMax, edgeWidthMin, edgeWidthMax));
        line(a.get_posX(), a.get_posY(), b.get_posX(), b.get_posY());
      } else {
        stroke(colsEdge);
        strokeWeight(map(edges.get(i).getWeight(), edgeWeightMin, edgeWeightMax, edgeWidthMin, edgeWidthMax));
        line(a.get_posX(), a.get_posY(), b.get_posX(), b.get_posY());
      }        
    }
  }

  void draw_nodes(ArrayList<Integer> ids) {

    for (int i = 0; i < nodes.size(); i++) {
      if (ids.contains(i)) {
        fill(colfHiNode);
        stroke(colsHiNode);    
        strokeWeight(strwNode);
        ellipse(nodes.get(i).get_posX(), nodes.get(i).get_posY(), nodes.get(i).r, nodes.get(i).r);
        float textx = nodes.get(i).get_posX(), texty = nodes.get(i).get_posY() - nodes.get(i).get_radius()/2 - textHeightAboveNode;
        String s = "IP: " + nodes.get(i).get_label();
        float texth = (textAscent() + textDescent())*textHeightMult, textw = textWidth(s)*textWidthMult;
        if(textx-textw/2<tlx) {
          textx = tlx+textw/2;
        }
        if(texty-texth<tly) {
          texty = tly + texth;
        }
        if(textx+textw/2>tlx+wid) {
          textx = tlx + wid - textw/2;
        }
        if(texty>tly+hei) {
          texty = tly + hei;
        }
        rect(textx-textw/2,texty-texth,textw,texth);
        textSize(12);
        textAlign(CENTER, BOTTOM);
        fill(colfHiText);
        text(s, textx, texty);
      } else {
        fill(colfNode);
        stroke(colsNode);    
        strokeWeight(strwNode);
        ellipse(nodes.get(i).get_posX(), nodes.get(i).get_posY(), nodes.get(i).r, nodes.get(i).r);
      }
    }
  }

  void draw(ArrayList<Integer> idsMarkedNodes, ArrayList<Integer> idsMarkedEdges) {
    if (isSimRunning()) {
      resetForces();
      updateForcesHookes();
      updateForcesCoulomb();
      updateForcesPotential();
      updateForcesDampening();
      updateForcesDrag();
      updatePositions();
      updateBoundary();
      update_total_energy();

      if ( total_energy < TOTAL_ENERGY_THRESHOLD ) {
        setSimRunning(false);
      }
    }

    pushStyle();
    draw_edges(idsMarkedEdges);
    draw_nodes(idsMarkedNodes);
    popStyle();
  }
  
  boolean isSimRunning() { return bSimRunning; }
  void setSimRunning(boolean b) { bSimRunning = b; }
  
}

