
import java.util.Iterator;

LeaningGraph graph = null;
StringList isect_names = null;
IntList isect_votes_with = null, isect_votes_against = null;
ToggleButton buttonToggle = null;
PImage donkey = null, elephant = null, imageTitle = null;
ToggleButton meanToggle = null;

void setup()
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

void draw()
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

void mouseClicked() {
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


