class Congress
{
  int id;
  HashMap<String, Senator> senators;
  Senator Rep_Leader;
  Senator Dem_Leader;
  Congress(int id)
  {
    this.id = id;
    senators = new HashMap<String, Senator>();
  }
  void setDemLeader(String name, String id, String state)
  {
    Dem_Leader = new Senator(name, "D", id, state);
    senators.put(name, Dem_Leader);
  }
  void setRepLeader(String name, String id, String state)
  {
    Rep_Leader = new Senator(name, "R",  id, state);
    senators.put(name, Rep_Leader);
  }
  boolean getDLeaderVote(String id)
  {
    Vote current = Dem_Leader.votes.get(id);
    if (current != null)
    {
      return current.v;
    }
    else return false;
  }
   boolean getRLeaderVote(String id)
  {
    Vote current = Rep_Leader.votes.get(id);
    if (current != null)
    {
      return current.v;
    }
    else return false;
  }
  boolean hasSenator(String name)
  {
    return senators.containsKey(name);
  }
  void addSenator(String name, String party, String id, String state)
  { 
    Senator s = new Senator(name, party, id, state);
    senators.put(name, s);
  }
  void addVote(String name, String id, String vote)
  {
    boolean v_;
    // print(vote);
    if (vote.equals("Yea"))
    {
      v_ = true;
    } else v_ = false;
    Vote v = new Vote(id, v_);
    senators.get(name).addVote(v);
  }
}

class Vote
{
  String id;
  Boolean v;
  
  Vote(String id, boolean vote)
  {
    this.id = id;
    v = vote;
  }
}
class Senator
{
  Senator Party_Leader = null;
  String name;
  String party;
  String id;
  String state;
  HashMap<String, Vote> votes;
  float partyPercentage;
  
  Senator(String name, String party, String id, String state)
  {
    this.name = name;
    this.party = party;
    this.id = id;
    this.state = state;
    votes = new HashMap<String, Vote>();
  }
  Senator(String name, String party)
  {
    this.name = name;
    this.party = party;
  }
  void addVote(Vote v)
  {
    votes.put(v.id, v);
  }
    
}


