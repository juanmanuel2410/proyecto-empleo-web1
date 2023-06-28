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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping; 
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cornejo.model.Solicitud;
import com.cornejo.model.Usuario;
import com.cornejo.service.ISolicitudesService;
import com.cornejo.service.IUsuariosService;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

@Controller
@RequestMapping("/usuarios")
public class UsuariosController {
	
	// Inyectamos una instancia desde nuestro ApplicationContext
    @Autowired
	private IUsuariosService serviceUsuarios;
    @Autowired
	private ISolicitudesService serviceSolicitud;
    
    /**
	 * Metodo que muestra la lista de usuarios sin paginacion
	 * @param model
	 * @param page
	 * @return
	 */
    @GetMapping("/index")
	public String mostrarIndex(Model model) {
    	List<Usuario> lista = serviceUsuarios.buscarRegistrados();
    	model.addAttribute("usuarios", lista);
		return "usuarios/listUsuarios";
	}
    
    /**
     * Método para eliminar un usuario de la base de datos.
     * @param idUsuario
     * @param attributes
     * @return
     */
    @GetMapping("/delete/{id}")
	public String eliminar(@PathVariable("id") int idUsuario, RedirectAttributes attributes) {
		    	
		// Eliminamos el usuario
    	serviceUsuarios.eliminar(idUsuario);
			
		attributes.addFlashAttribute("msg", "El usuario fue eliminado!.");
		return "redirect:/usuarios/index";
	}
    
    /**
     * Método para activar un usuario
     * @param idUsuario
     * @param attributes
     * @return
     */
    @GetMapping("/unlock/{id}")
	public String activar(@PathVariable("id") int idUsuario, RedirectAttributes attributes) {		
    	serviceUsuarios.activar(idUsuario);
		attributes.addFlashAttribute("msg", "El usuario fue activado y ahora tiene acceso al sistema.");		
		return "redirect:/usuarios/index";
	}
    
	/**
	 * Método para bloquear un usuario
	 * @param idUsuario
	 * @param attributes
	 * @return
	 */
	@GetMapping("/lock/{id}")
	public String bloquear(@PathVariable("id") int idUsuario, RedirectAttributes attributes) {		
		serviceUsuarios.bloquear(idUsuario);
		attributes.addFlashAttribute("msg", "El usuario fue bloqueado y no tendra acceso al sistema.");		
		return "redirect:/usuarios/index";
	}
	 
	@GetMapping("/pdf3/{id}")
	 public ResponseEntity<byte[]> generatePdf3(@PathVariable("id")Integer id ) throws FileNotFoundException, JRException { 
		 List<Solicitud> solicitud = serviceSolicitud.obtenerSolicitudesPorUsuario(id); 
		 JRBeanCollectionDataSource beanCollectionDataSource= new JRBeanCollectionDataSource(solicitud); 
		  JasperReport compileReport= JasperCompileManager.compileReport(new FileInputStream("src/reporte3.jrxml"));
		  HashMap<String, Object> map= new HashMap<>();
		  JasperPrint report= JasperFillManager.fillReport(compileReport, map, beanCollectionDataSource);
		  
		// Exportar el informe a un ByteArrayOutputStream en formato PDF
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    JasperExportManager.exportReportToPdfStream(report, outputStream);

		    // Obtener los bytes del PDF generado
		    byte[] pdfBytes = outputStream.toByteArray();

		    // Configurar los encabezados de la respuesta HTTP
		    HttpHeaders headers = new HttpHeaders();
		    headers.setContentType(MediaType.APPLICATION_PDF);
		    headers.setContentDisposition(ContentDisposition.attachment().filename("reporteUsuario_"+id+".pdf").build());

		    return ResponseEntity.ok()
		            .headers(headers)
		            .body(pdfBytes); 
		     
	 } 
	 
	 @GetMapping("/excel3/{id}")
	 public ResponseEntity<byte[]> generateExcelUsuario(@PathVariable("id")Integer id) throws JRException, IOException {
		 List<Solicitud> solicitud = serviceSolicitud.obtenerSolicitudesPorUsuario(id); 
	     // Compilar el archivo .jrxml y obtener el objeto JasperReport
	     String jrxmlFilePath = "src/reporte3.jrxml";
	     JasperReport report = JasperCompileManager.compileReport(jrxmlFilePath);

	     // Llenar el informe con los datos
	     JasperPrint jasperPrint = JasperFillManager.fillReport(report, null,
	             new JRBeanCollectionDataSource(solicitud));

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
