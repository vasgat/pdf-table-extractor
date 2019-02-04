/*
 * Copyright 2018 vasgat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package certh.iti.mklab.pdf.table.extractor.utlis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.TableWithRulingLines;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;
import technology.tabula.writers.JSONWriter;

/**
 *
 * @author vasgat
 */
public class PDFUtils {

    public static String extractTablesWithRulings(String PathToPDF, double column_threshold, double row_threshold) throws IOException {
        PDDocument document = PDDocument.load(PathToPDF);
        Path pdfPath = Paths.get(PathToPDF);
        String store_path = pdfPath.getParent().toString() + "\\" + pdfPath.getFileName().toString().replace(".pdf", "");

        ObjectExtractor oe = null;

        oe = new ObjectExtractor(document);

        SpreadsheetExtractionAlgorithm algorithm = new SpreadsheetExtractionAlgorithm();

        String s = store_path + ".csv";
        PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(
                        new FileOutputStream(s), Charset.forName("UTF-8")));

        for (int i = 1; i <= document.getNumberOfPages(); i++) {
            Page page = oe.extract(i);
            List<TableWithRulingLines> extracted_tables
                    = (List<TableWithRulingLines>) algorithm.extract(page);

            if (extracted_tables.size() > 0) {
                String previous_contents = "";
                for (int t = 0; t < extracted_tables.size(); t++) {
                    StringBuilder sb = new StringBuilder();

                    (new JSONWriter()).write(sb, extracted_tables.get(t));

                    JSONArray data = (new JSONObject(sb.toString())).getJSONArray("data");

                    TreeMap<Double, TreeMap> lines = new TreeMap();
                    TreeSet<Double> set = new TreeSet();

                    for (int j = 0; j < data.length(); j++) {
                        JSONArray columns = data.getJSONArray(j);

                        Double top = .0;
                        TreeMap<Double, String> map = new TreeMap();
                        for (int k = 0; k < columns.length(); k++) {
                            JSONObject line = columns.getJSONObject(k);
                            map.put(line.getDouble("left"), Normalizer
                                    .normalize(line.getString("text").replaceAll("\\r", " "), Normalizer.Form.NFD)
                                    .replaceAll("[^\\p{ASCII}]", ""));

                            set.add(line.getDouble("left"));
                            if (line.getDouble("top") > 0) {
                                top = line.getDouble("top");
                            }
                        }
                        lines.put(top, map);
                    }

                    set.remove(0.0);
                    if (lines.size() > 1) {
                        ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>(lines.size());
                        for (Map.Entry<Double, TreeMap> entry : lines.entrySet()) {
                            ArrayList line = new ArrayList(set.size());
                            for (Double left : set) {
                                try {
                                    line.add(entry.getValue().get(left).toString());
                                } catch (NullPointerException ex) {
                                    line.add("null");
                                }
                            }
                            table.add(line);
                        }

                        ArrayList<Integer> nulls = new ArrayList();
                        for (int c = 0; c < set.size(); c++) {
                            nulls.add(0);
                        }
                        ArrayList<Integer> rowNulls = new ArrayList();

                        for (int rows = 0; rows < table.size(); rows++) {
                            ArrayList row = table.get(rows);

                            for (int column = 0; column < row.size(); column++) {

                                if (row.get(column) == null || row.get(column).equals("null") || row.get(column).equals("") || row.get(column).equals(" ")) {
                                    nulls.set(column, nulls.get(column) + 1);
                                }
                            }
                        }

                        ArrayList<Integer> toRemove = new ArrayList();
                        for (int column = 0; column < nulls.size(); column++) {
                            if (nulls.get(column) * 1.0 / table.size() > column_threshold) {
                                toRemove.add(column);
                            }
                        }

                        for (int rows = 0; rows < table.size(); rows++) {
                            ArrayList row = table.get(rows);

                            int n = 0;
                            for (int r : toRemove) {
                                row.remove(r - n);
                                n++;
                            }
                            table.set(rows, row);
                        }

                        int num_of_col = 0;
                        for (int rows = 0; rows < table.size(); rows++) {
                            ArrayList row = table.get(rows);
                            rowNulls.add(0);
                            num_of_col = row.size();
                            for (int column = 0; column < row.size(); column++) {
                                if (row.get(column) == null || row.get(column).equals("null") || row.get(column).equals("") || row.get(column).equals(" ")) {
                                    rowNulls.set(rows, rowNulls.get(rows) + 1);
                                }
                            }
                        }

                        toRemove = new ArrayList();
                        for (int row = 0; row < rowNulls.size(); row++) {
                            if (rowNulls.get(row) * 1.0 / num_of_col > row_threshold) {
                                toRemove.add(row);
                            }
                        }

                        int n = 0;
                        for (int r : toRemove) {
                            table.remove(r - n);
                            n++;
                        }

                        String contents = "";
                        for (ArrayList<String> line : table) {

                            for (String entry : line) {

                                contents += entry.replace(";", "|") + ";";
                            }
                            contents += '\n';
                        }
                        if (!previous_contents.equals(contents)) {
                            writer.println("page_" + i + "_table_" + (t + 1));
                            writer.println(contents);
                            previous_contents = contents;
                        }
                    }
                }
            }
        }
        document.close();
        writer.close();
        return s;
    }

    public static String extractTablesWithoutRulings(String PathToPDF, MergeType type) throws IOException {
        PDDocument document = PDDocument.load(new File(PathToPDF));
        Path pdfPath = Paths.get(PathToPDF);
        String store_path = pdfPath.getParent().toString() + "\\" + pdfPath.getFileName().toString().replace(".pdf", "");

        ObjectExtractor oe = null;

        oe = new ObjectExtractor(document);

        BasicExtractionAlgorithm algorithm = new BasicExtractionAlgorithm();

        String s = store_path + ".csv";
        PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(
                        new FileOutputStream(s), "UTF8"));

        for (int i = 1; i <= document.getNumberOfPages(); i++) {
            Page page = oe.extract(i);
            writer.println("Page " + i);
            List<Table> extracted_tables
                    = (List<Table>) algorithm.extract(page);

            for (int j = 0; j < extracted_tables.size(); j++) {
                List<List<String>> table = new ArrayList();
                int numOfCols = extracted_tables.get(j).getCols().size();

                //System.out.println(extracted_tables.get(j).getCols());
                if (numOfCols > 1) {
                    List<List<RectangularTextContainer>> columns = extracted_tables.get(j).getCols();

                    for (int k = 0; k < columns.size(); k++) {
                        List<RectangularTextContainer> row = columns.get(k);

                        for (int l = 0; l < row.size(); l++) {
                            if (k == 0) {
                                table.add(new ArrayList());
                            }
                            table.get(l).add(row.get(l).getText());
                        }

                    }
                    if (type.equals(MergeType.UPPER_MERGE)) {
                        upperMerge(table);
                    } else if (type.equals(MergeType.LOWER_MERGE)) {
                        lowerMerge(table);
                    }
                    //System.out.println(table);
                    for (int k = 0; k < table.size(); k++) {
                        List<String> row_cells = table.get(k);
                        for (int l = 0; l < row_cells.size(); l++) {
                            writer.print(Normalizer
                                    .normalize(row_cells.get(l), Normalizer.Form.NFD)
                                    .replaceAll("[^\\p{ASCII}]", "") + ";");
                        }
                        writer.println();
                    }
                }
            }
        }
        writer.close();
        return s;
    }

    private static void lowerMerge(List<List<String>> table) {

        for (int i = 0; i < table.size(); i++) {
            List<String> row = table.get(i);
            double density = 0;
            for (int j = 0; j < row.size(); j++) {
                if (!row.get(j).equals("")) {
                    density += 1;
                }
            }
            density /= row.size();
            if (density <= 0.5 && (i + 1) < table.size()) {
                List<String> nextRow = table.get(i + 1);
                for (int j = 0; j < row.size(); j++) {

                    if (!row.get(j).equals("")) {
                        nextRow.set(j, row.get(j) + " " + nextRow.get(j));
                    }
                }
                table.remove(row);
                i--;
            }
        }
    }

    private static void upperMerge(List<List<String>> table) {

        for (int i = 0; i < table.size(); i++) {
            List<String> row = table.get(i);
            double density = 0;
            for (int j = 0; j < row.size(); j++) {
                if (!row.get(j).equals("")) {
                    density += 1;
                }
            }
            density /= row.size();
            if (density <= 0.5 && (i - 1) > 0) {
                List<String> prevRow = table.get(i - 1);
                for (int j = 0; j < row.size(); j++) {

                    if (!row.get(j).equals("")) {
                        prevRow.set(j, prevRow.get(j) + " " + row.get(j));
                    }
                }
                table.remove(row);
                i--;
            }
        }
    }
}
