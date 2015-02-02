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
  void setDemLeader(String name, String id, String state)
  {
    dem_leader_id = id;
 //   senators.put(name, Dem_Leader);
  }
  void setRepLeader(String name, String id, String state)
  {
    rep_leader_id = id;//new Senator(name, "R",  id, state);
   // senators.put(name, Rep_Leader);
  }
  boolean hasSenator(String id)
  {
    return senator_ids.hasValue(id);
  }
  void addSenator(String name, String party, String id, String state)
  { 
    senator_ids.append(id);
    if(party.equals("R")) {
      rep_count += 1;
    } else {
      dem_count += 1;
    }
  }
  void draw(float myY)
  {
    for (Senator senator: senators.values())
    {
      senator.draw(false);
    }
  }
  
  int getRepCount() { return rep_count; }
  int getDemCount() { return dem_count; }  

}
