final int SENTINEL = -1;

int curr_node_dragging  = SENTINEL;

void mousePressed()
{
  for (int i = 0; i < nodes.size(); i++) {
    if (isect(i)) {
      curr_node_dragging = i;
      break;  
    }
  }  
}

void mouseDragged()
{ 
  if (curr_node_dragging != SENTINEL) {
    isSimRunning = true;
    nodes.get(curr_node_dragging).set_pos(mouseX, mouseY);
  }
}

void mouseReleased() 
{
  curr_node_dragging = SENTINEL;
}

boolean isect(int node_id) {
  Node node = nodes.get(node_id);
  if (abs(node.get_posX() - mouseX) > node.get_radius()/2.0 || 
      abs(node.get_posY() - mouseY) > node.get_radius()/2.0)  {
    return false;
  } 
  else {
    return true;
  }
}

