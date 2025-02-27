package teamNotFound.controller;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import teamNotFound.daoimpl.AccountDao;
import teamNotFound.daoimpl.DataAppelloDao;
import teamNotFound.daoimpl.EsameDao;
import teamNotFound.daoimpl.PrenotazioneDao;
import teamNotFound.daoimpl.ProfessoreDao;
import teamNotFound.daoimpl.StudenteDao;
import teamNotFound.model.Account;
import teamNotFound.model.DataAppello;
import teamNotFound.model.Esame;
import teamNotFound.model.Professore;
import teamNotFound.model.Studente;

@Controller 
public class EsameController {

	@Autowired
	private EsameDao esameDao;
	@Autowired
	private DataAppelloDao dataDao;
	@Autowired
	private StudenteDao studenteDao;
	@Autowired
	private PrenotazioneDao prenotazioneDao;
	@Autowired
	private ProfessoreDao professoreDao;
	@Autowired
	private AccountDao accountDao;
	@Autowired
	private SmartValidator validator;

	@GetMapping("/Esami/Data/{id}")
	public String newEsame(@PathVariable Integer id,Model model) {
		DataAppello data = dataDao.getByIdWithPrenotazioni(id);
		model.addAttribute("prenotazioni",data.getPrenotazioni());
		return "esame/convalidaEsami";
	}

	@PostMapping("/Esami/Data/{id}")
	public String convalidaEsame(@PathVariable Integer id,@RequestParam("studente") Integer studenteId, 
														  @RequestParam("votoEsame") Integer votoEsame) {
		Esame esame = new Esame();

		DataAppello data = dataDao.getById(id);

		Studente studente = studenteDao.getById(studenteId);

		esame.setCorso(data.getCorso());
		esame.setFacolta(data.getFacolta());
		esame.setProfessore(data.getProfessore());
		esame.setStudente(studente);
		esame.setVotoEsame(votoEsame);

		
		
		BindingResult result = new BeanPropertyBindingResult(esame, "esame");
		validator.validate(esame, result);
		
		if(!result.hasErrors()) {
			esameDao.inserimento(esame);
			prenotazioneDao.remove(prenotazioneDao.getByComposedId(studenteId, id));
		}
		return "redirect:/Esami/Data/"+id;

	}

	@GetMapping("/Esami/Cattedre")
	public String selezionaCattedre(Model model, Principal principal) {
		Account a= accountDao.getByUsername(principal.getName());
		Professore p = (Professore) a.getUtente();

		p = professoreDao.getByIdWithCorsi(p.getId());

		model.addAttribute("cattedre",  p.getCattedra());

		return "esame/selezioneCattedreEsami";
	}

	@GetMapping("/Esami/Cattedra/{composedId}")
	public String selezionaDataAppelloEsami(@PathVariable String composedId,Model model) {
		String[] ids=composedId.split("-");

		int id_professore = Integer.parseInt(ids[0]);
		int id_facolta = Integer.parseInt(ids[1]);
		int id_corso = Integer.parseInt(ids[2]);

		List<DataAppello> date = dataDao.getByProfessoreFacoltaAndCorso(id_professore, id_facolta, id_corso);

		model.addAttribute("date", date);

		return "esame/selezioneDataAppelloEsami";
	}

	@GetMapping("/Esami/Visualizza")
	public String visualizzaEsami(Principal principal, Model model) {
		Account account = accountDao.getByUsername(principal.getName());
		Studente studente = (Studente) account.getUtente();

		studente = studenteDao.getByIdWithEsami(studente.getId());

		model.addAttribute("esami", studente.getEsami());

		return "esame/visualizzaEsami";
	}
}
