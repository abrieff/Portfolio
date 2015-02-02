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
    
void setCongresses()
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


void setLeaders(Table leaders_, Congress[] congresses_)
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

void setSenators(Table[] vote_data_, Congress[] congresses_, HashMap<String, Senator> senators_)
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
  
void calc_means() {
  calc_congress_means(congresses, senators);
  calc_congress_means(hcongresses, houses);  
}
void calc_medians() {
  calc_congress_medians(congresses, senators);
  calc_congress_medians(hcongresses, houses);
}

void calc_congress_medians(Congress[] _congresses, HashMap<String,Senator> people)
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
        
        
void calc_congress_means(Congress[] _congresses, HashMap<String,Senator> people) {
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

