import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.Iterator; 

import controlP5.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class final_proj extends PApplet {




LeaningGraph graph = null;
StringList isect_names = null;
IntList isect_votes_with = null, isect_votes_against = null;
ToggleButton buttonToggle = null;
PImage donkey = null, elephant = null, imageTitle = null;
ToggleButton meanToggle = null;

public void setup()
{ 
  size(canvasWidth,canvasHeight);
  background(bgColor);

  graph = new LeaningGraph();
  isect_names = new StringList();
  isect_votes_with = new IntList();
  isect_votes_against = new IntList();
   
  String Congressional_Data;
  String leader_data = "congressleaders.csv";
  String hleader_data = "houseleaders.csv";
  String House_Data;
  hvote_data = new Table[NUM_CONGRESSES];
  vote_data = new Table[NUM_CONGRESSES];
  for (int i = 0; i < NUM_CONGRESSES; i++)
  {
    House_Data = (i + start_congress) + "housepp.csv";
    Congressional_Data = (i + start_congress) + "congresspp.csv";
    vote_data[i] = loadTable(Congressional_Data,"header");
    hvote_data[i] = loadTable(House_Data, "header");
  }
  leaders = loadTable(leader_data, "header");
  hleaders = loadTable(hleader_data, "header");
  setCongresses();
  setSenators(vote_data, congresses, senators);
  setSenators(hvote_data, hcongresses, houses);
 // println(houses.get("G000132").sen_congresses.get(101).partyPercentage);
  for (Senator s : senators.values()) {
    s.setPositions();
  }
  for (Senator s: houses.values())
  {
    s.setPositions();
  }
  calc_means();
  calc_medians();
  
  textSize(titleSize);
  final float wToggleButton = textWidth(truestrToggleButton);
  final float wMeanButton = textWidth(truestrMeanButton);
  buttonToggle = new ToggleButton(truestrToggleButton,falsestrToggleButton,colorToggleButton,xToggleButton,yToggleButton,wToggleButton,hToggleButton);
  meanToggle = new ToggleButton(truestrMeanButton,falsestrMeanButton,colorMeanButton,xMeanButton,yMeanButton,wToggleButton,hMeanButton);

  donkey = loadImage("blue-donkey-hi.png");
  elephant = loadImage("elephant3.png");
  imageTitle = loadImage("PoliticalDonkey-Elephant.png");
  
 //for (Senator s: houses.values())
  //{
  //  s.setPositions();
 // }

}

public void draw()
{
  background(bgColor);
  textFont(font);
  textSize(titleSize);
  buttonToggle.draw();
  meanToggle.draw();
  graph.draw(buttonToggle.getState(), meanToggle.getState());
  cursor(ARROW);
  textAlign(RIGHT,TOP);
  stroke(255);
  textFont(font);
  textSize(titleSize-8);
  text("Chamber",xToggleButton-7,yToggleButton+2 );
  text("Statistic",xMeanButton-7,yMeanButton+2 );
}

public void mouseClicked() {
  if( buttonToggle.intersect(mouseX, mouseY) ) {
    buttonToggle.toggle();
    redraw();
  }
  if(meanToggle.intersect(mouseX, mouseY))
  {
    meanToggle.toggle();
    redraw();
  }  
}


 class Congress
{
  int id;
  StringList senator_ids;
  String rep_leader_id;
  String dem_leader_id;
  float dem_mean_x;
  float rep_mean_x;
  float dem_median_x;
  float rep_median_x;
  float median_y1, median_y2;
  float mean_y1, mean_y2;
  int dem_count = 0, rep_count = 0;

  Congress(int id)
  {
    this.id = id;
    senator_ids = new StringList();
  }
  public void setDemLeader(String name, String id, String state)
  {
    dem_leader_id = id;
 //   senators.put(name, Dem_Leader);
  }
  public void setRepLeader(String name, String id, String state)
  {
    rep_leader_id = id;//new Senator(name, "R",  id, state);
   // senators.put(name, Rep_Leader);
  }
  public boolean hasSenator(String id)
  {
    return senator_ids.hasValue(id);
  }
  public void addSenator(String name, String party, String id, String state)
  { 
    senator_ids.append(id);
    if(party.equals("R")) {
      rep_count += 1;
    } else {
      dem_count += 1;
    }
  }
  public void draw(float myY)
  {
    for (Senator senator: senators.values())
    {
      senator.draw(false);
    }
  }
  
  public int getRepCount() { return rep_count; }
  public int getDemCount() { return dem_count; }  

}
class LeaningGraph
{
  LeaningGraph() {
  }

  public void draw(boolean drawSenate, boolean drawMeans)
  {
    setGradient(graphPosX, graphPosY, graphWidth, graphHeight, demBGColor, repubBGColor);
    drawGridLines();
    drawLabels(drawSenate);
    drawCongress(drawSenate);
    drawNames(drawSenate);
    drawStats(drawSenate, drawMeans);
    drawKey(drawMeans);
    isect_names.clear();
    isect_votes_with.clear();
    isect_votes_against.clear();
  }

  public void drawStats(boolean drawSenate, boolean drawMeans)
  {
    if (drawMeans)
    {
      drawMeans(drawSenate);
    }
    else drawMedians(drawSenate);
  }
  public void drawNames(boolean drawSenate) 
  {
    Senator s = null;
    for (int i = 0; i < isect_names.size (); i++) {
      if (drawSenate){
        s = senators.get(isect_names.get(i));
      } else {
        s = houses.get(isect_names.get(i));
      }
      float y = namesY + 10 * i * 3;
      s.draw(true);
      textAlign(LEFT);
      fill(255);
      textFont(bold_font);
      textSize(namesSize);
      text(s.name, namesX, y);
      
      textFont(font);
      textSize(namesSize);
      fill(220);
      y = namesY + 10 * i * 3 + 10;
      text("No. Party Votes: " + isect_votes_with.get(i), namesX+10, y);
      y = namesY + 10 * i * 3 + 10*2;
      text("No. Total Votes: " + (isect_votes_with.get(i)+isect_votes_against.get(i)), namesX+10, y);
    }
  }

  public void setGradient(int x, int y, float w, float h, int c1, int c2) 
  {
    float my_y = y;

    for (int j = 0; j < numCongresses; j++) {
      for (int i = x; i <= x + w; i++) {
        float inter = map(i, x, x + w, 0, 1);
        int c = lerpColor(c1, c2, inter);
        stroke(c);
        line(i, my_y, i, my_y + h);
      }

      my_y = my_y + h * 2;
    }
  }

  public void drawCongress(boolean drawSenate)
  {

    if (drawSenate) {
      for (Senator senator : senators.values ()) {
        senator.draw(false);
      }
    } else {
      for (Senator senator : houses.values ()) {
        senator.draw(false);
      }
    }
  }

  public void drawMeans(boolean drawSenate) {
    strokeWeight(2);

    if (drawSenate) {
      for (int i = 0; i < congresses.length; i++) {
        Congress c = congresses[i];
        stroke(demMeanColor);
        line(c.dem_mean_x, c.mean_y1, c.dem_mean_x, c.mean_y2);
        stroke(repMeanColor);
        line(c.rep_mean_x, c.mean_y1, c.rep_mean_x, c.mean_y2);
      }
    } else {
      for (int i = 0; i < hcongresses.length; i++) {
        Congress c = hcongresses[i];
        stroke(demMeanColor);
        line(c.dem_mean_x, c.mean_y1, c.dem_mean_x, c.mean_y2);
        stroke(repMeanColor);
        line(c.rep_mean_x, c.mean_y1, c.rep_mean_x, c.mean_y2);
      }
    }
  }
  public void drawMedians(boolean drawSenate)
  {
    strokeWeight(2);
    if (drawSenate) {
      for (int i = 0; i < congresses.length; i++) {
        Congress c = congresses[i];
        stroke(demMeanColor);
        line(c.dem_median_x, c.median_y1, c.dem_median_x, c.median_y2);
        stroke(repMeanColor);
        line(c.rep_median_x, c.median_y1, c.rep_median_x, c.median_y2);
      }
    } else {
      for (int i = 0; i < hcongresses.length; i++) {
        Congress c = hcongresses[i];
        stroke(demMeanColor);
        line(c.dem_median_x, c.median_y1, c.dem_median_x, c.median_y2);
        stroke(repMeanColor);
        line(c.rep_median_x, c.median_y1, c.rep_median_x, c.median_y2);
      }
    }
  }
    


  public void drawGridLines() 
  {
    stroke(255, 75);
    strokeWeight(3);
    line(midX, graphPosY, midX, bottomY);

    strokeWeight(1);
    textSize(percTextSize);
    fill(255);	  

    int myY = graphPosY;

    for (int j = 0; j < numCongresses; j++) {
      float myX;
      for (int i = 0; i <= 20; i = i + 2) {
        myX = graphPosX + i * graphWidth / 20;
        line(myX, myY, myX, myY + graphHeight);
      }
      myY += graphHeight * 2;
    }
    
    textAlign(CENTER,BOTTOM);
    textFont(bold_font);
    textSize(percTextSize);
    text("Control", partyControlX+7, graphPosY - 5);

    textAlign(CENTER,BOTTOM);
    text("Years", yearX, graphPosY - 5);    

    textFont(font);
    textSize(percTextSize);
    int perc = 100;

    for (int i = 0; i <= 20; i = i +2) {
      float myX = graphPosX + i*graphWidth/20;
      textAlign(CENTER, BOTTOM);
      text(str(perc) + '%', myX, graphPosY - 3);
      textAlign(CENTER, TOP);
      text(str(perc) + '%', myX, bottomY + 3);

      if (i <10) {
        perc -= 20;
      } else {
        perc += 20;
      }
    }
  }

  public void drawLabels(boolean drawSenate)
  {
    fill(255);

    float start_y = graphPosY + graphHeight / 2;
    int c = 101;

    textAlign(CENTER, BOTTOM);
    textSize(termTextSize);
    //text("Term", termX, graphPosY - 7);

    textAlign(CENTER, CENTER);

    int first_yr = 1989;
    int second_yr = 1990; // changed from 1991
    // congress nums
    for (int i = 0; i < numCongresses; ++i) {
      //text(str(c), termX, start_y);
      String yr1 = str(first_yr % 100);
      String yr2 = str(second_yr % 100);
      if (yr1.length()  < 2) {
        String temp = "0";
        temp += yr1;
        yr1 = temp;
      }
      if (yr2.length() < 2) {
        String temp = "0";
        temp += yr2;
        yr2 = temp;
      }
      
      text("'" + yr1 + "-'" + yr2, yearX, start_y);
      
      if(drawSenate) {
        if( congresses[i].getDemCount() > congresses[i].getRepCount() ) {
          image(donkey, partyControlX, start_y - partyControlSize/2, partyControlSize, partyControlSize);
        } else {
          image(elephant, partyControlX, start_y - partyControlSize/2, partyControlSize, partyControlSize);
        }
      } else {
        if( hcongresses[i].getDemCount() > hcongresses[i].getRepCount() ) {
          image(donkey, partyControlX, start_y - partyControlSize/2, partyControlSize, partyControlSize);
        } else {
          image(elephant, partyControlX, start_y - partyControlSize/2, partyControlSize, partyControlSize);
        }
      }
      
      start_y += graphHeight*2;
      c++;
      first_yr += 2;
      second_yr += 2;
    }

    textFont(bold_font);
    textSize(labelTextSize);
    strokeWeight(3);
    strokeCap(ROUND);
    stroke(labelColor);
    fill(labelColor);

    // left arrow
    line(leftArrowX1, arrowY, leftArrowX2, arrowY);
    line(leftArrowX1, arrowY, leftArrowHeadX, arrowHeadY1);
    line(leftArrowX1, arrowY, leftArrowHeadX, arrowHeadY2);

    text(leftLabel, leftLabelX, labelY);
    // right arrow
    line(rightArrowX1, arrowY, rightArrowX2, arrowY);
    line(rightArrowX2, arrowY, rightArrowHeadX, arrowHeadY1);
    line(rightArrowX2, arrowY, rightArrowHeadX, arrowHeadY2);
    text(rightLabel, rightLabelX, labelY);
    //title
    textFont(bold_font);
    textSize(titleSize);
    text(title, canvasWidth / 2, graphPosY / 3);
    imageMode(CENTER);
    image(imageTitle, canvasWidth / 2, graphPosY / 3, 100, 100);
    imageMode(CORNER);
    textFont(font);
    textSize(titleSize);
  }

  public void drawKey(boolean drawMeans) 
  {
    stroke(colorMeanKey);
    fill(colorMeanKey);
    rect(xMeanKey, yMeanKey, wMeanKey, hMeanKey);

    stroke(demMeanColor);
    strokeWeight(2);
    line(xDemLine, y1DemLine, xDemLine, y2DemLine);
    
    textAlign(LEFT, CENTER);
    textSize(textSizeMeanKey);
    stroke(255);
    fill(255);
    if (drawMeans) text("Mean Democratic Party Loyalty", xDemKeyLabel, yKeyLabel);
    else text("Median Democratic Party Loyalty", xDemKeyLabel, yKeyLabel);

    stroke(repMeanColor);
    strokeWeight(2);
    line(xRepLine, y1RepLine, xRepLine, y2RepLine);
    
    textAlign(LEFT, CENTER);
    textSize(textSizeMeanKey);
    stroke(255);
    fill(255);
    if (drawMeans) text("Mean Republican Party Loyalty", xRepKeyLabel, yKeyLabel);
    else text("Median Republican Party Loyalty", xRepKeyLabel, yKeyLabel);
  }
}




class OurRect {  
  int fc, ec;
  float x, y, w, h;
  
  OurRect(int fc0, float x_, float y_, float w_, float h_) {
    fc = fc0; ec = fc;
    x = x_; y = y_; w = w_; h = h_;
  }
  
  OurRect(int fc0, int ec0, float x_, float y_, float w_, float h_) {
    fc = fc0; ec = ec0;
    x = x_; y = y_; w = w_; h = h_;
  }
  
  public void setColor (int r, int g, int b) {
    fc = color (r, g, b);
  }
  
  public void setColor (int c0) {
    fc = c0;
  }
  
  public int getColor() {return fc;}
  
  public void draw(){
    fill(fc);
    stroke(fc);
    rect(x,y,w,h, h / 4);
  }
  
  public boolean intersect (float mx, float my) {
    if((mx >= x) && (mx <= x+w) && (my >= y) && (my <= y+h)){
      return true;
    }
    else{
      return false;
    }
  }
  
  public float getX() { return x; }
  public float getY() { return y; }
  public float getW() { return w; }
  public float getH() { return h; }
  public float getCenterX() { return x+w/2.0f; }
  public float getCenterY() { return y+h/2.0f; }

}

final int start_congress = 101;
final int end_congress = 113;
final int NUM_CONGRESSES = 13;
Table[] vote_data; 
Table leaders;
Table hleaders;
Table[] hvote_data;
String[] header;
Congress[] congresses;
Congress[] hcongresses;
HashMap<String, Senator> senators;
HashMap<String, Senator> houses;
    
public void setCongresses()
{
  congresses = new Congress[NUM_CONGRESSES];
  hcongresses = new Congress[NUM_CONGRESSES];
  senators = new HashMap<String,Senator>();
  houses = new HashMap<String,Senator>();
  Congress curc;
  String curn;
  
  for (int i = 0; i < NUM_CONGRESSES; i++)
  {
    congresses[i] = new Congress(i + start_congress);
    hcongresses[i] = new Congress(i + start_congress);
  }
  setLeaders(leaders, congresses);
  setLeaders(hleaders, hcongresses);
  //setVotes(hvote_data[0], hcongresses);  
}


public void setLeaders(Table leaders_, Congress[] congresses_)
{
    for (TableRow row : leaders_.rows()) {
      if (row.getString("party").equals("D"))
      {
        congresses_[row.getInt("congress") - start_congress].setDemLeader(row.getString("name"), row.getString("id"), row.getString("state"));
      }
      else { 
        String id = row.getString("id");
        congresses_[row.getInt("congress") - start_congress].setRepLeader(row.getString("name"), id, row.getString("state"));
      }
    }
}

public void setSenators(Table[] vote_data_, Congress[] congresses_, HashMap<String, Senator> senators_)
{
  Congress curc;
  String curn;
  Senator s;
  for (int i = 0; i < NUM_CONGRESSES; i++)
  {
    for (TableRow row: vote_data_[i].rows())
    {
       curc = congresses_[(row.getInt("congress")) - start_congress];
       curn = row.getString("person_id");
       if (row.getString("party").equals("D") || row.getString("party").equals("R")) {
         if (!curc.hasSenator(curn))
         {
           curc.addSenator(row.getString("person_name"), row.getString("party"), curn, row.getString("state"));
         }
         if (!senators_.containsKey(curn)) {
             s = new Senator(row.getString("person_name"), row.getString("party"), curn, row.getString("state"), row.getInt("congress") - start_congress);
            senators_.put(curn, s);
         }
         else s = senators_.get(curn);
         s.addPercentage(row.getInt("congress")-start_congress,row.getFloat("party_percentage"),row.getInt("num_votes_with"),row.getInt("num_votes_against"));
         //s.addNumVotesWith(row.getInt("congress")-start_congress,row.getInt("num_votes_with"));
         //s.addNumVotesAgainst(row.getInt("congress")-start_congress,row.getInt("num_votes_against"));
         }
         
       }
    }
  }
  
public void calc_means() {
  calc_congress_means(congresses, senators);
  calc_congress_means(hcongresses, houses);  
}
public void calc_medians() {
  calc_congress_medians(congresses, senators);
  calc_congress_medians(hcongresses, houses);
}

public void calc_congress_medians(Congress[] _congresses, HashMap<String,Senator> people)
{
  Congress c;
  Senator s;
  float dem_num, rep_num;
  float dem_median, rep_median;
  SenCongress s_c;
  FloatList sortDems = new FloatList();
  FloatList sortReps = new FloatList();
  for (int i = 0; i < _congresses.length; i++)
  {
    sortDems.clear();
    sortReps.clear();
    dem_num = 0;
    rep_num = 0;
    dem_median = 0;
    rep_median = 0;
    c = _congresses[i];
    for (int j = 0; j < c.senator_ids.size(); j++)
    {
      s = people.get(c.senator_ids.get(j));
      if (s.sen_congresses.containsKey(c.id - start_congress)) {
        s_c = s.sen_congresses.get(c.id - start_congress);
        if (s.party.equals("D")) {
          sortDems.append(s_c.partyPercentage);
          dem_num++;
        }
        else {
          sortReps.append(s_c.partyPercentage);
          rep_num++;
        }
      }
    }
    sortDems.sort();
    sortReps.sort();
    if (dem_num == 0) {
      dem_median = 0;
    } else {
      dem_median = sortDems.get(floor(dem_num/2));
    }    
    if (rep_num == 0) {
      rep_median = 0;
    } else {
      rep_median = sortReps.get(floor(rep_num/2));
    }
    c.dem_median_x = ((graphWidth / 2) + graphPosX) - dem_median * graphWidth / 2;
    c.rep_median_x = ((graphWidth / 2) + graphPosX) + rep_median * graphWidth / 2;
    c.median_y1 = graphPosY + graphHeight * (c.id - start_congress) * 2;
    c.median_y2 = c.median_y1 + graphHeight;
    println(dem_median + " " + rep_median);
  }
}
        
        
public void calc_congress_means(Congress[] _congresses, HashMap<String,Senator> people) {
  Congress c;
  float dem_sum, rep_sum;
  float dem_num, rep_num;
  Senator s;
  SenCongress s_c;

  for (int i = 0; i < _congresses.length; i++) {
    c = _congresses[i];
    dem_sum = 0;
    rep_sum = 0;
    dem_num = 0;
    rep_num = 0;
    for (int j = 0; j < c.senator_ids.size(); j++) {
      s = people.get(c.senator_ids.get(j));
      if (s.sen_congresses.containsKey(c.id - start_congress)) {
        s_c = s.sen_congresses.get(c.id - start_congress);
        if (s.party.equals("D")) {
          dem_sum += s_c.partyPercentage;
          dem_num++; 
        }
        else {
          rep_sum += s_c.partyPercentage;
          rep_num++;
        }
      }
    }
    float d_mean, r_mean;
    if (dem_num == 0) {
      d_mean = 0;
    } else {
      d_mean = dem_sum / dem_num;
    }    
    if (rep_num == 0) {
      r_mean = 0;
    } else {
      r_mean = rep_sum / rep_num;
    }

    c.dem_mean_x = ((graphWidth / 2) + graphPosX) - d_mean * graphWidth / 2;
    c.rep_mean_x = ((graphWidth / 2) + graphPosX) + r_mean * graphWidth / 2;
    c.mean_y1 = graphPosY + graphHeight * (c.id - start_congress) * 2;
    c.mean_y2 = c.mean_y1 + graphHeight;
  }
}

class ToggleButton {
  OurRect r = null;
  String stringTrue, stringFalse;
  boolean state = true;

  ToggleButton(String State1, String State2, int c0, float x_, float y_, float w_, float h_) { // first state is true state, second state is false state
    stringTrue = State1; 
    stringFalse = State2;
    r = new OurRect(c0, c0, x_, y_, w_, h_);
  }

  public boolean intersect (float mx, float my) {
    return r.intersect(mx, my);
  }

  public String getT() {
    if (state) {
      return stringTrue;
    } else {
      return stringFalse;
    }
  }

  public void setColor (int rr, int g, int b) {
    r.setColor(rr, g, b);
  }

  public void toggle() {
    state = !state;
  }

  public void draw() {
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

  public boolean getState() {
    return state;
  }
}

final int canvasWidth = 1350;
final int canvasHeight = 675;
final int bgColor = color(100);

final int mouseOnColor = color(150);
final int notCheckColor = color(50);
final int mouseOnWarningColor = color(251,106,74);
final int checkedColor = color(120);
final int labelColor = color(255);

final int[] rangeCongressNum = {101, 113};
final int numCongresses = rangeCongressNum[1] - rangeCongressNum[0] + 1;

final int repubColor = color(255, 0, 0);
final int demColor = color(0, 0, 255);
final int repubBGColor = color(255,140,140);
final int demBGColor = color(120,120,255);
final int demMeanColor = color(255, 165, 0);
final int repMeanColor = color(0,255,0);

final int graphPosY = floor(canvasHeight * .175f);
final float graphWidth = canvasWidth * .75f;
final int graphPosX = floor(canvasWidth - graphWidth) / 2;
final float graphHeight = (canvasHeight - graphPosY * 2.5f) / 23;
final float wholeGraphHeight = (2 * numCongresses - 1) * graphHeight; 
final float midX = graphPosX + graphWidth / 2;
final float bottomY = graphPosY + wholeGraphHeight;

final String leftLabel = "Voted with the Democratic Party Leader on a Higher Percentage of Bills"; // figure out better label name
final String rightLabel = "Voted with the Republican Party Leader on a Higher Percentage of Bills"; // "... Leader for a Higher Percentage of Bills"?
final float leftLabelX = graphPosX + graphWidth / 4;
final float labelY = graphPosY + wholeGraphHeight + (canvasHeight - graphPosY - wholeGraphHeight) / 2;
final float rightLabelX = graphPosX + 3 * graphWidth / 4;
final String title = "Partisanship Over Time             in the US Congress     ";
final int titleSize = floor(graphPosY * .2f);
final int labelTextSize = floor(min(constrain(canvasWidth * .0105f, 4, 24), constrain(canvasHeight * .0215f, 4, 24)));
final float percTextSize = labelTextSize * .75f;

final int pointDiam = floor(graphHeight * .5f);
final int selectDiam = pointDiam/3;

final float namesX = graphPosX + graphWidth  + (canvasWidth - graphPosX - graphWidth) / 4 - 3;
final float namesY = graphPosY;
final float namesSize = percTextSize * .9f;

final float termTextSize = percTextSize;
//final float termX = 3 * graphPosX / 4;
final float yearX = 2.5f * graphPosX / 4;
final float partyControlX = 1 * graphPosX / 4;
final float partyControlSize = 0.5f * graphPosX / 4;

final float arrowY = labelY - (labelY - (graphPosY + wholeGraphHeight)) / 2.5f;
final float leftArrowX1 = leftLabelX - graphWidth / 6;
final float leftArrowX2 = leftLabelX + graphWidth / 6;
final float rightArrowX1 = rightLabelX - graphWidth / 6;
final float rightArrowX2 = rightLabelX + graphWidth / 6;
final float arrowHeadY1 = arrowY + 10;
final float arrowHeadY2 = arrowY - 10;
final float leftArrowHeadX = leftLabelX - graphWidth / 6 + graphWidth / 48;
final float rightArrowHeadX = rightLabelX + graphWidth / 6 - graphWidth / 48;

final String truestrToggleButton = "SENATE";
final String falsestrToggleButton = "HOUSE";
final int colorToggleButton = color(60);
final float xToggleButton = namesX;
final float yToggleButton = namesY/3 - titleSize;
final float hToggleButton = titleSize;
final float textSizeToggleButton = titleSize / 1.75f;

final String truestrMeanButton = "MEAN";
final String falsestrMeanButton = "MEDIAN";
final int colorMeanButton = color(0,0,0);
final float xMeanButton = namesX;
final float yMeanButton = namesY/3 + titleSize/2;
final float hMeanButton = titleSize;
final float wMeanKey = canvasWidth * .25f;
final float hMeanKey = (canvasHeight - labelY) / 3;
final float yMeanKey = labelY + (canvasHeight - labelY) / 2 - hMeanKey / 2.5f;

final float xMeanKey = canvasWidth / 2 - wMeanKey / 2;
final float textSizeMeanKey = percTextSize * .95f;
final int colorMeanKey = color(60);
final float xDemLine = xMeanKey + wMeanKey * .025f;
final float y1DemLine = yMeanKey + hMeanKey * .2f;
final float y2DemLine = yMeanKey + hMeanKey - hMeanKey * .2f;
final float xRepLine = xMeanKey + wMeanKey * .5f + wMeanKey * .025f;
final float y1RepLine = yMeanKey + hMeanKey * .2f;
final float y2RepLine = yMeanKey + hMeanKey - hMeanKey * .2f;
final float xDemKeyLabel = xMeanKey + wMeanKey * .025f * 2;
final float yKeyLabel = yMeanKey + hMeanKey / 2;
final float xRepKeyLabel  = xMeanKey + wMeanKey * .5f + wMeanKey * .05f;

 PFont font = createFont("Arial", 15);
 PFont bold_font = createFont("Arial Bold", 15); 
class SenCongress
{
  float partyPercentage;
  String senator_id;
  float x, y;
  int numVotesWith, numVotesAgainst;

  SenCongress(String id) {
    senator_id = id;
  }
  public void addPercentage(float partyPercentage_)
  {
    partyPercentage = partyPercentage_;
  }
  public void setNumVotesWith(int n) { numVotesWith = n; }
  public void setNumVotesAgainst(int n) { numVotesAgainst = n; }
}

class Senator
{
  Senator Party_Leader = null;
  String name;
  String party;
  String id;
  String state;
  HashMap<Integer,SenCongress> sen_congresses;
  IntList sc_keys;
  
  Senator(String name, String party, String id, String state, int congress_id)
  {
    this.name = name;
    this.party = party;
    this.id = id;
    this.state = state;
    sen_congresses = new HashMap<Integer, SenCongress>();
    sc_keys = null;
  }
  Senator(String name, String party)
  {
    this.name = name;
    this.party = party;
  }
  public void addCongress(int c_id) 
  {
    SenCongress s_c = new SenCongress(id);
    print(c_id);
    sen_congresses.put(c_id, s_c);
  }
  public void addPercentage(int congress_id, float partyPercentage_, int n1, int n2)
  {
    if (!sen_congresses.containsKey(congress_id)) 
    {
      SenCongress s_c = new SenCongress(id);
      s_c.addPercentage(partyPercentage_);
      s_c.setNumVotesWith(n1);
      s_c.setNumVotesAgainst(n2);
      sen_congresses.put(congress_id, s_c);
    }
  }
//  void addNumVotesWith(int congress_id, int n)
//  {
//    if (!sen_congresses.containsKey(congress_id)) 
//    {
//      SenCongress s_c = new SenCongress(id);
//      s_c.setNumVotesWith(n);
//      sen_congresses.put(congress_id, s_c);
//    }
//  }    
//  void addNumVotesAgainst(int congress_id, int n)
//  {
//    if (!sen_congresses.containsKey(congress_id)) 
//    {
//      SenCongress s_c = new SenCongress(id);
//      s_c.setNumVotesAgainst(n);
//      sen_congresses.put(congress_id, s_c);
//    }
//  }     
  public void setPositions() {
    for (Integer c_id : sen_congresses.keySet()) {
      SenCongress s_c = sen_congresses.get(c_id);
      float partyPercentage = s_c.partyPercentage;
      if (party.equals("D")) {
        s_c.x = ((graphWidth / 2) + graphPosX) - partyPercentage * graphWidth / 2;
        s_c.y = graphPosY + graphHeight / 2 + graphHeight * 2 * c_id;
      } else {
        s_c.x = ((graphWidth / 2) + graphPosX) + partyPercentage * graphWidth / 2;
        s_c.y = graphPosY + graphHeight  /2 + graphHeight * 2 * c_id;
      }
    }
    sc_keys = new IntList();

    for (Integer i : sen_congresses.keySet()) {
      sc_keys.append(i);
    }

    sc_keys.sort();
  } 
  public void draw(boolean highlight)
  {
    boolean intersecting = false;
    int votes_with = -1, votes_against = -1;
    float ix=0, iy=0;

    for (Integer i : sen_congresses.keySet()) {
      if (isect(sen_congresses.get(i).x, sen_congresses.get(i).y, 
        selectDiam)) {
        intersecting = true;
        votes_with = sen_congresses.get(i).numVotesWith;
        votes_against = sen_congresses.get(i).numVotesAgainst;
        ix = sen_congresses.get(i).x;
        iy = sen_congresses.get(i).y;
      } 
    }

    if (highlight) {
      stroke(255);
      fill(255);
    } else if (intersecting) {
      stroke(255);
      fill(255);
      isect_names.append(id);
      isect_votes_with.append(votes_with);
      isect_votes_against.append(votes_against);
      //println(votes_with + ", " + votes_against);
      //text(name, 10, 10);
    } else if (party.equals("D")) {
      stroke(demColor);
      fill(demColor);
    } else {
      stroke(repubColor);
      fill(repubColor);
    }

    connectCongresses();

    SenCongress s_c;
    for (Integer i : sen_congresses.keySet()) {
      s_c = sen_congresses.get(i);
      ellipse(s_c.x, s_c.y, pointDiam, pointDiam);
    }
    
    if(intersecting) {
      noFill();
      ellipse(ix, iy, pointDiam*2, pointDiam*2);
    }
  }

  public void connectCongresses() {
    strokeWeight(1);
    for (int i = 1; i < sc_keys.size(); i++) {
      SenCongress s_c = sen_congresses.get(sc_keys.get(i - 1));
      SenCongress s_c2 = sen_congresses.get(sc_keys.get(i));
      line(s_c.x, s_c.y, s_c2.x, s_c2.y);
    }
  }
  
    
  public boolean isect(float x, float y, float r) {
    if (mouseX < (x - r) || mouseX > (x + r) ||
        mouseY < (y - r) || mouseY > (y + r)) {
      return false;
    } else {
      return true;
    }

  }
    
}
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

}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "final_proj" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
