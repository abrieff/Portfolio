final float BAR_WIDTH_PERC = .8;
final color DEFAULT_COLOR = color(150, 0, 150);
final color HIGHLIGHT_COLOR = color(150, 0, 150);

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
       
    void set_maxs()
    {
        sum = 0;

        for (int i = 0; i < my_data.length; i++) {
            sum += my_data[i];
        }

        x_inc = b_h / sum;
    }

    void draw() 
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
            text(my_vals[i], ulx + b_w / 4 + b_w * .2, my_uly + my_h * .05);
        }
    }
    void inc_highlight(int i) 
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
   
    boolean intersect(float bar_ulx, float bar_uly, float bar_w, float bar_h) {
 
        if (mouseX < bar_ulx || mouseX > (bar_ulx + bar_w) || 
            mouseY < bar_uly || mouseY > (bar_uly + bar_h)) {
            return false;
        }  
        else {
            return true;
        } 
    }


    String[] intersectedBarTitles(Rectangle rectSub)
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
