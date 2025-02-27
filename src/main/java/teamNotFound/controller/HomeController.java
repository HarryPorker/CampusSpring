package teamNotFound.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import teamNotFound.daoimpl.FacoltaDao;
import teamNotFound.daoimpl.ProfessoreDao;
import teamNotFound.daoimpl.RuoloDao;
import teamNotFound.daoimpl.StudenteDao;
import teamNotFound.model.Professore;
import teamNotFound.model.Ruolo;
import teamNotFound.model.Studente;

@Controller
public class HomeController {
	@Autowired
	private ProfessoreDao professoreDao;
	@Autowired
	private BCryptPasswordEncoder cript;
	@Autowired
	private FacoltaDao facoltaDao;
	@Autowired
	private StudenteDao studenteDao;
	@Autowired
	private SmartValidator validator;
	@Autowired
	private RuoloDao ruoloDao;
	
	@RequestMapping(value= {"/","/Home"})
	public String index(ModelMap model) {
		if(professoreDao.getAll().isEmpty()) {
			model.addAttribute("professore", new Professore());
			return "redirect:/FirstAccess";
		}
		else {
			return "index";
		}
	}

	@RequestMapping(value="/FirstAccess", method=RequestMethod.GET)
	public String firstAccessForm(Model model) {
		model.addAttribute("professore", new Professore());
		return "firstAccess";
	}
	
	@RequestMapping(value="/FirstAccess", method= RequestMethod.POST)
	public String firstaccess(@Valid Professore professore, BindingResult result) {
		if(result.hasErrors()) {
			return "firstAccess";
		}else {
			professore.getAccount().setPassword(cript.encode(professore.getAccount().getPassword()));
			// Temporary solution
			Ruolo ruolo = ruoloDao.getByName("RETTORE");
			professore.getAccount().setRuolo(ruolo);
			//
			professoreDao.inserimento(professore);
			return "redirect:/login";
		}
	}
	
	@GetMapping("/Studente")
	public String inserimentoFacolta(Model model) {
		model.addAttribute("facolta", facoltaDao.getAll());
		model.addAttribute("studente", new Studente());
		return "studenteForm";
	}

	@PostMapping("/Studente")
	public String inserimentoStudente(Studente studente, BindingResult result,Model model, @RequestParam("selFacolta") Integer facoltaId) {
		
		studente.setFacolta(facoltaDao.getById(facoltaId));
		validator.validate(studente, result);
		
		if(result.hasErrors()) {
			model.addAttribute("facolta", facoltaDao.getAll());
			return "studenteForm";
		}else {
			studente.getAccount().setPassword(cript.encode(studente.getAccount().getPassword()));
			
			Ruolo ruolo = ruoloDao.getByName("STUDENTE");
			studente.getAccount().setRuolo(ruolo);
			
			studenteDao.inserimento(studente);
			return "redirect:/login";
		}
	}
	
	@GetMapping("/login")
	public String login(HttpServletRequest request) {
		request.getSession().setAttribute("facoltaList", facoltaDao.getAll());
		return "session/login";
	}
}

