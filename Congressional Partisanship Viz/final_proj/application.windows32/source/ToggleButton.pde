class ToggleButton {
  OurRect r = null;
  String stringTrue, stringFalse;
  boolean state = true;

  ToggleButton(String State1, String State2, color c0, float x_, float y_, float w_, float h_) { // first state is true state, second state is false state
    stringTrue = State1; 
    stringFalse = State2;
    r = new OurRect(c0, c0, x_, y_, w_, h_);
  }

  boolean intersect (float mx, float my) {
    return r.intersect(mx, my);
  }

  String getT() {
    if (state) {
      return stringTrue;
    } else {
      return stringFalse;
    }
  }

  void setColor (int rr, int g, int b) {
    r.setColor(rr, g, b);
  }

  void toggle() {
    state = !state;
  }

  void draw() {
    if (r.intersect(mouseX, mouseY)) {
      setColor(130,130,130);
      cursor(HAND);
    } else {
      setColor(60,60,60);
    }
    r.draw();
    fill(255);
    textAlign(CENTER, CENTER);
    textSize(textSizeToggleButton);
    text(getT(), r.getCenterX(), r.getCenterY());
  }

  boolean getState() {
    return state;
  }
}

