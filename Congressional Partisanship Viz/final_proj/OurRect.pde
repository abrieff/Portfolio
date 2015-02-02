class OurRect {  
  color fc, ec;
  float x, y, w, h;
  
  OurRect(color fc0, float x_, float y_, float w_, float h_) {
    fc = fc0; ec = fc;
    x = x_; y = y_; w = w_; h = h_;
  }
  
  OurRect(color fc0, color ec0, float x_, float y_, float w_, float h_) {
    fc = fc0; ec = ec0;
    x = x_; y = y_; w = w_; h = h_;
  }
  
  void setColor (int r, int g, int b) {
    fc = color (r, g, b);
  }
  
  void setColor (color c0) {
    fc = c0;
  }
  
  color getColor() {return fc;}
  
  void draw(){
    fill(fc);
    stroke(fc);
    rect(x,y,w,h, h / 4);
  }
  
  boolean intersect (float mx, float my) {
    if((mx >= x) && (mx <= x+w) && (my >= y) && (my <= y+h)){
      return true;
    }
    else{
      return false;
    }
  }
  
  float getX() { return x; }
  float getY() { return y; }
  float getW() { return w; }
  float getH() { return h; }
  float getCenterX() { return x+w/2.0; }
  float getCenterY() { return y+h/2.0; }

}

