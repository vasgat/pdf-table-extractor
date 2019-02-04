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
package certh.iti.mklab.pdf.table.extractor;

import certh.iti.mklab.pdf.table.extractor.utlis.MergeType;
import certh.iti.mklab.pdf.table.extractor.utlis.TableType;
import java.util.Arrays;

/**
 *
 * @author vasgat
 */
public class Main {

    public static void main(String[] args) throws Exception {
        PDFTableExtractor tableExtractor = new PDFTableExtractor();
        
        try {
            TableType type = TableType.WITH_RULINGS;
            if (args.length > 1 && args[1].equals("WITHOUT_RULINGS")) {
                type = TableType.WITHOUT_RULINGS;
            }

            if (type.equals(TableType.WITH_RULINGS) && args.length > 2) {
                tableExtractor.setRowsDensity(Double.parseDouble(args[2]));
            }

            if (type.equals(TableType.WITH_RULINGS) && args.length > 3) {
                tableExtractor.setColumnsDensity(Double.parseDouble(args[3]));
            }

            if (type.equals(TableType.WITHOUT_RULINGS) && args.length > 2) {
                MergeType mType = MergeType.NO_MERGE;
                if (args[2].equals("UPPER_MERGE")) {
                    mType = MergeType.UPPER_MERGE;
                } else if (args[2].equals("LOWER_MERGE")) {
                    mType = MergeType.LOWER_MERGE;
                }
                tableExtractor.setMergeType(mType);
            }

            tableExtractor.processTable(args[0], type);
        } catch (ArrayIndexOutOfBoundsException e) {

        }
    }
}
