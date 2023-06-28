package com.cornejo.controller;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List; 

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller; 
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping; 

import com.cornejo.model.Solicitud; 
import com.cornejo.model.Vacante;
import com.cornejo.service.ISolicitudesService; 
import com.cornejo.service.IVacantesService; 

import net.sf.jasperreports.engine.JRException; 
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput; 
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration; 



@Controller
@RequestMapping(value="/reportes")
public class ReportesController {
	@Autowired
	private IVacantesService serviceVacantes;
	
	@Autowired
	private ISolicitudesService serviceSolicitud; 
	
	
	    
	@GetMapping("/pdf")
	public ResponseEntity<byte[]> generatePdf() throws FileNotFoundException, JRException {
	    List<Vacante> lista = serviceVacantes.buscarTodas();
	    JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(lista);
	    JasperReport compileReport = JasperCompileManager.compileReport(new FileInputStream("src/reporte2.jrxml"));
	    HashMap<String, Object> map = new HashMap<>();
	    JasperPrint report = JasperFillManager.fillReport(compileReport, map, beanCollectionDataSource);

	    // Exportar el informe a un ByteArrayOutputStream en formato PDF
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    JasperExportManager.exportReportToPdfStream(report, outputStream);

	    // Obtener los bytes del PDF generado
	    byte[] pdfBytes = outputStream.toByteArray();
	    
	    LocalDateTime now = LocalDateTime.now();
	    
	    String filename = "reporte_Vacante_" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
 
	    // Configurar los encabezados de la respuesta HTTP
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_PDF);
	    headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

	    return ResponseEntity.ok()
	            .headers(headers)
	            .body(pdfBytes);
	}
	 
	  
	 @GetMapping("/pdf2")
	 public ResponseEntity<byte[]> generatePdf2() throws FileNotFoundException, JRException { 
		 List<Solicitud> lista = serviceSolicitud.buscarTodas();
		  JRBeanCollectionDataSource beanCollectionDataSource= new JRBeanCollectionDataSource(lista);
		  JasperReport compileReport= JasperCompileManager.compileReport(new FileInputStream("src/reporte.jrxml"));
		  HashMap<String, Object> map= new HashMap<>();
		  JasperPrint report= JasperFillManager.fillReport(compileReport, map, beanCollectionDataSource);
		// Exportar el informe a un ByteArrayOutputStream en formato PDF
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    JasperExportManager.exportReportToPdfStream(report, outputStream);

		    // Obtener los bytes del PDF generado
		    byte[] pdfBytes = outputStream.toByteArray();
		     
		    LocalDateTime now = LocalDateTime.now();
 
		    String filename = "reporte_Solicitudes_" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";

		    // Configurar los encabezados de la respuesta HTTP
		    HttpHeaders headers = new HttpHeaders();
		    headers.setContentType(MediaType.APPLICATION_PDF);
		    headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build()); 
		    
		    return ResponseEntity.ok()
		            .headers(headers)
		            .body(pdfBytes);  
	 } 
	 
	 
	 
	 @GetMapping("/excel")
	 public ResponseEntity<byte[]> generateExcel() throws JRException, IOException {
	     List<Vacante> lista = serviceVacantes.buscarTodas();

	     // Compilar el archivo .jrxml y obtener el objeto JasperReport
	     String jrxmlFilePath = "src/reporte2.jrxml";
	     JasperReport report = JasperCompileManager.compileReport(jrxmlFilePath);

	     // Llenar el informe con los datos
	     JasperPrint jasperPrint = JasperFillManager.fillReport(report, null,
	             new JRBeanCollectionDataSource(lista));

	     // Crear el objeto JRXlsxExporter
	     JRXlsxExporter exporter = new JRXlsxExporter();

	     // Configurar el JasperPrint como entrada del exportador
	     exporter.setExporterInput(new SimpleExporterInput(jasperPrint));

	     // Configurar la salida del exportador a un ByteArrayOutputStream
	     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	     exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

	     // Configurar la configuración del informe Excel
	     SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
	     configuration.setIgnoreCellBorder(false); // Mantener los bordes de las celdas
	     configuration.setDetectCellType(true); // Detectar automáticamente los tipos de celda
	     exporter.setConfiguration(configuration);

	     // Exportar el informe a Excel
	     exporter.exportReport();

	     // Obtener los bytes del archivo Excel generado
	     byte[] excelBytes = outputStream.toByteArray();

	     LocalDateTime now = LocalDateTime.now();
	     String filename = "reporte_" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

	     // Configurar los encabezados de la respuesta HTTP
	     HttpHeaders headers = new HttpHeaders();
	     headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
	     headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

	     return ResponseEntity.ok()
	             .headers(headers)
	             .body(excelBytes);
	 }
	 
	 
	 
	 
	 
	 @GetMapping("/excel2")
	 public ResponseEntity<byte[]> generateExcel2() throws JRException, IOException {
		 List<Solicitud> lista = serviceSolicitud.buscarTodas();
	     // Compilar el archivo .jrxml y obtener el objeto JasperReport
	     String jrxmlFilePath = "src/reporte.jrxml";
	     JasperReport report = JasperCompileManager.compileReport(jrxmlFilePath);

	     // Llenar el informe con los datos
	     JasperPrint jasperPrint = JasperFillManager.fillReport(report, null,
	             new JRBeanCollectionDataSource(lista));

	     // Crear el objeto JRXlsxExporter
	     JRXlsxExporter exporter = new JRXlsxExporter();

	     // Configurar el JasperPrint como entrada del exportador
	     exporter.setExporterInput(new SimpleExporterInput(jasperPrint));

	     // Configurar la salida del exportador a un ByteArrayOutputStream
	     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	     exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

	     // Configurar la configuración del informe Excel
	     SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
	     configuration.setIgnoreCellBorder(false); // Mantener los bordes de las celdas
	     configuration.setDetectCellType(true); // Detectar automáticamente los tipos de celda
	     exporter.setConfiguration(configuration);

	     // Exportar el informe a Excel
	     exporter.exportReport();

	     // Obtener los bytes del archivo Excel generado
	     byte[] excelBytes = outputStream.toByteArray();

	     LocalDateTime now = LocalDateTime.now();
	     String filename = "reporte_" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

	     // Configurar los encabezados de la respuesta HTTP
	     HttpHeaders headers = new HttpHeaders();
	     headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
	     headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

	     return ResponseEntity.ok()
	             .headers(headers)
	             .body(excelBytes);
	 }
	}
	 
	 
	 
	 
 
	 
	
 
