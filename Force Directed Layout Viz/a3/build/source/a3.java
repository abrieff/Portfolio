import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.awt.event.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class a3 extends PApplet {

// following code creates an event handler for detecting window resizing
// from http://forum.processing.org/two/discussion/327/trying-to-capture-window-resize-event-to-redraw-background-to-new-size-not-working/p1

int currWidth, currHeight;

public void pre() {
  if (currWidth != width || currHeight != height) {
    isSimRunning = true;
    redraw();
    currWidth = width;
    currHeight = height;
  }
}

ArrayList<Node> nodes = null;
ArrayList<Edge> edges = null;
float timestep = 0.1f*1.0f/frameRate;
float total_energy = 1e5f;

float RADIUS_CONSTANT = 5;
float BORDER_SIZE = 20;
float NUM_NODES = 0;
float constantCoulomb = 1e7f;
float constantPotential = 1e-1f;
float constantDrag = 1e-2f;
float constantSpeedLimit = 1e3f;
float SPRING_CONSTANT = 1e2f;
//final float TIMESTEP_SIZE = .005;
final float TOTAL_ENERGY_THRESHOLD = 15e3f;
boolean isSimRunning = true;
String datafile = "data/data_big.csv";
//String datafile = "data/data_three_nodes.csv";

public void resetForces() { // do this first
  int n = nodes.size();

  for ( int i=0; i<n; i++ ) {
    nodes.get(i).set_force(0.0f, 0.0f);
  }
}

public void updateForcesPotential() { // do this after resetForces() and before updateForcesDampening()
  int n = nodes.size();
  float pxcenter = width/2, pycenter = height/2;
  float gxcenter = 0.0f, gycenter = 0.0f;

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

public void updateForcesCoulomb() { // do this after resetForces() and before updateForcesDampening()
  int n = nodes.size();

  for ( int i=0; i<n; i++ ) {
    float fx_on_i = 0.0f, fy_on_i = 0.0f;
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

public float sign(float x) {
  if(x == 0.0f) {
    return 0.0f;
  } else if( x > 0.0f ) {
    return 1.0f;
  } else {
    return -1.0f;
  }
}

public void updateForcesDrag() { // do this after coulomb, spring, and potential forces have been calculated
  int n = nodes.size();

  for ( int i=0; i<n; i++ ) {
    Node c = nodes.get(i);
    float vx = c.get_velX(), vy = c.get_velY();
    float vms = sq(vx) + sq(vy);
    nodes.get(i).add_force(-sign(vx)*vms*constantDrag, -sign(vy)*vms*constantDrag);
  }
}

public void updateForcesHookes()
{
  int n = nodes.size();
  int e = edges.size();
  int b_id = -1;
  Node a, b;
  for (int i = 0; i<n; i++)
  {
    a = nodes.get(i);
    float fx_on_i = 0.0f, fy_on_i = 0.0f;
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

public void updateBoundary() {
  float left_boundary = 0.0f, right_boundary = width, top_boundary = 0.0f, bottom_boundary = height;
  float boundary_padding = BORDER_SIZE;
  int n = nodes.size();

  for ( int i=0; i<n; i++ ) {
    Node c = nodes.get(i);
    if ( c.get_posX() < boundary_padding & c.get_velX() < 0 ) { // left going left
      c.set_pos(boundary_padding, c.get_posY());
      c.set_vel(0.0f, c.get_velY());
    }
    if ( c.get_posX() > right_boundary - boundary_padding & c.get_velX() > 0 ) { // right going right
      c.set_pos(right_boundary - boundary_padding, c.get_posY());
      c.set_vel(0.0f, c.get_velY());
    }    
    if ( c.get_posY() < boundary_padding & c.get_velY() < 0 ) { // up going up
      c.set_pos(c.get_posX(), boundary_padding);
      c.set_vel(c.get_velX(), 0.0f);
    }
    if ( c.get_posY() > bottom_boundary - boundary_padding & c.get_velY() > 0 ) { // down going down
      c.set_pos(c.get_posX(), bottom_boundary - boundary_padding);
      c.set_vel(c.get_velX(), 0.0f);
    }        
  }
}

public int getNodeIndex(int id_)
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

public void draw_edges()
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

public void update_total_energy()
{
  Node n = nodes.get(0);
  float vel = sqrt(n.get_velX() * n.get_velX() + n.get_velY() * n.get_velY());
  total_energy = 0.5f * n.get_mass() * vel * vel;

  for (int i = 1; i < nodes.size (); i++) {
    n = nodes.get(i);
    vel = sqrt(n.get_velX() * n.get_velX() + n.get_velY() * n.get_velY());
    total_energy += 0.5f * n.get_mass() * vel * vel;
  }
}

public void updatePositions() {
  //println("hereee");
  for (int i = 0; i < nodes.size (); i++) {
    if(i != curr_node_dragging ) {
      nodes.get(i).update_pos();
    }
  }
}

public void setup() {
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

public void draw() {
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

final int SENTINEL = -1;

int curr_node_dragging  = SENTINEL;

public void mousePressed()
{
  for (int i = 0; i < nodes.size(); i++) {
    if (isect(i)) {
      curr_node_dragging = i;
      break;  
    }
  }  
}

public void mouseDragged()
{ 
  if (curr_node_dragging != SENTINEL) {
    isSimRunning = true;
    nodes.get(curr_node_dragging).set_pos(mouseX, mouseY);
  }
}

public void mouseReleased() 
{
  curr_node_dragging = SENTINEL;
}

public boolean isect(int node_id) {
  Node node = nodes.get(node_id);
  if (abs(node.get_posX() - mouseX) > node.get_radius()/2.0f || 
      abs(node.get_posY() - mouseY) > node.get_radius()/2.0f)  {
    return false;
  } 
  else {
    return true;
  }
}

class Edge {
  int id1, id2;
  float l;
  
  Edge(int id1_, int id2_, float l_) {
    id1 = id1_; id2 = id2_; l = l_;
  }
  
  public void setLength(float l_) {l = l_;}
  public float getLength() { return l; }
  public int get_id1() { return id1; }
  public int get_id2() { return id2; }
  
  public boolean containsNode(int id) {
    
    if( id1 == id || id2 == id ) {
      return true;
    } else {
      return false;
    }
    
  }
  public int getConnector(int id_)
  {
    if (id1 == id_) return id2;
    else if (id2 == id_) return id1;
    else return -1;
  }

  
}
class Node {
  float px, py, vx, vy, m, r;
  int id;
  float fx, fy, ax, ay;
  
  Node(float px_, float py_, float m_, float r_, int id_) { // x-pos, y-pos, mass, radius, (unique) id
    m = m_; px = px_; py = py_; r = r_; id = id_;
    fx = 0.0f; fy = 0.0f; vx = 0.0f; vy = 0.0f;
  }
  
  public void update_pos() 
  {
    //println("before: vx " + vx + " vy " + vy + " ax " + ax + " ay " + ay + " m " + m + " timestep " + timestep + " px " + px + " py " + py);
    ax = fx / m;
    ay = fy / m;
    
    vx = vx + ax * timestep;
    vy = vy + ay * timestep;
    
    float vm = sqrt( sq(vx) + sq(vy) );
    if( vm > constantSpeedLimit ) {
      vx = vx * constantSpeedLimit / vm;
      vy = vy * constantSpeedLimit / vm;
    }
    
    px = px + vx * timestep + 0.5f * ax * timestep * timestep;
    py = py + vy * timestep + 0.5f * ay * timestep * timestep; 
    //println("after: vx " + vx + " vy " + vy + " ax " + ax + " ay " + ay + " m " + m + " timestep " + timestep + " px " + px + " py " + py);
    

    //println("ax calc " + (ax * timestep));
  }

  public void set_pos(float px_, float py_) { px = px_; py = py_; }
  public void set_vel(float vx_, float vy_) { vx = vx_; vy = vy_; }
  public void set_acc(float ax_, float ay_) { ax = ax_; ay = ay_; }
  public void set_force(float fx_, float fy_) { fx = fx_; fy = fy_; }
  public void add_force(float fx_, float fy_) { fx = fx + fx_; fy = fy + fy_; }
  public float get_posX() { return px; }
  public float get_posY() { return py; }
  public float get_velX() { return vx; }
  public float get_velY() { return vy; }  
  public float get_accX() { return ax; }
  public float get_accY() { return ay; }  
  public float get_forceX() { return fx; }
  public float get_forceY() { return fy; } 
  public float get_radius() { return r; }
  public int get_id() { return id; }
  public float get_mass() { return m; }
  
}
BufferedReader reader;
//helper function to clean up reading lines
public String getLine(BufferedReader reader){
  String tmpline;
  try {
       tmpline = reader.readLine();
  } catch (IOException e) {
       tmpline = null;
  }
  return tmpline;

}
public void readFile(String fname){

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
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "a3" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
