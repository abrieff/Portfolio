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
        return w / 40.0;
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
        text(xTitles[0], leftX + w*.1, leftY + h +h*.05);

        bar_graph = bgs.get(1);
        bar_graph.b_w = w/5;
        bar_graph.b_h = h;
        bar_graph.ulx = leftX + 2 * w/5;
        bar_graph.lly = leftY + h;
        bar_graph.to_highlight = intdict2.valueArray();
        bar_graph.draw();
        fill(0);
        text(xTitles[1], leftX + 2 * w/5 + w*.1, leftY+h+h*.05);

        bar_graph = bgs.get(2);
        bar_graph.b_w = w/5;
        bar_graph.b_h = h;
        bar_graph.ulx = leftX + 4*w/5;
        bar_graph.lly = leftY + h;
        bar_graph.to_highlight = intdict3.valueArray();
        bar_graph.draw();
        fill(0);
        text(xTitles[2], leftX + 4*w/5 + w*.1, leftY + h+h *.05);

        line(leftX - w*.1, leftY + h, leftX + w +w*.1, leftY + h);
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
