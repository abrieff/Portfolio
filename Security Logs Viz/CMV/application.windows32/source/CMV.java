import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.Iterator; 
import java.lang.Iterable; 
import java.util.Collections; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class CMV extends PApplet {

public void setup() {
    size(PApplet.parseInt(displayHeight * 0.925f), PApplet.parseInt(displayHeight * 0.925f)); //, "processing.core.PGraphicsRetina2D");
    background(255, 255, 255);
    initSettings();
    frame.setResizable(true);
    frame.setTitle("CMV on - " + path);
}

public void draw() {
    if (isResized()) {
        contrl.setPosition();
    }

    background(255, 255, 255);
    textSize(fontSize);
    textLeading(lineHeight);

    contrl.hover();

    contrl.drawViews();
    contrl.drawSelectedArea();
    contrl.handleSelectedArea();
}

public void initSettings() {
    readData();
    marks = new boolean[data.getRowCount()];
    contrl = new CMVController();
    contrl.initViews();
}

public boolean isResized() {
    if (pw != width || ph != height) {
        pw = width;
        ph = height;
        return true;
    }
    return false;
}

public void mouseClicked(MouseEvent e) {
    contrl.cleanSelectedArea();
    pressPos = null;
}

public void mouseDragged(MouseEvent e) {
    if (e.getButton() == RIGHT) {
        contrl.cleanSelectedArea();
        return;
    }
    if (pressPos != null) {
        contrl.setSelectedArea(pressPos.x, pressPos.y, mouseX, mouseY);
    }  
}

public void mousePressed(MouseEvent e) {
    contrl.cleanSelectedArea();
    pressPos = new PVector(mouseX, mouseY);
    contrl.setSelectedArea(pressPos.x, pressPos.y, mouseX, mouseY);
}


public void readData(){
   data = loadTable(path, "header");
   header = data.getColumnTitles();
}


  

abstract class AbstractView {
    protected float leftX = -1;
    protected float leftY = -1;
    protected float w = -1;
    protected float h = -1;

    protected Controller contrl = null;
    protected String name = null;
    protected Table data = null;
    
    protected boolean[] marks = null;
    protected String[] header = null;

    public abstract void hover();
    public abstract void handleThisArea(Rectangle rect);
    public abstract void display();

    AbstractView() {
    }

    public AbstractView setController(Controller contrl) {
        this.contrl = contrl;
        return this;
    }

    public AbstractView setMarks(boolean[] ms) {
        this.marks = ms;
        return this;
    }

    public AbstractView setName(String name) {
        this.name = name;
        return this;
    }

    public AbstractView setDataSrc(Table t, String[] str, boolean[] marks) {
        this.data = t;
        this.header = str;
        this.marks = marks;
        return this;
    }

    public AbstractView setPosition(float x, float y) {
        this.leftX = x;
        this.leftY = y;
        return this;
    }

    public AbstractView setSize(float w, float h) {
        this.w = w;
        this.h = h;
        return this;
    }

    public void sendMsg(Message msg) {
        if(contrl != null){
            contrl.receiveMsg(msg);
        }
    }
    
    public boolean isOnMe() {
        return mouseX >= leftX && mouseX <= (leftX + w) && mouseY >= leftY && mouseY <= (leftY + h);
    }

    public boolean isIntersected(Rectangle rect1, Rectangle rect2) {
       boolean flag1 = abs(rect2.p2.x + rect2.p1.x - rect1.p2.x - rect1.p1.x) - (rect1.p2.x - rect1.p1.x + rect2.p2.x - rect2.p1.x) <= 0;
       boolean flag2 = abs(rect2.p2.y + rect2.p1.y - rect1.p2.y - rect1.p1.y) - (rect1.p2.y - rect1.p1.y + rect2.p2.y - rect2.p1.y) <= 0;
       return flag1 && flag2;
    }

    public Rectangle getIntersectRegion(Rectangle rect) {
        Rectangle rect2 = new Rectangle(leftX, leftY, leftX + w, leftY + h);
        return getIntersectRegion(rect, rect2);
    }

    private Rectangle getIntersectRegion(Rectangle rect1, Rectangle rect2){
          if(isIntersected(rect1, rect2)){
              float x1 = max(rect1.p1.x, rect2.p1.x);
              float y1 = max(rect1.p1.y, rect2.p1.y);
              float x2 = min(rect1.p2.x, rect2.p2.x);
              float y2 = min(rect1.p2.y, rect2.p2.y);
              return new Rectangle(x1, y1, x2, y2);
          }
          return null;
     }
}
class BarGraphView extends AbstractView {
	
    float[] xArray = null;
    float[] yArray = null;
    int[] xVals = null;
    String[] yVals1 = null;
    String[] yVals2 = null;
    String[] yVals3 = null;
    String[] xTitles = null;
    String yTitle = null;
    String[] keys1 = null;
    String[] keys2 = null;
    String[] keys3 = null;
    IntDict intdict1 = null;
    IntDict intdict2 = null;
    IntDict intdict3 = null;

    float xMax = -1;
    float yMax = -1;
    float xMin = -1;
    float yMin = -1;

    int xIndex = -1;
    int yIndex = -1;

    BarGraph bg = null;

    ArrayList<BarGraph> bgs = null;

    // get the width of bars for drawing
    public float getWidth() {
        return w / 40.0f;
    }

    // this deals with selection when items are under the mouse cursor
    public void hover() {
        // create the highlight Conditions to send as a message to all other scatter plots
        // through the Controller using the messages architecture
        // (highlight based on square surrounding the point with width 2*radius)

        int sumxs = 0;
        float my_uly, my_h;
        BarGraph bg;
        for (int j = 0; j < bgs.size(); j++) {
            bg = bgs.get(j);
            for (int i = 0; i < bg.my_data.length; i++) {
                sumxs += bg.my_data[i] * bg.x_inc;
                my_uly = bg.xaxis_ycoor - sumxs;
                my_h = bg.my_data[i] * bg.x_inc;
                if (bg.intersect(bg.ulx, my_uly, bg.b_w, my_h)) {
                    Message msg = new Message();
                    msg.setSource("bgs");
                    Condition[] conds = new Condition[1];
                    Condition cond = new Condition(bg.col_name, "=", bg.my_vals[i]);
                    conds[0] = cond;
                    msg.setConditions(conds);
                    msg.setAction("normal");
                    sendMsg(msg);
                }
            }
            sumxs = 0;
        }

      /*  Condition cond1 = new Condition(xTitle, "<=", inverseToXReal(mouseX + getRadius()));
        Condition cond2 = new Condition(xTitle, ">=", inverseToXReal(mouseX - getRadius()));
        Condition cond3 = new Condition(yTitle, "<=", inverseToYReal(mouseY - getRadius()));
        Condition cond4 = new Condition(yTitle, ">=", inverseToYReal(mouseY + getRadius()));
        Condition[] conds = new Condition[4];
        conds[0] = cond1;
        conds[1] = cond2;
        conds[2] = cond3;
        conds[3] = cond4;
         
        // Finish this:
        // Send a message to the Controller to provide the current conditions for highlighting
        // 1. create a new message instance (see Message.pde)
        // 2. set the source of this message (see Message.pde)
        // 3. set the conditions of this message (see Message.pde)
        // 4. send the message (see AbstractView.pde)
        Message msg = new Message();
        msg.setSource(name)
        .setConditions(conds);
        sendMsg(msg);*/
    }

    // handle sending messages to the Controller when a rectangle is selected
    public void handleThisArea(Rectangle rect) {
        // this rectangle holds the _pixel_ coordinates of the selection rectangle 
        Rectangle rectSub = getIntersectRegion(rect);
        String[] bars;
        BarGraph bg;
        ArrayList<Condition> conds = new ArrayList<Condition>();
        Condition[] conds_arr;
        Condition cond;

        if (rectSub != null) {
            for (int i = 0; i < 3; i++ ){ 
                bg = bgs.get(i);
                bars = bg.intersectedBarTitles(rectSub);
                for (int j = 0; j < bars.length; j++) {
                    cond = new Condition(bg.col_name, "=", bars[j]);
                    conds.add(cond);
               }
            }

            conds_arr = new Condition[conds.size()];
            for (int i = 0 ; i< conds.size(); i++) {
                conds_arr[i] = conds.get(i);
            }
            // Finish this:
            // Create the conditions for what points to highlight to send
            // in the message below. (reference Condition.pde, Rectangle.pde,
            // hover function above)
            // The getIntersectRegion() call above gets you the selection rectangle
            // in the current view.
            
            // send out the message
            Message msg = new Message();

            msg.setSource(name)
               .setConditions(conds_arr);
            sendMsg(msg);
        }
    }

    public void display() {
        pushStyle();
        stroke(0);
        strokeWeight(1);
        fill(255);
        rectMode(CORNER);

        BarGraph bar_graph;

        //rect(leftX, leftY, w, h);
        String key_val;

        int count = 0;

        for (int i = 0; i < marks.length; i++) {
            if (marks[i]) {
                key_val = yVals1[i];
                intdict1.set(key_val, intdict1.get(key_val) + 1);

                key_val = yVals2[i];
                intdict2.set(key_val, intdict2.get(key_val) + 1);

                key_val = yVals3[i];
                intdict3.set(key_val, intdict3.get(key_val) + 1);
            }
        }

        bar_graph = bgs.get(0);
        bar_graph.b_w = w/5;
        bar_graph.b_h = h;
        bar_graph.ulx = leftX;
        bar_graph.lly = leftY + h;
        bar_graph.to_highlight = intdict1.valueArray();
        bar_graph.draw();
        fill(0);
        textSize(9);
        text(xTitles[0], leftX + w*.1f, leftY + h +h*.05f);

        bar_graph = bgs.get(1);
        bar_graph.b_w = w/5;
        bar_graph.b_h = h;
        bar_graph.ulx = leftX + 2 * w/5;
        bar_graph.lly = leftY + h;
        bar_graph.to_highlight = intdict2.valueArray();
        bar_graph.draw();
        fill(0);
        text(xTitles[1], leftX + 2 * w/5 + w*.1f, leftY+h+h*.05f);

        bar_graph = bgs.get(2);
        bar_graph.b_w = w/5;
        bar_graph.b_h = h;
        bar_graph.ulx = leftX + 4*w/5;
        bar_graph.lly = leftY + h;
        bar_graph.to_highlight = intdict3.valueArray();
        bar_graph.draw();
        fill(0);
        text(xTitles[2], leftX + 4*w/5 + w*.1f, leftY + h+h *.05f);

        line(leftX - w*.1f, leftY + h, leftX + w +w*.1f, leftY + h);
        popStyle();

        String[] key_arr = intdict1.keyArray();
        for (int i = 0; i < key_arr.length; i++) {
            intdict1.set(key_arr[i], 0);
        }

        key_arr = intdict2.keyArray();

        for (int i = 0; i < key_arr.length; i++) {
            intdict2.set(key_arr[i], 0);
        }

        key_arr = intdict3.keyArray();
        for (int i = 0; i < key_arr.length; i++) {
            intdict3.set(key_arr[i], 0);
        }
    }

/*
    public ScatterplotView setXYIndice(int x, int y) {
        this.xIndex = x;
        this.yIndex = y;
        return this;
    }
*/
    // set the indice of columns that this view can see
    public BarGraphView setData(String[] colArray, String[] colArray2, String[] colArray3) {
    	IntDict hm = new IntDict();
        IntDict hm1 = new IntDict();
        IntDict hm2 = new IntDict();

        yVals1 = colArray;
        yVals2 = colArray2;
        yVals3 = colArray3;

        bgs = new ArrayList<BarGraph>(3);

    	for (int i = 0; i < colArray.length; i++) {
    		hm.add(colArray[i], 1);
    	}

        keys1 = hm.keyArray();
        BarGraph bg = new BarGraph(hm.valueArray(), leftX, leftY + h, w / 5, h, hm.keyArray(), xTitles[0]);

        for (int i = 0; i < colArray2.length; i++) {
            hm1.add(colArray2[i], 1);
        }

        keys2 = hm1.keyArray();
        BarGraph bg2 = new BarGraph(hm1.valueArray(), leftX + 2*w/5, leftY + h, w/5, h, hm1.keyArray(), xTitles[1]);

        for (int i = 0; i < colArray3.length; i++) {
            hm2.add(colArray3[i], 1);
        }

        keys3 = hm2.keyArray();
        BarGraph bg3 = new BarGraph(hm2.valueArray(), leftX + 4*w/5, leftY + h, w/5, h, hm2.keyArray(), xTitles[2]);

        intdict1 = hm;
        intdict2 = hm1;
        intdict3 = hm2;
        bgs.add(bg);
        bgs.add(bg2);
        bgs.add(bg3);

        return this;
    }


    public BarGraphView setTitles(String xStr, String xStr1, String xStr2) {
        xTitles = new String[3];

        xTitles[0] = xStr;
        xTitles[1] = xStr1;
        xTitles[2] = xStr2;
        return this;
    }

    public BarGraphView initXYRange() {
        xMin = 0;//min(xArray);
        yMin = 0;//min(yArray);
        //yMax = max(yVals) * 1.2;

        return this;
    }
/*
    float xScale(float x) {
        return leftX + (x - xMin) / (xMax - xMin) * w;
    }

    float yScale(float y) {
        return leftY + h - ((y - yMin) / (yMax - yMin) * h);
    }

    // convert from pixel coordinates to data coordinates
    float inverseToXReal(float px) {
        return (px - leftX) / w * (xMax - xMin) + xMin;
    }

    // convert from pixel coordinates to data coordinates
    float inverseToYReal(float py) {
        return (h - (py - leftY)) / h * (yMax - yMin) + yMin;
    }
*/


}
class Condition {
    String col = null;
    String operator = null;
    String value = null; 

    // create a new Condition object that specifies some data column
    // should have some relationship to some value
    //   col: column name of data the relationship applies to
    //   op: operator (e.g. "<=")
    //   value: value to compare to
    Condition(String col, String op, String value) {
        this.col = col;
        this.operator = op;
        this.value = value;
    }
    
    public String toString() {
        return col + " " + operator + " " + value + " ";
    }
    
    public boolean equals(Condition cond){
        return operator.equals(cond.operator) && 
        value.equals(cond.value) && 
        col.equals(cond.col);
    }
}


public boolean checkConditions(Condition[] conds, TableRow row) {
    if(conds == null || row == null){
        return false;
    }
    boolean and = true;
    for (int i = 0; i < conds.length; i++) {
        and = and && checkCondition(conds[i], row);
    }
    return and;
}

public boolean checkCondition(Condition cond, TableRow row) {
    if (cond.operator.equals("=")) { // no need to know col
        String cur = row.getString(cond.col);
        return cur.equals(cond.value);
    }
    return false;
}
  /*  if (cond.operator.equals("<=")) {
        float cur = row.getFloat(cond.col);
        return cur - cond.value <= 0.0001;
    }

    if (cond.operator.equals(">=")) {
        float cur = row.getFloat(cond.col);
        return cur - cond.value >= -0.0001;
    }
    
    */



abstract class Controller {
    protected ArrayList < AbstractView > vizs = null;
    protected Rectangle selectArea = null;
    protected Message preMsg = null;

    public abstract void receiveMsg(Message msg);
    public abstract void initViews();
    public abstract void setPosition();
    public abstract void handleSelectedArea();

    public void hover() {
        for (AbstractView v: vizs) {
          
            if (v.isOnMe()) {
                v.hover();
                break;
            }
        }
    }

    public void drawSelectedArea() {
        pushStyle();
        if (selectArea != null) {
            fill(selectColor);
            stroke(selectColor);
            rectMode(CORNER);
            rect(selectArea.p1.x, selectArea.p1.y,
                selectArea.p2.x - selectArea.p1.x, selectArea.p2.y - selectArea.p1.y);

        }
        popStyle();
    }

    public void drawViews() {
        for (AbstractView v: vizs) {
            v.display();
        }
    }

    public void cleanSelectedArea() {
        if (selectArea != null) {
            Message msg = new Message();
            msg.setSource("controller")
                .setAction("clean");
            receiveMsg(msg);
            selectArea = null;
        }
    }

    public void setSelectedArea(float x, float y, float x1, float y1) {
        selectArea = new Rectangle(x, y, x1, y1);
    }

    public void resetMarks() {
        // marks are global
        marks = new boolean[data.getRowCount()];
    }

    public void setMarksOfViews(){
        for (AbstractView abv: vizs) {
            abv.setMarks(marks);
        }
    }
}

class CMVController extends Controller {
    CMVController() {
        vizs = new ArrayList < AbstractView > ();
        selectArea = null;
    }

    public void initViews() {
        int row = data.getRowCount();
        int col = data.getColumnCount();

        float curX = margin, curY = margin;
        float xSeg = (width - margin * 2) / col;
        float ySeg = (height - margin * 2) / col;
        
        HeatmapView hmView = new HeatmapView();
        hmView
          .setController(this)
          .setName("hm")
          .setPosition(0, height/2)
          .setSize(width, height/4)
          .setMarks(marks)
          ;
        hmView.setData(data.getStringColumn(0),data.getStringColumn(4))
                .setTitles(header[0], header[4])
        //      .setXYIndice(0, 0)
                .initMinMaxRange()
              ;
              vizs.add(hmView);
              
        FDLView fdlView = new FDLView();
        fdlView
          .setController(this)
          .setPosition(0,0)
          .setSize(3*width/4,3*height/4)
          .setMarks(marks)
          .setName("fdl")
          ;
        fdlView
          .setData(data.getStringColumn(1),data.getStringColumn(3))
          .setSources(header[1],header[3]);
          ;
       
       vizs.add(fdlView);
        
        BarGraphView bgView = new BarGraphView();
        bgView
          .setController(this)
          .setPosition(width - width/4, height / 5)
          .setSize(width/5, height/2)
          .setMarks(marks)
          .setName("bgs")
          ;
        bgView.setTitles(header[5], header[6], header[7])
              .setData(data.getStringColumn(5), data.getStringColumn(6), data.getStringColumn(7))
              .initXYRange()

          ;

        vizs.add(bgView);

        /* for (int i = 0; i < col; i++) {
            for (int j = 0; j < col; j++) {
                float[] xArray = data.getFloatColumn(i);
                float[] yArray = data.getFloatColumn(j);

                ScatterplotView spView = new ScatterplotView();
                spView
                    .setController(this)
                    .setName(i + "-" + j)
                    .setPosition(curX + i * xSeg, curY + j * ySeg)
                    .setSize(xSeg, ySeg)
                    .setMarks(marks)
                    ;

                spView.setData(xArray, yArray)
                    .setTitles(header[i], header[j])
                    .setXYIndice(i, j)
                    .initXYRange()
                    ;
              
                vizs.add(spView);
            }
        }*/
    }

    public void setPosition() {
   //     int row = data.getRowCount();
     //   int col = data.getColumnCount();

        float curX = margin, curY = margin;
       // float xSeg = (width - margin * 2.0) / col;
        //float ySeg = (height - margin * 2.0) / col;
        AbstractView hpView = vizs.get(0);
        hpView.setPosition(0, 3*height/4);
        hpView.setSize(width, height/4);
        
        AbstractView fdlAView = vizs.get(1);
        fdlAView.setSize(3*width/4,3*height/4);

        AbstractView b1View = vizs.get(2);
        b1View.setPosition(width - width/4,height/5);
        b1View.setSize(width/5, height/2);

    }

    public void receiveMsg(Message msg) {
        if (msg.equals(preMsg)) {
            return;
        }

        preMsg = msg;

        if (msg.action.equals("clean")) {
            resetMarks();
            setMarksOfViews();
            return;
        }

        Iterator it = data.rows().iterator();
        int index = 0;
        while (it.hasNext()) {
            if (checkConditions(msg.conds, (TableRow) it.next())) {
                marks[index] = true;
            } 
            index++;
        }
        setMarksOfViews();
    }


    public void handleSelectedArea() {
        Message msg = new Message();
        msg.action = "clean";
        receiveMsg(msg);

        if (selectArea != null) {
            for (AbstractView absv: vizs) {
                absv.handleThisArea(selectArea);
            }
        }
    }
}
public int indexofEdge(ArrayList<Edge> edges, Edge e) {
  
  for( int i=0; i<edges.size(); i++ ) {
    if( edges.get(i).containsNode(e.get_id1()) & edges.get(i).containsNode(e.get_id2()) ) {
      return i;
    }
  }
  
  return -1;
}

public ArrayList<Integer> indicesofEdgesWithNodeId(ArrayList<Edge> edges, int id) {
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
  
  public void setLength(float l_) {l = l_;}
  public float getLength() { return l; }
  public int get_id1() { return id1; }
  public int get_id2() { return id2; }
  public void setWeight(float w_) {w = w_;}
  public float getWeight() { return w; }
  public void addWeight(float w_) {w += w_;}
  
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
final float timestep = 1.0f/frameRate*0.1f;
final float constantSpeedLimit = 1e3f;

class FDL {
  ArrayList<Node> nodes = null;
  ArrayList<Edge> edges = null;

  final int colfNode = color(0, 0, 255);
  final int colsNode = color(0, 0, 0);
  final int colfHiNode = color(100, 255, 100); //color(0, 191, 255);
  final int colsHiNode = color(0, 0, 0);
  final int colsEdge = color(0, 0, 0);
  final int colsHiEdge = color(100, 255, 100); //color(0, 191, 255);
  final int colfHiText = color(0, 0, 0);
  final float constantCoulomb = 5e8f;
  final float constantPotential = 1e0f;
  final float constantDrag = 1e-2f;
  final float SPRING_CONSTANT = 1e3f;
  final float constantDampening = (1-5e-1f);
  final float TOTAL_ENERGY_THRESHOLD = 25e3f;
  final float edgeWidthMax = 8, edgeWidthMin = 1; // in pixels
  final float strwNode = 1.5f;
  final float textHeightAboveNode = 5;
  final float textWidthMult = 1.5f, textHeightMult = 1.2f;

  float BORDER_SIZE = 20.0f;
  float edgeWeightMax = 0, edgeWeightMin = 0;
  float total_energy = 1e5f;
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

  public void setPosition(float tlx_, float tly_) {
    if ( tlx_ != tlx | tly_ != tly ) {
      setSimRunning(true);
    }
    tlx = tlx_; 
    tly = tly_;
  }

  public void setSize(float wid_, float hei_) {
    if ( wid_ != wid | hei_ != hei ) {
      setSimRunning(true);
    }
    wid = wid_; 
    hei = hei_;
  }

  public boolean isect(int node_id) {
    Node node = nodes.get(node_id);
    if (abs(node.get_posX() - mouseX) > node.get_radius()/2.0f || 
      abs(node.get_posY() - mouseY) > node.get_radius()/2.0f) {
      return false;
    } else {
      return true;
    }
  }
  
  public ArrayList<Integer> getNodeIdsWithinRect(Rectangle r) {
    ArrayList<Integer> n = new ArrayList<Integer>();
    
    for( int i=0; i<nodes.size(); i++ ) {
      float nx = nodes.get(i).get_posX(); float ny = nodes.get(i).get_posY();
      if(nx>=r.p1.x & nx<=r.p2.x & ny>=r.p1.y & ny<=r.p2.y) {
        n.add(i);
      }
    }
    
    return n;
  }

  public void resetForces() { // do this first
    int n = nodes.size();

    for ( int i=0; i<n; i++ ) {
      nodes.get(i).set_force(0.0f, 0.0f);
    }
  }

  public void updateForcesPotential() { // do this after resetForces() and before updateForcesDampening()
    int n = nodes.size();
    float pxcenter = tlx + wid/2, pycenter = tly + hei/2;
    float gxcenter = 0.0f, gycenter = 0.0f;

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
    if (x == 0.0f) {
      return 0.0f;
    } else if ( x > 0.0f ) {
      return 1.0f;
    } else {
      return -1.0f;
    }
  }

  public void updateForcesDampening() { // do this after coulomb, spring, and potential forces have been calculated
    int n = nodes.size();

    for ( int i=0; i<n; i++ ) {
      Node c = nodes.get(i);
      float fx = c.get_forceX(), fy = c.get_forceY();
      nodes.get(i).set_force(fx*constantDampening, fy*constantDampening);
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

  public void updateBoundary() {
    float left_boundary = tlx, right_boundary = tlx + wid, top_boundary = tly, bottom_boundary = tly + hei;
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
    for (int i = 0; i < nodes.size (); i++) {
      nodes.get(i).update_pos();
    }
  }

  public void draw_edges(ArrayList<Integer> ids)
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

  public void draw_nodes(ArrayList<Integer> ids) {

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

  public void draw(ArrayList<Integer> idsMarkedNodes, ArrayList<Integer> idsMarkedEdges) {
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
  
  public boolean isSimRunning() { return bSimRunning; }
  public void setSimRunning(boolean b) { bSimRunning = b; }
  
}

class FDLView extends AbstractView {
  ArrayList<Node> nodes = null;
  ArrayList<Edge> edges = null;
  String[] nodesFrom = null, nodesTo = null;
  final float massDefault = 1.0f, radiusDefault = 20.0f, borderDefault = radiusDefault, lengthDefault = 200;
  FDL fdl = null;
  String[] namesSources = new String[2];
  ArrayList<Integer> idsMarkedNodes = new ArrayList<Integer>(), idsMarkedEdges = new ArrayList<Integer>();
  boolean selfHighlightHover = false, selfHighlightRect = false;

  // this deals with selection when items are under the mouse cursor
  public void hover() {
    // create the highlight Conditions to send as a message to all other plots
    // through the Controller using the messages architecture

    boolean nodeIntersected = false;
    String nodeSentinel = "boo123@";

    // find the node the mouse is over, if any
    for ( int id=0; id<nodes.size (); id++ ) {
      if ( fdl.isect(id) ) {
        nodeIntersected = true;
        selfHighlightHover = true;
        if(!selfHighlightRect) {
          idsMarkedNodes = new ArrayList<Integer>();
        }
        idsMarkedNodes.add(id);

        // first send condition for "from" node
        Condition[] conds1 = new Condition[1];
        conds1[0] = new Condition(namesSources[0], "=", nodes.get(id).get_label());
        Message msg1 = new Message();
        msg1.setSource(name)
          .setAction("normal")
            .setConditions(conds1);
        sendMsg(msg1);

        // then send condition from "to" node
        Condition[] conds2 = new Condition[1];
        conds2[0] = new Condition(namesSources[1], "=", nodes.get(id).get_label());
        Message msg2 = new Message();
        msg2.setSource(name)
          .setAction("normal")
            .setConditions(conds2);
        sendMsg(msg2);

        break; // only one node can be hovered over at a time
      }
    }

    if (!nodeIntersected) {
      selfHighlightHover = false;
      if(!selfHighlightRect) {
        idsMarkedNodes = new ArrayList<Integer>();
        Condition[] conds1 = new Condition[1];
        conds1[0] = new Condition(namesSources[0], "=", nodeSentinel);
        Message msg1 = new Message();
        msg1.setSource(name)
          .setAction("normal")
            .setConditions(conds1);
        sendMsg(msg1);
      }
    }
  }

  // handle sending messages to the Controller when a rectangle is selected
  public void handleThisArea(Rectangle rect) {
    // this rectangle holds the _pixel_ coordinates of the selection rectangle 
    Rectangle rectSub = getIntersectRegion(rect);
    String nodeSentinel = "boo123@";

    if (rectSub != null) {
      idsMarkedNodes = fdl.getNodeIdsWithinRect(rectSub);       
      
      if (idsMarkedNodes.size()>0) { // node inside rect
        selfHighlightRect = true;
        for ( int i=0; i<idsMarkedNodes.size (); i++ ) {
          int id = idsMarkedNodes.get(i);

          // first send condition for "from" node
          Condition[] conds1 = new Condition[1];
          conds1[0] = new Condition(namesSources[0], "=", nodes.get(id).get_label());
          Message msg1 = new Message();
          msg1.setSource(name)
            .setAction("normal")
              .setConditions(conds1);
          sendMsg(msg1);

          // then send condition from "to" node
          Condition[] conds2 = new Condition[1];
          conds2[0] = new Condition(namesSources[1], "=", nodes.get(id).get_label());
          Message msg2 = new Message();
          msg2.setSource(name)
            .setAction("normal")
              .setConditions(conds2);
          sendMsg(msg2);
        }
      } else { // no nodes inside rect
        selfHighlightRect = false;
        if(!selfHighlightHover) {
          idsMarkedNodes = new ArrayList<Integer>();
          Condition[] conds1 = new Condition[1];
          conds1[0] = new Condition(namesSources[0], "=", nodeSentinel);
          Message msg1 = new Message();
          msg1.setSource(name)
            .setAction("normal")
              .setConditions(conds1);
          sendMsg(msg1);
        }
      }
    }
  }

  public void display() {

    fdl.setSize(w, h);

    pushStyle();

    if (!(selfHighlightHover|selfHighlightRect)) { // if highlighting by other vizes, convert marks to marked nodes and edges
      idsMarkedNodes = new ArrayList<Integer>();
      idsMarkedEdges = new ArrayList<Integer>();
      for ( int i=0; i<marks.length; i++ ) {
        if (marks[i]) {
          int id1 = indexofNodeLabel(nodes, nodesFrom[i]);
          if ( !idsMarkedNodes.contains(id1) ) {
            idsMarkedNodes.add(id1);
          }
          int id2 = indexofNodeLabel(nodes, nodesTo[i]);
          if ( !idsMarkedNodes.contains(id2) ) {
            idsMarkedNodes.add(id2);
          }
          int ide = indexofEdge(edges, new Edge(id1, id2, 0, 0));
          if ( !idsMarkedEdges.contains(ide) ) {
            idsMarkedEdges.add(ide);
          }
        }
      }
    } else { // self-highlighting
      // highlight edges emanating from (self-)marked nodes
      idsMarkedEdges = new ArrayList<Integer>();
      for ( int i=0; i<idsMarkedNodes.size (); i++ ) {
        ArrayList<Integer> l = indicesofEdgesWithNodeId(edges, idsMarkedNodes.get(i));
        for ( int j=0; j<l.size (); j++ ) {
          if ( !idsMarkedEdges.contains(l.get(j)) ) {
            idsMarkedEdges.add(l.get(j));
          }
        }
      }
    }
    
//          for( int i=0;i<idsMarkedNodes.size(); i++ ) {
//        System.out.printf("%d ",idsMarkedNodes.get(i));
//      }
//      println("");

    // draw
    fdl.draw(idsMarkedNodes, idsMarkedEdges);

    popStyle();
  }

  public FDLView setSources(String source1, String source2) {

    namesSources[0] = source1; 
    namesSources[1] = source2;

    return this;
  }

  public FDLView setData(String[] nodesFrom, String[] nodesTo) {
    this.nodesFrom = nodesFrom;
    this.nodesTo = nodesTo;

    nodes = new ArrayList<Node>();
    ArrayList<String> nodesAll = new ArrayList<String>();
    Collections.addAll(nodesAll, unique(concatStringArrays(nodesFrom, nodesTo)));
    for ( int i=0; i<nodesAll.size (); i++ ) {
      float px = random(leftX+w/4, leftX+w-borderDefault-w/4);
      float py = random(leftY+h/4, leftY+h-borderDefault-h/4);
      nodes.add(new Node(px, py, massDefault, radiusDefault, i, nodesAll.get(i)));
    }

    edges = new ArrayList<Edge>();
    String[] sa = new String[nodesFrom.length];
    for ( int i=0; i<nodesFrom.length; i++ ) {
      sa[i] = nodesFrom[i] + "," + nodesTo[i];
    }
    ArrayList<String> sal = new ArrayList<String>();
    Collections.addAll(sal, sa);
    for ( int i=0; i<sal.size (); i++ ) {
      if ( sal.get(i) != null ) {
        int weight = Collections.frequency(sal, sal.get(i));
        String[] thesenodes = sal.get(i).split(",");
        int idNode1 = indexofNodeLabel(nodes, thesenodes[0]);
        int idNode2 = indexofNodeLabel(nodes, thesenodes[1]);
        int indexEdge = indexofEdge(edges, new Edge(idNode1, idNode2, 0, 0));
        if ( indexEdge == -1 ) { // new edge
          edges.add(new Edge(idNode1, idNode2, lengthDefault, weight));
        } else {
          edges.get(indexEdge).addWeight(weight);
        }
        Collections.replaceAll(sal, sal.get(i), null);
      }
    }

    fdl = new FDL(nodes, edges, leftX, leftY, w, h, borderDefault);

    return this;
  }
}

class HeatmapView extends AbstractView {
    String[] xArray = null;
    String[] yArray = null;
    IntDict xuMarks, yuMarks;
    String[] xUnique = null;
    String[] yUnique = null;
    IntDict valueArray = null;
    String xTitle = null;
    String yTitle = null;

    int min = -1;
    int max = -1;

    int xIndex = -1;
    int yIndex = -1;

    // get the radius of points for drawing
    public float getRadius() {
        return w / 70.0f;
    }
    public float getWidth()
    {
      return w/(xUnique.length+3);
    }
    public float getHeight()
    {
      return h/yUnique.length;
    }
    // this deals with selection when items are under the mouse cursor
    public void hover() {
        // create the highlight Conditions to send as a message to all other scatter plots
        // through the Controller using the messages architecture
        // (highlight based on square surrounding the point with width 2*radius)
      PVector tP = getTablePosition();
        if (tP.x != -1 && tP.y != -1) {
            Condition cond1 = new Condition(xTitle, "=", xUnique[(int) tP.x]);
            Condition cond2 = new Condition(yTitle, "=", yUnique[(int) tP.y]);
            Condition[] conds = new Condition[2];
            conds[0] = cond1;
            conds[1] = cond2;
         
        // Finish this:
        // Send a message to the Controller to provide the current conditions for highlighting
        // 1. create a new message instance (see Message.pde)
        // 2. set the source of this message (see Message.pde)
        // 3. set the conditions of this message (see Message.pde)
        // 4. send the message (see AbstractView.pde)
        Message msg = new Message();
        msg.setSource(name)
        .setConditions(conds);
        sendMsg(msg);
    }
    }
    public PVector getTablePosition()
    {
      PVector tP = new PVector(); //table Position
      PVector mP = new PVector(mouseX, mouseY); //mousePosition
      float cleft = 0;
      float cright = 0;
      float cdown = 0;
      float cup = 0;
      tP.x = -1; //If not in table
      tP.y = -1; //If not in table
      if ( (mP.x >= (leftX + (getWidth()*3))) && (mP.x <= (leftX + w)) && (mP.y >= leftY) && (mP.y <= (leftY + h))) //in bounds of table
      {
        for (int i = 0; i < xUnique.length; i++)
        {
           cleft = leftX + (i + 3) * (getWidth());
           cright = cleft + getWidth();
          if (mP.x >= cleft && mP.x <= cright)
          {
            tP.x = i;
          }
        }
        for (int i = 0; i < yUnique.length; i++)
        {
           cup = (leftY + (i * (getHeight())));
           cdown = cup + getHeight();
          if (mP.y >= cup && mP.y <= cdown)
          {
            tP.y = i;
          }
        }
      }
        return tP; 
    }
          

    // handle sending messages to the Controller when a rectangle is selected
    public void handleThisArea(Rectangle rect) {
        // this rectangle holds the _pixel_ coordinates of the selection rectangle 
        Rectangle rectSub = getIntersectRegion(rect);
        PVector tP = getTablePosition();
    /*    if (rectSub != null && tP.x != -1 && tP.y != -1) {
            Condition cond1 = new Condition(xTitle, "==", xUnique[(int) tP.x]);
            Condition cond2 = new Condition(yTitle, "==", yUnique[(int) tP.y]);
            Condition[] conds = new Condition[2];
            conds[0] = cond1;
            conds[1] = cond2;
        
            // Finish this:
            // Create the conditions for what points to highlight to send
            // in the message below. (reference Condition.pde, Rectangle.pde,
            // hover function above)
            // The getIntersectRegion() call above gets you the selection rectangle
            // in the current view.
            
            // send out the message
            Message msg = new Message();
            msg.setSource(name)
               .setConditions(conds);
            sendMsg(msg);
        }*/
    }

    public void display() {
       addMarks();
       pushStyle();
        stroke(0);
        strokeWeight(1);
        fill(255);
        rectMode(CORNER);

        rect(leftX, leftY-1, w, h+1);
        popStyle();
        pushStyle();
        int[] values = valueArray.valueArray();
        for (int i = 0; i < yUnique.length; i++) {
          fill(0);
          strokeWeight(1);
          stroke(0);
          textSize(10);
          text(yUnique[i], leftX + getWidth()*3/2, leftY + (i+ 1)*getHeight());
          fill(255);
          rect(leftX, leftY + i*getHeight(), getWidth()*3, getHeight());
            for (int j = 0; j < xUnique.length; j++){
            if (i == 0) {
                textSize(8);
                fill(0);
                text(xUnique[j], leftX + (j+ 3)*getWidth() + getWidth()/2 , leftY);
            }
              colorMode(HSB, 100);
              if (xuMarks.get(xUnique[j]) == 1 && yuMarks.get(yUnique[i]) == 1)
              {
                fill(100, 100, 100);
              }
              else fill(vColor(valueArray.get(xUnique[j] + " " + yUnique[i])));
              rect(leftX + (j+ 3)*getWidth(), leftY + i*getHeight(), getWidth(), getHeight());
        }
        textAlign(CENTER);
        colorMode(RGB);
        fill(0);
        
        // draw labels
        if(yIndex == 0){
        }

        if(xIndex == 0){
            pushMatrix();
            translate(leftX - fontSize / 2.0f, leftY + w / 2.0f);
            rotate(radians(-90));
            text(yTitle, 0, 0);
            popMatrix();
        }
      
    }
    }
    private void addMarks()
    {
      xuMarks.clear();
      yuMarks.clear();
      int index = 0;
      for (int i = 0; i < this.xArray.length; i++)
      {
        if (this.marks[i])
        {
   //       index++;
          xuMarks.set(xArray[i], 1);
          yuMarks.set(yArray[i], 1);
        }
    }
   // print("\n index: " + index);
    }
          
    public HeatmapView setXYIndice(int x, int y) {
        this.xIndex = x;
        this.yIndex = y;
        return this;
    }

    // set the indice of columns that this view can see
    public HeatmapView setData(String[] xArray, String[] yArray) {
        this.xArray = xArray;
        this.yArray = yArray;
        String keyname;
        this.valueArray = new IntDict();
        this.xuMarks = new IntDict();
        this.yuMarks = new IntDict();
        for (int i = 0; i < xArray.length; i++)
        {
            keyname = xArray[i] + " " + yArray[i];
            print("keyname: " + keyname);
            print ("\n value: " + valueArray.get(keyname) + "\n");
            if (valueArray.hasKey(keyname))
            {
               valueArray.increment(keyname);
            }
            else valueArray.set(keyname, 1);
        }
        valueArray.sortKeys();
        return this;
    }

    public HeatmapView setTitles(String xTitle, String yTitle) {
        this.xTitle = xTitle;
        this.yTitle = yTitle;
        StringDict xUnique_ = new StringDict();
        StringDict yUnique_ = new StringDict();
        for (int i = 0; i < this.xArray.length; i++)
        {
          xUnique_.set(this.xArray[i], "");
        }
        xUnique_.sortKeys();
        for (int i = 0; i < this.yArray.length; i++)
        {
        yUnique_.set(this.yArray[i], "");
        }
        yUnique_.sortKeys();
        this.xUnique = xUnique_.keyArray();
        this.yUnique = yUnique_.keyArray();
        String keyname;
        for (int i = 0; i < this.xUnique.length; i++)
        {
          for (int j = 0; j < this.yUnique.length; j++)
          {
            keyname = xUnique[i] + " " + yUnique[j];
            if (valueArray.hasKey(keyname))
            {
               valueArray.increment(keyname);
            }
            else valueArray.set(keyname, 0);
          }
        }
        valueArray.sortKeys();
        return this;
    }

    public HeatmapView initMinMaxRange() {
        valueArray.sortValues();
        int[] values = valueArray.valueArray();
        this.min = values[0];
        this.max = values[values.length - 1];
    //    print("min" + min);
     //   print("max" + max);
        valueArray.sortKeys();
        println(valueArray);
        return this;
    }
    
  public int vColor(float x)
  {
    float minc = min;
    float maxc = max;
    return color(100, ((x-min)/(max-min)) * 100, 100);
  }
}

class Message {
    String src = null;
    Condition[] conds = null;
    String action = "normal";

    Message() {

    }

    public Message setSource(String str) {
        this.src = str;
        return this;
    }

    public Message setAction(String str) {
        this.action = str;
        return this;
    }

    public Message setConditions(Condition[] conds) {
        this.conds = conds;
        return this;
    }

    public boolean equals(Message msg) {
        if (msg == null) {
            return false;
        }
        if (src == null && msg.src == null) {
            return true;
        }
        if (src == null || msg.src == null) {
            return false;
        }
        if (!src.equals(msg.src)) {
            return false;
         }
        if (conds != null && msg.conds != null) {
            if (conds.length != msg.conds.length) {
                return false;
            }
            for (int i = 0; i < conds.length; i++) {
                if (!conds[i].equals(msg.conds[i])) {
                    return false;
                }
            }
            return true;
        } else {
            if (conds == null && msg.conds == null) {
                return true;
            } else {
                return false;
            }
        }
    }

    public String toString() {
        String str = "";
        for (Condition cond: conds) {
            str += cond.toString();
        }
        return str + "\n\n";
    }
}
public int indexofNodeLabel(ArrayList<Node> nodes, String l) {
  
  for( int i=0; i<nodes.size(); i++ ) {
    if( nodes.get(i).get_label().equals(l) ) {
      return i;
    }
  }
  
  return -1;
}

class Node {
  float px, py, vx, vy, m, r;
  int id;
  float fx, fy, ax, ay;
  String label;
  
  Node(float px_, float py_, float m_, float r_, int id_, String label_) { // x-pos, y-pos, mass, radius, (unique) id, label
    m = m_; px = px_; py = py_; r = r_; id = id_; label = label_;
    fx = 0.0f; fy = 0.0f; vx = 0.0f; vy = 0.0f;
  }
  
  public void update_pos() 
  {
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
  public String get_label() { return label; }
  
}
class Rectangle { // model dragging area
    PVector p1 = null;
    PVector p2 = null;

    Rectangle(float x1_, float y1_, float x2_, float y2_) {
        float x1 = x1_ < x2_ ? x1_ : x2_;
        float x2 = x1_ >= x2_ ? x1_ : x2_;
        float y1 = y1_ < y2_ ? y1_ : y2_;
        float y2 = y1_ >= y2_ ? y1_ : y2_;
        p1 = new PVector(x1, y1);
        p2 = new PVector(x2, y2);
    }
}
class ScatterplotView extends AbstractView {
    float[] xArray = null;
    float[] yArray = null;
    String xTitle = null;
    String yTitle = null;

    float xMax = -1;
    float yMax = -1;
    float xMin = -1;
    float yMin = -1;

    int xIndex = -1;
    int yIndex = -1;

    // get the radius of points for drawing
    public float getRadius() {
        return w / 70.0f;
    }

    // this deals with selection when items are under the mouse cursor
    public void hover() {
        // create the highlight Conditions to send as a message to all other scatter plots
        // through the Controller using the messages architecture
        // (highlight based on square surrounding the point with width 2*radius)
      /*  Condition cond1 = new Condition(xTitle, "<=", inverseToXReal(mouseX + getRadius()));
        Condition cond2 = new Condition(xTitle, ">=", inverseToXReal(mouseX - getRadius()));
        Condition cond3 = new Condition(yTitle, "<=", inverseToYReal(mouseY - getRadius()));
        Condition cond4 = new Condition(yTitle, ">=", inverseToYReal(mouseY + getRadius()));
        Condition[] conds = new Condition[4];
        conds[0] = cond1;
        conds[1] = cond2;
        conds[2] = cond3;
        conds[3] = cond4;
         
        // Finish this:
        // Send a message to the Controller to provide the current conditions for highlighting
        // 1. create a new message instance (see Message.pde)
        // 2. set the source of this message (see Message.pde)
        // 3. set the conditions of this message (see Message.pde)
        // 4. send the message (see AbstractView.pde)
        Message msg = new Message();
        msg.setSource(name)
        .setConditions(conds);
        sendMsg(msg);*/
    }

    // handle sending messages to the Controller when a rectangle is selected
    public void handleThisArea(Rectangle rect) {
   /*     // this rectangle holds the _pixel_ coordinates of the selection rectangle 
        Rectangle rectSub = getIntersectRegion(rect);

        if (rectSub != null) {
            Condition cond1 = new Condition(xTitle, "<=", inverseToXReal(rectSub.p2.x));
            Condition cond2 = new Condition(xTitle, ">=", inverseToXReal(rectSub.p1.x));
            Condition cond3 = new Condition(yTitle, "<=", inverseToYReal(rectSub.p1.y));
            Condition cond4 = new Condition(yTitle, ">=", inverseToYReal(rectSub.p2.y));
            Condition[] conds = new Condition[4];
            conds[0] = cond1;
            conds[1] = cond2;
            conds[2] = cond3;
            conds[3] = cond4;

            // Finish this:
            // Create the conditions for what points to highlight to send
            // in the message below. (reference Condition.pde, Rectangle.pde,
            // hover function above)
            // The getIntersectRegion() call above gets you the selection rectangle
            // in the current view.
            
            // send out the message
            Message msg = new Message();
            msg.setSource(name)
               .setConditions(conds);
            sendMsg(msg);
        }*/
    }

    public void display() {
        pushStyle();
        stroke(0);
        strokeWeight(1);
        fill(255);
        rectMode(CORNER);

        rect(leftX, leftY, w, h);

        ellipseMode(CENTER);
        for (int i = 0; i < xArray.length; i++) {
            if (marks[i]) {
                fill(pointHighLight);
            } else {
                fill(pointColor);
            }
            noStroke();
            // draw points
            ellipse(xScale(xArray[i]), yScale(yArray[i]), getRadius() * 2, getRadius() * 2);
        }

        textAlign(CENTER);
        fill(0);
        
        // draw labels
        if(yIndex == 0){
            text(xTitle, leftX + w / 2.0f, leftY - fontSize / 2.0f);
        }

        if(xIndex == 0){
            pushMatrix();
            translate(leftX - fontSize / 2.0f, leftY + w / 2.0f);
            rotate(radians(-90));
            text(yTitle, 0, 0);
            popMatrix();
        }
        popStyle();
    }

    public ScatterplotView setXYIndice(int x, int y) {
        this.xIndex = x;
        this.yIndex = y;
        return this;
    }

    // set the indice of columns that this view can see
    public ScatterplotView setData(float[] xArray, float[] yArray) {
        this.xArray = xArray;
        this.yArray = yArray;
        return this;
    }

    public ScatterplotView setTitles(String xStr, String yStr) {
        this.xTitle = xStr;
        this.yTitle = yStr;
        return this;
    }

    public ScatterplotView initXYRange() {
        xMin = 0;//min(xArray);
        xMax = max(xArray) * 1.2f;
        yMin = 0;//min(yArray);
        yMax = max(yArray) * 1.2f;

        return this;
    }

    public float xScale(float x) {
        return leftX + (x - xMin) / (xMax - xMin) * w;
    }

    public float yScale(float y) {
        return leftY + h - ((y - yMin) / (yMax - yMin) * h);
    }

    // convert from pixel coordinates to data coordinates
    public float inverseToXReal(float px) {
        return (px - leftX) / w * (xMax - xMin) + xMin;
    }

    // convert from pixel coordinates to data coordinates
    public float inverseToYReal(float py) {
        return (h - (py - leftY)) / h * (yMax - yMin) + yMin;
    }


}
final float BAR_WIDTH_PERC = .8f;
final int DEFAULT_COLOR = color(150, 0, 150);
final int HIGHLIGHT_COLOR = color(150, 0, 150);

public class BarGraph {  
    int[] my_data;
    String[] my_vals;
    int[] to_highlight;
    float ulx, lly, b_w, b_h;
    int sum;
    float x_inc;
    float xaxis_ycoor;
    String col_name;

    BarGraph(int[] _my_data, float _ulx, float _lly, float _b_w, float _b_h, String[] _my_vals, String _col_name) {
        my_data = _my_data;
        to_highlight = new int[my_data.length];
        ulx = _ulx;
        lly = _lly;
        b_w = _b_w;
        b_h = _b_h;
        my_vals = _my_vals;
        col_name = _col_name;

        xaxis_ycoor = lly;

        set_maxs();
    }
       
    public void set_maxs()
    {
        sum = 0;

        for (int i = 0; i < my_data.length; i++) {
            sum += my_data[i];
        }

        x_inc = b_h / sum;
    }

    public void draw() 
    {
        x_inc = b_h / sum;
        xaxis_ycoor = lly;
        float sumxs = 0;
        float my_uly, my_h;
        for (int i = 0; i < my_data.length; i++) {
            fill(color(0, min(255,0 + i * 50),max(0, 255 - i * 50), 70));
            sumxs += my_data[i] * x_inc;
            my_uly = xaxis_ycoor - sumxs;
            my_h = my_data[i] * x_inc;
            if (intersect(ulx, my_uly, b_w, my_h)) {
              fill(color(40, 100, 50), 100);
            }

            rect(ulx, my_uly, b_w, my_h);
            fill(color(40, 100, 50), 100);

            rect(ulx, my_uly + my_h - to_highlight[i] * x_inc, b_w, to_highlight[i] * x_inc);
            
            textSize(8);
            fill(0);
            text(my_vals[i], ulx + b_w / 4 + b_w * .2f, my_uly + my_h * .05f);
        }
    }
    public void inc_highlight(int i) 
    {
        to_highlight[i] = to_highlight[i] + 1;
    }
   /*
    void draw_stacked_bars() {
      for (int i = 0; i < xycoors.length; i++) {
        for (int j = 1; j < xycoors[0].length; j++) {
          fill(255,255,255);
          rect(calc_ul_posx(xycoors[i][j].x), xycoors[i][j].y, calc_bar_w(), axisy - xycoors[i][j].y);
        }  
      }
    }
   */
   
    public boolean intersect(float bar_ulx, float bar_uly, float bar_w, float bar_h) {
 
        if (mouseX < bar_ulx || mouseX > (bar_ulx + bar_w) || 
            mouseY < bar_uly || mouseY > (bar_uly + bar_h)) {
            return false;
        }  
        else {
            return true;
        } 
    }


    public String[] intersectedBarTitles(Rectangle rectSub)
    {
        x_inc = b_h / sum;
        xaxis_ycoor = lly;
        float sumxs = 0;
        float my_uly, my_h;
        ArrayList<String> b_titles = new ArrayList<String>();
        for (int i = 0; i < my_data.length; i++) {
            sumxs += my_data[i] * x_inc;
            my_uly = xaxis_ycoor - sumxs;
            my_h = my_data[i] * x_inc;
            if (ulx > rectSub.p2.x || my_uly > rectSub.p2.y ||
                (ulx + b_w) < rectSub.p1.x || (my_uly + my_h) < rectSub.p1.y) {
                // do nothing;
            } else {
                b_titles.add(my_vals[i]);
            }
        }
        String[] bars = new String[b_titles.size()];
        for (int i = 0; i < bars.length; i++) {
            bars[i] = b_titles.get(i);
            println(col_name);
        }
        return bars;
    }

   /*
    float calc_ul_posx(float xcoor) { return xcoor - (xpix_interval * BAR_WIDTH_PERC/2); }
    float calc_bar_w() { return xpix_interval * BAR_WIDTH_PERC; }
    float calc_bar_h(float ycoor) { return axisy - ycoor; }
    
}
*/
}
class Parser {
 
  int num_cols;
  int num_rows;
  String[] lines;
  
  Parser(String file_name) {
    lines = loadStrings(file_name);
    num_rows = lines.length;
    num_cols = splitTokens(lines[0], ",").length;
  }

  public String[] parse_row(int row_index) {
    String temp[] = splitTokens(lines[0], ",");
    String data[] = new String[num_rows];
    temp = splitTokens(lines[row_index], ",");
    for (int j = 0; j < temp.length; j++) {
      data[j] = trim(temp[j]);  
    }
    return data;
  } 
}


public int countBoolean(boolean[] b) {
  int c = 0;
  
  for( int i=0; i<b.length; i++ ) {
    if(b[i]) {
      c++;
    }
  }
  
  return c;
}

public String[] concatStringArrays(String[] s1, String[] s2) {
  ArrayList<String> s = new ArrayList<String>();
  
  Collections.addAll(s,s1);
  Collections.addAll(s,s2);
  
  String[] sout = new String[s.size()];
  sout = s.toArray(sout);
  
  return sout;
}

public String[] unique(String[] sin) {
  ArrayList<String> s = new ArrayList<String>();
  
  s.add(sin[0]);
  
  for( int i=1; i<sin.length; i++ ) {
    if( !s.contains(sin[i]) ) {
      s.add(sin[i]);
    }
  }
  
  String[] sout = new String[s.size()];
  sout = s.toArray(sout);
  
  return sout;
}

public boolean contains(int x[], int e) {
  
  for( int i=0; i<x.length; i++ ) {
    if( x[i] == e ) {
      return true;
    }
  }
  
  return false;
}

public float min_arraylist(ArrayList<Float> x) {
  
  if( x.size()==0 ) {
    System.out.printf("Input length is zero.\n");
    exit();
  }
  
  float minx = x.get(0);
  
  for (int i = 1; i < x.size(); i++) {
      if (x.get(i) < minx) {
        minx = x.get(i);
      }
  }
  
  return minx;
}

public float max_arraylist(ArrayList<Float> x) {
  
  if( x.size()==0 ) {
    System.out.printf("Input length is zero.\n");
    exit();
  }
  
  float maxx = x.get(0);
  
  for (int i = 1; i < x.size(); i++) {
      if (x.get(i) > maxx) {
        maxx = x.get(i);
      }
  }
  
  return maxx;
}

public float min_array(float[] x, int len) {
  
  if( len==0 ) {
    System.out.printf("Input length is zero.\n");
    exit();
  }
  
  float minx = x[0];
  
  for (int i = 1; i < len; i++) {
      if (x[i] < minx) {
        minx = x[i];
      }
  }
  
  return minx;
}

public float max_array(float[] x, int len) {
  
  if( len==0 ) {
    System.out.printf("Input length is zero.\n");
    exit();
  }
  
  float maxx = x[0];
  
  for (int i = 1; i < len; i++) {
      if (x[i] > maxx) {
        maxx = x[i];
      }
  }
  
  return maxx;
}

public float sum_array(float[] x) {
  float s = 0;
  
  for( int i=0; i<x.length; i++ ) {
    s = s+x[i];
  }
  
  return s;
}

public int sum_array(int[] x) {
  int s = 0;
  
  for( int i=0; i<x.length; i++ ) {
    s = s+x[i];
  }
  
  return s;
}
float margin = 20;
int pw = -1, ph = -1;

float tipHeight = 10;
float tipWidth = 20;

int labelBackground = color(204,204,204,90);
int pointColor = color(69,117,180,128);
int pointHighLight = color(244,109,67,128);
int selectColor = color(171,217,233,80);

PVector pressPos = null;
int fontSize = 12;
float lineHeight = 10;

String path = "data_aggregate.csv";

Table data = null;
boolean[] marks = null;
String[] header = null;

Controller contrl = null;
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
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "CMV" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
