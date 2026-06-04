package tw.edu.fju.miniclinic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import tw.edu.fju.miniclinic.model.Doctor;
import tw.edu.fju.miniclinic.model.DoctorRepository;
import tw.edu.fju.miniclinic.model.LoginForm;

@Controller
public class LoginController {

    @Autowired
    private DoctorRepository doctorRepo;

    // GET：顯示登入頁
    @GetMapping("/login")
    public String loginForm(Model model) {
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        return "login";
    }

    // POST：處理登入
    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("loginForm") LoginForm form,
            BindingResult result,
            HttpSession session,
            Model model) {

        // 步驟 1：檢查表單驗證
        if (result.hasErrors()) {
            return "login"; // 顯示錯誤訊息
        }

        // 步驟 2：查詢醫師
        Doctor doctor = doctorRepo.findById(form.getDoctorId()).orElse(null);

        // 步驟 3：檢查密碼（醫師不存在或密碼錯都給同樣的錯誤訊息，避免洩漏帳號是否存在）
        if (doctor == null || !BCrypt.checkpw(form.getPassword(), doctor.getPasswordHash())) {
            model.addAttribute("errorMessage", "醫師編號或密碼錯誤");
            return "login";
        }

        // 步驟 4：登入成功，存入 Session
        session.setAttribute("loggedInDoctorId", doctor.getDoctorId());
        session.setAttribute("loggedInDoctorName", doctor.getName());

        return "redirect:/dashboard";
    }

    // 登出
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/password")
    public String passwordForm(HttpSession session) {
        if (session.getAttribute("loggedInDoctorId") == null) {
            return "redirect:/login";
        }
        return "password";
    }

    @PostMapping("/password")
    public String changePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            Model model) {

        String doctorId = (String) session.getAttribute("loggedInDoctorId");
        if (doctorId == null) {
            return "redirect:/login";
        }
        Doctor doctor = doctorRepo.findById(doctorId).orElse(null);

        if (doctor == null || !BCrypt.checkpw(oldPassword, doctor.getPasswordHash())) {
            model.addAttribute("errorMessage", "舊密碼錯誤");
            return "password";
        }

        if (newPassword.length() < 8) {
            model.addAttribute("errorMessage", "新密碼至少需要 8 碼");
            return "password";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "兩次輸入的新密碼不一致");
            return "password";
        }

        String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        doctor.setPasswordHash(hashedNewPassword);
        doctorRepo.save(doctor);

        model.addAttribute("successMessage", "密碼修改成功！下次請使用新密碼登入");
        return "password";
    }
}