// following code creates an event handler for detecting window resizing
// from http://forum.processing.org/two/discussion/327/trying-to-capture-window-resize-event-to-redraw-background-to-new-size-not-working/p1
import java.awt.event.*;
int currWidth, currHeight;

void pre() {
  if (currWidth != width || currHeight != height) {
    isSimRunning = true;
    redraw();
    currWidth = width;
    currHeight = height;
  }
}

ArrayList<Node> nodes = null;
ArrayList<Edge> edges = null;
float timestep = 0.1*1.0/frameRate;
float total_energy = 1e5;

float RADIUS_CONSTANT = 5;
float BORDER_SIZE = 20;
float NUM_NODES = 0;
float constantCoulomb = 1e7;
float constantPotential = 1e-1;
float constantDrag = 1e-2;
float constantSpeedLimit = 1e3;
float SPRING_CONSTANT = 1e2;
//final float TIMESTEP_SIZE = .005;
final float TOTAL_ENERGY_THRESHOLD = 15e3;
boolean isSimRunning = true;
String datafile = "data/data_big.csv";
//String datafile = "data/data_three_nodes.csv";

void resetForces() { // do this first
  int n = nodes.size();

  for ( int i=0; i<n; i++ ) {
    nodes.get(i).set_force(0.0, 0.0);
  }
}

void updateForcesPotential() { // do this after resetForces() and before updateForcesDampening()
  int n = nodes.size();
  float pxcenter = width/2, pycenter = height/2;
  float gxcenter = 0.0, gycenter = 0.0;

  for ( int i=0; i<n; i++ ) {
    Node c = nodes.get(i);
    gxcenter += pxcenter - c.get_posX();
    gycenter += pycenter - c.get_posY();
  }

  float magf = constantPotential*(sq(gxcenter) + sq(gycenter));
  float th = atan2(gycenter,gxcenter);
  
  for ( int i=0; i<n; i++ ) {
    Node c = nodes.get(i);
    c.add_force(magf*cos(th),magf*sin(th));
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
        //float magf = constantCoulomb*ni.get_mass()*nj.get_mass()/(sq(dx)+sq(dy)); // magnitude of force
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
  if(x == 0.0) {
    return 0.0;
  } else if( x > 0.0 ) {
    return 1.0;
  } else {
    return -1.0;
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
        float deltax = edges.get(j).getLength() - dist(b.get_posX(), b.get_posY(), a.get_posX(), a.get_posY()); // fixed typo
        //System.out.printf("%f - %f = %f\n",edges.get(j).getLength(),dist(b.get_posX(), b.get_posY(), a.get_posX(), a.get_posY()),deltax);
        float magf = SPRING_CONSTANT * deltax;
        float th = atan2(dy, dx);
        float fx_on_a_by_b = magf*cos(th), fy_on_a_by_b = magf*sin(th);
        fx_on_i += fx_on_a_by_b; 
        fy_on_i += fy_on_a_by_b;
      }
    }
    nodes.get(i).add_force(fx_on_i, fy_on_i);
    //System.out.printf("(%f,%f)\n",fx_on_i, fy_on_i);
  }
}

void updateBoundary() {
  float left_boundary = 0.0, right_boundary = width, top_boundary = 0.0, bottom_boundary = height;
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

void draw_edges()
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
    line(a.get_posX(), a.get_posY(), b.get_posX(), b.get_posY());
  }
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
  //println("hereee");
  for (int i = 0; i < nodes.size (); i++) {
    if(i != curr_node_dragging ) {
      nodes.get(i).update_pos();
    }
  }
}

void setup() {
  nodes = new ArrayList<Node>();
  edges = new ArrayList<Edge>();
  background(255, 255, 255);
  size(600, 600);
  readFile(datafile);
  frame.setResizable(true);
  registerMethod("pre", this);
  textSize(12);
  textAlign(CENTER,BOTTOM);
}

void draw() {
  background(255, 255, 255); // erase
  if (isSimRunning) {
    //println("here");
    resetForces();
    updateForcesHookes();
    updateForcesCoulomb();
    updateForcesPotential();
    updateForcesDrag();
    updatePositions();
    updateBoundary();
    update_total_energy();
    
    if( total_energy < TOTAL_ENERGY_THRESHOLD ) {
      isSimRunning = false;
    }
  }

  stroke(0);
  draw_edges();
  boolean isect_already = false;
  for (int i = 0; i < nodes.size (); i++)
  {
    fill(0);
    stroke(0);
    ellipse(nodes.get(i).get_posX(), nodes.get(i).get_posY(), nodes.get(i).r, nodes.get(i).r);
    if(isect(i) & !isect_already) {
      isect_already = true;
      fill(0,127,255);
      stroke(0,127,255);
      ellipse(nodes.get(i).get_posX(), nodes.get(i).get_posY(), nodes.get(i).r, nodes.get(i).r);
      stroke(0);
      fill(0);
      String s = "id: " + nodes.get(i).get_id() + "\r\nmass: " + nodes.get(i).get_mass();
      text(s,nodes.get(i).get_posX(),nodes.get(i).get_posY()-nodes.get(i).get_radius()/2);
    }
  }
  
  //timestep += TIMESTEP_SIZE;
  //vpMain.setW((float) width); vpMain.setH((float) height); // always need to re-size main viewport in draw() function
  //exit();
  System.out.printf("Energy: %f\n",total_energy);
}

