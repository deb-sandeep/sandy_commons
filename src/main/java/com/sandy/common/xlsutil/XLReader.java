package com.sandy.common.xlsutil ;

import java.io.File ;
import java.io.FileInputStream ;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.poi.ss.usermodel.Sheet ;
import org.apache.poi.ss.usermodel.Workbook ;
import org.apache.poi.xssf.usermodel.XSSFWorkbook ;

public class XLReader {

    public List<List<String>> getRows( File file, String sheetName, 
                                       int startRow, int startCol, int endCol )
            throws Exception {

        List<List<String>> rows = new ArrayList<>() ;
        Workbook workbook = null ;
        FileInputStream fIs = null ;

        try {
            fIs = new FileInputStream( file ) ;
            workbook = new XSSFWorkbook( fIs ) ;

            Sheet sheet = getSheet( workbook, sheetName ) ;
            int numPhyCols = sheet.getRow( startRow ).getLastCellNum() ;
            int numRows = sheet.getLastRowNum() ;

            if( endCol == -1 || endCol > numPhyCols ) {
                endCol = numPhyCols - 1 ;
            }

            for ( int i = startRow + 1; i<= numRows; i++ ) {
                List<String> cellValues = null ;
                cellValues = XLSUtil.getCellValues( sheet.getRow( i ), startCol, endCol ) ;
                rows.add( cellValues ) ;
            }
        } finally {
            fIs.close() ;
        }

        return rows ;
    }

    private Sheet getSheet( Workbook workbook, String sheetName ) {

        Sheet sheet = null ;
        if( sheetName != null ) {
            sheet = workbook.getSheet( sheetName ) ;
        } else {
            sheet = workbook.getSheetAt( 0 ) ;
        }
        return sheet ;
    }
}
