//Tiffany Tsang
//Analyze LA bikeshare data
//October 30, 2018

import com.opencsv.CSVReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ParseData {
	private String baseFolder = "/Users/tiffanyimovie/Desktop/coding-challenges/bikeshare/";
	private String csvFile = "los-angeles-metro-bike-share-trip-data/metro-bike-share-trip-data.csv";
	private ArrayList<String> data = new ArrayList<>();


	public ParseData() {
		//to convert socrata_metadata.json into a more readable form
		//convertToReadableJSON();

		//Data Visuals: Display or graph 3 metrics or trends from the data set that are interesting to you.
		//1. Car gas saved
		double totalDistance = totalDistance();
		int numTrips = numTrips();
		data.add("Car gas saved," + totalDistance/24);
		data.add("Car money saved," + totalDistance*0.12);

		//2. Average trip duration versus average plan duration
		estimationAccuracy();

		//3. Freqeuncy of AM rides versus PM rides
		tripsPerHour();

		//Which start/stop stations are most popular?
		data.add("Popular start," + popularStations(4));
		data.add("Popular end," + popularStations(7));

		//What is the average distance traveled?
		data.add("Average distance," + totalDistance/numTrips);

		//How many riders include bike sharing as a regular part of their commute?
		data.add("Regular riders," + numRiders());

		//Add number of trips to data array
		data.add("Number of trips," + numTrips);

		writeResultsToFile();
	}

	public void convertToReadableJSON() {
		try {
            String content = new String(Files.readAllBytes(Paths.get(baseFolder + "los-angeles-metro-bike-share-trip-data/socrata_metadata.json")),
                    StandardCharsets.UTF_8);
            JSONObject module = new JSONObject(content);

            BufferedWriter writer = new BufferedWriter(new FileWriter(baseFolder + "los-angeles-metro-bike-share-trip-data/socrata_metadata_readable.json"));
            writer.write(module.toString(4));
            writer.close();

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
	}

	//Get number of total trips
	public int numTrips() {
		int numTrips = 0;
		try {
			//csv file containing data
			String strFile = baseFolder + csvFile;
			CSVReader reader = new CSVReader(new FileReader(strFile));
			String [] nextLine = reader.readNext();
			while ((nextLine = reader.readNext()) != null) {
				numTrips++;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		return numTrips;
	}

	//Compare actual and estimated trip duration
	public void estimationAccuracy() {
		int actualDuration = 0;
		int estimatedDuration = 0;
		int numTrips = 0;
		try {
			//csv file containing data
			String strFile = baseFolder + csvFile;
			CSVReader reader = new CSVReader(new FileReader(strFile));
			String [] nextLine = reader.readNext();
			while ((nextLine = reader.readNext()) != null) {
				if(nextLine[1].length() > 0 && nextLine[11].length() > 0) {
					actualDuration += Integer.parseInt(nextLine[1]);
					estimatedDuration += Integer.parseInt(nextLine[11]);
					numTrips++;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		data.add("Actual duration,"+ (double)(actualDuration) / numTrips);
		data.add("Estimated duration," + (double)(estimatedDuration) / numTrips);
	}

	//Find number of trips that start at each hour
	//Find number of trips that end at each hour
	public void tripsPerHour() {
		HashMap<Integer, Integer> start = new HashMap<>();
		HashMap<Integer, Integer> end = new HashMap<>();

		for(int i = 0; i < 24; i++) {
			start.put(i,0);
			end.put(i,0);
		}

		try {
			//csv file containing data
			String strFile = baseFolder + csvFile;
			CSVReader reader = new CSVReader(new FileReader(strFile));
			String [] nextLine = reader.readNext();
			while ((nextLine = reader.readNext()) != null) {
				String s = nextLine[2].substring(nextLine[2].indexOf("T")+1);
				String e = nextLine[3].substring(nextLine[3].indexOf("T")+1);
				Integer startHour = Integer.parseInt(s.substring(0,s.indexOf(":")));
				Integer endHour = Integer.parseInt(e.substring(0,e.indexOf(":")));

				start.put(startHour, start.get(startHour)+1);
				end.put(endHour, end.get(endHour)+1);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		data.add("Start");
		for(Map.Entry<Integer,Integer> m : start.entrySet()) {
			data.add(m.getKey() + "," + m.getValue());
		}

		data.add("End");
		for(Map.Entry<Integer,Integer> m : end.entrySet()) {
			data.add(m.getKey() + "," + m.getValue());
		}
	}

	//Get most popular stations
	public String popularStations(int col) {
		//find most frequent station ID
		//start or stop is indicated by column number of csv
		HashMap<String, Integer> starting = new HashMap<>();
		try {
			//csv file containing data
			String strFile = baseFolder + csvFile;
			CSVReader reader = new CSVReader(new FileReader(strFile));
			String [] nextLine = reader.readNext();
			while ((nextLine = reader.readNext()) != null) {
				if(starting.containsKey(nextLine[col])) {
					starting.put(nextLine[col],starting.get(nextLine[col]).intValue()+1);
				}
				else {
					starting.put(nextLine[col],1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		int max = -1;
		String maxStation = "No station found.";

		for(Map.Entry<String, Integer> m : starting.entrySet()) {
			if(max < m.getValue()) {
				max = m.getValue();
				maxStation = m.getKey();
			}
		}

		return "Station " + maxStation + " with " + max + " visits.";
	}

	//Get total distance traveled
	public double totalDistance() {
		//Use start station coordinates and end station coordinates to calculate distance
		//Add distances to variable
		//Take the average based on number of trips
		//Does not count entries with empty data for coordinates
		double sumDistance = 0.0;
		try {
			//csv file containing data
			String strFile = baseFolder + csvFile;
			CSVReader reader = new CSVReader(new FileReader(strFile));
			String [] nextLine = reader.readNext();
			while ((nextLine = reader.readNext()) != null) {

				double startLat;
				double startLon;
				double endLat;
				double endLon;

				if(!(nextLine[5].length() <= 0 || nextLine[6].length() <= 0 || nextLine[8].length() <= 0 || nextLine[9].length() <= 0)) {
					startLat = Double.parseDouble(nextLine[5]);
					startLon = Double.parseDouble(nextLine[6]);
					endLat = Double.parseDouble(nextLine[8]);
					endLon = Double.parseDouble(nextLine[9]);

					double earthRadius = 6371; //km

					double lat1 = Math.toRadians(startLat);
					double lon1 = Math.toRadians(startLon);
					double lat2 = Math.toRadians(endLat);
					double lon2 = Math.toRadians(endLon);
					    
					double dLng = (lon2 - lon1);
					double dLat = (lat2 - lat1);
					    
					double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
					           Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
					           Math.sin(dLng/2) * Math.sin(dLng/2);
					double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
					double km = earthRadius * c;
					    
					double distance = km * 0.621371; //miles

					sumDistance += distance;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		return sumDistance;

	}

	//Get number of regular riders
	public int numRiders() {
		//Every pass holder that is not walkup
		int num = 0;
		try {
			//csv file containing data
			String strFile = baseFolder + csvFile;
			CSVReader reader = new CSVReader(new FileReader(strFile));
			String [] nextLine = reader.readNext();
			while ((nextLine = reader.readNext()) != null) {
				if(!nextLine[13].equals("Walk-up")) {
					num++;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return num;
	}

	//Write results to txt for javascript and to csv for Excel graph making
	public void writeResultsToFile() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(baseFolder + "public/results.txt"));
			BufferedWriter writer2 = new BufferedWriter(new FileWriter(baseFolder + "public/results.csv"));
			for(String d : data) {
				writer.write(d + "\n");
				writer2.write(d + "\n");
			}
			writer.close();
			writer2.close();
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
	}

	public static void main(String [] args) {
		new ParseData();
	}
}