class LeaningGraph
{
  LeaningGraph() {
  }

  void draw(boolean drawSenate, boolean drawMeans)
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

  void drawStats(boolean drawSenate, boolean drawMeans)
  {
    if (drawMeans)
    {
      drawMeans(drawSenate);
    }
    else drawMedians(drawSenate);
  }
  void drawNames(boolean drawSenate) 
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

  void setGradient(int x, int y, float w, float h, color c1, color c2) 
  {
    float my_y = y;

    for (int j = 0; j < numCongresses; j++) {
      for (int i = x; i <= x + w; i++) {
        float inter = map(i, x, x + w, 0, 1);
        color c = lerpColor(c1, c2, inter);
        stroke(c);
        line(i, my_y, i, my_y + h);
      }

      my_y = my_y + h * 2;
    }
  }

  void drawCongress(boolean drawSenate)
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

  void drawMeans(boolean drawSenate) {
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
  void drawMedians(boolean drawSenate)
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
    


  void drawGridLines() 
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

  void drawLabels(boolean drawSenate)
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

  void drawKey(boolean drawMeans) 
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




