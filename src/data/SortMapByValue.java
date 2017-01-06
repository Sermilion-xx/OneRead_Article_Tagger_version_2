package data;



import org.javatuples.Pair;

import java.util.*;


public class SortMapByValue {

    public int correctCount = 0;

    public Map<Integer, Pair<String, Double>> sortByComparator(Map<Integer, Pair<String, Double>> unsortMap, final boolean order) {
        List<Map.Entry<Integer, Pair<String, Double>>> list = new LinkedList<>(unsortMap.entrySet());
        // Sorting the list based on values
        Collections.sort(list, (o1, o2) -> {
            if (order) {
                return o1.getValue().compareTo(o2.getValue());
            } else {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        // Maintaining insertion order with the help of LinkedList
        Map<Integer, Pair<String, Double>> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, Pair<String, Double>> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    public void printMap(Map<Integer, Pair<String, Double>> map, int correct) {
        int pos = new ArrayList<>(map.keySet()).indexOf(correct);
        if(pos == 0){
            correctCount++;
        }
        for (Map.Entry<Integer, Pair<String, Double>> entry : map.entrySet()) {
            System.out.println("Id : " + entry.getKey() + " Name: " +entry.getValue().getValue0()+ " Value : " + entry.getValue().getValue1());
        }
    }
}