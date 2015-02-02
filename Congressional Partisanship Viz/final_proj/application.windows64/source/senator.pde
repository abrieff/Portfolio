class SenCongress
{
  float partyPercentage;
  String senator_id;
  float x, y;
  int numVotesWith, numVotesAgainst;

  SenCongress(String id) {
    senator_id = id;
  }
  void addPercentage(float partyPercentage_)
  {
    partyPercentage = partyPercentage_;
  }
  void setNumVotesWith(int n) { numVotesWith = n; }
  void setNumVotesAgainst(int n) { numVotesAgainst = n; }
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
  void addCongress(int c_id) 
  {
    SenCongress s_c = new SenCongress(id);
    print(c_id);
    sen_congresses.put(c_id, s_c);
  }
  void addPercentage(int congress_id, float partyPercentage_, int n1, int n2)
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
  void setPositions() {
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
  void draw(boolean highlight)
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

  void connectCongresses() {
    strokeWeight(1);
    for (int i = 1; i < sc_keys.size(); i++) {
      SenCongress s_c = sen_congresses.get(sc_keys.get(i - 1));
      SenCongress s_c2 = sen_congresses.get(sc_keys.get(i));
      line(s_c.x, s_c.y, s_c2.x, s_c2.y);
    }
  }
  
    
  boolean isect(float x, float y, float r) {
    if (mouseX < (x - r) || mouseX > (x + r) ||
        mouseY < (y - r) || mouseY > (y + r)) {
      return false;
    } else {
      return true;
    }

  }
    
}
