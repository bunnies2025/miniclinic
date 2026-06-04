package tw.edu.fju.miniclinic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import tw.edu.fju.miniclinic.model.Appointment;
import tw.edu.fju.miniclinic.model.AppointmentRepository;
import tw.edu.fju.miniclinic.model.DoctorRepository;
import tw.edu.fju.miniclinic.model.PatientRepository;

import java.util.List;

@Controller
public class AppointmentPageController {

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private PatientRepository patientRepo;

    
    @GetMapping("/appointments")
    public String listAppointments(Model model) {
        model.addAttribute("appointments", appointmentRepo.findAll());
        return "appointments";
    }

    
    @GetMapping("/stats")
    public String showStats(Model model) {
        
    
        long doctorCount = doctorRepo.count();
        long patientCount = patientRepo.count();
        long appointmentCount = appointmentRepo.count();
        
      
        model.addAttribute("doctorCount", doctorCount);
        model.addAttribute("patientCount", patientCount);
        model.addAttribute("appointmentCount", appointmentCount);
        
       
        int homeCount = 0;     
        int internalCount = 0; 
        int rehabCount = 0;    
        int pedsCount = 0;     
        int psychCount = 0;    
        
        List<Appointment> appointments = appointmentRepo.findAll();
        for (Appointment appt : appointments) {
            if (appt.getDoctor() != null) {
                String dept = appt.getDoctor().getDepartment();
                
                if ("家醫科".equals(dept)) {
                    homeCount++;
                } else if ("內科".equals(dept)) {
                    internalCount++;
                } else if ("復健科".equals(dept)) {
                    rehabCount++;
                } else if ("小兒科".equals(dept)) {
                    pedsCount++;
                } else if ("身心科".equals(dept)) {
                    psychCount++;
                }
            }
        }
        
        
        model.addAttribute("homeCount", homeCount);
        model.addAttribute("internalCount", internalCount);
        model.addAttribute("rehabCount", rehabCount);
        model.addAttribute("pedsCount", pedsCount);
        model.addAttribute("psychCount", psychCount);
        
        return "stats";
    }
}