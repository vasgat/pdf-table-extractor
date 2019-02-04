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
import certh.iti.mklab.pdf.table.extractor.utlis.PDFUtils;
import certh.iti.mklab.pdf.table.extractor.utlis.TableType;
import java.io.IOException;

/**
 *
 * @author vasgat
 */
public class PDFTableExtractor {

    private MergeType merge_type;
    private double columns_density;
    private double rows_density;

    public PDFTableExtractor() {
        this.merge_type = MergeType.NO_MERGE;
        this.columns_density = 0.5;
        this.rows_density = 0.5;
    }

    public void setMergeType(MergeType type) {
        this.merge_type = type;
    }

    public void setColumnsDensity(double columns_density) {
        this.columns_density = 1 - columns_density;
    }

    public void setRowsDensity(double rows_density) {
        this.rows_density = 1 - rows_density;
    }

    public void processTable(String PDFFilePath, TableType table_type) throws IOException, Exception {
        if (table_type.equals(TableType.WITH_RULINGS)) {
            PDFUtils.extractTablesWithRulings(PDFFilePath, this.columns_density, this.rows_density);
        } else if (table_type.equals(TableType.WITHOUT_RULINGS)) {
            PDFUtils.extractTablesWithoutRulings(PDFFilePath, this.merge_type);
        } else {
            throw new Exception("Invalid TableType");
        }
    }
}
