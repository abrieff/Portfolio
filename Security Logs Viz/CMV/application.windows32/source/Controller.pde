import java.util.Iterator;
import java.lang.Iterable;

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
