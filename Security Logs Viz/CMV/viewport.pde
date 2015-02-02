public class Viewport {
	private float x, y, w, h;
	Viewport() {
		x = 0;
		y = 0;
		w = 1;
		h = 1;
	}
	Viewport(Viewport parent, float _x, float _y, float _w, float _h) {
		x = parent.getrelX() + _x/parent.getrelW();
		y = parent.getrelY() + _y/parent.getrelH();
		w = _w/parent.getrelW();
		h = _h/parent.getrelH();
	}

	public float getrelX() {return x;}
	public float getrelY() {return y;}
	public float getrelW() {return w;}
	public float getrelH() {return h;}
	public float getX() {return width * x;}
	public float getY() {return height * y;}
	public float getW() {return width * w;}
	public float getH() {return height * h;}
};