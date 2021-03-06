package sg.gov.mom.wins2rules;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.text.*;

/**
 * Hello world!
 *
 */
public class App 
{
    // static DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
    public static void main( String[] args ) throws IOException
    {
        String runLogs = "";
        try {
            Scanner myReader = new Scanner(new File("test.log"));
            runLogs = runLogs + myReader.useDelimiter("\\Z").next();
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        
        String[] arrOfLogs = runLogs.split("\\r?\\n");
        // Map to store 
        // key: scn, value: list of all the responsetime for scn
        Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();
        for (String log : arrOfLogs) {
            String[] splitLogs = log.split(" ");
            String scn = splitLogs[0];
            int responseTime = Integer.parseInt(splitLogs[1]);
            if(!map.containsKey(scn)) {
                map.put(scn, new ArrayList<Integer>());
            }
            map.get(scn).add(responseTime);
        }

        List<Scn> allScns = new ArrayList<Scn>();
        // loop through each scn to obtain 95 percentile, min, max, mean
        for (Map.Entry<String, List<Integer>> set : map.entrySet()) {
            List<Integer> sortedList = set.getValue();
            Collections.sort(sortedList);
            int sum = 0;
            for (int num : sortedList) {
                // System.out.println(num);
                sum = sum + num;
            }
            String name = set.getKey();
            int max = sortedList.get(sortedList.size()-1);
            int min = sortedList.get(0);
            int mean = (sum/sortedList.size());
            int percentile95 = percentile(sortedList, 95.0);
            int percentile90 = percentile(sortedList, 90.0);

            Scn newScn = new Scn(name, percentile95, percentile90, min, max, mean);
            allScns.add(newScn);
        }
        Collections.sort(allScns);

        // create new run
        long unixTime = System.currentTimeMillis() / 1000L;
        Run run = new Run(unixTime, allScns);

        Gson gson = new Gson();
        History history;
        // print history
        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        try (Reader reader = new FileReader("test.json")) {

            // Convert JSON File to Java Object
            history = gson.fromJson(reader, History.class);

            history.getRuns().add(run);
            Collections.sort(history.getRuns(), Collections.reverseOrder());

            try (FileWriter writer = new FileWriter("history.json")) {
                gsonBuilder.toJson(history, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            try (Reader reader2 = new FileReader("template.html")) {
                int data = reader2.read();
                String fileString = "";
                while (data != -1) {  
                    fileString += (char) data;  
                    data = reader2.read();  
                }
                // 95 percentile 
                Map<String, List<Integer>> rows95Pencentile = new HashMap<String, List<Integer>>();
                int count = 0;
                for (Run r : history.getRuns()) {
                    count += 1;
                    for (Scn scn : r.getScn()) {
                        if (rows95Pencentile.containsKey(scn.getName())){
                            rows95Pencentile.get(scn.getName()).add(scn.getPercentile95());
                        } else {
                            rows95Pencentile.put(scn.getName(), new ArrayList<Integer>());
                            int subCount = count;
                            while (subCount > 1) {
                                rows95Pencentile.get(scn.getName()).add(0);
                                subCount -= 1;
                            }
                            rows95Pencentile.get(scn.getName()).add(scn.getPercentile95());
                        }
                    }
                    for (Map.Entry<String, List<Integer>> row : rows95Pencentile.entrySet()) {
                        if (count > row.getValue().size()){
                            row.getValue().add(0);
                        }
                    }
                }
                // mean
                Map<String, List<Integer>> rowsMean = new HashMap<String, List<Integer>>();
                count = 0;
                for (Run r : history.getRuns()) {     
                    count += 1;
                    for (Scn scn : r.getScn()) {
                        if (rowsMean.containsKey(scn.getName())){
                            rowsMean.get(scn.getName()).add(scn.getMean());
                        } else {
                            rowsMean.put(scn.getName(), new ArrayList<Integer>());
                            int subCount = count;
                            while (subCount > 1) {
                                rowsMean.get(scn.getName()).add(0);
                                subCount -= 1;
                            }
                            rowsMean.get(scn.getName()).add(scn.getMean());
                        }
                    }
                    for (Map.Entry<String, List<Integer>> row : rowsMean.entrySet()) {
                        if (count > row.getValue().size()){
                            row.getValue().add(0);
                        }
                    }
                    
                }
                // max
                Map<String, List<Integer>> rowsMax = new HashMap<String, List<Integer>>();
                count = 0;
                for (Run r : history.getRuns()) {
                    count += 1;
                    for (Scn scn : r.getScn()) {
                        if (rowsMax.containsKey(scn.getName())){
                            rowsMax.get(scn.getName()).add(scn.getMaximum());
                        } else {
                            rowsMax.put(scn.getName(), new ArrayList<Integer>());
                            int subCount = count;
                            while (subCount > 1) {
                                rowsMax.get(scn.getName()).add(0);
                                subCount -= 1;
                            }
                            rowsMax.get(scn.getName()).add(scn.getMaximum());
                        }
                    }
                    for (Map.Entry<String, List<Integer>> row : rowsMax.entrySet()) {
                        if (count > row.getValue().size()){
                            row.getValue().add(0);
                        }
                    }
                }
                // min 
                Map<String, List<Integer>> rowsMin = new HashMap<String, List<Integer>>();
                count = 0;
                for (Run r : history.getRuns()) {
                    count += 1;
                    for (Scn scn : r.getScn()) {
                        if (rowsMin.containsKey(scn.getName())){
                            rowsMin.get(scn.getName()).add(scn.getMinimum());
                        } else {
                            rowsMin.put(scn.getName(), new ArrayList<Integer>());
                            int subCount = count;
                            while (subCount > 1) {
                                rowsMin.get(scn.getName()).add(0);
                                subCount -= 1;
                            }
                            rowsMin.get(scn.getName()).add(scn.getMinimum());
                        }
                    }
                    for (Map.Entry<String, List<Integer>> row : rowsMin.entrySet()) {
                        if (count > row.getValue().size()){
                            row.getValue().add(0);
                        }
                    }
                }
                fileString += appendToHTMLFile(rows95Pencentile, history, "95 Percentile");
                fileString += appendToHTMLFile(rowsMean, history, "Mean");
                fileString += appendToHTMLFile(rowsMax, history, "Max");
                fileString += appendToHTMLFile(rowsMin, history, "Min");
                fileString += "<script>var acc = document.getElementsByClassName(\"accordion\");var i;" +
                "for (i = 0; i < acc.length; i++) {" +
                  "acc[i].addEventListener(\"click\", function() {" +
                    "this.classList.toggle(\"active\");" +
                    "var panel = this.nextElementSibling;" +
                    "if (panel.style.maxHeight) {" +
                      "panel.style.maxHeight = null;" +
                    "} else {" +
                      "panel.style.maxHeight = panel.scrollHeight + \"px\";" +
                    "}" +
                  "});" +
                "}" +
                "</script>";
                fileString += "</body></html>\n";

                // write file to a resulting HTML file
                try (FileWriter writer = new FileWriter("result.html")) {
                    writer.write(fileString);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String appendToHTMLFile(Map<String, List<Integer>> rows, History history, String metricName) {
        String fileString = "";
        fileString += "<button class=\"accordion\"><h2>" + metricName + " Response Times</h2></button><div class=\"panel\">\n";
        // fileString += "<h2>"+ metricName + " Response Times</h2>\n";
        fileString += "<table>\n";
        // add the scn name as headers
        fileString += "<tr>\n";
        fileString += "<th>Performance Test Runs</th>\n";
        for (Run r : history.getRuns()) {
            long unixDate = r.getDate();
            Date date = new Date(unixDate*1000L);
            SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            jdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            String dateFormatted = jdf.format(date);
            String stringToAdd = "<th colspan=\"2\">" + dateFormatted + "</th>";
            fileString += stringToAdd;
        }
        fileString += "</tr>\n";
        fileString += "<tr>\n";
        fileString += "<th>Scenario</th>\n";
        // add header for response time and delta
        for (Run r : history.getRuns()) {
            String stringToAdd = "<th>Response(ms)</th><th>Delta(ms)</th>";
            fileString += stringToAdd;
        }
        fileString += "</tr>\n";
        int totalCount = 0;
        int totalAbove50 = 0;
        int totalIncrease = 0;
        // iterate through each scn to parse into table row
        for (Map.Entry<String, List<Integer>> row : rows.entrySet()) {
            String rowName = row.getKey();
            List<Integer> rowValues = row.getValue();
            fileString += "<tr>\n";
            String scnName = "<th>"+ rowName +"</th>\n";
            fileString += scnName;
            // calculate for latest run difference to break pipeline
            if (rowValues.get(0) != 0) {
                totalCount += 1;
                int diff = rowValues.get(0) - rowValues.get(1);
                if (diff > 0) {
                    double percentRate = 100 * ((rowValues.get(0) - rowValues.get(1)) / (double)rowValues.get(1));
                    if (percentRate > 0) {
                        totalIncrease += 1;
                    }
                    if (percentRate >= 50.0) {
                        totalAbove50 += 1;
                    }
                }
            }
            
            for (int i=0; i<rowValues.size()-1; i++) {
                // delta value benchedmark on preivous run
                int diff = rowValues.get(i) - rowValues.get(i+1);
                double percentRate = 100 * ((rowValues.get(i) - rowValues.get(i+1)) / (double)rowValues.get(i+1));
                percentRate = (double)Math.round(percentRate * 10d) / 10d;
                if (rowValues.get(i) == 0) {
                    String noRun = "NO RUNS";
                    String stringToAdd = "<td>" + noRun + "</td><td style=\"background-color:#FF0000\"> KO </td>" ;
                    fileString += stringToAdd;
                }
                else if (rowValues.get(i+1) == 0) {
                    String noRun = " - ";
                    String stringToAdd = "<td>" + rowValues.get(i) + "</td><td style=\"background-color:#D3D3D3\">" + noRun + "</td>" ;
                    fileString += stringToAdd;
                }
                else if (diff < 0) {
                    String stringToAdd = "<td>" + rowValues.get(i) + "</td><td style=\"background-color:#00FF00\"> &#8593; " + diff*-1 + " (" + percentRate + "%) </td>" ;
                    fileString += stringToAdd;
                } else if (diff > 0) {
                    String stringToAdd = "<td>" + rowValues.get(i) + "</td><td style=\"background-color:#FF0000\"> &#8595; " + diff + " (" + percentRate + "%) </td>" ;
                    fileString += stringToAdd;
                } else {
                    String stringToAdd = "<td>" + rowValues.get(i) + "</td><td style=\"background-color:#D3D3D3\">" + diff + "</td>" ;
                    fileString += stringToAdd;
                }
                
            }
            // add last run one with delta == 0
            if (rowValues.get(rowValues.size()-1) == 0) {
                String noRun = "NO RUNS";
                String stringToAdd = "<td>" + noRun + "</td><td>FIRST RUN</td>" ;
                fileString += stringToAdd;
            }
            else {
                String stringToAdd = "<td>" + rowValues.get(rowValues.size()-1) + "</td><td>FIRST RUN</td>" ;
                fileString += stringToAdd;
            }
            fileString += "</tr>\n";
        }
        try {
            String filename= "result.log";
            FileWriter fw = new FileWriter(filename,true); //the true will append the new data
            if (totalCount == totalIncrease) {
                String appendStr = metricName + " - Every scenario performance decreased. KO \n";
                fw.write(appendStr);//appends the string to the file
            }
            if (totalAbove50 > 0) {
                String appendStr = metricName + " - One or more scenario performance decreased by more than 50%. KO \n";
                fw.write(appendStr);//appends the string to the file
            }
            
            fw.close();
        }
        catch(IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
        fileString += "</table></div>\n";
        return fileString;
    }
    
    public static int percentile(List<Integer> latencies, double percentile) {
        Collections.sort(latencies);
        int index = (int) Math.ceil(percentile / 100.0 * latencies.size());
        return latencies.get(index-1);
    }
}


