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
        return w / 70.0;
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
            translate(leftX - fontSize / 2.0, leftY + w / 2.0);
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
    
  color vColor(float x)
  {
    float minc = min;
    float maxc = max;
    return color(100, ((x-min)/(max-min)) * 100, 100);
  }
}

