file = File.new("house113.csv")

d_leader_id = "P000197"
r_leader_id = "C001046"

people = {}

# person = {person_id: id, party: party, person_name: name, state: state,
#          congress: 1, votes: { vote_id: [vote_id, vote] }}

puts "congress,person_id,person_name,party,state,party_percentage"

index = -1
file.each_line do |line|
  index += 1
  next if index == 0
  line_arr = line.split(",")
  congress = line_arr[0]
  vote_id = line_arr[2]
  person_id = line_arr[3]
  vote = line_arr[4]
  party = line_arr[5]
  person_name = line_arr[6]
  state = line_arr[7]

  next if party.strip != "D" && party.strip != "R"
  person = people[person_id]

  if person
    person[:votes][vote_id] = [vote_id, vote]
  else
    people[person_id] = {person_id: person_id, party: party, 
                         person_name: person_name, state: state,
                         congress: congress, votes: {},
                         party_percentage: 0 }
    person = people[person_id]
    arr = [vote_id,vote]
    hash = Hash.new []
    hash[vote_id] += arr
    person[:votes] = hash
  end
end

people.each do |key, person|
  if person[:party].strip == "D"
    leader = people[d_leader_id]
  else
    leader = people[r_leader_id]
  end

  num_votes = 0
  num_party_votes = 0

  person[:votes].each do |v_id, v|
    l_v = leader[:votes][v_id]
    num_votes += 1
    if l_v
      if (l_v[1] && v[1]) || (!l_v[1] && !v[1]) 
        num_party_votes += 1
      else
        num_party_votes -= 1
      end
    else
      num_party_votes -= 1
    end
  end

  if num_votes == 0
    person[:party_percentage] = 0
  else
    person[:party_percentage] = num_party_votes.to_f / num_votes.to_f
  end
  
  arr = [person[:congress].strip,person[:person_id].strip,
         person[:person_name].strip, person[:party].strip, 
         person[:state].strip, person[:party_percentage]]
  str = arr.join(",")
  puts str
end

file.close

