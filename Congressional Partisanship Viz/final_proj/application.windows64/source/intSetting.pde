final int canvasWidth = 1350;
final int canvasHeight = 675;
final color bgColor = color(100);

final color mouseOnColor = color(150);
final color notCheckColor = color(50);
final color mouseOnWarningColor = color(251,106,74);
final color checkedColor = color(120);
final color labelColor = color(255);

final int[] rangeCongressNum = {101, 113};
final int numCongresses = rangeCongressNum[1] - rangeCongressNum[0] + 1;

final color repubColor = color(255, 0, 0);
final color demColor = color(0, 0, 255);
final color repubBGColor = color(255,140,140);
final color demBGColor = color(120,120,255);
final color demMeanColor = color(255, 165, 0);
final color repMeanColor = color(0,255,0);

final int graphPosY = floor(canvasHeight * .175);
final float graphWidth = canvasWidth * .75;
final int graphPosX = floor(canvasWidth - graphWidth) / 2;
final float graphHeight = (canvasHeight - graphPosY * 2.5) / 23;
final float wholeGraphHeight = (2 * numCongresses - 1) * graphHeight; 
final float midX = graphPosX + graphWidth / 2;
final float bottomY = graphPosY + wholeGraphHeight;

final String leftLabel = "Voted with the Democratic Party Leader on a Higher Percentage of Bills"; // figure out better label name
final String rightLabel = "Voted with the Republican Party Leader on a Higher Percentage of Bills"; // "... Leader for a Higher Percentage of Bills"?
final float leftLabelX = graphPosX + graphWidth / 4;
final float labelY = graphPosY + wholeGraphHeight + (canvasHeight - graphPosY - wholeGraphHeight) / 2;
final float rightLabelX = graphPosX + 3 * graphWidth / 4;
final String title = "Partisanship Over Time             in the US Congress     ";
final int titleSize = floor(graphPosY * .2);
final int labelTextSize = floor(min(constrain(canvasWidth * .0105, 4, 24), constrain(canvasHeight * .0215, 4, 24)));
final float percTextSize = labelTextSize * .75;

final int pointDiam = floor(graphHeight * .5);
final int selectDiam = pointDiam/3;

final float namesX = graphPosX + graphWidth  + (canvasWidth - graphPosX - graphWidth) / 4 - 3;
final float namesY = graphPosY;
final float namesSize = percTextSize * .9;

final float termTextSize = percTextSize;
//final float termX = 3 * graphPosX / 4;
final float yearX = 2.5 * graphPosX / 4;
final float partyControlX = 1 * graphPosX / 4;
final float partyControlSize = 0.5 * graphPosX / 4;

final float arrowY = labelY - (labelY - (graphPosY + wholeGraphHeight)) / 2.5;
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
final color colorToggleButton = color(60);
final float xToggleButton = namesX;
final float yToggleButton = namesY/3 - titleSize;
final float hToggleButton = titleSize;
final float textSizeToggleButton = titleSize / 1.75;

final String truestrMeanButton = "MEAN";
final String falsestrMeanButton = "MEDIAN";
final color colorMeanButton = color(0,0,0);
final float xMeanButton = namesX;
final float yMeanButton = namesY/3 + titleSize/2;
final float hMeanButton = titleSize;
final float wMeanKey = canvasWidth * .25;
final float hMeanKey = (canvasHeight - labelY) / 3;
final float yMeanKey = labelY + (canvasHeight - labelY) / 2 - hMeanKey / 2.5;

final float xMeanKey = canvasWidth / 2 - wMeanKey / 2;
final float textSizeMeanKey = percTextSize * .95;
final color colorMeanKey = color(60);
final float xDemLine = xMeanKey + wMeanKey * .025;
final float y1DemLine = yMeanKey + hMeanKey * .2;
final float y2DemLine = yMeanKey + hMeanKey - hMeanKey * .2;
final float xRepLine = xMeanKey + wMeanKey * .5 + wMeanKey * .025;
final float y1RepLine = yMeanKey + hMeanKey * .2;
final float y2RepLine = yMeanKey + hMeanKey - hMeanKey * .2;
final float xDemKeyLabel = xMeanKey + wMeanKey * .025 * 2;
final float yKeyLabel = yMeanKey + hMeanKey / 2;
final float xRepKeyLabel  = xMeanKey + wMeanKey * .5 + wMeanKey * .05;

 PFont font = createFont("Arial", 15);
 PFont bold_font = createFont("Arial Bold", 15); 
