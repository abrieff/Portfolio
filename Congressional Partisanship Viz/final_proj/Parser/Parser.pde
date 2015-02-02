final int start_congress = 101;
final int end_congress = 113;
final int NUM_CONGRESSES = 13;
Table vote_data; 
Table leaders;
String[] header;
Congress[] congresses;
void setup()
{
  String Congressional_Data = "congress100_to_113.csv";
  String leader_data = "congressleaders.csv";
//  String Congressional_Data = "congress" + int.toString(start_congress) + "_to_" + int.toString(end_congress) + ".csv";
  vote_data = loadTable(Congressional_Data, "header");
  leaders = loadTable(leader_data, "header");
  header = vote_data.getColumnTitles();
  setCongresses();
  calculate_percentages();
}

void draw()
{
}

void setCongresses()
{
  congresses = new Congress[NUM_CONGRESSES];
  Congress curc;
  String curn;
  
  for (int i = 0; i < NUM_CONGRESSES; i++)
  {
    congresses[i] = new Congress(i + start_congress);
  }
  for (TableRow row : leaders.rows()) {
    if (row.getString("party").equals("D"))
    {
      print("here");
      congresses[row.getInt("congress") - start_congress].setDemLeader(row.getString("name"), row.getString("id"), row.getString("state"));
    }
    else congresses[row.getInt("congress") - start_congress].setRepLeader(row.getString("name"), row.getString("id"), row.getString("state"));
  }
  for (TableRow row: vote_data.rows())
  {
     if (row.getInt("congress") != 100) {
     curc = congresses[(row.getInt("congress")) - start_congress];
     curn = row.getString("person_name");
     if (!curc.hasSenator(curn))
     {
       curc.addSenator(curn, row.getString("party"), row.getString("person_id"), row.getString("state"));
     }
     curc.addVote(curn, row.getString("vote_id"), row.getString("vote"));
  }
  }
  
}
void calculate_percentages()
{
  int num_votes = 0;
  int num_party_votes = 0;
  for (int i = 0; i < NUM_CONGRESSES; i++)
  {
    for(Senator senator: congresses[i].senators.values())
    {
      num_party_votes = 0;
      //print (senator.votes.size() + " " +  congresses[i].Dem_Leader.votes.size() + " " + congresses[i].Rep_Leader.votes.size() + "\n");
        num_votes = senator.votes.size();
      for (Vote v: senator.votes.values())
      {
          if (v.v)
          {
                  if (senator.party.equals("D") && congresses[i].getDLeaderVote(v.id))
                  {
                    num_party_votes++;
                  }
                  else if (senator.party.equals("R") && congresses[i].getRLeaderVote(v.id))
                  {
                    num_party_votes++;
                  }
          }
          else 
          {
            if (senator.party.equals("D") && !congresses[i].getDLeaderVote(v.id))
                  {
                    num_party_votes++;
                  }
                  else if (senator.party.equals("R") && !congresses[i].getRLeaderVote(v.id))
                  {
                    num_party_votes++;
                  }
          }
          
      }
      senator.partyPercentage = ((float) num_party_votes)/ ((float) num_votes);
    }
  }
  for (int i = 0; i < NUM_CONGRESSES; i++)
  {
   // print(congresses[i].Dem_Leader.name + " " + congresses[i].Rep_Leader.name + "\n");
    for (Senator senator: congresses[i].senators.values())
    {
     print("Name: " + senator.name + " Percentage: " + senator.partyPercentage + "\n");
    }
  }
}
