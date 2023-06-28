package com.cornejo.controller;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; 
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cornejo.model.Solicitud;
import com.cornejo.model.Vacante;
import com.cornejo.service.ICategoriasService;
import com.cornejo.service.ISolicitudesService;
import com.cornejo.service.IVacantesService;
import com.cornejo.util.Utileria;

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
@RequestMapping(value="/vacantes")
public class VacantesController {
	
	@Value("${empleosapp.ruta.imagenes}")
	private String ruta;
	
	// Inyectamos una instancia desde nuestro ApplicationContext
    @Autowired
	private IVacantesService serviceVacantes;
    
    @Autowired
   	private ISolicitudesService serviceSolicitud;
    
    @Autowired
   	private ICategoriasService serviceCategorias;
	  
    @GetMapping("/index")
	public String mostrarIndex(Model model) {
    	List<Vacante> lista = serviceVacantes.buscarTodas();
    	model.addAttribute("vacantes", lista);
		return "vacantes/listVacantes";
	}
    
    /**
	 * Metodo que muestra la lista de vacantes con paginacion
	 * @param model
	 * @param page
	 * @return
	 */
	@GetMapping("/indexPaginate")
	public String mostrarIndexPaginado(Model model, Pageable page) {
		Page<Vacante> lista = serviceVacantes.buscarTodas(page);
		model.addAttribute("vacantes", lista);
		return "vacantes/listVacantes";
	}
    
	/**
	 * Método que muestra el formulario para crear una nueva Vacante
	 * @param vacante
	 * @return
	 */
	@GetMapping("/create")
	public String crear(@ModelAttribute Vacante vacante) {		
		return "vacantes/formVacante";
	}
	
	/**
	 * Método que guarda la Vacante en la base de datos
	 * @param vacante
	 * @param result
	 * @param model
	 * @param multiPart
	 * @param attributes
	 * @return
	 */
	@PostMapping("/save")
	public String guardar(@ModelAttribute Vacante vacante, BindingResult result, Model model,
			@RequestParam("archivoImagen") MultipartFile multiPart, RedirectAttributes attributes ) {	
		
		if (result.hasErrors()){
			
			System.out.println("Existieron errores");
			return "vacantes/formVacante";
		}	
		
		if (!multiPart.isEmpty()) {
			//String ruta = "/empleos/img-vacantes/"; // Linux/MAC
			//String ruta = "c:/empleos/img-vacantes/"; // Windows
			String nombreImagen = Utileria.guardarArchivo(multiPart, ruta);
			if (nombreImagen!=null){ // La imagen si se subio				
				vacante.setImagen(nombreImagen); // Asignamos el nombre de la imagen
			}	
		}
				
		// Guadamos el objeto vacante en la bd
		serviceVacantes.guardar(vacante);
		attributes.addFlashAttribute("msg", "Los datos de la vacante fueron guardados!");
			
		//return "redirect:/vacantes/index";
		return "redirect:/vacantes/indexPaginate";		
	}
	
	/**
	 * Método para renderizar la vista de los Detalles para una  determinada Vacante
	 * @param idVacante
	 * @param model
	 * @return
	 */
	@GetMapping("/view/{id}")
	public String verDetalle(@PathVariable("id") int idVacante, Model model) {		
		Vacante vacante = serviceVacantes.buscarPorId(idVacante);			
		model.addAttribute("vacante", vacante);
		return "detalle";
	}
	
	/**
	 * Método que renderiza el formulario HTML para editar una vacante
	 * @param idVacante
	 * @param model
	 * @return
	 */
	@GetMapping("/edit/{id}")
	public String editar(@PathVariable("id") int idVacante, Model model) {		
		Vacante vacante = serviceVacantes.buscarPorId(idVacante);			
		model.addAttribute("vacante", vacante);
		return "vacantes/formVacante";
	}
	
	/**
	 * Método que elimina una Vacante de la base de datos
	 * @param idVacante
	 * @param attributes
	 * @return
	 */
	@GetMapping("/delete/{id}")
	public String eliminar(@PathVariable("id") int idVacante, RedirectAttributes attributes) {
		
		// Eliminamos la vacante.
		serviceVacantes.eliminar(idVacante);
			
		attributes.addFlashAttribute("msg", "La vacante fue eliminada!.");
		//return "redirect:/vacantes/index";
		return "redirect:/vacantes/indexPaginate";
	}
	
	/**
	 * Agregamos al Model la lista de Categorias: De esta forma nos evitamos agregarlos en los metodos
	 * crear y editar. 
	 * @return
	 */	
	@ModelAttribute
	public void setGenericos(Model model){
		model.addAttribute("categorias", serviceCategorias.buscarTodas());	
	}
	
	/**
	 * Personalizamos el Data Binding para todas las propiedades de tipo Date
	 * @param webDataBinder
	 */
	@InitBinder
	public void initBinder(WebDataBinder webDataBinder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
	}
	
	
	 
	@GetMapping("/pdfVacante/{id}")
	 public ResponseEntity<byte[]> generatePdfVacante(@PathVariable("id")Integer id ) throws FileNotFoundException, JRException { 
		 List<Solicitud> solicitud = serviceSolicitud.obtenerSolicitudesPorVacante(id); 
		 JRBeanCollectionDataSource beanCollectionDataSource= new JRBeanCollectionDataSource(solicitud); 
		  JasperReport compileReport= JasperCompileManager.compileReport(new FileInputStream("src/reporte4.jrxml"));
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
		    headers.setContentDisposition(ContentDisposition.attachment().filename("reporteVacante_"+id+".pdf").build());

		    return ResponseEntity.ok()
		            .headers(headers)
		            .body(pdfBytes);  
	 } 
	 
	 @GetMapping("/excelVacante/{id}")
	 public ResponseEntity<byte[]> generateExcelVacantes(@PathVariable("id")Integer id) throws JRException, IOException {
		 List<Solicitud> solicitud = serviceSolicitud.obtenerSolicitudesPorVacante(id); 
	     // Compilar el archivo .jrxml y obtener el objeto JasperReport
	     String jrxmlFilePath = "src/reporte4.jrxml";
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
