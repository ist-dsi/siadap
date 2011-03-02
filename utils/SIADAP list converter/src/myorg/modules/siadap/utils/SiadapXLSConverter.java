/**
 * 
 */
package myorg.modules.siadap.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt)
 * 
 */
public class SiadapXLSConverter {
    /**
     * creates an {@link HSSFWorkbook} the specified OS filename.
     */
    private static HSSFWorkbook readFile(String filename) throws IOException {
	return new HSSFWorkbook(new FileInputStream(filename));
    }

    /**
     * given a filename this outputs a sample sheet with just a set of
     * rows/cells.
     */
    private static void testCreateSampleSheet(String outputFilename) throws IOException {
	int rownum;
	HSSFWorkbook wb = new HSSFWorkbook();
	HSSFSheet s = wb.createSheet();
	HSSFCellStyle cs = wb.createCellStyle();
	HSSFCellStyle cs2 = wb.createCellStyle();
	HSSFCellStyle cs3 = wb.createCellStyle();
	HSSFFont f = wb.createFont();
	HSSFFont f2 = wb.createFont();

	f.setFontHeightInPoints((short) 12);
	f.setColor((short) 0xA);
	f.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
	f2.setFontHeightInPoints((short) 10);
	f2.setColor((short) 0xf);
	f2.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
	cs.setFont(f);
	cs.setDataFormat(HSSFDataFormat.getBuiltinFormat("($#,##0_);[Red]($#,##0)"));
	cs2.setBorderBottom(HSSFCellStyle.BORDER_THIN);
	cs2.setFillPattern((short) 1); // fill w fg
	cs2.setFillForegroundColor((short) 0xA);
	cs2.setFont(f2);
	wb.setSheetName(0, "HSSF Test");
	for (rownum = 0; rownum < 300; rownum++) {
	    HSSFRow r = s.createRow(rownum);
	    if ((rownum % 2) == 0) {
		r.setHeight((short) 0x249);
	    }

	    for (int cellnum = 0; cellnum < 50; cellnum += 2) {
		HSSFCell c = r.createCell(cellnum);
		c.setCellValue(rownum * 10000 + cellnum + (((double) rownum / 1000) + ((double) cellnum / 10000)));
		if ((rownum % 2) == 0) {
		    c.setCellStyle(cs);
		}
		c = r.createCell(cellnum + 1);
		c.setCellValue(new HSSFRichTextString("TEST"));
		// 50 characters divided by 1/20th of a point
		s.setColumnWidth(cellnum + 1, (int) (50 * 8 / 0.05));
		if ((rownum % 2) == 0) {
		    c.setCellStyle(cs2);
		}
	    }
	}

	// draw a thick black border on the row at the bottom using BLANKS
	rownum++;
	rownum++;
	HSSFRow r = s.createRow(rownum);
	cs3.setBorderBottom(HSSFCellStyle.BORDER_THICK);
	for (int cellnum = 0; cellnum < 50; cellnum++) {
	    HSSFCell c = r.createCell(cellnum);
	    c.setCellStyle(cs3);
	}
	s.addMergedRegion(new CellRangeAddress(0, 3, 0, 3));
	s.addMergedRegion(new CellRangeAddress(100, 110, 100, 110));

	// end draw thick black border
	// create a sheet, set its title then delete it
	s = wb.createSheet();
	wb.setSheetName(1, "DeletedSheet");
	wb.removeSheetAt(1);

	// end deleted sheet
	FileOutputStream out = new FileOutputStream(outputFilename);
	wb.write(out);
	out.close();
    }

    /**
     * Method main
     * 
     * 
     * given 2 arguments where the first is an input filename and the second an
     * output filename (not write), attempts to fully read in the spreadsheet
     * and fully write it out.<br/>
     * 
     */
    public static void main(String[] args) {
	String fileName = args[0];
	if (args.length != 2) {
	    System.out.println("Wrong number of arguments. The first argument should be the input and second the output");
	    System.exit(1);
	}
	try {
	    System.out.println("Workbook conversion, input file: " + fileName + " output file: " + args[1]);
	    HSSFWorkbook wb = SiadapXLSConverter.readFile(fileName);
	    FileWriter fileWriter = new FileWriter(args[1]);
	    extractIstIds(wb, fileWriter);

	    //		    wb.write(stream);
	    fileWriter.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private static final boolean debug = true;

    private static void debug(String stringToPrint) {
	if (debug)
	    System.out.println(stringToPrint);
    }

    private static void extractIstIds(HSSFWorkbook wb, FileWriter fileWriter) {
	int nrOfValidRows = 0;
	int nrOfSheets = 0;
	int nrOfCCCaught = 0;
	int nrOfUndefinedDetected = 0;
	int nrOfPossibleCCFormatErrors = 0;
	//let's adapt to the new format of the ADIST and IST workbook that has two sheets now
	ArrayList<HSSFSheet> sheetsToImport = new ArrayList<HSSFSheet>();
	sheetsToImport.add(wb.getSheet("Ordenação por Serviço (IST)"));
	sheetsToImport.add(wb.getSheet("Ordenação por Serviço (ADIST)"));
	DataFormatter dataFormatter = new DataFormatter();
	boolean changedCC = false;
	boolean changedFromAValidRegionToAnInvalid = false;
	for (HSSFSheet sheet : sheetsToImport) {
	    nrOfSheets++;
	    debug("Processing sheet " + sheet.getSheetName());
	    Row row;
	    Iterator<Row> rowIterator = sheet.rowIterator();
	    String lastCostCenterCaught = null;
	    while (rowIterator.hasNext()) {
		row = rowIterator.next();
		Cell cell = row.getCell(0);
		Integer firstMecNumber = null;
		Integer secondMecNumber = null;
		try {
		    //lets get the cost centers and keep track of them
		    String rowCellA = dataFormatter.formatCellValue(cell);
		    //let's check if it has the cost center there on the first column
		    if (rowCellA.contains("- CC ")) {
			//let's extract the cost center
			lastCostCenterCaught = rowCellA.substring(rowCellA.lastIndexOf("- CC ") + 5);
			nrOfCCCaught++;
			changedCC = true;
		    }
		    //if it isn't on the first let's try the second
		    else {
			Cell cellB = row.getCell(1);
			String rowCellB = dataFormatter.formatCellValue(cellB);

			//let's check if it contains the cost center or not
			if (rowCellB.contains("- CC ")) {
			    //let's extract the cost center
			    lastCostCenterCaught = rowCellB.substring(rowCellB.lastIndexOf("- CC ") + 5);
			    nrOfCCCaught++;
			    changedCC = true;
			}

		    }

		    firstMecNumber = Integer.valueOf(dataFormatter.formatCellValue(cell));
		    //ok, we got the first number, now let's check if the D column has a valid number as well

		    cell = row.getCell(3);
		    nrOfUndefinedDetected++;
		    cell.getNumericCellValue();
		    secondMecNumber = Integer.valueOf(dataFormatter.formatCellValue(cell));

		    //let's check if we caught a cost center or not, if not, something went wrong and we must exit
		    if (lastCostCenterCaught == null) {
			System.out.println("Aborted, no cost center found before finding a valid row");
			System.exit(1);
		    }

		    //so we have a valid one!
		    nrOfValidRows++;

		    if (changedFromAValidRegionToAnInvalid && !changedCC) {
			//the idea here is that between each valid set of evaluated/evaluator there should be a change of the CC
			nrOfPossibleCCFormatErrors++;
			//let's print the row and sheet name
			System.out.println("* Possible CC format error near row: " + (cell.getRow().getRowNum() + 1) + " sheet: "
				+ cell.getSheet().getSheetName());
		    }

		    changedFromAValidRegionToAnInvalid = false;
		    changedCC = false;
		    //let's check if this is someone that doesn't count for the quotas or not
		    cell = row.getCell(6);
		    boolean doesntCountForQuota = false;
		    if (sheet.getSheetName().equalsIgnoreCase("Ordenação por Serviço (ADIST)")) {
			//then it doesn't count for the quotas
			doesntCountForQuota = true;
		    }
		    //write the user to the provided file
		    fileWriter.write(lastCostCenterCaught + "," + firstMecNumber + "," + secondMecNumber + ",");
		    if (doesntCountForQuota)
			fileWriter.write("1");
		    fileWriter.write("\n");

		} catch (NumberFormatException e) {
		    //		    debug("No valid number found on row: " + (row.getRowNum() + 1));
		    changedFromAValidRegionToAnInvalid = true;
		} catch (IOException e) {
		    System.out.println("Aborted, catched an error while writing to the output file");
		    e.printStackTrace();
		    System.exit(1);
		}
	    }

	}

	System.out.println(" * Results: ");
	System.out.println("Number of Sheets = " + nrOfSheets);
	System.out.println("Number of valid rows = " + nrOfValidRows);
	System.out.println("Number of Cost Centers caught = " + nrOfCCCaught);
	System.out.println("Number of undefined detected (might be wrong) = " + (nrOfUndefinedDetected - nrOfValidRows));
	System.out.println("Number of possible CC format errors detected (might be wrong) = " + nrOfPossibleCCFormatErrors);
    }
}
