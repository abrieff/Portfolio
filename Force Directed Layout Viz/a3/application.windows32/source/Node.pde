class Node {
  float px, py, vx, vy, m, r;
  int id;
  float fx, fy, ax, ay;
  
  Node(float px_, float py_, float m_, float r_, int id_) { // x-pos, y-pos, mass, radius, (unique) id
    m = m_; px = px_; py = py_; r = r_; id = id_;
    fx = 0.0; fy = 0.0; vx = 0.0; vy = 0.0;
  }
  
  void update_pos() 
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
    
    px = px + vx * timestep + 0.5 * ax * timestep * timestep;
    py = py + vy * timestep + 0.5 * ay * timestep * timestep; 
    //println("after: vx " + vx + " vy " + vy + " ax " + ax + " ay " + ay + " m " + m + " timestep " + timestep + " px " + px + " py " + py);
    

    //println("ax calc " + (ax * timestep));
  }

  void set_pos(float px_, float py_) { px = px_; py = py_; }
  void set_vel(float vx_, float vy_) { vx = vx_; vy = vy_; }
  void set_acc(float ax_, float ay_) { ax = ax_; ay = ay_; }
  void set_force(float fx_, float fy_) { fx = fx_; fy = fy_; }
  void add_force(float fx_, float fy_) { fx = fx + fx_; fy = fy + fy_; }
  float get_posX() { return px; }
  float get_posY() { return py; }
  float get_velX() { return vx; }
  float get_velY() { return vy; }  
  float get_accX() { return ax; }
  float get_accY() { return ay; }  
  float get_forceX() { return fx; }
  float get_forceY() { return fy; } 
  float get_radius() { return r; }
  int get_id() { return id; }
  float get_mass() { return m; }
  
}
